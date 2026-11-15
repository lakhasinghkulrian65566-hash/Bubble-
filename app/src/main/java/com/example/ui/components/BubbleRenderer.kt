package com.example.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.model.BubbleType
import com.example.model.CharacterSkin
import com.example.model.LevelThemeConfig
import com.example.model.LevelThemeId
import com.example.model.SkinId
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

fun DrawScope.drawThemedBackdrop(
    theme: LevelThemeConfig,
    animPhase: Float
) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(theme.skyTop, theme.skyMid, theme.skyBottom)
        )
    )

    // Subtle atmospheric radial glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(theme.accent.copy(alpha = 0.22f), Color.Transparent),
            center = Offset(size.width * 0.5f, size.height * 0.28f),
            radius = size.width * 0.75f
        ),
        center = Offset(size.width * 0.5f, size.height * 0.28f),
        radius = size.width * 0.75f
    )

    // Animated ambient particles matched to each level theme
    val count = 18
    for (i in 0 until count) {
        val seed = (i * 137.5f)
        val xFrac = ((seed * 0.618f) % 1f)
        val speed = 0.18f + (i % 5) * 0.07f
        val yFrac = when (theme.id) {
            LevelThemeId.STARLIGHT_NEBULA -> ((seed * 0.37f) % 1f)
            else -> {
                val raw = ((seed * 0.29f) - animPhase * speed) % 1f
                if (raw < 0f) raw + 1f else raw
            }
        }
        val px = xFrac * size.width + sin(animPhase * 2f + i) * 10f
        val py = yFrac * size.height
        val r = 2.5f + (i % 4) * 2.2f

        when (theme.id) {
            LevelThemeId.CORAL_LAGOON -> {
                drawCircle(
                    color = Color.White.copy(alpha = 0.16f),
                    radius = r * 1.4f,
                    center = Offset(px, py),
                    style = Stroke(width = 1.5f)
                )
            }
            LevelThemeId.EMERALD_CANOPY -> {
                val pulse = 0.25f + 0.35f * ((sin(animPhase * 4f + i) + 1f) / 2f)
                drawCircle(
                    color = Color(0xFFB7F468).copy(alpha = pulse),
                    radius = r,
                    center = Offset(px, py)
                )
            }
            LevelThemeId.MAGMA_CAVERN -> {
                val glow = 0.25f + 0.45f * ((sin(animPhase * 5f + i) + 1f) / 2f)
                drawCircle(
                    color = Color(0xFFFF9F1C).copy(alpha = glow),
                    radius = r * 1.1f,
                    center = Offset(px, py)
                )
            }
            LevelThemeId.STARLIGHT_NEBULA -> {
                val twinkle = 0.2f + 0.55f * ((sin(animPhase * 3.5f + i * 1.3f) + 1f) / 2f)
                drawCircle(
                    color = Color.White.copy(alpha = twinkle),
                    radius = r * 0.7f,
                    center = Offset(px, py)
                )
            }
        }
    }
}

