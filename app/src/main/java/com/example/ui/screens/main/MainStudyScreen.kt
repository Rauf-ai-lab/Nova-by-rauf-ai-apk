package com.example.ui.screens.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ai.LiveSessionManager
import com.example.domain.model.Speaker
import com.example.domain.model.StudyMode
import com.example.domain.model.ZoyaState
import com.example.domain.repository.StudyRepository
import com.example.service.ZoyaLiveVoiceService
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.StudyTopBar
import com.example.ui.components.WaveformVisualizer
import com.example.ui.components.ZoyaOrbView
import com.example.ui.theme.NovaBorderGlow
import com.example.ui.theme.NovaCardGlass
import com.example.ui.theme.NovaDarkElevated
import com.example.ui.theme.NovaDarkSurface
import com.example.ui.theme.NovaObsidian
import com.example.ui.theme.TextCyanSub
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ZoyaAmber
import com.example.ui.theme.ZoyaAmberGlass
import com.example.ui.theme.ZoyaCoral
import com.example.ui.theme.ZoyaCyan
import com.example.ui.theme.ZoyaCyanBright
import com.example.ui.theme.ZoyaCyanDeep
import com.example.ui.theme.ZoyaCyanGlass
import com.example.ui.theme.ZoyaCyanGlow
import com.example.ui.theme.ZoyaElectricBlue
import com.example.ui.theme.ZoyaEmerald
import com.example.ui.theme.ZoyaEmeraldGlass
import com.example.ui.theme.ZoyaPurpleGlass
import com.example.ui.theme.ZoyaViolet
import com.example.ui.theme.ZoyaVioletBright

data class StudyModeCardItem(
    val mode: StudyMode,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val iconTint: Color
)

