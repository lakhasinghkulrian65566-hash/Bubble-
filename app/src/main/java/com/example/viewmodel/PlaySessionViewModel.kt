package com.example.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEffectManager
import com.example.data.LevelCompletionResult
import com.example.game.BubblePhysics
import com.example.game.GridBubble
import com.example.game.GridCell
import com.example.game.LevelGenerator
import com.example.game.PraiseCallout
import com.example.model.BoosterType
import com.example.model.BubbleType
import com.example.model.LevelCatalog
import com.example.model.LevelDefinition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

data class FlyingBubble(
    val position: Offset,
    val velocity: Offset,
    val type: BubbleType
)

data class PopParticle(
    val position: Offset,
    val velocity: Offset,
    val color: Color,
    val radius: Float,
    val life: Float,
    val maxLife: Float
)

data class FallingBubbleAnim(
    val position: Offset,
    val velocity: Offset,
    val type: BubbleType
)

data class FloatingScoreText(
    val id: Long,
    val position: Offset,
    val text: String,
    val color: Color,
    val life: Float,
    val maxLife: Float
)

data class ShockwaveRing(
    val center: Offset,
    val maxRadius: Float,
    val color: Color,
    val life: Float,
    val maxLife: Float
)

sealed interface MatchStatus {
    data object Playing : MatchStatus
    data class Won(
        val score: Int,
        val stars: Int,
        val completionResult: LevelCompletionResult?
    ) : MatchStatus
    data class Lost(val reason: String, val score: Int) : MatchStatus
}

data class PlaySessionUiState(
    val levelDef: LevelDefinition = LevelCatalog.get(1),
    val boardWidth: Float = 0f,
    val boardHeight: Float = 0f,
    val grid: Map<GridCell, GridBubble> = emptyMap(),
    val currentOrb: BubbleType = BubbleType.RED,
    val nextOrb: BubbleType = BubbleType.BLUE,
    val flyingBubble: FlyingBubble? = null,
    val aimDirection: Offset = Offset(0f, -1f),
    val isAiming: Boolean = false,
    val shotsRemaining: Int = 24,
    val score: Int = 0,
    val comboStreak: Int = 0,
    val eagleEyeActive: Boolean = false,
    val doubleScoreActive: Boolean = false,
    val activePraise: PraiseCallout? = null,
    val praiseVisible: Boolean = false,
    val particles: List<PopParticle> = emptyList(),
    val fallingBubbles: List<FallingBubbleAnim> = emptyList(),
    val scorePopups: List<FloatingScoreText> = emptyList(),
    val shockwaves: List<ShockwaveRing> = emptyList(),
    val cannonRecoil: Float = 0f,
    val screenShake: Float = 0f,
    val matchStatus: MatchStatus = MatchStatus.Playing
)

