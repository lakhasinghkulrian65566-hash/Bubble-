package com.example.game

import com.example.model.BubbleType
import com.example.model.LevelCatalog
import com.example.model.LevelDefinition

object LevelGenerator {

    fun generateBoard(levelNumber: Int): Map<GridCell, GridBubble> {
        val def = LevelCatalog.get(levelNumber)
        val colors = BubbleType.standardColors.take(def.colorCount)
        val board = linkedMapOf<GridCell, GridBubble>()

        when (levelNumber) {
            1 -> buildCoralShallows(board, colors)
            2 -> buildPearlReefs(board, colors)
            3 -> buildTidalCrown(board, colors)
            4 -> buildMossyArchway(board, colors)
            5 -> buildFireflyHollow(board, colors)
            6 -> buildElderwoodBastion(board, colors)
            7 -> buildObsidianGate(board, colors)
            8 -> buildCinderSpire(board, colors)
            9 -> buildCalderaCore(board, colors)
            10 -> buildCometDrift(board, colors)
            11 -> buildAuroraPrism(board, colors)
            else -> buildCelestialCrown(board, colors)
        }
        return board
    }

    private fun put(
        board: MutableMap<GridCell, GridBubble>,
        row: Int,
        col: Int,
        type: BubbleType
    ) {
        if (row in 0 until BubblePhysics.MAX_ROWS && col in 0 until BubblePhysics.colsInRow(row)) {
            board[GridCell(row, col)] = GridBubble(row, col, type)
        }
    }

    private fun buildCoralShallows(board: MutableMap<GridCell, GridBubble>, colors: List<BubbleType>) {
        for (r in 0..4) {
            val cols = BubblePhysics.colsInRow(r)
            for (c in 0 until cols) {
                if (r == 4 && (c == 0 || c == cols - 1)) continue
                val color = colors[(c / 2 + r) % colors.size]
                put(board, r, c, color)
            }
        }
    }

    private fun buildPearlReefs(board: MutableMap<GridCell, GridBubble>, colors: List<BubbleType>) {
        for (r in 0..5) {
            val cols = BubblePhysics.colsInRow(r)
            for (c in 0 until cols) {
                if (r >= 3 && (c == 3 || c == 4)) continue // open central lagoon channel
                val color = colors[(r * 2 + c / 2) % colors.size]
                put(board, r, c, color)
            }
        }
    }

    private fun buildTidalCrown(board: MutableMap<GridCell, GridBubble>, colors: List<BubbleType>) {
        for (r in 0..5) {
            val cols = BubblePhysics.colsInRow(r)
            for (c in 0 until cols) {
                val distFromCenter = kotlin.math.abs(c - cols / 2)
                if (r == 5 && distFromCenter > 2) continue
                val color = colors[(distFromCenter + r) % colors.size]
                put(board, r, c, color)
            }
        }
        // Center gem power cluster
        put(board, 2, 4, colors[0])
    }

    private fun buildMossyArchway(board: MutableMap<GridCell, GridBubble>, colors: List<BubbleType>) {
        for (r in 0..5) {
            val cols = BubblePhysics.colsInRow(r)
            for (c in 0 until cols) {
                if (r == 3 && (c == 2 || c == 5)) {
                    put(board, r, c, BubbleType.STONE)
                } else if (r == 5 && c in 2..5) {
                    continue
                } else {
                    val color = colors[(c + r * 2) % colors.size]
                    put(board, r, c, color)
                }
            }
        }
    }

    private fun buildFireflyHollow(board: MutableMap<GridCell, GridBubble>, colors: List<BubbleType>) {
        for (r in 0..6) {
            val cols = BubblePhysics.colsInRow(r)
            for (c in 0 until cols) {
                if (r in 2..4 && c in 3..4) continue // hollow eye
                if (r == 4 && (c == 1 || c == cols - 2)) {
                    put(board, r, c, BubbleType.STONE)
                } else {
                    val color = colors[(c / 2 + r * 3) % colors.size]
                    put(board, r, c, color)
                }
            }
        }
    }

    private fun buildElderwoodBastion(board: MutableMap<GridCell, GridBubble>, colors: List<BubbleType>) {
        for (r in 0..6) {
            val cols = BubblePhysics.colsInRow(r)
            for (c in 0 until cols) {
                if (r == 6 && (c == 0 || c == cols - 1 || c == 4)) continue
                if (r == 3 && (c == 1 || c == 4 || c == 7)) {
                    put(board, r, c, BubbleType.STONE)
                } else {
                    val color = colors[(r + c) % colors.size]
                    put(board, r, c, color)
                }
            }
        }
    }