fun DrawScope.drawGameBubble(
    center: Offset,
    radius: Float,
    type: BubbleType,
    pulsePhase: Float = 0f
) {
    val r = radius * 0.94f
    when (type) {
        BubbleType.RAINBOW -> {
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFFFF3B5C),
                        Color(0xFFFFC800),
                        Color(0xFF00D68F),
                        Color(0xFF00E5FF),
                        Color(0xFF2979FF),
                        Color(0xFFB54CFF),
                        Color(0xFFFF3B5C)
                    ),
                    center = center
                ),
                radius = r,
                center = center
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.75f), Color.Transparent),
                    center = Offset(center.x - r * 0.25f, center.y - r * 0.25f),
                    radius = r * 0.85f
                ),
                radius = r,
                center = center
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = r,
                center = center,
                style = Stroke(width = 2.2f)
            )
        }

        BubbleType.BOMB -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF525870), Color(0xFF1F2233), Color(0xFF0E101A)),
                    center = Offset(center.x - r * 0.25f, center.y - r * 0.25f),
                    radius = r * 1.3f
                ),
                radius = r,
                center = center
            )
            drawCircle(
                color = Color(0xFFFF4D6D).copy(alpha = 0.55f + 0.3f * sin(pulsePhase * 6f)),
                radius = r,
                center = center,
                style = Stroke(width = 2.6f)
            )
            // Inner glowing blast core
            drawCircle(
                color = Color(0xFFFF6B35),
                radius = r * 0.36f,
                center = center
            )
            drawCircle(
                color = Color(0xFFFFD166),
                radius = r * 0.18f,
                center = center
            )
        }

        BubbleType.LIGHTNING -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF80F3FF), Color(0xFF0096C7), Color(0xFF023E8A)),
                    center = Offset(center.x - r * 0.2f, center.y - r * 0.2f),
                    radius = r * 1.25f
                ),
                radius = r,
                center = center
            )
            // Electric bolt glyph
            val bolt = Path().apply {
                moveTo(center.x + r * 0.14f, center.y - r * 0.52f)
                lineTo(center.x - r * 0.26f, center.y + r * 0.04f)
                lineTo(center.x + r * 0.02f, center.y + r * 0.04f)
                lineTo(center.x - r * 0.14f, center.y + r * 0.54f)
                lineTo(center.x + r * 0.28f, center.y - r * 0.06f)
                lineTo(center.x, center.y - r * 0.06f)
                close()
            }
            drawPath(bolt, color = Color(0xFFFFEA70))
        }

        BubbleType.SUPERNOVA -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFF8DC), Color(0xFFFF9F1C), Color(0xFFD90429)),
                    center = center,
                    radius = r * 1.1f
                ),
                radius = r,
                center = center
            )
            // Rotating starburst rays
            rotate(degrees = (pulsePhase * 45f) % 360f, pivot = center) {
                for (k in 0 until 4) {
                    val angle = k * (PI / 4f).toFloat()
                    val dx = cos(angle) * r * 0.68f
                    val dy = sin(angle) * r * 0.68f
                    drawLine(
                        color = Color.White,
                        start = Offset(center.x - dx, center.y - dy),
                        end = Offset(center.x + dx, center.y + dy),
                        strokeWidth = 2.6f,
                        cap = StrokeCap.Round
                    )
                }
            }
            drawCircle(color = Color.White, radius = r * 0.26f, center = center)
        }

        BubbleType.STONE -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF9CA3AF), Color(0xFF4B5563), Color(0xFF1F2937)),
                    center = Offset(center.x - r * 0.28f, center.y - r * 0.28f),
                    radius = r * 1.3f
                ),
                radius = r,
                center = center
            )
            drawCircle(
                color = Color(0xFFCBD5E1).copy(alpha = 0.5f),
                radius = r * 0.68f,
                center = center,
                style = Stroke(width = 2f)
            )
            drawLine(
                color = Color(0xFF1E293B),
                start = Offset(center.x - r * 0.35f, center.y - r * 0.1f),
                end = Offset(center.x + r * 0.32f, center.y + r * 0.25f),
                strokeWidth = 2.2f,
                cap = StrokeCap.Round
            )
        }

        BubbleType.ICE -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color(0xFF8BE9FD), Color(0xFF38BDF8)),
                    center = Offset(center.x - r * 0.25f, center.y - r * 0.25f),
                    radius = r * 1.25f
                ),
                radius = r,
                center = center
            )
            // Crystal diamond facets
            val diamond = Path().apply {
                moveTo(center.x, center.y - r * 0.52f)
                lineTo(center.x + r * 0.45f, center.y)
                lineTo(center.x, center.y + r * 0.52f)
                lineTo(center.x - r * 0.45f, center.y)
                close()
            }
            drawPath(diamond, color = Color.White.copy(alpha = 0.65f), style = Stroke(width = 2.2f))
        }

        else -> {
            // Glossy 3D Colored Bubble
            val lightTone = type.glowColor
            val midTone = type.color
            val darkTone = Color(
                red = (midTone.red * 0.48f).coerceIn(0f, 1f),
                green = (midTone.green * 0.48f).coerceIn(0f, 1f),
                blue = (midTone.blue * 0.48f).coerceIn(0f, 1f),
                alpha = 1f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(lightTone, midTone, darkTone),
                    center = Offset(center.x - r * 0.3f, center.y - r * 0.3f),
                    radius = r * 1.35f
                ),
                radius = r,
                center = center
            )
            // Rim highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.28f),
                radius = r,
                center = center,
                style = Stroke(width = 1.6f)
            )
        }
    }

    // Glossy upper-left specular shine (except stone)
    if (type != BubbleType.STONE) {
        drawOval(
            color = Color.White.copy(alpha = 0.58f),
            topLeft = Offset(center.x - r * 0.52f, center.y - r * 0.62f),
            size = Size(r * 0.58f, r * 0.32f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.45f),
            radius = r * 0.11f,
            center = Offset(center.x + r * 0.35f, center.y + r * 0.35f)
        )
    }
}

