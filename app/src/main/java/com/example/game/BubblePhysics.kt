package com.example.game

import androidx.compose.ui.geometry.Offset
import com.example.model.BubbleType
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sqrt

data class GridCell(
    val row: Int,
    val col: Int
)

data class GridBubble(
    val row: Int,
    val col: Int,
    val type: BubbleType,
    val hitPoints: Int = if (type == BubbleType.ICE) 2 else 1
) {
    val cell: GridCell get() = GridCell(row, col)
}

data class PraiseCallout(
    val id: Long,
    val title: String,
    val subtitle: String,
    val tier: Int // 1 = NICE, 2 = AWESOME, 3 = EXCELLENT, 4 = FANTASTIC/UNSTOPPABLE
)

data class PopEffectData(
    val clearedBubbles: List<GridBubble>,
    val droppedBubbles: List<GridBubble>,
    val thawedCells: List<GridCell>,
    val pointsEarned: Int,
    val isCombo: Boolean,
    val praise: PraiseCallout?
)

object BubblePhysics {
    const val COLS_EVEN = 9
    const val COLS_ODD = 8
    const val MAX_ROWS = 14
    const val DANGER_ROW = 11
    private val SQRT_3 = sqrt(3f)

    fun colsInRow(row: Int): Int = if (row % 2 == 0) COLS_EVEN else COLS_ODD

    fun bubbleRadius(boardWidth: Float): Float = boardWidth / (COLS_EVEN * 2f)

    fun rowHeight(radius: Float): Float = radius * SQRT_3

    fun cellCenter(row: Int, col: Int, boardWidth: Float, topPadding: Float = 8f): Offset {
        val r = bubbleRadius(boardWidth)
        val xOffset = if (row % 2 == 0) r else r * 2f
        val x = xOffset + col * (r * 2f)
        val y = topPadding + r + row * rowHeight(r)
        return Offset(x, y)
    }

    fun dangerLineY(boardWidth: Float, topPadding: Float = 8f): Float {
        val r = bubbleRadius(boardWidth)
        return topPadding + r + DANGER_ROW * rowHeight(r) - r * 0.4f
    }

    fun neighbors(row: Int, col: Int): List<GridCell> {
        val deltas = if (row % 2 == 0) {
            listOf(
                0 to -1, 0 to 1,
                -1 to -1, -1 to 0,
                1 to -1, 1 to 0
            )
        } else {
            listOf(
                0 to -1, 0 to 1,
                -1 to 0, -1 to 1,
                1 to 0, 1 to 1
            )
        }
        return deltas.mapNotNull { (dr, dc) ->
            val nr = row + dr
            val nc = col + dc
            if (nr in 0 until MAX_ROWS && nc in 0 until colsInRow(nr)) {
                GridCell(nr, nc)
            } else null
        }
    }

    fun findNearestEmptyCell(
        position: Offset,
        occupied: Map<GridCell, GridBubble>,
        boardWidth: Float,
        topPadding: Float = 8f
    ): GridCell {
        val r = bubbleRadius(boardWidth)
        val approxRow = (((position.y - topPadding - r) / rowHeight(r)).toInt())
            .coerceIn(0, MAX_ROWS - 1)

        var bestCell = GridCell(0, 0)
        var bestDist = Float.MAX_VALUE

        val minR = (approxRow - 2).coerceAtLeast(0)
        val maxR = (approxRow + 2).coerceAtMost(MAX_ROWS - 1)
        for (row in minR..maxR) {
            for (col in 0 until colsInRow(row)) {
                val cell = GridCell(row, col)
                if (occupied.containsKey(cell)) continue
                val center = cellCenter(row, col, boardWidth, topPadding)
                val dist = hypot(position.x - center.x, position.y - center.y)
                if (dist < bestDist) {
                    bestDist = dist
                    bestCell = cell
                }
            }
        }
        return bestCell
    }

    /**
     * Computes reflected raycast points from cannon tip for the aiming guide.
     * Standard guide shows 1 bounce; Eagle Eye temporary booster shows 3 bounces.
     */
    fun computeAimTrajectory(
        start: Offset,
        direction: Offset,
        boardWidth: Float,
        occupied: Map<GridCell, GridBubble>,
        maxBounces: Int,
        topPadding: Float = 8f
    ): List<Offset> {
        val r = bubbleRadius(boardWidth)
        val points = mutableListOf(start)
        var cx = start.x
        var cy = start.y
        var vx = direction.x
        var vy = direction.y
        val len = hypot(vx, vy).coerceAtLeast(0.0001f)
        vx /= len
        vy /= len
        if (vy > -0.15f) vy = -0.15f

        var bounces = 0
        val step = (r * 0.35f).coerceAtLeast(4f)
        var steps = 0
        while (steps < 420) {
            steps++
            cx += vx * step
            cy += vy * step

            if (cx <= r) {
                cx = r
                vx = abs(vx)
                points.add(Offset(cx, cy))
                bounces++
                if (bounces > maxBounces) break
            } else if (cx >= boardWidth - r) {
                cx = boardWidth - r
                vx = -abs(vx)
                points.add(Offset(cx, cy))
                bounces++
                if (bounces > maxBounces) break
            }

            if (cy <= topPadding + r) {
                points.add(Offset(cx, topPadding + r))
                break
            }

            val hit = occupied.values.any { bubble ->
                val center = cellCenter(bubble.row, bubble.col, boardWidth, topPadding)
                hypot(cx - center.x, cy - center.y) <= r * 1.78f
            }
            if (hit) {
                points.add(Offset(cx, cy))
                break
            }
        }
        if (points.size == 1 || points.last() != Offset(cx, cy)) {
            points.add(Offset(cx, cy))
        }
        return points
    }

