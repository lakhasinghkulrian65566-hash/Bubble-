package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceElevated
import com.example.ui.theme.RubyHeart
import com.example.ui.theme.SunlitGold

@Composable
fun GameTopHud(
    lives: Int,
    maxLives: Int,
    nextLifeInSeconds: Long,
    coins: Int,
    totalStars: Int,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
    onOpenStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hearts Pill
            HudStatPill(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = stringResource(R.string.cd_heart_icon),
                        tint = RubyHeart,
                        modifier = Modifier.size(18.dp)
                    )
                },
                text = if (lives < maxLives && nextLifeInSeconds > 0L) {
                    val m = nextLifeInSeconds / 60
                    val s = nextLifeInSeconds % 60
                    "$lives/$maxLives (${m}:${s.toString().padStart(2, '0')})"
                } else {
                    "$lives/$maxLives"
                },
                showPlus = lives < maxLives,
                onClick = onOpenStore,
                testTag = "hud_lives_pill"
            )

            // Coins Pill
            HudStatPill(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.MonetizationOn,
                        contentDescription = stringResource(R.string.cd_coin_icon),
                        tint = SunlitGold,
                        modifier = Modifier.size(18.dp)
                    )
                },
                text = "$coins",
                showPlus = true,
                onClick = onOpenStore,
                testTag = "hud_coins_pill"
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Total Stars Pill
            Surface(
                shape = RoundedCornerShape(50),
                color = CosmicSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4C3D99))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = stringResource(R.string.cd_star_icon),
                        tint = SunlitGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "$totalStars",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }

            // Sound Toggle
            IconButton(
                onClick = onToggleSound,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CosmicSurfaceElevated)
                    .border(1.dp, Color(0xFF4C3D99), CircleShape)
                    .testTag("hud_sound_toggle")
            ) {
                Icon(
                    imageVector = if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    contentDescription = stringResource(R.string.cd_toggle_sound),
                    tint = if (soundEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun HudStatPill(
    icon: @Composable () -> Unit,
    text: String,
    showPlus: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = CosmicSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4C3D99)),
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(Modifier.width(5.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            if (showPlus) {
                Spacer(Modifier.width(5.dp))
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
