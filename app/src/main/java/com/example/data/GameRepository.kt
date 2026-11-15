package com.example.data

import com.example.model.BoosterType
import com.example.model.CharacterSkins
import com.example.model.LevelCatalog
import com.example.model.SkinId
import com.example.model.StoreCoinPack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class LevelCompletionResult(
    val coinsEarned: Int,
    val newlyUnlockedSkin: SkinId?,
    val newlyUnlockedRareBooster: BoosterType?,
    val rareBoosterQty: Int,
    val phoenixMilestoneUnlocked: Boolean
)

data class PlayerSnapshot(
    val profile: PlayerProfileEntity,
    val levels: List<LevelProgressEntity>,
    val skins: List<SkinUnlockEntity>,
    val boosters: List<BoosterInventoryEntity>
) {
    val totalStars: Int get() = levels.sumOf { it.stars }
}

class GameRepository(private val dao: GameDao) {

    companion object {
        const val LIFE_REGEN_INTERVAL_MS = 15 * 60 * 1000L // 15 minutes per life
        const val SINGLE_LIFE_COIN_PRICE = 100
        const val FULL_LIVES_COIN_PRICE = 350
    }

    private val mutex = Mutex()

    val playerSnapshot: Flow<PlayerSnapshot> = combine(
        dao.observeProfile(),
        dao.observeAllLevels(),
        dao.observeSkins(),
        dao.observeBoosters()
    ) { profile, levels, skins, boosters ->
        PlayerSnapshot(
            profile = profile ?: PlayerProfileEntity(),
            levels = levels,
            skins = skins,
            boosters = boosters
        )
    }

    suspend fun ensureInitialized() = mutex.withLock {
        if (dao.getProfileOnce() == null) {
            dao.upsertProfile(
                PlayerProfileEntity(
                    id = 1,
                    coins = 350,
                    lives = 5,
                    maxLives = 5,
                    lastLifeRegenTimestampMs = System.currentTimeMillis(),
                    equippedSkinId = SkinId.CORAL_GUARDIAN.name,
                    soundEnabled = true,
                    totalScore = 0L
                )
            )
        }

        val existingLevels = dao.getLevelOnce(1)
        if (existingLevels == null) {
            val initialLevels = LevelCatalog.levels.map { def ->
                LevelProgressEntity(
                    levelNumber = def.levelNumber,
                    isUnlocked = def.levelNumber == 1,
                    stars = 0,
                    highScore = 0,
                    timesCompleted = 0
                )
            }
            dao.upsertLevels(initialLevels)
        }

        val starterSkin = dao.getSkinOnce(SkinId.CORAL_GUARDIAN.name)
        if (starterSkin == null) {
            val initialSkins = CharacterSkins.all.map { skin ->
                SkinUnlockEntity(
                    skinId = skin.id.name,
                    isUnlocked = skin.id == SkinId.CORAL_GUARDIAN,
                    unlockedAtMs = if (skin.id == SkinId.CORAL_GUARDIAN) System.currentTimeMillis() else 0L
                )
            }
            dao.upsertSkins(initialSkins)
        }

        val firstBooster = dao.getBoosterOnce(BoosterType.BOMB_ORB.id)
        if (firstBooster == null) {
            val initialBoosters = BoosterType.entries.map { booster ->
                val startQty = when (booster) {
                    BoosterType.BOMB_ORB -> 2
                    BoosterType.EAGLE_EYE -> 1
                    BoosterType.RAINBOW_ORB -> 1
                    else -> 0
                }
                BoosterInventoryEntity(
                    boosterId = booster.id,
                    quantity = startQty,
                    isRareUnlocked = !booster.isRare
                )
            }
            dao.upsertBoosters(initialBoosters)
        }
    }

    suspend fun refreshLivesIfNeeded(nowMs: Long = System.currentTimeMillis()) = mutex.withLock {
        val profile = dao.getProfileOnce() ?: return@withLock
        if (profile.lives >= profile.maxLives) return@withLock
        val elapsed = nowMs - profile.lastLifeRegenTimestampMs
        if (elapsed >= LIFE_REGEN_INTERVAL_MS && profile.lastLifeRegenTimestampMs > 0L) {
            val gained = (elapsed / LIFE_REGEN_INTERVAL_MS).toInt()
            val newLives = (profile.lives + gained).coerceAtMost(profile.maxLives)
            val newTimestamp = if (newLives >= profile.maxLives) {
                nowMs
            } else {
                profile.lastLifeRegenTimestampMs + gained * LIFE_REGEN_INTERVAL_MS
            }
            dao.upsertProfile(
                profile.copy(
                    lives = newLives,
                    lastLifeRegenTimestampMs = newTimestamp
                )
            )
        }
    }