    fun resolveShotImpact(
        snapCell: GridCell,
        shotType: BubbleType,
        grid: MutableMap<GridCell, GridBubble>,
        boardWidth: Float,
        comboStreak: Int,
        doubleScoreActive: Boolean,
        availableColors: List<BubbleType>,
        praiseIdCounter: Long
    ): PopEffectData {
        val cleared = linkedSetOf<GridBubble>()
        val thawed = mutableListOf<GridCell>()

        when (shotType) {
            BubbleType.BOMB -> {
                val originCenter = cellCenter(snapCell.row, snapCell.col, boardWidth)
                val r = bubbleRadius(boardWidth)
                val blastRadius = r * 4.35f
                grid.values.filter { b ->
                    val c = cellCenter(b.row, b.col, boardWidth)
                    hypot(c.x - originCenter.x, c.y - originCenter.y) <= blastRadius
                }.forEach { cleared.add(it) }
                cleared.add(GridBubble(snapCell.row, snapCell.col, shotType))
            }

            BubbleType.LIGHTNING -> {
                grid.values.filter { it.row == snapCell.row }.forEach { cleared.add(it) }
                // Also zap one cell above/below at snap column for dramatic arc
                neighbors(snapCell.row, snapCell.col).forEach { n ->
                    grid[n]?.let { nb ->
                        if (nb.type == BubbleType.ICE) thawed.add(n)
                    }
                }
                cleared.add(GridBubble(snapCell.row, snapCell.col, shotType))
            }

            BubbleType.SUPERNOVA -> {
                val originCenter = cellCenter(snapCell.row, snapCell.col, boardWidth)
                val r = bubbleRadius(boardWidth)
                val blastRadius = r * 4.5f
                grid.values.filter { b ->
                    val c = cellCenter(b.row, b.col, boardWidth)
                    val inCircle = hypot(c.x - originCenter.x, c.y - originCenter.y) <= blastRadius
                    val sameRow = b.row == snapCell.row
                    val diag = abs(b.row - snapCell.row) == abs(b.col - snapCell.col)
                    inCircle || sameRow || diag
                }.forEach { cleared.add(it) }
                cleared.add(GridBubble(snapCell.row, snapCell.col, shotType))
            }

            BubbleType.RAINBOW -> {
                // Find the color among neighbors that produces the largest match cluster
                val neighborColors = neighbors(snapCell.row, snapCell.col)
                    .mapNotNull { grid[it]?.type }
                    .filter { it.isColorMatchable }
                    .distinct()

                if (neighborColors.isNotEmpty()) {
                    var bestCluster = emptyList<GridBubble>()
                    for (color in neighborColors) {
                        val tempGrid = grid.toMutableMap()
                        tempGrid[snapCell] = GridBubble(snapCell.row, snapCell.col, color)
                        val cluster = findConnectedSameColor(snapCell, color, tempGrid)
                        if (cluster.size > bestCluster.size) {
                            bestCluster = cluster
                        }
                    }
                    cleared.addAll(bestCluster.filter { it.cell != snapCell })
                } else {
                    // Pop all immediate neighbors if surrounded by obstacles
                    neighbors(snapCell.row, snapCell.col).mapNotNull { grid[it] }.forEach {
                        cleared.add(it)
                    }
                }
                cleared.add(GridBubble(snapCell.row, snapCell.col, shotType))
            }

            else -> {
                val placed = GridBubble(snapCell.row, snapCell.col, shotType)
                grid[snapCell] = placed
                val cluster = findConnectedSameColor(snapCell, shotType, grid)
                if (cluster.size >= 3) {
                    cleared.addAll(cluster)
                }
            }
        }

        // Remove cleared bubbles from grid
        val removedFromGrid = cleared.filter { grid.containsKey(it.cell) }
        removedFromGrid.forEach { grid.remove(it.cell) }

        // Thaw adjacent ICE bubbles when standard or rainbow match happens
        if (shotType != BubbleType.BOMB && shotType != BubbleType.SUPERNOVA && removedFromGrid.isNotEmpty()) {
            val fallbackColor = availableColors.firstOrNull() ?: BubbleType.BLUE
            val adjacentCells = removedFromGrid.flatMap { neighbors(it.row, it.col) }.distinct()
            for (cell in adjacentCells) {
                val bubble = grid[cell]
                if (bubble != null && bubble.type == BubbleType.ICE) {
                    val nextColor = availableColors[(cell.row + cell.col) % availableColors.size.coerceAtLeast(1)]
                    grid[cell] = GridBubble(cell.row, cell.col, nextColor, hitPoints = 1)
                    thawed.add(cell)
                }
            }
            if (availableColors.isEmpty() && thawed.isEmpty()) {
                // no-op safeguard
                fallbackColor.name
            }
        }

        // Find disconnected floating bubbles
        val dropped = if (removedFromGrid.isNotEmpty() || shotType.isSpecial) {
            findFloatingBubbles(grid).also { floaters ->
                floaters.forEach { grid.remove(it.cell) }
            }
        } else {
            emptyList()
        }

        val totalClearedCount = removedFromGrid.size + dropped.size + if (shotType.isSpecial) 1 else 0
        val hasPop = removedFromGrid.isNotEmpty() || shotType.isSpecial
        val multiplier = (if (doubleScoreActive) 2 else 1) * (1 + (comboStreak.coerceAtMost(4) * 0.25f))
        val rawPoints = (removedFromGrid.size * 50) + (dropped.size * 120) + (if (shotType.isSpecial) 150 else 0)
        val pointsEarned = if (hasPop) (rawPoints * multiplier).toInt() else 0

        val praise = buildPraiseCallout(
            id = praiseIdCounter,
            poppedCount = removedFromGrid.size,
            droppedCount = dropped.size,
            comboStreak = comboStreak,
            shotType = shotType,
            hasPop = hasPop
        )

        return PopEffectData(
            clearedBubbles = cleared.toList(),
            droppedBubbles = dropped,
            thawedCells = thawed,
            pointsEarned = pointsEarned,
            isCombo = hasPop,
            praise = praise
        )
    }

