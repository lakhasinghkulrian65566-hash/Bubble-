package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEffectManager
import com.example.data.BoosterInventoryEntity
import com.example.data.GameDatabase
import com.example.data.GameRepository
import com.example.data.LevelCompletionResult
import com.example.data.PlayerSnapshot
import com.example.model.BoosterType
import com.example.model.SkinId
import com.example.model.StoreCoinPack
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StoreFeedback(
    val message: String,
    val isPositive: Boolean = true
)

class ProgressionViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GameDatabase.getInstance(application)
    val repository = GameRepository(database.gameDao())
    val soundManager = SoundEffectManager()

    val playerSnapshot: StateFlow<PlayerSnapshot> = repository.playerSnapshot
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = PlayerSnapshot(
                profile = com.example.data.PlayerProfileEntity(),
                levels = emptyList(),
                skins = emptyList(),
                boosters = emptyList()
            )
        )

    private val _nextLifeInSeconds = MutableStateFlow(0L)
    val nextLifeInSeconds: StateFlow<Long> = _nextLifeInSeconds.asStateFlow()

    private val _storeFeedback = MutableSharedFlow<StoreFeedback>()
    val storeFeedback: SharedFlow<StoreFeedback> = _storeFeedback.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
            repository.refreshLivesIfNeeded()
        }
        viewModelScope.launch {
            playerSnapshot.collect { snap ->
                soundManager.isEnabled = snap.profile.soundEnabled
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(1000L)
                val snap = playerSnapshot.value
                if (snap.profile.lives < snap.profile.maxLives && snap.profile.lastLifeRegenTimestampMs > 0L) {
                    val now = System.currentTimeMillis()
                    val elapsed = now - snap.profile.lastLifeRegenTimestampMs
                    if (elapsed >= GameRepository.LIFE_REGEN_INTERVAL_MS) {
                        repository.refreshLivesIfNeeded(now)
                    } else {
                        _nextLifeInSeconds.value =
                            ((GameRepository.LIFE_REGEN_INTERVAL_MS - elapsed) / 1000L).coerceAtLeast(0L)
                    }
                } else {
                    _nextLifeInSeconds.value = 0L
                }
            }
        }
    }

    fun toggleSound() {
        viewModelScope.launch {
            val enabled = repository.toggleSound()
            soundManager.isEnabled = enabled
            if (enabled) soundManager.playButtonClick()
        }
    }

    fun equipSkin(skinId: SkinId) {
        viewModelScope.launch {
            if (repository.equipSkin(skinId)) {
                soundManager.playUnlock()
                _storeFeedback.emit(StoreFeedback("Equipped ${skinId.name.replace('_', ' ')}!"))
            }
        }
    }

    fun buyBooster(booster: BoosterType) {
        viewModelScope.launch {
            val result = repository.buyBoosterWithCoins(booster)
            result.onSuccess { qty ->
                soundManager.playCoinPurchase()
                _storeFeedback.emit(
                    StoreFeedback("Purchased ${booster.title}! (Owned: $qty)")
                )
            }.onFailure { err ->
                _storeFeedback.emit(
                    StoreFeedback(err.message ?: "Not enough coins!", isPositive = false)
                )
            }
        }
    }

    fun buyLifeRefill(singleLife: Boolean) {
        viewModelScope.launch {
            val result = repository.buyLifeRefillWithCoins(singleLife)
            result.onSuccess { newLives ->
                soundManager.playCoinPurchase()
                _storeFeedback.emit(
                    StoreFeedback("Hearts refilled to $newLives/5!")
                )
            }.onFailure { err ->
                _storeFeedback.emit(
                    StoreFeedback(err.message ?: "Cannot refill hearts", isPositive = false)
                )
            }
        }
    }

    fun purchaseCoinPack(pack: StoreCoinPack) {
        viewModelScope.launch {
            repository.purchaseCoinPack(pack).onSuccess {
                soundManager.playUnlock()
                _storeFeedback.emit(
                    StoreFeedback("Received +${pack.coins} Coins from ${pack.title}!")
                )
            }
        }
    }

    suspend fun spendLifeToEnterOrRetry(): Boolean {
        return repository.spendLifeIfNeeded()
    }

    suspend fun consumeBoosterItem(booster: BoosterType): Boolean {
        return repository.consumeBooster(booster)
    }

    suspend fun recordLevelWin(levelNumber: Int, score: Int, stars: Int): LevelCompletionResult {
        return repository.completeLevel(levelNumber, score, stars)
    }

    fun boosterQuantity(boosters: List<BoosterInventoryEntity>, type: BoosterType): Int {
        return boosters.firstOrNull { it.boosterId == type.id }?.quantity ?: 0
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