fun DrawScope.drawCharacterCannon(
    origin: Offset,
    aimDirection: Offset,
    bubbleRadius: Float,
    skin: CharacterSkin,
    recoil: Float,
    animPhase: Float
) {
    val angleRad = atan2(aimDirection.y.toDouble(), aimDirection.x.toDouble())
    val angleDeg = (Math.toDegrees(angleRad) + 90.0).toFloat()
    val r = bubbleRadius

    // Ambient aura ring around character base
    val pulse = 1f + 0.08f * sin(animPhase * 4f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(skin.auraColor.copy(alpha = 0.38f), Color.Transparent),
            center = origin,
            radius = r * 2.4f * pulse
        ),
        center = origin,
        radius = r * 2.4f * pulse
    )

    rotate(degrees = angleDeg, pivot = origin) {
        val recoilShift = recoil * r * 0.45f
        val cy = origin.y + recoilShift

        // Skin-specific character wings / horns / crown
        when (skin.id) {
            SkinId.FOREST_SPRITE -> {
                // Leafy side wings
                val leftLeaf = Path().apply {
                    moveTo(origin.x - r * 0.8f, cy)
                    quadraticTo(origin.x - r * 2.2f, cy - r * 1.1f, origin.x - r * 1.9f, cy + r * 0.6f)
                    close()
                }
                val rightLeaf = Path().apply {
                    moveTo(origin.x + r * 0.8f, cy)
                    quadraticTo(origin.x + r * 2.2f, cy - r * 1.1f, origin.x + r * 1.9f, cy + r * 0.6f)
                    close()
                }
                drawPath(leftLeaf, color = skin.primaryColor)
                drawPath(rightLeaf, color = skin.primaryColor)
            }
            SkinId.EMBER_DRAGON -> {
                // Dragon horns & flame fins
                val leftHorn = Path().apply {
                    moveTo(origin.x - r * 0.9f, cy + r * 0.3f)
                    lineTo(origin.x - r * 2.1f, cy - r * 1.4f)
                    lineTo(origin.x - r * 0.4f, cy - r * 0.5f)
                    close()
                }
                val rightHorn = Path().apply {
                    moveTo(origin.x + r * 0.9f, cy + r * 0.3f)
                    lineTo(origin.x + r * 2.1f, cy - r * 1.4f)
                    lineTo(origin.x + r * 0.4f, cy - r * 0.5f)
                    close()
                }
                drawPath(leftHorn, color = skin.accentColor)
                drawPath(rightHorn, color = skin.accentColor)
            }
            SkinId.NEBULA_VALKYRIE -> {
                // Astral prism wings
                for (side in listOf(-1f, 1f)) {
                    val wing = Path().apply {
                        moveTo(origin.x + side * r * 0.7f, cy)
                        lineTo(origin.x + side * r * 2.3f, cy - r * 0.7f)
                        lineTo(origin.x + side * r * 1.8f, cy + r * 0.9f)
                        close()
                    }
                    drawPath(wing, color = skin.accentColor.copy(alpha = 0.85f))
                }
            }
            SkinId.GOLDEN_MONARCH -> {
                // Royal crown spikes
                val crown = Path().apply {
                    moveTo(origin.x - r * 1.6f, cy + r * 0.2f)
                    lineTo(origin.x - r * 1.8f, cy - r * 1.1f)
                    lineTo(origin.x - r * 0.9f, cy - r * 0.5f)
                    lineTo(origin.x, cy - r * 1.6f)
                    lineTo(origin.x + r * 0.9f, cy - r * 0.5f)
                    lineTo(origin.x + r * 1.8f, cy - r * 1.1f)
                    lineTo(origin.x + r * 1.6f, cy + r * 0.2f)
                    close()
                }
                drawPath(crown, color = skin.primaryColor)
            }
            SkinId.PRISMATIC_PHOENIX -> {
                // Iridescent dual phoenix feathers
                for (side in listOf(-1f, 1f)) {
                    drawOval(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFFF4D6D), Color(0xFFFFD166), Color(0xFF00E5FF))
                        ),
                        topLeft = Offset(
                            if (side < 0) origin.x - r * 2.5f else origin.x + r * 0.7f,
                            cy - r * 0.9f
                        ),
                        size = Size(r * 1.8f, r * 1.4f)
                    )
                }
            }
            SkinId.CORAL_GUARDIAN -> {
                // Shell fins
                drawCircle(
                    color = skin.accentColor.copy(alpha = 0.7f),
                    radius = r * 0.7f,
                    center = Offset(origin.x - r * 1.1f, cy + r * 0.2f)
                )
                drawCircle(
                    color = skin.accentColor.copy(alpha = 0.7f),
                    radius = r * 0.7f,
                    center = Offset(origin.x + r * 1.1f, cy + r * 0.2f)
                )
            }
        }

        // Cannon Barrel
        val barrelPath = Path().apply {
            moveTo(origin.x - r * 0.65f, cy)
            lineTo(origin.x - r * 0.45f, cy - r * 1.9f)
            lineTo(origin.x + r * 0.45f, cy - r * 1.9f)
            lineTo(origin.x + r * 0.65f, cy)
            close()
        }
        drawPath(
            path = barrelPath,
            brush = Brush.verticalGradient(
                colors = listOf(skin.accentColor, skin.primaryColor, skin.secondaryColor)
            )
        )
        drawPath(
            path = barrelPath,
            color = Color.White.copy(alpha = 0.45f),
            style = Stroke(width = 2f)
        )

        // Character Pedestal Ring
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(skin.primaryColor, skin.secondaryColor),
                center = Offset(origin.x, cy),
                radius = r * 1.35f
            ),
            radius = r * 1.35f,
            center = Offset(origin.x, cy)
        )
        drawCircle(
            color = skin.accentColor,
            radius = r * 1.35f,
            center = Offset(origin.x, cy),
            style = Stroke(width = 3f)
        )
    }
}
