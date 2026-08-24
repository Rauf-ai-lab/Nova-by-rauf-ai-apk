package com.example.ui.screens.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ai.LiveSessionManager
import com.example.domain.model.ChatMessage
import com.example.domain.model.Speaker
import com.example.domain.model.StudyMode
import com.example.domain.model.ZoyaState
import com.example.domain.repository.StudyRepository
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.WaveformVisualizer
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

@OptIn(ExperimentalLayoutApi::class)
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
    val studentProfile by repository.studentProfile.collectAsState()
    val isListening by liveSessionManager.speechInput.isListening.collectAsState()
    val recognizedSpeechText by liveSessionManager.speechInput.partialTranscript.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val recordAudioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true
        if (recordAudioGranted) {
            liveSessionManager.toggleMicrophone(true)
        }
    }

    fun handleMicClick() {
        val hasAudioPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasAudioPerm) {
            permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
        } else {
            liveSessionManager.toggleMicrophone()
        }
    }

    fun handleSend() {
        val message = textInput.ifBlank { recognizedSpeechText }
        if (message.isNotBlank()) {
            liveSessionManager.sendTextQuestion(message)
            textInput = ""
            focusManager.clearFocus()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NovaObsidian)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp)
        ) {
            // TOP HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "NOVA",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = " BY RAUF",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZoyaCyanBright
                        )
                    }
                    Text(
                        text = "AI Study Companion: ZOYA",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = { onNavigateMode(StudyMode.Settings) },
                    modifier = Modifier
                        .size(40.dp)
                        .background(NovaDarkElevated, CircleShape)
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // STUDENT PROFILE BOARD BADGE
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ZoyaCyanGlass)
                        .border(1.dp, ZoyaCyanGlow, RoundedCornerShape(14.dp))
                        .clickable { onNavigateMode(StudyMode.YourBoard) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("board_badge")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Board",
                                tint = ZoyaCyanBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${studentProfile.boardName} • ${studentProfile.classLevel} • ${studentProfile.subject}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "Your Board →",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZoyaCyanBright
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // MAIN GREETING
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Ready to learn?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Text(
                    text = "Ask any doubt, concept, formula, or problem.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // QUESTION INPUT BAR (Voice Input -> Text Output)
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (isListening) ZoyaCyanBright else NovaBorderGlow, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (isListening) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(ZoyaCoral, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Listening to your voice...",
                                    fontSize = 12.sp,
                                    color = ZoyaCyanBright,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = if (isListening && recognizedSpeechText.isNotBlank()) recognizedSpeechText else textInput,
                                onValueChange = { textInput = it },
                                placeholder = {
                                    Text(
                                        "Ask Zoya a question, concept or homework doubt...",
                                        fontSize = 13.sp,
                                        color = TextMuted
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("question_input_field"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                singleLine = false,
                                maxLines = 4,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = { handleSend() })
                            )

                            // Mic Button
                            IconButton(
                                onClick = { handleMicClick() },
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(if (isListening) ZoyaCoral else ZoyaPurpleGlass, CircleShape)
                                    .border(1.dp, if (isListening) ZoyaCoral else ZoyaViolet, CircleShape)
                                    .testTag("voice_input_mic_button")
                            ) {
                                Icon(
                                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "Voice Input",
                                    tint = if (isListening) Color.White else ZoyaVioletBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Send Button
                            IconButton(
                                onClick = { handleSend() },
                                enabled = textInput.isNotBlank() || recognizedSpeechText.isNotBlank(),
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(ZoyaCyan, CircleShape)
                                    .testTag("send_question_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = NovaObsidian,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // THINKING STATE
            if (zoyaState is ZoyaState.Thinking) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = ZoyaCyanBright,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Zoya is analyzing your question for ${studentProfile.boardName}...",
                        fontSize = 12.sp,
                        color = TextCyanSub
                    )
                }
            }

            // CHAT CONVERSATION LOG (Voice Input -> Text Output)
            if (chatTranscript.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    chatTranscript.takeLast(4).forEach { msg ->
                        ChatBubbleItem(
                            message = msg,
                            onCopy = { clipboardManager.setText(AnnotatedString(msg.text)) },
                            onSpeak = { liveSessionManager.audioPlayback.speakText(msg.text) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PROMINENT "YOUR BOARD" BANNER
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Brush.horizontalGradient(listOf(ZoyaCyanBright, ZoyaVioletBright)), RoundedCornerShape(20.dp))
                        .clickable { onNavigateMode(StudyMode.YourBoard) }
                        .testTag("your_board_dashboard_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(ZoyaCyanGlass, RoundedCornerShape(14.dp))
                                .border(1.dp, ZoyaCyanGlow, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Board",
                                tint = ZoyaCyanBright,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "YOUR BOARD",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "DASHBOARD",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ZoyaCyanBright
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Your study space, built around your board (${studentProfile.boardName} ${studentProfile.classLevel})",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // QUICK STUDY MODULES
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "STUDY MODES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                val quickModes = listOf(
                    StudyModeCardItem(
                        mode = StudyMode.OneShotLecture,
                        title = "One-Shot Lectures",
                        subtitle = "Master full chapter in 14 steps",
                        icon = Icons.Default.ElectricBolt,
                        iconBgColor = ZoyaPurpleGlass,
                        iconTint = ZoyaVioletBright
                    ),
                    StudyModeCardItem(
                        mode = StudyMode.QuizArena,
                        title = "Quiz Arena",
                        subtitle = "Instant score & mistake analysis",
                        icon = Icons.Default.SportsEsports,
                        iconBgColor = ZoyaCyanGlass,
                        iconTint = ZoyaCyanBright
                    ),
                    StudyModeCardItem(
                        mode = StudyMode.ConceptExplainer,
                        title = "Concept Explainer",
                        subtitle = "Intuitive analogical breakdowns",
                        icon = Icons.Outlined.Lightbulb,
                        iconBgColor = ZoyaEmeraldGlass,
                        iconTint = ZoyaEmerald
                    ),
                    StudyModeCardItem(
                        mode = StudyMode.StepByStepSolver,
                        title = "Step-by-Step Solver",
                        subtitle = "Clear mathematical working",
                        icon = Icons.Default.Calculate,
                        iconBgColor = ZoyaAmberGlass,
                        iconTint = ZoyaAmber
                    ),
                    StudyModeCardItem(
                        mode = StudyMode.FlashcardDeck,
                        title = "Active Flashcards",
                        subtitle = "Spaced repetition term review",
                        icon = Icons.Default.Style,
                        iconBgColor = ZoyaPurpleGlass,
                        iconTint = ZoyaVioletBright
                    ),
                    StudyModeCardItem(
                        mode = StudyMode.QuickRevision,
                        title = "Rapid Revision",
                        subtitle = "High-yield formulas & facts",
                        icon = Icons.Default.AutoAwesome,
                        iconBgColor = ZoyaCyanGlass,
                        iconTint = ZoyaCyanBright
                    )
                )

                quickModes.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { item ->
                            Box(modifier = Modifier.weight(1f)) {
                                GlassmorphicCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .border(1.dp, NovaBorderGlow, RoundedCornerShape(16.dp))
                                        .clickable { onNavigateMode(item.mode) }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(item.iconBgColor, RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.title,
                                                tint = item.iconTint,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = item.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = item.subtitle,
                                                fontSize = 10.sp,
                                                color = TextMuted,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun ChatBubbleItem(
    message: ChatMessage,
    onCopy: () -> Unit,
    onSpeak: () -> Unit
) {
    val isUser = message.sender == Speaker.USER

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isUser) ZoyaCyanGlass else NovaDarkElevated)
            .border(
                1.dp,
                if (isUser) ZoyaCyanGlow else NovaBorderGlow,
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isUser) "YOU" else "ZOYA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isUser) ZoyaCyanBright else ZoyaVioletBright,
                    letterSpacing = 1.sp
                )

                if (!isUser) {
                    Row {
                        IconButton(onClick = onSpeak, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Speak", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Copy", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message.text,
                fontSize = 13.sp,
                color = TextPrimary,
                lineHeight = 19.sp
            )
        }
    }
}