class PlaySessionViewModel(
    private val progressionViewModel: ProgressionViewModel,
    private val soundManager: SoundEffectManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaySessionUiState())
    val uiState: StateFlow<PlaySessionUiState> = _uiState.asStateFlow()

    private var praiseCounter = 1L
    private var popupCounter = 1L
    private var praiseTimer = 0f
    private var lastBoardWidth = 0f

    fun startLevel(
        levelNumber: Int,
        activateEagleEye: Boolean = false,
        activateDoubleScore: Boolean = false
    ) {
        val def = LevelCatalog.get(levelNumber)
        val initialBoard = LevelGenerator.generateBoard(levelNumber)
        val firstOrb = LevelGenerator.pickAvailableColor(initialBoard, def)
        val secondOrb = LevelGenerator.pickAvailableColor(initialBoard, def)

        _uiState.value = PlaySessionUiState(
            levelDef = def,
            boardWidth = lastBoardWidth,
            grid = initialBoard,
            currentOrb = firstOrb,
            nextOrb = secondOrb,
            shotsRemaining = def.maxShots,
            eagleEyeActive = activateEagleEye,
            doubleScoreActive = activateDoubleScore,
            matchStatus = MatchStatus.Playing
        )
    }

    fun updateBoardDimensions(width: Float, height: Float) {
        if (width <= 0f || height <= 0f) return
        lastBoardWidth = width
        if (_uiState.value.boardWidth != width || _uiState.value.boardHeight != height) {
            _uiState.update { it.copy(boardWidth = width, boardHeight = height) }
        }
    }

    fun cannonOrigin(state: PlaySessionUiState): Offset {
        val w = state.boardWidth.coerceAtLeast(1f)
        val h = state.boardHeight.coerceAtLeast(1f)
        val r = BubblePhysics.bubbleRadius(w)
        return Offset(w / 2f, h - r * 2.15f)
    }

    fun updateAim(pointerPos: Offset) {
        val state = _uiState.value
        if (state.matchStatus != MatchStatus.Playing || state.flyingBubble != null) return
        val origin = cannonOrigin(state)
        val dx = pointerPos.x - origin.x
        var dy = pointerPos.y - origin.y
        if (dy > -35f) dy = -35f // Prevent shooting downward
        val len = hypot(dx, dy).coerceAtLeast(1f)
        val nx = dx / len
        val ny = (dy / len).coerceAtMost(-0.20f)
        val renorm = hypot(nx, ny)
        _uiState.update {
            it.copy(
                aimDirection = Offset(nx / renorm, ny / renorm),
                isAiming = true
            )
        }
    }

    fun fireShot(pointerPos: Offset? = null) {
        val state = _uiState.value
        if (state.matchStatus != MatchStatus.Playing || state.flyingBubble != null) return
        if (state.shotsRemaining <= 0) return

        val origin = cannonOrigin(state)
        val dir = if (pointerPos != null) {
            val dx = pointerPos.x - origin.x
            var dy = pointerPos.y - origin.y
            if (dy > -35f) dy = -35f
            val len = hypot(dx, dy).coerceAtLeast(1f)
            Offset(dx / len, (dy / len).coerceAtMost(-0.20f))
        } else {
            state.aimDirection
        }
        val normLen = hypot(dir.x, dir.y).coerceAtLeast(0.001f)
        val unitDir = Offset(dir.x / normLen, dir.y / normLen)
        val speed = (state.boardWidth * 2.55f).coerceAtLeast(1400f)

        soundManager.playShoot()

        _uiState.update {
            it.copy(
                flyingBubble = FlyingBubble(
                    position = origin,
                    velocity = Offset(unitDir.x * speed, unitDir.y * speed),
                    type = it.currentOrb
                ),
                aimDirection = unitDir,
                isAiming = false,
                shotsRemaining = it.shotsRemaining - 1,
                cannonRecoil = 1f
            )
        }
    }

    fun swapCurrentAndNextOrb() {
        val state = _uiState.value
        if (state.matchStatus != MatchStatus.Playing || state.flyingBubble != null) return
        soundManager.playButtonClick()
        _uiState.update {
            it.copy(currentOrb = it.nextOrb, nextOrb = it.currentOrb)
        }
    }

    fun activateInMatchBooster(booster: BoosterType) {
        val state = _uiState.value
        if (state.matchStatus != MatchStatus.Playing) return
        if (booster == BoosterType.EAGLE_EYE && state.eagleEyeActive) return
        if (booster == BoosterType.DOUBLE_SCORE && state.doubleScoreActive) return

        viewModelScope.launch {
            val consumed = progressionViewModel.consumeBoosterItem(booster)
            if (!consumed) return@launch

            soundManager.playUnlock()
            when (booster) {
                BoosterType.EAGLE_EYE -> {
                    _uiState.update {
                        it.copy(
                            eagleEyeActive = true,
                            activePraise = PraiseCallout(
                                id = ++praiseCounter,
                                title = "EAGLE EYE!",
                                subtitle = "Extended Laser Active",
                                tier = 2
                            ),
                            praiseVisible = true
                        )
                    }
                    praiseTimer = 1.6f
                }
                BoosterType.DOUBLE_SCORE -> {
                    _uiState.update {
                        it.copy(
                            doubleScoreActive = true,
                            activePraise = PraiseCallout(
                                id = ++praiseCounter,
                                title = "2X ELIXIR!",
                                subtitle = "Double Score Active",
                                tier = 2
                            ),
                            praiseVisible = true
                        )
                    }
                    praiseTimer = 1.6f
                }
                BoosterType.EXTRA_SHOTS -> {
                    _uiState.update {
                        it.copy(
                            shotsRemaining = it.shotsRemaining + 5,
                            activePraise = PraiseCallout(
                                id = ++praiseCounter,
                                title = "+5 ORBS!",
                                subtitle = "Extra Shots Ready",
                                tier = 2
                            ),
                            praiseVisible = true
                        )
                    }
                    praiseTimer = 1.6f
                }
                BoosterType.BOMB_ORB -> loadSpecialOrb(BubbleType.BOMB)
                BoosterType.LIGHTNING_ORB -> loadSpecialOrb(BubbleType.LIGHTNING)
                BoosterType.RAINBOW_ORB -> loadSpecialOrb(BubbleType.RAINBOW)
                BoosterType.SUPERNOVA_ORB -> loadSpecialOrb(BubbleType.SUPERNOVA)
            }
        }
    }

    private fun loadSpecialOrb(specialType: BubbleType) {
        _uiState.update {
            it.copy(
                nextOrb = if (!it.currentOrb.isSpecial) it.currentOrb else it.nextOrb,
                currentOrb = specialType
            )
        }
    }

    fun stepFrame(dtSeconds: Float) {
        val dt = dtSeconds.coerceIn(0f, 0.04f)
        val state = _uiState.value
        val w = state.boardWidth
        if (w <= 0f) return

        // Update transient timers & particles
        if (praiseTimer > 0f) {
            praiseTimer -= dt
            if (praiseTimer <= 0f) {
                _uiState.update { it.copy(praiseVisible = false) }
            }
        }

        val updatedParticles = state.particles.mapNotNull { p ->
            val nl = p.life - dt
            if (nl <= 0f) null
            else p.copy(
                position = p.position + p.velocity * dt,
                velocity = Offset(p.velocity.x * 0.97f, p.velocity.y + 420f * dt),
                life = nl
            )
        }

        val updatedFalling = state.fallingBubbles.mapNotNull { fb ->
            val nextPos = fb.position + fb.velocity * dt
            if (nextPos.y > state.boardHeight + 60f) null
            else fb.copy(
                position = nextPos,
                velocity = Offset(fb.velocity.x, fb.velocity.y + 1250f * dt)
            )
        }

        val updatedPopups = state.scorePopups.mapNotNull { sp ->
            val nl = sp.life - dt
            if (nl <= 0f) null
            else sp.copy(
                position = Offset(sp.position.x, sp.position.y - 68f * dt),
                life = nl
            )
        }

        val updatedWaves = state.shockwaves.mapNotNull { sw ->
            val nl = sw.life - dt
            if (nl <= 0f) null else sw.copy(life = nl)
        }

        val newRecoil = (state.cannonRecoil - dt * 5.5f).coerceAtLeast(0f)
        val newShake = (state.screenShake - dt * 4.5f).coerceAtLeast(0f)

        val flying = state.flyingBubble
        if (flying == null) {
            if (updatedParticles != state.particles ||
                updatedFalling != state.fallingBubbles ||
                updatedPopups != state.scorePopups ||
                updatedWaves != state.shockwaves ||
                newRecoil != state.cannonRecoil ||
                newShake != state.screenShake
            ) {
                _uiState.update {
                    it.copy(
                        particles = updatedParticles,
                        fallingBubbles = updatedFalling,
                        scorePopups = updatedPopups,
                        shockwaves = updatedWaves,
                        cannonRecoil = newRecoil,
                        screenShake = newShake
                    )
                }
            }
            return
        }

        // Sub-step projectile movement for accurate collision without tunneling
        val r = BubblePhysics.bubbleRadius(w)
        val subSteps = 4
        val subDt = dt / subSteps
        var pos = flying.position
        var vel = flying.velocity
        var bouncedThisFrame = false
        var collided = false

        for (step in 0 until subSteps) {
            pos = Offset(pos.x + vel.x * subDt, pos.y + vel.y * subDt)

            if (pos.x <= r) {
                pos = Offset(r, pos.y)
                vel = Offset(abs(vel.x), vel.y)
                bouncedThisFrame = true
            } else if (pos.x >= w - r) {
                pos = Offset(w - r, pos.y)
                vel = Offset(-abs(vel.x), vel.y)
                bouncedThisFrame = true
            }

            if (pos.y <= 8f + r) {
                pos = Offset(pos.x, 8f + r)
                collided = true
                break
            }

            val hit = state.grid.values.any { b ->
                val center = BubblePhysics.cellCenter(b.row, b.col, w)
                hypot(pos.x - center.x, pos.y - center.y) <= r * 1.78f
            }
            if (hit) {
                collided = true
                break
            }
        }

        if (bouncedThisFrame) {
            soundManager.playBounce()
        }

        if (!collided) {
            _uiState.update {
                it.copy(
                    flyingBubble = flying.copy(position = pos, velocity = vel),
                    particles = updatedParticles,
                    fallingBubbles = updatedFalling,
                    scorePopups = updatedPopups,
                    shockwaves = updatedWaves,
                    cannonRecoil = newRecoil,
                    screenShake = newShake
                )
            }
            return
        }

        // Impact resolved! Snap to nearest empty hex cell
        val snapCell = BubblePhysics.findNearestEmptyCell(pos, state.grid, w)
        val mutableGrid = state.grid.toMutableMap()
        val colors = BubbleType.standardColors.take(state.levelDef.colorCount)
        val nextStreak = if (flying.type.isSpecial) state.comboStreak + 1 else state.comboStreak

        val popResult = BubblePhysics.resolveShotImpact(
            snapCell = snapCell,
            shotType = flying.type,
            grid = mutableGrid,
            boardWidth = w,
            comboStreak = nextStreak,
            doubleScoreActive = state.doubleScoreActive,
            availableColors = colors,
            praiseIdCounter = ++praiseCounter
        )

        val finalCombo = if (popResult.isCombo) state.comboStreak + 1 else 0
        val newParticles = updatedParticles.toMutableList()
        val newWaves = updatedWaves.toMutableList()
        val newFalling = updatedFalling.toMutableList()
        val newPopups = updatedPopups.toMutableList()
        var shake = newShake

        // Spawn burst particles & shockwaves for cleared bubbles
        val impactCenter = BubblePhysics.cellCenter(snapCell.row, snapCell.col, w)
        if (flying.type == BubbleType.BOMB || flying.type == BubbleType.SUPERNOVA) {
            soundManager.playExplosion()
            shake = if (flying.type == BubbleType.SUPERNOVA) 1f else 0.7f
            newWaves.add(
                ShockwaveRing(
                    center = impactCenter,
                    maxRadius = r * (if (flying.type == BubbleType.SUPERNOVA) 6.5f else 4.8f),
                    color = flying.type.glowColor,
                    life = 0.42f,
                    maxLife = 0.42f
                )
            )
        } else if (flying.type == BubbleType.LIGHTNING) {
            soundManager.playLightning()
            shake = 0.5f
            newWaves.add(
                ShockwaveRing(
                    center = impactCenter,
                    maxRadius = w * 0.65f,
                    color = BubbleType.LIGHTNING.glowColor,
                    life = 0.35f,
                    maxLife = 0.35f
                )
            )
        }

        if (popResult.clearedBubbles.isNotEmpty()) {
            soundManager.playPop(popResult.clearedBubbles.size, finalCombo)
            popResult.clearedBubbles.forEach { b ->
                val c = BubblePhysics.cellCenter(b.row, b.col, w)
                newWaves.add(
                    ShockwaveRing(
                        center = c,
                        maxRadius = r * 2.2f,
                        color = b.type.glowColor,
                        life = 0.32f,
                        maxLife = 0.32f
                    )
                )
                repeat(7) {
                    val angle = Random.nextFloat() * 6.283f
                    val spd = 120f + Random.nextFloat() * 260f
                    newParticles.add(
                        PopParticle(
                            position = c,
                            velocity = Offset(cos(angle) * spd, sin(angle) * spd - 80f),
                            color = b.type.color,
                            radius = r * (0.16f + Random.nextFloat() * 0.16f),
                            life = 0.48f,
                            maxLife = 0.48f
                        )
                    )
                }
            }
        }

        if (popResult.droppedBubbles.isNotEmpty()) {
            soundManager.playDropCascade(popResult.droppedBubbles.size)
            popResult.droppedBubbles.forEach { b ->
                val c = BubblePhysics.cellCenter(b.row, b.col, w)
                newFalling.add(
                    FallingBubbleAnim(
                        position = c,
                        velocity = Offset((Random.nextFloat() - 0.5f) * 140f, -80f),
                        type = b.type
                    )
                )
            }
        }

        if (popResult.pointsEarned > 0) {
            newPopups.add(
                FloatingScoreText(
                    id = ++popupCounter,
                    position = impactCenter,
                    text = "+${popResult.pointsEarned}",
                    color = if (state.doubleScoreActive) Color(0xFFFFD166) else Color.White,
                    life = 0.95f,
                    maxLife = 0.95f
                )
            )
        }

        if (popResult.praise != null) {
            praiseTimer = 1.45f
            soundManager.playPraise(popResult.praise.tier)
        }

        // Check win / loss conditions
        val hasMatchableOrObstacleLeft = mutableGrid.isNotEmpty()
        val hitDangerLine = mutableGrid.keys.any { it.row >= BubblePhysics.DANGER_ROW }
        val updatedScore = state.score + popResult.pointsEarned

        if (!hasMatchableOrObstacleLeft) {
            // Victory! Add remaining shots bonus
            val shotBonus = state.shotsRemaining * (if (state.doubleScoreActive) 200 else 100)
            val totalScore = updatedScore + shotBonus
            val def = state.levelDef
            val stars = when {
                totalScore >= def.threeStarScore -> 3
                totalScore >= def.twoStarScore -> 2
                else -> 1
            }
            soundManager.playLevelWin()

            _uiState.update {
                it.copy(
                    grid = mutableGrid,
                    flyingBubble = null,
                    score = totalScore,
                    comboStreak = finalCombo,
                    activePraise = PraiseCallout(
                        id = ++praiseCounter,
                        title = "VICTORY!",
                        subtitle = "Shot Bonus +$shotBonus",
                        tier = 4
                    ),
                    praiseVisible = true,
                    particles = newParticles,
                    fallingBubbles = newFalling,
                    scorePopups = newPopups,
                    shockwaves = newWaves,
                    cannonRecoil = newRecoil,
                    screenShake = shake,
                    matchStatus = MatchStatus.Won(totalScore, stars, completionResult = null)
                )
            }
            praiseTimer = 2.0f

            viewModelScope.launch {
                val res = progressionViewModel.recordLevelWin(def.levelNumber, totalScore, stars)
                if (res.newlyUnlockedSkin != null || res.newlyUnlockedRareBooster != null) {
                    soundManager.playUnlock()
                }
                _uiState.update { st ->
                    val currentWon = st.matchStatus as? MatchStatus.Won
                    if (currentWon != null) {
                        st.copy(matchStatus = currentWon.copy(completionResult = res))
                    } else st
                }
            }
            return
        }

        if (hitDangerLine || state.shotsRemaining <= 0) {
            soundManager.playLevelFail()
            val reason = if (hitDangerLine) {
                "Bubbles crossed the danger barrier!"
            } else {
                "Out of orbs! Use +5 Orbs or Boosters next time."
            }
            viewModelScope.launch {
                progressionViewModel.spendLifeToEnterOrRetry()
            }
            _uiState.update {
                it.copy(
                    grid = mutableGrid,
                    flyingBubble = null,
                    score = updatedScore,
                    comboStreak = 0,
                    particles = newParticles,
                    fallingBubbles = newFalling,
                    scorePopups = newPopups,
                    shockwaves = newWaves,
                    cannonRecoil = newRecoil,
                    screenShake = shake,
                    matchStatus = MatchStatus.Lost(reason, updatedScore)
                )
            }
            return
        }

        // Advance orb queue cleanly using colors remaining on the board
        val nextQueuedColor = LevelGenerator.pickAvailableColor(mutableGrid, state.levelDef)
        val promotedCurrent = if (state.nextOrb.isSpecial) state.nextOrb else {
            val boardColors = mutableGrid.values.map { it.type }.filter { it.isColorMatchable }.toSet()
            if (boardColors.isEmpty() || boardColors.contains(state.nextOrb)) state.nextOrb else nextQueuedColor
        }

        _uiState.update {
            it.copy(
                grid = mutableGrid,
                flyingBubble = null,
                currentOrb = promotedCurrent,
                nextOrb = nextQueuedColor,
                score = updatedScore,
                comboStreak = finalCombo,
                activePraise = popResult.praise ?: it.activePraise,
                praiseVisible = if (popResult.praise != null) true else it.praiseVisible,
                particles = newParticles,
                fallingBubbles = newFalling,
                scorePopups = newPopups,
                shockwaves = newWaves,
                cannonRecoil = newRecoil,
                screenShake = shake
            )
        }
    }
}
