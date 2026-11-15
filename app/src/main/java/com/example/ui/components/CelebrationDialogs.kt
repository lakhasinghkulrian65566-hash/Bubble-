package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.LevelCompletionResult
import com.example.model.CharacterSkins
import com.example.model.LevelCatalog
import com.example.model.SkinId
import com.example.ui.theme.CosmicIndigo
import com.example.ui.theme.CosmicSurfaceElevated
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.RubyHeart
import com.example.ui.theme.SunlitGold

@Composable
fun VictoryDialog(
    levelNumber: Int,
    score: Int,
    stars: Int,
    completionResult: LevelCompletionResult?,
    onEquipSkin: (SkinId) -> Unit,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onBackToMap: () -> Unit
) {
    var animTrigger by remember { mutableStateOf(false) }
    var skinEquippedNow by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animTrigger = true }

    val scale by animateFloatAsState(
        targetValue = if (animTrigger) 1f else 0.72f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "victory_scale"
    )
    val hasNextLevel = levelNumber < LevelCatalog.levels.size

    Dialog(onDismissRequest = {}) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale),
            shape = RoundedCornerShape(28.dp),
            color = CosmicIndigo,
            border = BorderStroke(2.dp, SunlitGold.copy(alpha = 0.75f))
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LEVEL $levelNumber CLEARED!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = SunlitGold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))

                // 3-Star Rating Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        val earned = i <= stars
                        Icon(
                            imageVector = if (earned) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = "Star $i",
                            tint = if (earned) SunlitGold else Color(0xFF5A4E99),
                            modifier = Modifier.size(if (i == 2) 48.dp else 38.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Score + Coins Reward Card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CosmicSurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SCORE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$score", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("COINS EARNED", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = SunlitGold, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "+${completionResult?.coinsEarned ?: 0}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = SunlitGold
                                )
                            }
                        }
                    }
                }

                // Exclusive Skin Unlock Banner
                val unlockedSkinId = completionResult?.newlyUnlockedSkin
                    ?: if (completionResult?.phoenixMilestoneUnlocked == true) SkinId.PRISMATIC_PHOENIX else null

                if (unlockedSkinId != null) {
                    val skin = CharacterSkins.byId(unlockedSkinId.name)
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF2B1B5E),
                        border = BorderStroke(1.5.dp, ElectricCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        Brush.radialGradient(listOf(skin.auraColor.copy(alpha = 0.35f), Color.Transparent)),
                                        RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(52.dp)) {
                                    drawCharacterCannon(
                                        origin = Offset(size.width / 2f, size.height * 0.65f),
                                        aimDirection = Offset(0f, -1f),
                                        bubbleRadius = 11f,
                                        skin = skin,
                                        recoil = 0f,
                                        animPhase = 0.5f
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "SKIN UNLOCKED!",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ElectricCyan
                                    )
                                }
                                Text(skin.name, style = MaterialTheme.typography.titleMedium, color = Color.White)
                                Text(skin.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(
                                onClick = {
                                    onEquipSkin(skin.id)
                                    skinEquippedNow = true
                                },
                                enabled = !skinEquippedNow,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (skinEquippedNow) EmeraldMint else ElectricCyan,
                                    contentColor = Color(0xFF0E0A24)
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("victory_equip_skin_btn")
                            ) {
                                if (skinEquippedNow) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                } else {
                                    Text("Equip", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }

                // Rare Booster Unlock / Grant Banner
                val rareBooster = completionResult?.newlyUnlockedRareBooster
                if (rareBooster != null && completionResult.rareBoosterQty > 0) {
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF28194E),
                        border = BorderStroke(1.dp, rareBooster.accentColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Bolt,
                                contentDescription = null,
                                tint = rareBooster.accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (rareBooster.isRare) "RARE BOOSTER UNLOCKED!" else "BOOSTER REWARD!",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = rareBooster.accentColor
                                )
                                Text(
                                    text = "+${completionResult.rareBoosterQty} ${rareBooster.title}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                if (hasNextLevel) {
                    Button(
                        onClick = onNextLevel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("victory_next_level_btn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldMint,
                            contentColor = Color(0xFF0E0A24)
                        )
                    ) {
                        Text("Next Level", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReplay,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("victory_replay_btn"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Replay")
                    }
                    OutlinedButton(
                        onClick = onBackToMap,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("victory_map_btn"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Worlds")
                    }
                }
            }
        }
    }
}

@Composable
fun DefeatDialog(
    reason: String,
    score: Int,
    livesRemaining: Int,
    onRetry: () -> Unit,
    onGoToStore: () -> Unit,
    onBackToMap: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = CosmicIndigo,
            border = BorderStroke(2.dp, RubyHeart.copy(alpha = 0.7f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = RubyHeart,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "LEVEL FAILED",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = reason,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Score: $score  •  Hearts left: $livesRemaining",
                    style = MaterialTheme.typography.labelLarge,
                    color = SunlitGold
                )
                Spacer(Modifier.height(18.dp))

                if (livesRemaining > 0) {
                    Button(
                        onClick = onRetry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("defeat_retry_btn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = Color(0xFF0E0A24)
                        )
                    ) {
                        Icon(Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Try Again", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    Button(
                        onClick = onGoToStore,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("defeat_store_btn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RubyHeart,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Filled.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Refill Hearts in Store", style = MaterialTheme.typography.titleMedium)
                    }
                }

                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onBackToMap,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("defeat_map_btn"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Back to Worlds")
                }
            }
        }
    }
}