    private fun buildObsidianGate(board: MutableMap<GridCell, GridBubble>, colors: List<BubbleType>) {
        for (r in 0..6) {
            val cols = BubblePhysics.colsInRow(r)
            for (c in 0 until cols) {
                if (r >= 5 && (c < 2 || c > cols - 3)) continue
                if (r == 4 && c in 2..5) {
                    put(board, r, c, BubbleType.ICE)
                } else {
                    val color = colors[(c + r * 2) % colors.size]
                    put(board, r, c, color)
                }
            }
        }
    }

    private fun buildCinderSpire(board: MutableMap<GridCell, GridBubble>, colors: List<BubbleType>) {
        for (r in 0..6) {
            val cols = BubblePhysics.colsInRow(r)
            for (c in 0 until cols) {
                if (r == 2 && (c == 3 || c == 5)) {
                    put(board, r, c, BubbleType.STONE)
                } else if (r == 5 && c in 2..5) {
                    put(board, r, c, BubbleType.ICE)
                } else if (r == 6 && c % 2 == 1) {
                    continue
                } else {
                    val color = colors[(c / 2 + r) % colors.size]
                    put(board, r, c, color)
                }
            }
        }
    }

    private fun buildCalderaCore(board: MutableMap<GridCell, GridBubble>, colors: List<BubbleType>) {
        for (r in 0..7) {
            val cols = BubblePhysics.colsInRow(r)
            for (c in 0 until cols) {
                if (r >= 6 && (c < 2 || c > cols - 3)) continue
                when {
                    r == 3 && (c == 2 || c == 5) -> put(board, r, c, BubbleType.STONE)
                    r == 5 && (c == 1 || c == 4 || c == 6) -> put(board, r, c, BubbleType.ICE)
                    else -> put(board, r, c, colors[(r * 2 + c) % colors.size])
                }
            }
        }
    }

    private fun buildCometDrift(board: MutableMap<GridCell, GridBubble>, colors: List<BubbleType>) {
        for (r in 0..6) {
            val cols = BubblePhysics.colsInRow(r)
            for (c in 0 until cols) {
                if ((r + c) % 7 == 0 && r > 1) continue // diagonal starlight gaps
                if (r == 4 && (c == 2 || c == 6)) {
                    put(board, r, c, BubbleType.ICE)
                } else {
                    put(board, r, c, colors[(r + c * 2) % colors.size])
                }
            }
        }
    }

    private fun buildAuroraPrism(board: MutableMap<GridCell, GridBubble>, colors: List<BubbleType>) {
        for (r in 0..7) {
            val cols = BubblePhysics.colsInRow(r)
            for (c in 0 until cols) {
                if (r == 7 && (c < 2 || c > cols - 3)) continue
                when {
                    r == 2 && (c == 2 || c == 6) -> put(board, r, c, BubbleType.STONE)
                    r == 4 && c in 3..5 -> put(board, r, c, BubbleType.ICE)
                    else -> put(board, r, c, colors[(c / 2 + r * 2) % colors.size])
                }
            }
        }
    }

    private fun buildCelestialCrown(board: MutableMap<GridCell, GridBubble>, colors: List<BubbleType>) {
        for (r in 0..7) {
            val cols = BubblePhysics.colsInRow(r)
            for (c in 0 until cols) {
                val centerDist = kotlin.math.abs(c - cols / 2)
                if (r == 7 && centerDist > 2) continue
                when {
                    r == 3 && (c == 1 || c == 4 || c == 7) -> put(board, r, c, BubbleType.STONE)
                    r == 5 && (c == 2 || c == 5) -> put(board, r, c, BubbleType.ICE)
                    else -> put(board, r, c, colors[(centerDist + r * 2) % colors.size])
                }
            }
        }
    }

    fun pickAvailableColor(
        board: Map<GridCell, GridBubble>,
        def: LevelDefinition
    ): BubbleType {
        val activeColors = board.values
            .map { it.type }
            .filter { it.isColorMatchable }
            .distinct()
        return if (activeColors.isNotEmpty()) {
            activeColors.random()
        } else {
            BubbleType.standardColors.take(def.colorCount).random()
        }
    }
}
