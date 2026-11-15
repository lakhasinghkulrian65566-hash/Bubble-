package com.example

import com.example.game.BubblePhysics
import com.example.game.GridBubble
import com.example.game.GridCell
import com.example.game.LevelGenerator
import com.example.model.BubbleType
import com.example.model.LevelCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BubblePhysicsTest {

    @Test
    fun levelGenerator_createsValidBoardsForAll12Levels() {
        for (def in LevelCatalog.levels) {
            val board = LevelGenerator.generateBoard(def.levelNumber)
            assertTrue("Level ${def.levelNumber} should not be empty", board.isNotEmpty())
            assertTrue(
                "Level ${def.levelNumber} should have anchored top-row bubbles",
                board.keys.any { it.row == 0 }
            )
        }
    }

    @Test
    fun matchThreeAndFloatingDrop_triggersPraiseAndPoints() {
        val boardWidth = 900f
        val grid = linkedMapOf(
            GridCell(0, 0) to GridBubble(0, 0, BubbleType.RED),
            GridCell(0, 1) to GridBubble(0, 1, BubbleType.RED),
            // Floating child anchored only to (0,1) -> (1,1)
            GridCell(1, 1) to GridBubble(1, 1, BubbleType.BLUE),
            GridCell(2, 1) to GridBubble(2, 1, BubbleType.GREEN),
            // Separate anchored cluster
            GridCell(0, 5) to GridBubble(0, 5, BubbleType.YELLOW)
        )

        val result = BubblePhysics.resolveShotImpact(
            snapCell = GridCell(0, 2),
            shotType = BubbleType.RED,
            grid = grid,
            boardWidth = boardWidth,
            comboStreak = 1,
            doubleScoreActive = false,
            availableColors = listOf(BubbleType.RED, BubbleType.BLUE, BubbleType.GREEN, BubbleType.YELLOW),
            praiseIdCounter = 1L
        )

        assertEquals(3, result.clearedBubbles.size)
        assertTrue("Floating bubbles should drop when anchor pops", result.droppedBubbles.size >= 2)
        assertNotNull("Successful pop should trigger praise callout", result.praise)
        assertTrue("Points should include pop + drop bonus", result.pointsEarned > 200)
        assertEquals(1, grid.size) // Only yellow at (0,5) remains
    }

    @Test
    fun bombAndLightningBoosters_clearObstaclesAndRows() {
        val boardWidth = 900f
        val grid = linkedMapOf(
            GridCell(0, 0) to GridBubble(0, 0, BubbleType.STONE),
            GridCell(0, 1) to GridBubble(0, 1, BubbleType.ICE),
            GridCell(0, 2) to GridBubble(0, 2, BubbleType.RED),
            GridCell(0, 3) to GridBubble(0, 3, BubbleType.BLUE),
            GridCell(0, 4) to GridBubble(0, 4, BubbleType.GREEN)
        )

        val lightningResult = BubblePhysics.resolveShotImpact(
            snapCell = GridCell(0, 5),
            shotType = BubbleType.LIGHTNING,
            grid = grid,
            boardWidth = boardWidth,
            comboStreak = 0,
            doubleScoreActive = true,
            availableColors = listOf(BubbleType.RED, BubbleType.BLUE),
            praiseIdCounter = 2L
        )

        assertTrue("Lightning should clear entire row 0", grid.isEmpty())
        assertEquals("EXCELLENT!", lightningResult.praise?.title)
        assertTrue(lightningResult.pointsEarned > 0)
    }
}