    suspend fun toggleSound(): Boolean = mutex.withLock {
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        val updated = !profile.soundEnabled
        dao.upsertProfile(profile.copy(soundEnabled = updated))
        updated
    }

    suspend fun spendLifeIfNeeded(): Boolean = mutex.withLock {
        val profile = dao.getProfileOnce() ?: return@withLock false
        if (profile.lives <= 0) return@withLock false
        val now = System.currentTimeMillis()
        val nextTimestamp = if (profile.lives == profile.maxLives) now else profile.lastLifeRegenTimestampMs
        dao.upsertProfile(
            profile.copy(
                lives = (profile.lives - 1).coerceAtLeast(0),
                lastLifeRegenTimestampMs = nextTimestamp
            )
        )
        true
    }

    suspend fun completeLevel(
        levelNumber: Int,
        score: Int,
        stars: Int
    ): LevelCompletionResult = mutex.withLock {
        val levelDef = LevelCatalog.get(levelNumber)
        val existing = dao.getLevelOnce(levelNumber) ?: LevelProgressEntity(levelNumber, isUnlocked = true)
        val isFirstClear = existing.timesCompleted == 0
        val bestStars = maxOf(existing.stars, stars)
        val bestScore = maxOf(existing.highScore, score)

        dao.upsertLevel(
            existing.copy(
                isUnlocked = true,
                stars = bestStars,
                highScore = bestScore,
                timesCompleted = existing.timesCompleted + 1
            )
        )

        // Unlock next level
        val nextLevelNum = levelNumber + 1
        if (nextLevelNum <= LevelCatalog.levels.size) {
            val nextLevel = dao.getLevelOnce(nextLevelNum)
                ?: LevelProgressEntity(nextLevelNum, isUnlocked = true)
            if (!nextLevel.isUnlocked) {
                dao.upsertLevel(nextLevel.copy(isUnlocked = true))
            }
        }

        // Calculate coin reward (full on first clear, 40% replay bonus + star bonus)
        val coinsEarned = if (isFirstClear) {
            levelDef.rewardCoins + stars * 15
        } else {
            (levelDef.rewardCoins * 0.4f).toInt() + stars * 10
        }

        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        dao.upsertProfile(
            profile.copy(
                coins = profile.coins + coinsEarned,
                totalScore = profile.totalScore + score
            )
        )

        // Unlock exclusive character skin on level completion
        var unlockedSkin: SkinId? = null
        levelDef.unlockSkinId?.let { skinId ->
            val currentSkin = dao.getSkinOnce(skinId.name)
            if (currentSkin == null || !currentSkin.isUnlocked) {
                dao.upsertSkin(
                    SkinUnlockEntity(
                        skinId = skinId.name,
                        isUnlocked = true,
                        unlockedAtMs = System.currentTimeMillis()
                    )
                )
                unlockedSkin = skinId
            }
        }

        // Unlock & grant rare/bonus booster on level completion
        var unlockedBooster: BoosterType? = null
        var boosterQty = 0
        if (levelDef.unlockRareBooster != null && levelDef.unlockRareBoosterCount > 0) {
            val booster = levelDef.unlockRareBooster
            val currentBooster = dao.getBoosterOnce(booster.id)
                ?: BoosterInventoryEntity(booster.id, quantity = 0, isRareUnlocked = false)
            val qtyToGrant = if (isFirstClear) levelDef.unlockRareBoosterCount else 1
            dao.upsertBooster(
                currentBooster.copy(
                    quantity = currentBooster.quantity + qtyToGrant,
                    isRareUnlocked = true
                )
            )
            unlockedBooster = booster
            boosterQty = qtyToGrant
        }

        // Check total stars milestone for Prismatic Phoenix skin (24+ stars)
        var phoenixUnlocked = false
        val totalStarsAfter = LevelCatalog.levels.sumOf { def ->
            if (def.levelNumber == levelNumber) bestStars
            else dao.getLevelOnce(def.levelNumber)?.stars ?: 0
        }
        if (totalStarsAfter >= 24) {
            val phoenix = dao.getSkinOnce(SkinId.PRISMATIC_PHOENIX.name)
            if (phoenix == null || !phoenix.isUnlocked) {
                dao.upsertSkin(
                    SkinUnlockEntity(
                        skinId = SkinId.PRISMATIC_PHOENIX.name,
                        isUnlocked = true,
                        unlockedAtMs = System.currentTimeMillis()
                    )
                )
                phoenixUnlocked = true
            }
        }

        LevelCompletionResult(
            coinsEarned = coinsEarned,
            newlyUnlockedSkin = unlockedSkin,
            newlyUnlockedRareBooster = unlockedBooster,
            rareBoosterQty = boosterQty,
            phoenixMilestoneUnlocked = phoenixUnlocked
        )
    }