    private fun buildPraiseCallout(
        id: Long,
        poppedCount: Int,
        droppedCount: Int,
        comboStreak: Int,
        shotType: BubbleType,
        hasPop: Boolean
    ): PraiseCallout? {
        if (!hasPop) return null
        val total = poppedCount + droppedCount
        return when {
            shotType == BubbleType.SUPERNOVA || total >= 10 || droppedCount >= 5 || comboStreak >= 4 ->
                PraiseCallout(
                    id = id,
                    title = if (comboStreak >= 5) "UNSTOPPABLE!" else "FANTASTIC!",
                    subtitle = if (droppedCount > 0) "Mega Drop +$droppedCount!" else "$total Bubbles Cleared!",
                    tier = 4
                )
            shotType == BubbleType.BOMB || shotType == BubbleType.LIGHTNING || total >= 6 || droppedCount >= 2 || comboStreak >= 3 ->
                PraiseCallout(
                    id = id,
                    title = "EXCELLENT!",
                    subtitle = if (droppedCount > 0) "Cascade Drop x$droppedCount" else "Combo x$comboStreak!",
                    tier = 3
                )
            total >= 4 || comboStreak >= 2 || shotType == BubbleType.RAINBOW ->
                PraiseCallout(
                    id = id,
                    title = "AWESOME!",
                    subtitle = if (comboStreak >= 2) "Streak x$comboStreak!" else "Great Shot!",
                    tier = 2
                )
            poppedCount >= 3 ->
                PraiseCallout(
                    id = id,
                    title = "NICE!",
                    subtitle = "+${poppedCount} Pop",
                    tier = 1
                )
            else -> null
        }
    }

    private fun findConnectedSameColor(
        start: GridCell,
        targetColor: BubbleType,
        grid: Map<GridCell, GridBubble>
    ): List<GridBubble> {
        val visited = hashSetOf<GridCell>()
        val result = mutableListOf<GridBubble>()
        val queue = ArrayDeque<GridCell>()
        queue.add(start)
        visited.add(start)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val bubble = grid[current] ?: continue
            if (bubble.type != targetColor) continue
            result.add(bubble)
            for (n in neighbors(current.row, current.col)) {
                if (visited.add(n) && grid[n]?.type == targetColor) {
                    queue.add(n)
                }
            }
        }
        return result
    }

    private fun findFloatingBubbles(grid: Map<GridCell, GridBubble>): List<GridBubble> {
        val anchored = hashSetOf<GridCell>()
        val queue = ArrayDeque<GridCell>()
        for (col in 0 until COLS_EVEN) {
            val topCell = GridCell(0, col)
            if (grid.containsKey(topCell)) {
                anchored.add(topCell)
                queue.add(topCell)
            }
        }
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            for (n in neighbors(current.row, current.col)) {
                if (grid.containsKey(n) && anchored.add(n)) {
                    queue.add(n)
                }
            }
        }
        return grid.values.filter { !anchored.contains(it.cell) }
    }
}