@Composable
fun MainStudyScreen(
    liveSessionManager: LiveSessionManager,
    repository: StudyRepository,
    onNavigateMode: (StudyMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val zoyaState by liveSessionManager.zoyaState.collectAsState()
    val chatTranscript by liveSessionManager.chatTranscript.collectAsState()
    val isLiveActive by liveSessionManager.isLiveSessionActive.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var isTextInputVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val recordAudioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true
        if (recordAudioGranted) {
            liveSessionManager.toggleMicrophone(true)
            ZoyaLiveVoiceService.start(context)
        }
    }

    fun handleMicClick() {
        val hasAudioPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasAudioPerm) {
            permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
            return
        }

        if (zoyaState is ZoyaState.Speaking) {
            // Barge-in interruption
            liveSessionManager.handleBargeInInterruption()
        } else if (zoyaState is ZoyaState.Listening) {
            liveSessionManager.toggleMicrophone(false)
        } else {
            if (!isLiveActive) {
                liveSessionManager.startLiveSession()
            }
            liveSessionManager.toggleMicrophone(true)
            ZoyaLiveVoiceService.start(context)
        }
    }

    val studyCards = listOf(
        StudyModeCardItem(
            mode = StudyMode.QuizArena,
            title = "Quiz Generator",
            subtitle = "MC • True/False • Blanks • Short",
            icon = Icons.Default.SportsEsports,
            iconBgColor = ZoyaPurpleGlass,
            iconTint = ZoyaVioletBright
        ),
        StudyModeCardItem(
            mode = StudyMode.FlashcardDeck,
            title = "Flashcards",
            subtitle = "Spaced Repetition • 3D Flip",
            icon = Icons.Default.Style,
            iconBgColor = ZoyaCyanGlass,
            iconTint = ZoyaCyan
        ),
        StudyModeCardItem(
            mode = StudyMode.ConceptExplainer,
            title = "Concept Explainer",
            subtitle = "Breakdowns & Analogies",
            icon = Icons.Outlined.Lightbulb,
            iconBgColor = ZoyaAmberGlass,
            iconTint = ZoyaAmber
        ),
        StudyModeCardItem(
            mode = StudyMode.StepByStepSolver,
            title = "Step-by-Step Solver",
            subtitle = "Math & Physics with 'Why'",
            icon = Icons.Default.Calculate,
            iconBgColor = ZoyaEmeraldGlass,
            iconTint = ZoyaEmerald
        )
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = NovaObsidian
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NovaObsidian)
        ) {
            // Top Header: NOVA BY RAUF | Study Assistant | Settings
            StudyTopBar(
                onSettingsClick = { onNavigateMode(StudyMode.Settings) },
                state = zoyaState,
                title = "Study Assistant"
            )

            // Center Scrollable Body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Hero Area: Orb & Atmospheric Glow
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    ZoyaOrbView(
                        state = zoyaState,
                        size = 250.dp,
                        onClick = { handleMicClick() }
                    )
                }

                // Monologue & State Headline
                val lastZoyaMsg = chatTranscript.lastOrNull { it.sender == Speaker.ZOYA }?.text
                val stateTitle = when (zoyaState) {
                    is ZoyaState.Listening -> "Zoya is listening..."
                    is ZoyaState.Thinking -> "Zoya is thinking..."
                    is ZoyaState.Speaking -> "Zoya is speaking..."
                    is ZoyaState.Error -> "Connection Alert"
                    is ZoyaState.Disconnected -> "Zoya is in standby"
                    else -> "Zoya is ready"
                }

                val quoteText = when {
                    zoyaState is ZoyaState.Listening -> "\"Speak naturally. I'm ready to explain, solve, or drill any topic.\""
                    zoyaState is ZoyaState.Thinking -> "\"Connecting intuitive concepts and formulating step-by-step guidance...\""
                    lastZoyaMsg != null -> "\"$lastZoyaMsg\""
                    else -> "\"Wait. Don't memorize it yet. First understand why it works.\""
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stateTitle,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Light,
                            color = TextPrimary,
                            fontSize = 22.sp,
                            letterSpacing = 0.5.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = quoteText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextCyanSub,
                            fontStyle = FontStyle.Italic,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp,
                            letterSpacing = (-0.1).sp
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Accelerators Grid Cards
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "STUDY ACCELERATORS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.8.sp,
                            fontSize = 10.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2x2 Grid of Accelerators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (i in 0..1) {
                            val card = studyCards[i]
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("study_card_${card.mode.name}")
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(NovaCardGlass)
                                    .border(1.dp, NovaBorderGlow, RoundedCornerShape(24.dp))
                                    .clickable { onNavigateMode(card.mode) }
                                    .padding(16.dp)
                                    .height(112.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(card.iconBgColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = card.icon,
                                            contentDescription = null,
                                            tint = card.iconTint,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = card.title,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary,
                                                fontSize = 13.5.sp
                                            ),
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = card.subtitle,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = TextMuted,
                                                fontSize = 9.5.sp,
                                                letterSpacing = 0.5.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (i in 2..3) {
                            val card = studyCards[i]
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("study_card_${card.mode.name}")
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(NovaCardGlass)
                                    .border(1.dp, NovaBorderGlow, RoundedCornerShape(24.dp))
                                    .clickable { onNavigateMode(card.mode) }
                                    .padding(16.dp)
                                    .height(112.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(card.iconBgColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = card.icon,
                                            contentDescription = null,
                                            tint = card.iconTint,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = card.title,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary,
                                                fontSize = 13.5.sp
                                            ),
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = card.subtitle,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = TextMuted,
                                                fontSize = 9.5.sp,
                                                letterSpacing = 0.5.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Quick Text Input Drawer (Expandable)
            AnimatedVisibility(
                visible = isTextInputVisible,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = NovaDarkElevated
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("main_text_question_input"),
                            placeholder = {
                                Text(
                                    text = "Ask Zoya any study question...",
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ZoyaCyan,
                                unfocusedBorderColor = NovaBorderGlow,
                                focusedContainerColor = NovaDarkSurface,
                                unfocusedContainerColor = NovaDarkSurface,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(20.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (textInput.isNotBlank()) {
                                        liveSessionManager.sendTextQuestion(textInput)
                                        textInput = ""
                                        focusManager.clearFocus()
                                    }
                                }
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    liveSessionManager.sendTextQuestion(textInput)
                                    textInput = ""
                                    focusManager.clearFocus()
                                }
                            },
                            modifier = Modifier
                                .testTag("send_question_btn")
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(ZoyaCyan)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Navigation Pill Bar (Floating Capsule Style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(40.dp))
                        .border(1.dp, NovaBorderGlow, RoundedCornerShape(40.dp)),
                    color = NovaCardGlass,
                    shape = RoundedCornerShape(40.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Explainer Shortcut
                        IconButton(
                            onClick = { onNavigateMode(StudyMode.ConceptExplainer) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lightbulb,
                                contentDescription = "Explain",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Flashcards Shortcut
                        IconButton(
                            onClick = { onNavigateMode(StudyMode.FlashcardDeck) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Style,
                                contentDescription = "Flashcards",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Center Live Mic Orb Trigger (Cyan Glow Pill)
                        Box(
                            modifier = Modifier
                                .testTag("main_mic_button")
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(
                                    when (zoyaState) {
                                        is ZoyaState.Speaking -> Brush.radialGradient(listOf(ZoyaCoral, Color(0xFF991B1B)))
                                        is ZoyaState.Listening -> Brush.radialGradient(listOf(ZoyaCyanBright, ZoyaCyan))
                                        else -> Brush.radialGradient(listOf(ZoyaCyan, ZoyaCyanDeep))
                                    }
                                )
                                .border(
                                    1.5.dp,
                                    if (zoyaState is ZoyaState.Listening) ZoyaCyanBright else Color.White.copy(alpha = 0.2f),
                                    CircleShape
                                )
                                .clickable { handleMicClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (zoyaState) {
                                    is ZoyaState.Speaking -> Icons.Default.Stop
                                    is ZoyaState.Listening -> Icons.Default.Mic
                                    else -> Icons.Default.Mic
                                },
                                contentDescription = "Voice Assistant",
                                tint = if (zoyaState is ZoyaState.Speaking) Color.White else Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Quiz Arena Shortcut
                        IconButton(
                            onClick = { onNavigateMode(StudyMode.QuizArena) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = "Quiz Arena",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Toggle Text Prompt Bar
                        IconButton(
                            onClick = { isTextInputVisible = !isTextInputVisible },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (isTextInputVisible) Icons.Default.Close else Icons.Default.Keyboard,
                                contentDescription = "Toggle Keyboard",
                                tint = if (isTextInputVisible) ZoyaCyan else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