    suspend fun equipSkin(skinId: SkinId): Boolean = mutex.withLock {
        val skin = dao.getSkinOnce(skinId.name)
        if (skin?.isUnlocked != true) return@withLock false
        val profile = dao.getProfileOnce() ?: return@withLock false
        dao.upsertProfile(profile.copy(equippedSkinId = skinId.name))
        true
    }

    suspend fun buyBoosterWithCoins(booster: BoosterType): Result<Int> = mutex.withLock {
        val profile = dao.getProfileOnce() ?: return@withLock Result.failure(IllegalStateException("Missing profile"))
        if (profile.coins < booster.coinPrice) {
            return@withLock Result.failure(IllegalStateException("Not enough coins"))
        }
        val current = dao.getBoosterOnce(booster.id)
            ?: BoosterInventoryEntity(booster.id, quantity = 0, isRareUnlocked = true)
        dao.upsertProfile(profile.copy(coins = profile.coins - booster.coinPrice))
        val updatedQty = current.quantity + 1
        dao.upsertBooster(current.copy(quantity = updatedQty, isRareUnlocked = true))
        Result.success(updatedQty)
    }

    suspend fun consumeBooster(booster: BoosterType): Boolean = mutex.withLock {
        val current = dao.getBoosterOnce(booster.id) ?: return@withLock false
        if (current.quantity <= 0) return@withLock false
        dao.upsertBooster(current.copy(quantity = current.quantity - 1))
        true
    }

    suspend fun buyLifeRefillWithCoins(singleLife: Boolean): Result<Int> = mutex.withLock {
        val profile = dao.getProfileOnce() ?: return@withLock Result.failure(IllegalStateException("Missing profile"))
        if (profile.lives >= profile.maxLives) {
            return@withLock Result.failure(IllegalStateException("Lives are already full"))
        }
        val cost = if (singleLife) SINGLE_LIFE_COIN_PRICE else FULL_LIVES_COIN_PRICE
        if (profile.coins < cost) {
            return@withLock Result.failure(IllegalStateException("Not enough coins"))
        }
        val newLives = if (singleLife) (profile.lives + 1).coerceAtMost(profile.maxLives) else profile.maxLives
        dao.upsertProfile(
            profile.copy(
                coins = profile.coins - cost,
                lives = newLives,
                lastLifeRegenTimestampMs = System.currentTimeMillis()
            )
        )
        Result.success(newLives)
    }

    suspend fun purchaseCoinPack(pack: StoreCoinPack): Result<Unit> = mutex.withLock {
        val profile = dao.getProfileOnce() ?: return@withLock Result.failure(IllegalStateException("Missing profile"))
        val newLives = (profile.lives + pack.bonusLives).coerceAtMost(profile.maxLives)
        dao.upsertProfile(
            profile.copy(
                coins = profile.coins + pack.coins,
                lives = newLives,
                lastLifeRegenTimestampMs = System.currentTimeMillis()
            )
        )
        if (pack.bonusSupernovas > 0) {
            val currentNova = dao.getBoosterOnce(BoosterType.SUPERNOVA_ORB.id)
                ?: BoosterInventoryEntity(BoosterType.SUPERNOVA_ORB.id, 0, true)
            dao.upsertBooster(
                currentNova.copy(
                    quantity = currentNova.quantity + pack.bonusSupernovas,
                    isRareUnlocked = true
                )
            )
        }
        Result.success(Unit)
    }
}
