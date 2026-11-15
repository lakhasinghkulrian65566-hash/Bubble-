package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.LevelProgressEntity
import com.example.data.PlayerSnapshot
import com.example.model.BoosterType
import com.example.model.CharacterSkins
import com.example.model.LevelCatalog
import com.example.model.LevelDefinition
import com.example.model.LevelThemeId
import com.example.model.LevelThemes
import com.example.ui.theme.CosmicIndigo
import com.example.ui.theme.CosmicMidnight
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceElevated
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.RubyHeart
import com.example.ui.theme.SunlitGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldsScreen(
    snapshot: PlayerSnapshot,
    onLaunchLevel: (levelNumber: Int, useEagleEye: Boolean, useDoubleScore: Boolean) -> Unit,
    onGoToStore: () -> Unit,
    onGoToSkins: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedLevel by remember { mutableStateOf<LevelDefinition?>(null) }
    val equippedSkin = CharacterSkins.byId(snapshot.profile.equippedSkinId)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicMidnight),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Banner
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF4C3D99)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.height(172.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_banner_1794712844606),
                            contentDescription = stringResource(R.string.cd_hero_banner),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xD90E0A24)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Bubble Quest",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White
                            )
                            Spacer(Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Pop combos, unlock skins & master 4 realms",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFE2DCFF),
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = equippedSkin.primaryColor.copy(alpha = 0.25f),
                                    border = BorderStroke(1.dp, equippedSkin.accentColor),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable(onClick = onGoToSkins)
                                        .testTag("worlds_equipped_skin_chip")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.AutoAwesome,
                                            contentDescription = null,
                                            tint = equippedSkin.accentColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = equippedSkin.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // World Theme Chapters
            items(LevelThemeId.entries) { themeId ->
                val themeConfig = LevelThemes.all.getValue(themeId)
                val themeLevels = LevelCatalog.levels.filter { it.themeId == themeId }
                val milestoneSkin = CharacterSkins.all.first { it.id == themeConfig.milestoneSkinId }

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = CosmicIndigo,
                    border = BorderStroke(1.5.dp, themeConfig.accent.copy(alpha = 0.55f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        themeConfig.cardGradientStart.copy(alpha = 0.72f),
                                        themeConfig.cardGradientEnd.copy(alpha = 0.45f)
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = themeConfig.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White
                                )
                                Text(
                                    text = themeConfig.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFD8D0F5)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = CosmicMidnight.copy(alpha = 0.65f),
                                border = BorderStroke(1.dp, themeConfig.accent.copy(alpha = 0.7f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        tint = themeConfig.accent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = milestoneSkin.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = themeConfig.accent
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            themeLevels.forEach { levelDef ->
                                val progress = snapshot.levels.firstOrNull { it.levelNumber == levelDef.levelNumber }
                                    ?: LevelProgressEntity(
                                        levelNumber = levelDef.levelNumber,
                                        isUnlocked = levelDef.levelNumber == 1
                                    )
                                LevelNodeCard(
                                    levelDef = levelDef,
                                    progress = progress,
                                    accent = themeConfig.accent,
                                    onClick = {
                                        if (progress.isUnlocked) {
                                            selectedLevel = levelDef
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    // Pre-level Launch & Temporary Power-Up Loadout Sheet
    val levelToLaunch = selectedLevel
    if (levelToLaunch != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val eagleQty = snapshot.boosters.firstOrNull { it.boosterId == BoosterType.EAGLE_EYE.id }?.quantity ?: 0
        val doubleQty = snapshot.boosters.firstOrNull { it.boosterId == BoosterType.DOUBLE_SCORE.id }?.quantity ?: 0
        var useEagleEye by remember(levelToLaunch.levelNumber) { mutableStateOf(false) }
        var useDoubleScore by remember(levelToLaunch.levelNumber) { mutableStateOf(false) }
        val theme = LevelThemes.all.getValue(levelToLaunch.themeId)

        ModalBottomSheet(
            onDismissRequest = { selectedLevel = null },
            sheetState = sheetState,
            containerColor = CosmicIndigo
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LEVEL ${levelToLaunch.levelNumber} • ${theme.title.uppercase()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = theme.accent
                        )
                        Text(
                            text = levelToLaunch.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CosmicSurfaceElevated
                    ) {
                        Text(
                            text = "${levelToLaunch.maxShots} Orbs",
                            style = MaterialTheme.typography.labelLarge,
                            color = SunlitGold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Reward Preview
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CosmicSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "LEVEL COMPLETION REWARDS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "+${levelToLaunch.rewardCoins} Coins",
                                style = MaterialTheme.typography.labelLarge,
                                color = SunlitGold
                            )
                            levelToLaunch.unlockRareBooster?.let { b ->
                                Text(
                                    text = "+${levelToLaunch.unlockRareBoosterCount} ${b.shortLabel}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = b.accentColor
                                )
                            }
                            levelToLaunch.unlockSkinId?.let { skinId ->
                                val skin = CharacterSkins.byId(skinId.name)
                                Text(
                                    text = "Skin: ${skin.name}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ElectricCyan
                                )
                            }
                        }
                    }
                }

                // Temporary Power-Up Loadout
                Text(
                    text = "ACTIVATE TEMPORARY LEVEL BOOSTERS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                BoosterToggleRow(
                    booster = BoosterType.EAGLE_EYE,
                    ownedQty = eagleQty,
                    checked = useEagleEye,
                    onCheckedChange = { useEagleEye = it }
                )
                BoosterToggleRow(
                    booster = BoosterType.DOUBLE_SCORE,
                    ownedQty = doubleQty,
                    checked = useDoubleScore,
                    onCheckedChange = { useDoubleScore = it }
                )

                Spacer(Modifier.height(4.dp))

                if (snapshot.profile.lives > 0) {
                    Button(
                        onClick = {
                            val lvl = levelToLaunch.levelNumber
                            selectedLevel = null
                            onLaunchLevel(lvl, useEagleEye, useDoubleScore)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("launch_level_confirm_btn"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.accent,
                            contentColor = CosmicMidnight
                        )
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Play Level ${levelToLaunch.levelNumber}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            selectedLevel = null
                            onGoToStore()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("launch_no_lives_store_btn"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RubyHeart,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Filled.ShoppingBag, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Out of Hearts — Refill in Store", style = MaterialTheme.typography.titleMedium)
                    }
                }

                if (eagleQty == 0 || doubleQty == 0) {
                    OutlinedButton(
                        onClick = {
                            selectedLevel = null
                            onGoToStore()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Buy More Boosters in Store")
                    }
                }

                Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun LevelNodeCard(
    levelDef: LevelDefinition,
    progress: LevelProgressEntity,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (progress.isUnlocked) CosmicMidnight.copy(alpha = 0.72f) else CosmicMidnight.copy(alpha = 0.38f),
        border = BorderStroke(
            1.dp,
            if (progress.isUnlocked) accent.copy(alpha = 0.6f) else Color(0xFF392E70)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = progress.isUnlocked, onClick = onClick)
            .testTag("level_node_${levelDef.levelNumber}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (progress.isUnlocked) accent else Color(0xFF2D2459)),
                contentAlignment = Alignment.Center
            ) {
                if (progress.isUnlocked) {
                    Text(
                        text = "${levelDef.levelNumber}",
                        style = MaterialTheme.typography.titleLarge,
                        color = CosmicMidnight
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Locked",
                        tint = Color(0xFF9C93C9),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = levelDef.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (progress.isUnlocked) Color.White else Color(0xFF9C93C9)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (progress.highScore > 0) {
                        Text(
                            text = "Best: ${progress.highScore}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SunlitGold
                        )
                    } else {
                        Text(
                            text = "${levelDef.maxShots} Orbs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    levelDef.unlockSkinId?.let { skinId ->
                        val skin = CharacterSkins.byId(skinId.name)
                        Text(
                            text = "• Unlocks ${skin.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (i in 1..3) {
                    Icon(
                        imageVector = if (i <= progress.stars) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = null,
                        tint = if (i <= progress.stars) SunlitGold else Color(0xFF564B8C),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BoosterToggleRow(
    booster: BoosterType,
    ownedQty: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CosmicSurfaceElevated,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (checked) Icons.Filled.CheckCircle else Icons.Filled.Bolt,
                contentDescription = null,
                tint = booster.accentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${booster.title} (Owned: $ownedQty)",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = booster.durationLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = booster.accentColor
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = ownedQty > 0
            )
        }
    }
}
