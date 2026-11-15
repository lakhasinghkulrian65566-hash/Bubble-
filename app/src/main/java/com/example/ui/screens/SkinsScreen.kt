package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.PlayerSnapshot
import com.example.model.CharacterSkin
import com.example.model.CharacterSkins
import com.example.model.SkinId
import com.example.ui.components.drawCharacterCannon
import com.example.ui.theme.CosmicIndigo
import com.example.ui.theme.CosmicMidnight
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldMint
import com.example.ui.theme.SunlitGold

@Composable
fun SkinsScreen(
    snapshot: PlayerSnapshot,
    onEquipSkin: (SkinId) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skin_preview_anim")
    val animPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "skin_phase"
    )

    val unlockedCount = CharacterSkins.all.count { skin ->
        snapshot.skins.firstOrNull { it.skinId == skin.id.name }?.isUnlocked == true
    }

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = CosmicIndigo,
                    border = BorderStroke(1.5.dp, ElectricCyan.copy(alpha = 0.55f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Character Skins",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Complete world finales & collect stars to unlock exclusive cannon guardians.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = CosmicSurface
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$unlockedCount/${CharacterSkins.all.size}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = SunlitGold
                                )
                                Text(
                                    text = "UNLOCKED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            items(CharacterSkins.all, key = { it.id.name }) { skin ->
                val isUnlocked = snapshot.skins.firstOrNull { it.skinId == skin.id.name }?.isUnlocked == true
                val isEquipped = snapshot.profile.equippedSkinId == skin.id.name

                SkinCard(
                    skin = skin,
                    isUnlocked = isUnlocked,
                    isEquipped = isEquipped,
                    animPhase = animPhase,
                    onEquip = { onEquipSkin(skin.id) }
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SkinCard(
    skin: CharacterSkin,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    animPhase: Float,
    onEquip: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = if (isEquipped) Color(0xFF281D63) else CosmicIndigo,
        border = BorderStroke(
            width = if (isEquipped) 2.dp else 1.2.dp,
            color = when {
                isEquipped -> SunlitGold
                isUnlocked -> skin.accentColor.copy(alpha = 0.65f)
                else -> Color(0xFF3E317A)
            }
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                skin.auraColor.copy(alpha = if (isUnlocked) 0.35f else 0.1f),
                                CosmicMidnight
                            )
                        ),
                        RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(68.dp)) {
                    drawCharacterCannon(
                        origin = Offset(size.width / 2f, size.height * 0.66f),
                        aimDirection = Offset(0f, -1f),
                        bubbleRadius = 14f,
                        skin = skin,
                        recoil = 0f,
                        animPhase = if (isUnlocked) animPhase else 0f
                    )
                }
                if (!isUnlocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x880E0A24), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Locked skin",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = skin.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    if (skin.isExclusive) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = skin.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = skin.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = skin.accentColor
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = skin.unlockHint,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(8.dp))

            when {
                isEquipped -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = EmeraldMint.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, EmeraldMint)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldMint, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Active", style = MaterialTheme.typography.labelLarge, color = EmeraldMint)
                        }
                    }
                }
                isUnlocked -> {
                    Button(
                        onClick = onEquip,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = skin.accentColor,
                            contentColor = CosmicMidnight
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("equip_skin_${skin.id.name}")
                    ) {
                        Text("Equip", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
                else -> {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CosmicSurface
                    ) {
                        Text(
                            text = "Locked",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF9C93C9),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
