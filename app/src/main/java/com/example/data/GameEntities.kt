package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.SkinId

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 350,
    val lives: Int = 5,
    val maxLives: Int = 5,
    val lastLifeRegenTimestampMs: Long = 0L,
    val equippedSkinId: String = SkinId.CORAL_GUARDIAN.name,
    val soundEnabled: Boolean = true,
    val totalScore: Long = 0L
)

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey val levelNumber: Int,
    val isUnlocked: Boolean,
    val stars: Int = 0,
    val highScore: Int = 0,
    val timesCompleted: Int = 0
)

@Entity(tableName = "skin_unlocks")
data class SkinUnlockEntity(
    @PrimaryKey val skinId: String,
    val isUnlocked: Boolean,
    val unlockedAtMs: Long = 0L
)

@Entity(tableName = "booster_inventory")
data class BoosterInventoryEntity(
    @PrimaryKey val boosterId: String,
    val quantity: Int = 0,
    val isRareUnlocked: Boolean = false
)
