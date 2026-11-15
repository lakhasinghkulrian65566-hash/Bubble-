package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Query("SELECT * FROM player_profile WHERE id = 1")
    fun observeProfile(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 1")
    suspend fun getProfileOnce(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: PlayerProfileEntity)

    @Query("SELECT * FROM level_progress ORDER BY levelNumber ASC")
    fun observeAllLevels(): Flow<List<LevelProgressEntity>>

    @Query("SELECT * FROM level_progress WHERE levelNumber = :levelNumber")
    suspend fun getLevelOnce(levelNumber: Int): LevelProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLevels(levels: List<LevelProgressEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLevel(level: LevelProgressEntity)

    @Query("SELECT * FROM skin_unlocks")
    fun observeSkins(): Flow<List<SkinUnlockEntity>>

    @Query("SELECT * FROM skin_unlocks WHERE skinId = :skinId")
    suspend fun getSkinOnce(skinId: String): SkinUnlockEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSkins(skins: List<SkinUnlockEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSkin(skin: SkinUnlockEntity)

    @Query("SELECT * FROM booster_inventory")
    fun observeBoosters(): Flow<List<BoosterInventoryEntity>>

    @Query("SELECT * FROM booster_inventory WHERE boosterId = :boosterId")
    suspend fun getBoosterOnce(boosterId: String): BoosterInventoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBoosters(boosters: List<BoosterInventoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBooster(booster: BoosterInventoryEntity)
}
