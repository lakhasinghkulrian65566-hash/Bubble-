package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.PlayerSnapshot
import com.example.game.BubblePhysics
import com.example.model.BoosterType
import com.example.model.CharacterSkins
import com.example.model.LevelThemes
import com.example.model.SkinId
import com.example.ui.components.DefeatDialog
import com.example.ui.components.VictoryDialog
import com.example.ui.components.drawCharacterCannon
import com.example.ui.components.drawGameBubble
import com.example.ui.components.drawThemedBackdrop
import com.example.ui.theme.CoralPop
import com.example.ui.theme.CosmicIndigo
import com.example.ui.theme.CosmicMidnight
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.SunlitGold
import com.example.viewmodel.MatchStatus
import com.example.viewmodel.PlaySessionViewModel
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun GamePlayScreen(
    playViewModel: PlaySessionViewModel,
    snapshot: PlayerSnapshot,
    onToggleSound: () -> Unit,
    onEquipSkin: (SkinId) -> Unit,
    onBuyBooster: (BoosterType) -> Unit,
    onRetryLevel: (Int) -> Unit,
    onNextLevel: (Int) -> Unit,
    onGoToStore: () -> Unit,
    onExitToWorlds: () -> Unit
) {
    val uiState by playViewModel.uiState.collectAsStateWithLifecycle()
    val theme = LevelThemes.all.getValue(uiState.levelDef.themeId)
    val equippedSkin = CharacterSkins.byId(snapshot.profile.equippedSkinId)
    var animPhase by remember { mutableFloatStateOf(0f) }
    var quickBuyBooster by remember { mutableStateOf<BoosterType?>(null) }

    // 60fps simulation loop
    LaunchedEffect(uiState.levelDef.levelNumber) {
        var lastNanos = 0L
        while (true) {
            withFrameNanos { frameNanos ->
                if (lastNanos != 0L) {
                    val dt = ((frameNanos - lastNanos) / 1_000_000_000f).coerceAtMost(0.05f)
                    animPhase += dt
                    playViewModel.stepFrame(dt)
                }
                lastNanos = frameNanos
            }
        }
    }

    val shakeOffset = remember(uiState.screenShake) {
        if (uiState.screenShake > 0.01f) {
            val mag = uiState.screenShake * 10f
            IntOffset(
                ((Random.nextFloat() - 0.5f) * mag).roundToInt(),
                ((Random.nextFloat() - 0.5f) * mag).roundToInt()
            )
        } else IntOffset.Zero
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.skyTop)
            .offset { shakeOffset }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Compact Top Gameplay HUD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onExitToWorlds,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CosmicMidnight.copy(alpha = 0.6f))
                        .testTag("gameplay_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back_to_worlds),
                        tint = Color.White
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lv.${uiState.levelDef.levelNumber} • ${theme.title}",
                            style = MaterialTheme.typography.labelMedium,
                            color = theme.accent
                        )
                        Text(
                            text = "${uiState.score}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (uiState.doubleScoreActive) SunlitGold else Color.White
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    val progress = (uiState.score.toFloat() / uiState.levelDef.threeStarScore.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(50)),
                        color = SunlitGold,
                        trackColor = CosmicMidnight.copy(alpha = 0.55f)
                    )
                }

                // Shots Remaining Pill
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (uiState.shotsRemaining <= 5) CoralPop else CosmicMidnight.copy(alpha = 0.72f),
                    border = BorderStroke(1.5.dp, theme.accent.copy(alpha = 0.7f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${uiState.shotsRemaining}",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = "ORBS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(Modifier.width(6.dp))

                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(CosmicMidnight.copy(alpha = 0.6f))
                        .testTag("gameplay_sound_btn")
                ) {
                    Icon(
                        imageVector = if (snapshot.profile.soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = stringResource(R.string.cd_toggle_sound),
                        tint = Color.White
                    )
                }
            }

            // Active Temporary Power-Up Status Bar
            if (uiState.eagleEyeActive || uiState.doubleScoreActive || uiState.comboStreak >= 2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.eagleEyeActive) {
                        ActivePowerBadge(label = "Eagle Eye Active", color = ElectricCyan)
                    }
                    if (uiState.doubleScoreActive) {
                        ActivePowerBadge(label = "2x Score Active", color = SunlitGold)
                    }
                    if (uiState.comboStreak >= 2) {
                        ActivePowerBadge(label = "Combo x${uiState.comboStreak}", color = CoralPop)
                    }
                }
            }

            // Playfield Board Canvas
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                val density = LocalDensity.current
                val boardMaxWidthPx = with(density) { maxWidth.coerceAtMost(520.dp).toPx() }
                val boardMaxHeightPx = with(density) { maxHeight.toPx() }

                LaunchedEffect(boardMaxWidthPx, boardMaxHeightPx) {
                    playViewModel.updateBoardDimensions(boardMaxWidthPx, boardMaxHeightPx)
                }

                Box(
                    modifier = Modifier
                        .width(with(density) { boardMaxWidthPx.toDp() })
                        .height(with(density) { boardMaxHeightPx.toDp() })
                        .clip(RoundedCornerShape(22.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset -> playViewModel.updateAim(offset) },
                                onDrag = { change, _ ->
                                    change.consume()
                                    playViewModel.updateAim(change.position)
                                },
                                onDragEnd = { playViewModel.fireShot() }
                            )
                        }
                        .pointerInput(Unit) {
                            detectTapGestures { tapOffset ->
                                playViewModel.fireShot(tapOffset)
                            }
                        }
                        .testTag("gameplay_board_canvas")
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val r = BubblePhysics.bubbleRadius(w)
                        drawThemedBackdrop(theme, animPhase)

                        // Danger barrier line
                        val dangerY = BubblePhysics.dangerLineY(w)
                        drawLine(
                            color = CoralPop.copy(alpha = 0.5f),
                            start = Offset(12f, dangerY),
                            end = Offset(w - 12f, dangerY),
                            strokeWidth = 3f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), animPhase * 30f)
                        )

                        // Trajectory Laser Preview
                        val cannonOrigin = playViewModel.cannonOrigin(uiState)
                        if (uiState.flyingBubble == null && uiState.matchStatus == MatchStatus.Playing) {
                            val bounces = if (uiState.eagleEyeActive) 3 else 1
                            val points = BubblePhysics.computeAimTrajectory(
                                start = cannonOrigin,
                                direction = uiState.aimDirection,
                                boardWidth = w,
                                occupied = uiState.grid,
                                maxBounces = bounces
                            )
                            val guideColor = if (uiState.eagleEyeActive) ElectricCyan else uiState.currentOrb.glowColor
                            for (i in 0 until points.size - 1) {
                                drawLine(
                                    color = guideColor.copy(alpha = if (uiState.eagleEyeActive) 0.85f else 0.6f),
                                    start = points[i],
                                    end = points[i + 1],
                                    strokeWidth = if (uiState.eagleEyeActive) 5f else 4f,
                                    cap = StrokeCap.Round,
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(12f, 14f),
                                        -animPhase * 90f
                                    )
                                )
                            }
                        }

                        // Grid Bubbles
                        uiState.grid.values.forEach { bubble ->
                            val center = BubblePhysics.cellCenter(bubble.row, bubble.col, w)
                            drawGameBubble(center, r, bubble.type, animPhase)
                        }

                        // Falling Unanchored Cascade Bubbles
                        uiState.fallingBubbles.forEach { fb ->
                            drawGameBubble(fb.position, r, fb.type, animPhase)
                        }

                        // Flying Projectile Bubble
                        uiState.flyingBubble?.let { fb ->
                            drawGameBubble(fb.position, r, fb.type, animPhase)
                        }

                        // Expanding Shockwave Rings
                        uiState.shockwaves.forEach { sw ->
                            val progress = 1f - (sw.life / sw.maxLife)
                            drawCircle(
                                color = sw.color.copy(alpha = (1f - progress) * 0.85f),
                                radius = sw.maxRadius * progress,
                                center = sw.center,
                                style = Stroke(width = 5f * (1f - progress) + 1f)
                            )
                        }

                        // Burst Particles
                        uiState.particles.forEach { p ->
                            val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
                            drawCircle(
                                color = p.color.copy(alpha = alpha),
                                radius = p.radius * (0.4f + 0.6f * alpha),
                                center = p.position
                            )
                        }

                        // Character Cannon Skin + Chambered Orb + Next Orb Preview
                        drawCharacterCannon(
                            origin = cannonOrigin,
                            aimDirection = uiState.aimDirection,
                            bubbleRadius = r,
                            skin = equippedSkin,
                            recoil = uiState.cannonRecoil,
                            animPhase = animPhase
                        )
                        if (uiState.flyingBubble == null) {
                            drawGameBubble(cannonOrigin, r * 0.96f, uiState.currentOrb, animPhase)
                        }
                        val nextPos = Offset(cannonOrigin.x - r * 3.1f, cannonOrigin.y + r * 0.4f)
                        drawCircle(
                            color = CosmicMidnight.copy(alpha = 0.65f),
                            radius = r * 0.95f,
                            center = nextPos
                        )
                        drawGameBubble(nextPos, r * 0.72f, uiState.nextOrb, animPhase)
                    }

                    // Floating Score Popups
                    uiState.scorePopups.forEach { sp ->
                        val alpha = (sp.life / sp.maxLife).coerceIn(0f, 1f)
                        Text(
                            text = sp.text,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = sp.color.copy(alpha = alpha),
                            modifier = Modifier.offset {
                                IntOffset(
                                    (sp.position.x - 24f).roundToInt(),
                                    (sp.position.y - 16f).roundToInt()
                                )
                            }
                        )
                    }

                    // Swap Bubble Interactive Button over Next Orb slot
                    val swapButtonSize = with(density) {
                        (BubblePhysics.bubbleRadius(boardMaxWidthPx.coerceAtLeast(320f)) * 2.4f).toDp()
                    }.coerceAtLeast(48.dp)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 18.dp, bottom = 10.dp)
                            .size(swapButtonSize)
                            .clip(CircleShape)
                            .clickable { playViewModel.swapCurrentAndNextOrb() }
                            .testTag("gameplay_swap_orb_btn"),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CosmicSurface,
                            border = BorderStroke(1.dp, theme.accent)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SwapHoriz,
                                contentDescription = stringResource(R.string.cd_swap_bubble),
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(16.dp)
                            )
                        }
                    }

                    // Praise Banner ("AWESOME!", "EXCELLENT!", "FANTASTIC!")
                    val praise = uiState.activePraise
                    androidx.compose.animation.AnimatedVisibility(
                        visible = uiState.praiseVisible && praise != null,
                        enter = scaleIn(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ) + fadeIn(),
                        exit = scaleOut() + fadeOut(),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 80.dp)
                    ) {
                        if (praise != null) {
                            PraiseCalloutOverlay(praiseTitle = praise.title, subtitle = praise.subtitle, tier = praise.tier)
                        }
                    }
                }
            }

            // Bottom One-Time Booster Tray
            Surface(
                color = CosmicMidnight.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, Color(0xFF3E307A)),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BoosterType.entries.forEach { booster ->
                        val qty = snapshot.boosters.firstOrNull { it.boosterId == booster.id }?.quantity ?: 0
                        val alreadyActive = (booster == BoosterType.EAGLE_EYE && uiState.eagleEyeActive) ||
                            (booster == BoosterType.DOUBLE_SCORE && uiState.doubleScoreActive)
                        BoosterTrayButton(
                            booster = booster,
                            quantity = qty,
                            isActiveNow = alreadyActive,
                            onClick = {
                                if (alreadyActive) return@BoosterTrayButton
                                if (qty > 0) {
                                    playViewModel.activateInMatchBooster(booster)
                                } else {
                                    quickBuyBooster = booster
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Quick In-Game Booster Purchase Dialog (for 0-count one-time items)
    val toBuy = quickBuyBooster
    if (toBuy != null) {
        val canAfford = snapshot.profile.coins >= toBuy.coinPrice
        AlertDialog(
            onDismissRequest = { quickBuyBooster = null },
            containerColor = CosmicIndigo,
            title = {
                Text(toBuy.title, color = Color.White, style = MaterialTheme.typography.headlineMedium)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(toBuy.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${toBuy.durationLabel} • Cost: ${toBuy.coinPrice} Coins (Balance: ${snapshot.profile.coins})",
                        color = SunlitGold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            confirmButton = {
                if (canAfford) {
                    Button(
                        onClick = {
                            onBuyBooster(toBuy)
                            quickBuyBooster = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldMint, contentColor = CosmicMidnight)
                    ) {
                        Text("Buy 1 (${toBuy.coinPrice} Coins)")
                    }
                } else {
                    Button(
                        onClick = {
                            quickBuyBooster = null
                            onGoToStore()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SunlitGold, contentColor = CosmicMidnight)
                    ) {
                        Text("Get Coins in Store")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { quickBuyBooster = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Victory / Defeat Modals
    when (val status = uiState.matchStatus) {
        is MatchStatus.Won -> {
            VictoryDialog(
                levelNumber = uiState.levelDef.levelNumber,
                score = status.score,
                stars = status.stars,
                completionResult = status.completionResult,
                onEquipSkin = onEquipSkin,
                onNextLevel = { onNextLevel(uiState.levelDef.levelNumber + 1) },
                onReplay = { onRetryLevel(uiState.levelDef.levelNumber) },
                onBackToMap = onExitToWorlds
            )
        }
        is MatchStatus.Lost -> {
            DefeatDialog(
                reason = status.reason,
                score = status.score,
                livesRemaining = snapshot.profile.lives,
                onRetry = { onRetryLevel(uiState.levelDef.levelNumber) },
                onGoToStore = onGoToStore,
                onBackToMap = onExitToWorlds
            )
        }
        MatchStatus.Playing -> Unit
    }
}

@Composable
private fun ActivePowerBadge(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, color)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun PraiseCalloutOverlay(
    praiseTitle: String,
    subtitle: String,
    tier: Int
) {
    val bannerColor = when {
        tier >= 4 -> Color(0xFFFF9F1C)
        tier == 3 -> ElectricCyan
        tier == 2 -> EmeraldMint
        else -> SunlitGold
    }
    val scale by animateFloatAsState(
        targetValue = 1.06f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "praise_bounce"
    )
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = CosmicMidnight.copy(alpha = 0.88f),
        border = BorderStroke(2.5.dp, bannerColor),
        shadowElevation = 12.dp,
        modifier = Modifier.scale(scale)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = praiseTitle,
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 30.sp),
                color = bannerColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
            )
        }
    }
}

@Composable
private fun BoosterTrayButton(
    booster: BoosterType,
    quantity: Int,
    isActiveNow: Boolean,
    onClick: () -> Unit
) {
    val icon = when (booster) {
        BoosterType.EAGLE_EYE -> Icons.Filled.Visibility
        BoosterType.DOUBLE_SCORE -> Icons.Filled.Star
        BoosterType.EXTRA_SHOTS -> Icons.Filled.Add
        BoosterType.BOMB_ORB -> Icons.Filled.Whatshot
        BoosterType.LIGHTNING_ORB -> Icons.Filled.Bolt
        BoosterType.RAINBOW_ORB -> Icons.Filled.AutoAwesome
        BoosterType.SUPERNOVA_ORB -> Icons.Filled.AutoAwesome
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isActiveNow) booster.accentColor.copy(alpha = 0.30f) else CosmicSurface,
        border = BorderStroke(
            1.5.dp,
            if (quantity > 0 || isActiveNow) booster.accentColor else Color(0xFF4B3D8C)
        ),
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("booster_tray_${booster.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = booster.title,
                tint = booster.accentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Column {
                Text(
                    text = booster.shortLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
                Text(
                    text = when {
                        isActiveNow -> "ACTIVE"
                        quantity > 0 -> "x$quantity"
                        else -> "${booster.coinPrice}c"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActiveNow) EmeraldMint else if (quantity > 0) booster.accentColor else SunlitGold
                )
            }
        }
    }
}
