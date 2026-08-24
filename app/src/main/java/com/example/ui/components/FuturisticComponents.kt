package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ZoyaState
import com.example.ui.theme.NovaBorderGlow
import com.example.ui.theme.NovaCardGlass
import com.example.ui.theme.NovaDarkElevated
import com.example.ui.theme.NovaDarkSurface
import com.example.ui.theme.NovaObsidian
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ZoyaAmber
import com.example.ui.theme.ZoyaBlueGradient
import com.example.ui.theme.ZoyaCoral
import com.example.ui.theme.ZoyaCyan
import com.example.ui.theme.ZoyaCyanBright
import com.example.ui.theme.ZoyaCyanDeep
import com.example.ui.theme.ZoyaElectricBlue
import com.example.ui.theme.ZoyaEmerald
import com.example.ui.theme.ZoyaIndigoGradient
import com.example.ui.theme.ZoyaViolet
import com.example.ui.theme.ZoyaVioletBright

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    borderColor: Color = NovaBorderGlow,
    cornerRadius: Dp = 24.dp,
    backgroundColor: Color = NovaCardGlass,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(cornerRadius)
            ),
        color = backgroundColor,
        shape = RoundedCornerShape(cornerRadius)
    ) {
        content()
    }
}

@Composable
fun FuturisticButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isSecondary: Boolean = false,
    testTag: String = "futuristic_button"
) {
    val bgBrush = if (isSecondary) {
        Brush.horizontalGradient(listOf(NovaCardGlass, NovaDarkElevated))
    } else {
        Brush.horizontalGradient(listOf(ZoyaCyanDeep, ZoyaCyan, ZoyaBlueGradient))
    }

    val contentColor = if (isSecondary) ZoyaCyanBright else Color.Black

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .testTag(testTag)
            .height(52.dp)
            .border(
                width = 1.dp,
                color = if (isSecondary) NovaBorderGlow else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = contentColor,
            disabledContainerColor = NovaDarkElevated.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgBrush, shape = RoundedCornerShape(20.dp))
                .padding(vertical = 14.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}

@Composable
fun StudyTopBar(
    onSettingsClick: () -> Unit,
    state: ZoyaState,
    title: String = "Study Assistant",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "NOVA BY RAUF",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = ZoyaCyan,
                    fontSize = 10.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    letterSpacing = (-0.2).sp
                )
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            ZoyaStateBadge(state = state)
            Spacer(modifier = Modifier.width(10.dp))
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .testTag("settings_button")
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NovaCardGlass)
                    .border(1.dp, NovaBorderGlow, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextPrimary.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ZoyaStateBadge(
    state: ZoyaState,
    modifier: Modifier = Modifier
) {
    val (label, color) = when (state) {
        is ZoyaState.Idle -> "READY" to ZoyaEmerald
        is ZoyaState.Listening -> "LISTENING" to ZoyaCyan
        is ZoyaState.Thinking -> "THINKING" to ZoyaVioletBright
        is ZoyaState.Speaking -> "SPEAKING" to ZoyaCyanBright
        is ZoyaState.Disconnected -> "OFFLINE" to TextMuted
        is ZoyaState.Error -> "ALERT" to ZoyaCoral
    }

    val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, NovaBorderGlow, RoundedCornerShape(20.dp)),
        color = NovaCardGlass
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FiberManualRecord,
                contentDescription = null,
                tint = color.copy(alpha = if (state !is ZoyaState.Disconnected) pulseAlpha else 0.5f),
                modifier = Modifier.size(8.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = color,
                    letterSpacing = 0.8.sp,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
fun WaveformVisualizer(
    amplitude: Float,
    barCount: Int = 16,
    modifier: Modifier = Modifier,
    activeColor: Color = ZoyaCyan
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_osc")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in 0 until barCount) {
            val normalizedIdx = i.toFloat() / barCount
            val sinVal = kotlin.math.sin(normalizedIdx * 3.14f * 2f + waveOffset)
            val dynamicHeight = (4.dp + (32.dp * amplitude * (0.35f + 0.65f * kotlin.math.abs(sinVal.toFloat())))).coerceIn(4.dp, 36.dp)

            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .width(3.5.dp)
                    .height(dynamicHeight)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                ZoyaCyanBright,
                                ZoyaCyan,
                                ZoyaCyanDeep.copy(alpha = 0.5f)
                            )
                        )
                    )
            )
        }
    }
}

