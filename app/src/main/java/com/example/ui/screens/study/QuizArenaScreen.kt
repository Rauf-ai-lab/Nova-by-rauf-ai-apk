package com.example.ui.screens.study

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.QuestionType
import com.example.domain.model.QuizQuestion
import com.example.domain.model.QuizSession
import com.example.domain.repository.StudyRepository
import com.example.ui.components.FuturisticButton
import com.example.ui.components.GlassmorphicCard
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuizArenaScreen(
    repository: StudyRepository,
    onClose: () -> Unit,
    onSpeakText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var topicInput by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf("Medium") }
    var selectedTypeFilter by remember { mutableStateOf("All Types") }
    var selectedQuestionCount by remember { mutableIntStateOf(4) }
    var isLoading by remember { mutableStateOf(false) }
    var isEvaluatingShortAnswer by remember { mutableStateOf(false) }
    var currentQuiz by remember { mutableStateOf<QuizSession?>(null) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var quizFinished by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Interactive inputs for text-based questions
    var textAnswerInput by remember { mutableStateOf("") }

    val difficulties = listOf("Easy", "Medium", "Hard")
    val questionTypeOptions = listOf("All Types", "Multiple Choice", "True / False", "Fill in the Blank", "Short Answer")
    val questionCounts = listOf(3, 4, 6, 8)
    val quickTopics = listOf(
        "Thermodynamics",
        "Organic Chemistry",
        "Calculus & Integrals",
        "Cellular Respiration",
        "Data Structures",
        "Quantum Physics",
        "Macroeconomics",
        "World History"
    )

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    fun startQuiz(topic: String) {
        if (topic.isBlank()) return
        isLoading = true
        errorMessage = null
        currentQuiz = null
        currentIndex = 0
        score = 0
        quizFinished = false
        textAnswerInput = ""

        scope.launch {
            val res = repository.generateQuiz(
                topic = topic,
                count = selectedQuestionCount,
                difficulty = selectedDifficulty,
                questionTypeFilter = selectedTypeFilter
            )
            isLoading = false
            res.onSuccess { session ->
                if (session.questions.isNotEmpty()) {
                    currentQuiz = session
                } else {
                    errorMessage = "Zoya couldn't generate questions for this topic. Try another!"
                }
            }.onFailure {
                errorMessage = it.localizedMessage ?: "Failed to generate quiz. Check network or API key."
            }
        }
    }

    fun submitShortAnswer(question: QuizQuestion) {
        if (textAnswerInput.isBlank()) return
        isEvaluatingShortAnswer = true
        focusManager.clearFocus()

        scope.launch {
            val evalResult = repository.evaluateShortAnswer(
                question = question.question,
                modelAnswer = question.correctAnswerText.ifEmpty { question.explanation },
                studentAnswer = textAnswerInput
            )
            isEvaluatingShortAnswer = false
            evalResult.onSuccess { eval ->
                question.userTextAnswer = textAnswerInput
                question.answered = true
                question.isCorrect = eval.isCorrect
                question.evaluationFeedback = eval.scoreGrade
                if (eval.isCorrect) {
                    score++
                }
            }.onFailure {
                // Fallback local evaluation if offline
                val matchesKeyword = question.correctAnswerText.split(" ")
                    .any { word -> word.length > 3 && textAnswerInput.contains(word, ignoreCase = true) }
                question.userTextAnswer = textAnswerInput
                question.answered = true
                question.isCorrect = matchesKeyword
                question.evaluationFeedback = if (matchesKeyword) "Good attempt" else "Needs review"
                if (matchesKeyword) score++
            }
        }
    }

    fun submitFillInBlank(question: QuizQuestion) {
        if (textAnswerInput.isBlank()) return
        focusManager.clearFocus()
        val expected = question.correctAnswerText.trim().lowercase()
        val cleanedInput = textAnswerInput.trim().lowercase()
        val isMatch = cleanedInput == expected || cleanedInput.contains(expected) || expected.contains(cleanedInput)
        
        question.userTextAnswer = textAnswerInput
        question.answered = true
        question.isCorrect = isMatch
        if (isMatch) score++
    }

    Surface(modifier = modifier.fillMaxSize(), color = NovaObsidian) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ZoyaPurpleGlass)
                            .border(1.dp, ZoyaViolet.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = null,
                            tint = ZoyaVioletBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Quiz Generator & Arena",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "AI-Driven Diagnostics & Concept Feedback",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextCyanSub,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_quiz_arena")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (currentQuiz == null) {
                // ==================== QUIZ SETUP PANEL ====================
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = NovaBorderGlow,
                    cornerRadius = 20.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "CUSTOMIZE YOUR QUIZ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ZoyaCyanBright,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                fontSize = 10.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Topic input field
                        OutlinedTextField(
                            value = topicInput,
                            onValueChange = { topicInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("quiz_topic_input"),
                            placeholder = {
                                Text("Enter any academic topic or formula...", color = TextMuted, fontSize = 13.sp)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ZoyaCyan,
                                unfocusedBorderColor = NovaBorderGlow,
                                focusedContainerColor = NovaDarkElevated,
                                unfocusedContainerColor = NovaDarkElevated,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = { startQuiz(topicInput) }),
                            trailingIcon = {
                                if (topicInput.isNotBlank()) {
                                    IconButton(onClick = { topicInput = "" }) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Picks
                        Text(
                            text = "POPULAR TOPICS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 9.5.sp,
                                letterSpacing = 0.8.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(quickTopics) { pick ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(NovaDarkSurface)
                                        .border(1.dp, ZoyaCyan.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                                        .clickable {
                                            topicInput = pick
                                        }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = pick,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (topicInput == pick) ZoyaCyanBright else TextSecondary,
                                                fontWeight = if (topicInput == pick) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Difficulty Level Selector
                Text(
                    text = "DIFFICULTY LEVEL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 10.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    difficulties.forEach { diff ->
                        val isSelected = diff == selectedDifficulty
                        val diffColor = when (diff) {
                            "Easy" -> ZoyaEmerald
                            "Medium" -> ZoyaCyan
                            else -> ZoyaCoral
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) diffColor.copy(alpha = 0.2f) else NovaDarkElevated)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) diffColor else NovaBorderGlow,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedDifficulty = diff }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = diff,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) diffColor else TextPrimary,
                                        fontSize = 13.sp
                                    )
                                )
                                Text(
                                    text = when (diff) {
                                        "Easy" -> "Foundational"
                                        "Medium" -> "Application"
                                        else -> "Advanced"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextMuted,
                                        fontSize = 9.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Question Types Filter
                Text(
                    text = "SUPPORTED QUESTION TYPES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 10.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    questionTypeOptions.forEach { qType ->
                        val isSelected = qType == selectedTypeFilter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ZoyaViolet.copy(alpha = 0.25f) else NovaDarkElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) ZoyaVioletBright else NovaBorderGlow,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedTypeFilter = qType }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = qType,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) ZoyaVioletBright else TextPrimary,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Question Count
                Text(
                    text = "NUMBER OF QUESTIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 10.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    questionCounts.forEach { count ->
                        val isSelected = count == selectedQuestionCount
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ZoyaCyan.copy(alpha = 0.2f) else NovaDarkElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) ZoyaCyan else NovaBorderGlow,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedQuestionCount = count }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$count Questions",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) ZoyaCyanBright else TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Generate Button
                FuturisticButton(
                    text = if (isLoading) "Zoya is Generating Quiz..." else "Generate Quiz with Zoya",
                    onClick = { startQuiz(topicInput) },
                    enabled = !isLoading && topicInput.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("start_quiz_btn")
                )

                if (isLoading) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = ZoyaCyan,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Zoya is formulating questions, mistake traps & weak concept maps...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextCyanSub,
                                    fontSize = 12.5.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                errorMessage?.let { err ->
                    Spacer(modifier = Modifier.height(16.dp))
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = ZoyaCoral.copy(alpha = 0.4f)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Outlined.Warning, contentDescription = null, tint = ZoyaCoral)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = err, color = ZoyaCoral, fontSize = 12.5.sp)
                        }
                    }
                }
            } else {
                val session = currentQuiz!!
                if (quizFinished) {
                    // ==================== QUIZ COMPLETED DIAGNOSTIC SUMMARY ====================
                    val weakConcepts = session.questions
                        .filter { !it.isCorrect || it.weakConcept.isNotBlank() }
                        .map { it.weakConcept.ifEmpty { session.topic } }
                        .distinct()

                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = ZoyaCyan.copy(alpha = 0.5f),
                        cornerRadius = 24.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(if (score >= session.questions.size / 2) ZoyaAmberGlass else ZoyaPurpleGlass),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.EmojiEvents,
                                    contentDescription = null,
                                    tint = if (score >= session.questions.size / 2) ZoyaAmber else ZoyaVioletBright,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "QUIZ DIAGNOSTIC COMPLETE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextMuted,
                                    letterSpacing = 1.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = session.topic,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$score / ${session.questions.size}",
                                style = MaterialTheme.typography.displayMedium.copy(
                                    color = if (score == session.questions.size) ZoyaEmerald else if (score >= session.questions.size / 2) ZoyaCyanBright else ZoyaAmber,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 36.sp
                                )
                            )
                            val percentage = ((score.toFloat() / session.questions.size) * 100).toInt()
                            Text(
                                text = "$percentage% Mastery • ${session.difficulty} Level",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Zoya summary critique
                            val zoyaSummary = when {
                                score == session.questions.size -> "Flawless mastery! You demonstrated deep conceptual clarity across all question types."
                                score >= session.questions.size / 2 -> "Solid foundation! A few edge cases caught you off guard. Focus on the identified weak concepts below."
                                else -> "Great active recall practice! Review the explanations and mistake breakdowns below to solidify your understanding."
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(NovaDarkSurface)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Zoya: \"$zoyaSummary\"",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextCyanSub,
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 12.5.sp,
                                        lineHeight = 18.sp
                                    )
                                )
                            }

                            // Weak Concepts to Focus on
                            if (weakConcepts.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Psychology,
                                            contentDescription = null,
                                            tint = ZoyaAmber,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "IDENTIFIED WEAK CONCEPTS TO REINFORCE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = ZoyaAmber,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.8.sp,
                                                fontSize = 9.5.sp
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        weakConcepts.forEach { concept ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(ZoyaAmberGlass)
                                                    .border(1.dp, ZoyaAmber.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = "• $concept",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = TextPrimary,
                                                        fontSize = 11.5.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            FuturisticButton(
                                text = "Create Another Quiz",
                                onClick = { currentQuiz = null },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "restart_quiz_btn"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Review All Questions
                    Text(
                        text = "QUESTION BY QUESTION REVIEW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    session.questions.forEachIndexed { qIdx, question ->
                        GlassmorphicCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            borderColor = if (question.isCorrect) ZoyaEmerald.copy(alpha = 0.4f) else ZoyaCoral.copy(alpha = 0.4f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Q${qIdx + 1} • ${question.type.displayName}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = ZoyaCyanBright,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (question.isCorrect) ZoyaEmeraldGlass else ZoyaCoral.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = if (question.isCorrect) "CORRECT" else "MISSED",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (question.isCorrect) ZoyaEmerald else ZoyaCoral,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = question.question,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Explanation: ${question.explanation}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                )
                                if (question.mistakeAnalysis.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Mistake Note: ${question.mistakeAnalysis}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = ZoyaAmber,
                                            fontSize = 11.5.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // ==================== ACTIVE QUIZ QUESTION ====================
                    val q = session.questions[currentIndex]

                    // Top Bar Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NovaDarkElevated)
                                    .border(1.dp, ZoyaCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Q ${currentIndex + 1} of ${session.questions.size}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ZoyaCyanBright,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ZoyaPurpleGlass)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = q.type.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ZoyaVioletBright,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        // Score Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(ZoyaEmeraldGlass)
                                .border(1.dp, ZoyaEmerald.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Score: $score",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ZoyaEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Question Box
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = ZoyaCyan.copy(alpha = 0.3f),
                        cornerRadius = 18.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = q.question,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 22.sp,
                                    fontSize = 15.5.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ==================== INTERACTION AREA BY QUESTION TYPE ====================
                    when (q.type) {
                        QuestionType.MULTIPLE_CHOICE -> {
                            q.options.forEachIndexed { optIdx, optText ->
                                val isSelected = q.selectedIndex == optIdx
                                val isCorrect = optIdx == q.correctIndex
                                val borderCol = when {
                                    q.answered && isCorrect -> ZoyaEmerald
                                    q.answered && isSelected && !isCorrect -> ZoyaCoral
                                    isSelected -> ZoyaVioletBright
                                    else -> NovaBorderGlow
                                }
                                val bgCol = when {
                                    q.answered && isCorrect -> ZoyaEmeraldGlass
                                    q.answered && isSelected && !isCorrect -> ZoyaCoral.copy(alpha = 0.15f)
                                    isSelected -> ZoyaViolet.copy(alpha = 0.15f)
                                    else -> NovaDarkElevated
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(bgCol)
                                        .border(if (q.answered && (isCorrect || isSelected)) 1.5.dp else 1.dp, borderCol, RoundedCornerShape(14.dp))
                                        .clickable(enabled = !q.answered) {
                                            q.selectedIndex = optIdx
                                            q.answered = true
                                            q.isCorrect = (optIdx == q.correctIndex)
                                            if (q.isCorrect) score++
                                        }
                                        .padding(14.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val letter = when (optIdx) {
                                            0 -> "A"
                                            1 -> "B"
                                            2 -> "C"
                                            else -> "D"
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (q.answered && isCorrect) ZoyaEmerald else NovaCardGlass
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = letter,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (q.answered && isCorrect) NovaObsidian else TextPrimary
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = optText,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = TextPrimary,
                                                fontSize = 13.5.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        QuestionType.TRUE_FALSE -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                listOf("True" to 0, "False" to 1).forEach { (label, idx) ->
                                    val isSelected = q.selectedIndex == idx
                                    val isCorrect = idx == q.correctIndex
                                    val borderCol = when {
                                        q.answered && isCorrect -> ZoyaEmerald
                                        q.answered && isSelected && !isCorrect -> ZoyaCoral
                                        isSelected -> ZoyaCyan
                                        else -> NovaBorderGlow
                                    }
                                    val bgCol = when {
                                        q.answered && isCorrect -> ZoyaEmeraldGlass
                                        q.answered && isSelected && !isCorrect -> ZoyaCoral.copy(alpha = 0.15f)
                                        isSelected -> ZoyaCyanGlass
                                        else -> NovaDarkElevated
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(bgCol)
                                            .border(1.5.dp, borderCol, RoundedCornerShape(16.dp))
                                            .clickable(enabled = !q.answered) {
                                                q.selectedIndex = idx
                                                q.answered = true
                                                q.isCorrect = (idx == q.correctIndex)
                                                if (q.isCorrect) score++
                                            }
                                            .padding(vertical = 18.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (idx == 0) Icons.Default.Done else Icons.Default.Close,
                                                contentDescription = null,
                                                tint = if (idx == 0) ZoyaEmerald else ZoyaCoral,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary,
                                                    fontSize = 15.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        QuestionType.FILL_IN_BLANKS -> {
                            if (!q.answered) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = textAnswerInput,
                                        onValueChange = { textAnswerInput = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("fill_in_blank_input"),
                                        placeholder = {
                                            Text("Type the missing term or formula here...", color = TextMuted, fontSize = 13.sp)
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = ZoyaCyan,
                                            unfocusedBorderColor = NovaBorderGlow,
                                            focusedContainerColor = NovaDarkElevated,
                                            unfocusedContainerColor = NovaDarkElevated,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = { submitFillInBlank(q) })
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    FuturisticButton(
                                        text = "Check Answer",
                                        onClick = { submitFillInBlank(q) },
                                        enabled = textAnswerInput.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth(),
                                        testTag = "submit_blank_btn"
                                    )
                                }
                            } else {
                                GlassmorphicCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    borderColor = if (q.isCorrect) ZoyaEmerald else ZoyaCoral
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "Your Answer: ${q.userTextAnswer}",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                                        )
                                        if (!q.isCorrect) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Expected: ${q.correctAnswerText}",
                                                style = MaterialTheme.typography.bodyMedium.copy(color = ZoyaEmerald, fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        QuestionType.SHORT_ANSWER -> {
                            if (!q.answered) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = textAnswerInput,
                                        onValueChange = { textAnswerInput = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp)
                                            .testTag("short_answer_input"),
                                        placeholder = {
                                            Text("Explain the concept or mechanism concisely...", color = TextMuted, fontSize = 13.sp)
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = ZoyaVioletBright,
                                            unfocusedBorderColor = NovaBorderGlow,
                                            focusedContainerColor = NovaDarkElevated,
                                            unfocusedContainerColor = NovaDarkElevated,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        maxLines = 4
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    FuturisticButton(
                                        text = if (isEvaluatingShortAnswer) "Zoya is evaluating response..." else "Submit for AI Evaluation",
                                        onClick = { submitShortAnswer(q) },
                                        enabled = !isEvaluatingShortAnswer && textAnswerInput.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth(),
                                        testTag = "submit_short_answer_btn"
                                    )
                                }
                            } else {
                                GlassmorphicCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    borderColor = if (q.isCorrect) ZoyaEmerald else ZoyaAmber
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "Your Answer: \"${q.userTextAnswer}\"",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontStyle = FontStyle.Italic)
                                        )
                                        if (q.evaluationFeedback.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Zoya Evaluation: ${q.evaluationFeedback}",
                                                style = MaterialTheme.typography.labelSmall.copy(color = if (q.isCorrect) ZoyaEmerald else ZoyaAmber, fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ==================== ZOYA'S POST-ANSWER FEEDBACK (MANDATORY REQUIREMENT) ====================
                    if (q.answered) {
                        Spacer(modifier = Modifier.height(16.dp))

                        GlassmorphicCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = if (q.isCorrect) ZoyaEmerald.copy(alpha = 0.5f) else ZoyaCoral.copy(alpha = 0.5f),
                            cornerRadius = 20.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Status & Audio Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(if (q.isCorrect) ZoyaEmerald else ZoyaCoral),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (q.isCorrect) Icons.Default.Check else Icons.Default.Close,
                                                contentDescription = null,
                                                tint = NovaObsidian,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (q.isCorrect) "CORRECT!" else "INCORRECT / NEEDS WORK",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                color = if (q.isCorrect) ZoyaEmerald else ZoyaCoral,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }

                                    // Voice read button
                                    IconButton(
                                        onClick = {
                                            val speechText = "Here is the explanation for this question. ${q.explanation}. ${q.mistakeAnalysis}. Make sure to review the concept of ${q.weakConcept}."
                                            onSpeakText(speechText)
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(ZoyaCyanGlass)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Listen to Zoya",
                                            tint = ZoyaCyanBright,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Section 1: Zoya's Explanation of Correctness
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "ZOYA'S CONCEPTUAL EXPLANATION",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = ZoyaCyanBright,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.5.sp,
                                            letterSpacing = 0.8.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = q.explanation,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        )
                                    )
                                }

                                // Section 2: Mistake Elaboration
                                if (q.mistakeAnalysis.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(NovaDarkSurface)
                                            .padding(10.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Outlined.Warning,
                                                contentDescription = null,
                                                tint = ZoyaAmber,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "COMMON MISTAKES & TRAPS ELABORATION",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = ZoyaAmber,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp,
                                                    letterSpacing = 0.5.sp
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = q.mistakeAnalysis,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = TextSecondary,
                                                fontSize = 12.sp,
                                                lineHeight = 16.sp
                                            )
                                        )
                                    }
                                }

                                // Section 3: Weak Concept Identification
                                if (q.weakConcept.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ZoyaPurpleGlass)
                                            .border(1.dp, ZoyaViolet.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Psychology,
                                            contentDescription = null,
                                            tint = ZoyaVioletBright,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Weak Concept to Focus On: ${q.weakConcept}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Next Question or Finish Button
                        FuturisticButton(
                            text = if (currentIndex < session.questions.size - 1) "Next Question" else "View Diagnostic Report",
                            onClick = {
                                textAnswerInput = ""
                                if (currentIndex < session.questions.size - 1) {
                                    currentIndex++
                                } else {
                                    quizFinished = true
                                    scope.launch {
                                        repository.recordQuizSession(session)
                                        repository.logSession(
                                            topic = session.topic,
                                            mode = "Quiz Arena",
                                            summary = "Score: $score / ${session.questions.size} (${session.difficulty})",
                                            durationSec = 200
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("next_quiz_q_btn")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
