package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.ZoyaState
import com.example.ui.theme.NovaBorderGlow
import com.example.ui.theme.NovaObsidian
import com.example.ui.theme.ZoyaAmber
import com.example.ui.theme.ZoyaBlueGradient
import com.example.ui.theme.ZoyaCoral
import com.example.ui.theme.ZoyaCyan
import com.example.ui.theme.ZoyaCyanBright
import com.example.ui.theme.ZoyaCyanDeep
import com.example.ui.theme.ZoyaCyanGlow
import com.example.ui.theme.ZoyaElectricBlue
import com.example.ui.theme.ZoyaIndigoGradient
import com.example.ui.theme.ZoyaViolet
import com.example.ui.theme.ZoyaVioletBright
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ZoyaOrbView(
    state: ZoyaState,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_infinite")

    // Slow ambient breathing pulse
    val breathingGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    // Orbital ring pulse
    val ringPulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_pulse"
    )

    // Fast rotation for Thinking / Energy
    val thinkingRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "thinking_rot"
    )

    // Audio bar visualizer phase oscillation
    val barPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bar_phase"
    )

    // Smooth amplitude tracker
    val animatedAmplitude = remember { Animatable(0.1f) }
    LaunchedEffect(state) {
        when (state) {
            is ZoyaState.Listening -> animatedAmplitude.animateTo(state.amplitude.coerceIn(0.15f, 1f), tween(70))
            is ZoyaState.Speaking -> animatedAmplitude.animateTo(state.audioAmplitude.coerceIn(0.2f, 1f), tween(80))
            is ZoyaState.Thinking -> animatedAmplitude.animateTo(0.5f, tween(150))
            else -> animatedAmplitude.animateTo(0.1f, tween(250))
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Concentric Ambient Pulse Rings and Gradient Orb Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2, this.size.height / 2)
            val orbRadius = this.size.minDimension / 3.2f
            val amp = animatedAmplitude.value

            // 1. Outermost Ambient Atmospheric Glow (shadow-[0_0_80px_rgba(34,211,238,0.4)])
            val glowColor = when (state) {
                is ZoyaState.Error -> ZoyaCoral.copy(alpha = 0.35f)
                is ZoyaState.Thinking -> ZoyaVioletBright.copy(alpha = 0.4f)
                is ZoyaState.Speaking -> ZoyaCyanBright.copy(alpha = 0.5f)
                else -> ZoyaCyan.copy(alpha = 0.35f)
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = (glowColor.alpha * breathingGlow).coerceIn(0f, 1f)),
                        ZoyaIndigoGradient.copy(alpha = 0.15f * breathingGlow),
                        Color.Transparent
                    ),
                    center = center,
                    radius = orbRadius * 2.2f * breathingGlow
                ),
                radius = orbRadius * 2.2f * breathingGlow,
                center = center
            )

            // 2. Concentric Pulse Ring 1 (outer border-cyan-500/20)
            drawCircle(
                color = when (state) {
                    is ZoyaState.Error -> ZoyaCoral.copy(alpha = 0.25f)
                    is ZoyaState.Thinking -> ZoyaVioletBright.copy(alpha = 0.3f)
                    else -> ZoyaCyan.copy(alpha = 0.2f * ringPulse)
                },
                radius = orbRadius * 1.65f * ringPulse,
                center = center,
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = if (state is ZoyaState.Thinking) {
                        PathEffect.dashPathEffect(floatArrayOf(30f, 20f), thinkingRotation * 2)
                    } else null
                )
            )

            // 3. Concentric Pulse Ring 2 (inner border-cyan-500/10)
            drawCircle(
                color = when (state) {
                    is ZoyaState.Error -> ZoyaCoral.copy(alpha = 0.15f)
                    is ZoyaState.Thinking -> ZoyaViolet.copy(alpha = 0.2f)
                    else -> ZoyaCyan.copy(alpha = 0.12f)
                },
                radius = orbRadius * 1.35f,
                center = center,
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = if (state is ZoyaState.Speaking) {
                        PathEffect.dashPathEffect(floatArrayOf(15f, 10f), -thinkingRotation)
                    } else null
                )
            )

            // 4. Vibrant Gradient Orb Sphere (from-cyan-600 via-blue-500 to-indigo-600)
            val orbColors = when (state) {
                is ZoyaState.Error -> listOf(ZoyaCoral, ZoyaAmber, Color(0xFF3B0B0B))
                is ZoyaState.Thinking -> listOf(ZoyaVioletBright, ZoyaIndigoGradient, ZoyaCyanDeep)
                is ZoyaState.Speaking -> listOf(ZoyaCyanBright, ZoyaBlueGradient, ZoyaIndigoGradient)
                is ZoyaState.Listening -> listOf(Color.White, ZoyaCyan, ZoyaBlueGradient, ZoyaIndigoGradient)
                is ZoyaState.Disconnected -> listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A))
                else -> listOf(ZoyaCyanDeep, ZoyaBlueGradient, ZoyaIndigoGradient)
            }

            drawCircle(
                brush = Brush.linearGradient(
                    colors = orbColors,
                    start = Offset(center.x - orbRadius, center.y - orbRadius),
                    end = Offset(center.x + orbRadius, center.y + orbRadius)
                ),
                radius = orbRadius * (1f + amp * 0.08f),
                center = center
            )

            // 5. Ambient Inner Ring Border (border-4 border-white/10)
            drawCircle(
                color = Color.White.copy(alpha = 0.12f),
                radius = orbRadius * 0.88f,
                center = center,
                style = Stroke(width = 3.5.dp.toPx())
            )

            // 6. Frosted Inner Core (bg-black/20)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.35f),
                        Color.Black.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = orbRadius * 0.85f
                ),
                radius = orbRadius * 0.85f,
                center = center
            )
        }

        // Center Dynamic Audio Equalizer Bars (embedded inside the orb core)
        Row(
            modifier = Modifier
                .height(42.dp)
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val barCount = 5
            val barColors = listOf(
                ZoyaCyanBright,
                ZoyaCyan,
                Color.White,
                ZoyaCyan,
                ZoyaCyanBright
            )

            for (i in 0 until barCount) {
                val amp = animatedAmplitude.value
                val offset = (i * 1.1f) + barPhase
                val sinHeight = abs(sin(offset.toDouble())).toFloat()
                val isInteractive = state is ZoyaState.Speaking || state is ZoyaState.Listening || state is ZoyaState.Thinking
                val heightFactor = if (isInteractive) {
                    (0.2f + 0.8f * sinHeight) * amp
                } else {
                    0.25f + 0.15f * sinHeight
                }
                val dynamicHeight = (6.dp + 32.dp * heightFactor).coerceIn(6.dp, 38.dp)

                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .width(4.dp)
                        .height(dynamicHeight)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.White,
                                    barColors[i % barColors.size],
                                    barColors[i % barColors.size].copy(alpha = 0.5f)
                                )
                            )
                        )
                )
            }
        }
    }
}

