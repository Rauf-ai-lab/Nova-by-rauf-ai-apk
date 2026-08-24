package com.example.ui.screens.study

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.NovaBorderGlow
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
import com.example.ui.theme.ZoyaEmerald
import com.example.ui.theme.ZoyaEmeraldGlass
import com.example.ui.theme.ZoyaPurpleGlass
import com.example.ui.theme.ZoyaViolet
import com.example.ui.theme.ZoyaVioletBright
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuizArenaScreen(
    repository: StudyRepository,
    onClose: () -> Unit,
    onSpeakText: (String) -> Unit = {},
    initialTopic: String = "",
    modifier: Modifier = Modifier
) {
    val currentProfile by repository.studentProfile.collectAsState()

    var topicInput by remember { mutableStateOf(initialTopic.ifBlank { "" }) }
    var selectedDifficulty by remember { mutableStateOf("Medium") }
    var selectedTypeFilter by remember { mutableStateOf("All Types") }
    var selectedQuestionCount by remember { mutableIntStateOf(5) }
    var isLoading by remember { mutableStateOf(false) }

    var currentQuiz by remember { mutableStateOf<QuizSession?>(null) }
    var currentIndex by remember { mutableIntStateOf(0) }

    // Real-Time Stats
    var score by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var wrongCount by remember { mutableIntStateOf(0) }
    var attemptedCount by remember { mutableIntStateOf(0) }

    var isAnswerLocked by remember { mutableStateOf(false) }
    var selectedOptionIndex by remember { mutableIntStateOf(-1) }
    var showFeedback by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("") }
    var isFeedbackCorrect by remember { mutableStateOf(false) }
    var quizFinished by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var textAnswerInput by remember { mutableStateOf("") }
    var isEvaluatingShortAnswer by remember { mutableStateOf(false) }

    val difficulties = listOf("Easy", "Medium", "Hard")
    val questionTypeOptions = listOf("All Types", "Multiple Choice", "True / False", "Fill in the Blank", "Short Answer")
    val questionCounts = listOf(3, 5, 8, 10)

    val quickTopics = remember(currentProfile.subject) {
        when (currentProfile.subject.lowercase()) {
            "physics" -> listOf("Optics & Mirrors", "Electric Circuits", "Ohm's Law", "Thermodynamics", "Gravitation", "Newton's Laws")
            "chemistry" -> listOf("Acids & Bases", "Chemical Reactions", "Periodic Table", "Carbon Compounds", "Atomic Structure")
            "biology" -> listOf("Photosynthesis", "Circulatory System", "Genetics & DNA", "Nervous System", "Ecology")
            "mathematics", "math" -> listOf("Quadratic Equations", "Trigonometry Ratios", "Coordinate Geometry", "Probability", "Arithmetic Progression")
            else -> listOf("Chapter Revision", "Core Definitions", "Important Formulas", "Exam Hotspots")
        }
    }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    fun resetStats() {
        score = 0
        correctCount = 0
        wrongCount = 0
        attemptedCount = 0
        currentIndex = 0
        isAnswerLocked = false
        selectedOptionIndex = -1
        showFeedback = false
        feedbackMessage = ""
        quizFinished = false
    }

    fun startQuiz(topic: String) {
        if (topic.isBlank()) return
        isLoading = true
        errorMessage = null
        currentQuiz = null
        resetStats()

        scope.launch {
            val result = repository.generateQuiz(
                topic = topic,
                count = selectedQuestionCount,
                difficulty = selectedDifficulty,
                questionTypeFilter = selectedTypeFilter
            )
            isLoading = false
            result.onSuccess { session ->
                currentQuiz = session
            }.onFailure { err ->
                errorMessage = err.localizedMessage ?: "Failed to generate quiz."
            }
        }
    }

    fun handleOptionSelected(q: QuizQuestion, optionIdx: Int) {
        if (isAnswerLocked) return
        isAnswerLocked = true
        selectedOptionIndex = optionIdx
        q.selectedIndex = optionIdx
        q.answered = true

        val isCorrect = optionIdx == q.correctIndex
        q.isCorrect = isCorrect
        attemptedCount++

        if (isCorrect) {
            score++
            correctCount++
            isFeedbackCorrect = true
            feedbackMessage = "Correct! Nice one."
            showFeedback = true

            // Smooth auto-slide transition after brief feedback
            scope.launch {
                delay(1200)
                if (currentIndex < (currentQuiz?.questions?.size ?: 0) - 1) {
                    currentIndex++
                    isAnswerLocked = false
                    selectedOptionIndex = -1
                    showFeedback = false
                } else {
                    quizFinished = true
                    currentQuiz?.let { repository.recordQuizSession(it) }
                }
            }
        } else {
            wrongCount++
            isFeedbackCorrect = false
            val correctOpt = q.options.getOrElse(q.correctIndex) { q.correctAnswerText }
            feedbackMessage = "Wrong answer. Correct answer: $correctOpt\nWhy? ${q.explanation}"
            showFeedback = true
        }
    }

    fun handleNextAfterWrong() {
        if (currentIndex < (currentQuiz?.questions?.size ?: 0) - 1) {
            currentIndex++
            isAnswerLocked = false
            selectedOptionIndex = -1
            showFeedback = false
        } else {
            quizFinished = true
            currentQuiz?.let { session ->
                scope.launch { repository.recordQuizSession(session) }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NovaObsidian)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (currentQuiz != null && !quizFinished) {
                                currentQuiz = null
                            } else {
                                onClose()
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(NovaDarkElevated, CircleShape)
                            .testTag("quiz_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "QUIZ ARENA",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = ZoyaVioletBright,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${currentProfile.boardName} • ${currentProfile.subject}",
                            fontSize = 12.sp,
                            color = TextCyanSub
                        )
                    }
                }

                if (currentQuiz != null && !quizFinished) {
                    IconButton(
                        onClick = { currentQuiz?.let { startQuiz(it.topic) } },
                        modifier = Modifier
                            .size(38.dp)
                            .background(NovaDarkElevated, CircleShape)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = TextMuted)
                    }
                }
            }

            // REAL-TIME STATS BAR (When quiz is active)
            if (currentQuiz != null && !quizFinished) {
                val totalQuestions = currentQuiz!!.questions.size
                val currentQNum = currentIndex + 1

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NovaDarkElevated)
                        .border(1.dp, NovaBorderGlow)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Score Pill
                        StatBadge("SCORE", score.toString(), ZoyaCyanBright)
                        StatBadge("CORRECT", correctCount.toString(), ZoyaEmerald)
                        StatBadge("WRONG", wrongCount.toString(), ZoyaCoral)
                        StatBadge("ATTEMPTED", attemptedCount.toString(), ZoyaAmber)
                        StatBadge("QUESTION", "$currentQNum / $totalQuestions", TextPrimary)
                    }
                }
            }

            // MAIN CONTENT
            if (currentQuiz == null) {
                // QUIZ CONFIGURATOR & LAUNCHER
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(modifier = Modifier.height(10.dp))

                    GlassmorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ZoyaPurpleGlass, RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(ZoyaPurpleGlass, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SportsEsports,
                                        contentDescription = "Quiz",
                                        tint = ZoyaVioletBright,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Active AI Recall Challenge",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Instant scoring, animated feedback & weak area analysis",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = topicInput,
                                onValueChange = { topicInput = it },
                                placeholder = { Text("Enter Topic or Chapter...") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ZoyaVioletBright,
                                    unfocusedBorderColor = NovaBorderGlow,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Difficulty
                            Text(text = "DIFFICULTY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                difficulties.forEach { diff ->
                                    val isSel = diff == selectedDifficulty
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) ZoyaVioletBright else NovaDarkElevated)
                                            .clickable { selectedDifficulty = diff }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = diff,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) NovaObsidian else TextSecondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Question Count
                            Text(text = "QUESTIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                questionCounts.forEach { count ->
                                    val isSel = count == selectedQuestionCount
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) ZoyaVioletBright else NovaDarkElevated)
                                            .clickable { selectedQuestionCount = count }
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "$count Qs",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) NovaObsidian else TextSecondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = { startQuiz(topicInput) },
                                enabled = topicInput.isNotBlank() && !isLoading,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = ZoyaVioletBright),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = NovaObsidian, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generating Questions...", color = NovaObsidian, fontWeight = FontWeight.Bold)
                                } else {
                                    Text("START QUIZ ARENA", color = NovaObsidian, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = errorMessage!!, color = ZoyaCoral, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "QUICK TOPICS FOR ${currentProfile.subject.uppercase()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickTopics.forEach { top ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NovaDarkElevated)
                                    .border(1.dp, NovaBorderGlow, RoundedCornerShape(10.dp))
                                    .clickable {
                                        topicInput = top
                                        startQuiz(top)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(text = top, fontSize = 12.sp, color = TextPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            } else if (quizFinished) {
                // QUIZ RESULT SCREEN
                val totalQ = currentQuiz!!.questions.size
                val accuracy = if (totalQ > 0) ((correctCount.toFloat() / totalQ.toFloat()) * 100).toInt() else 0

                val weakTopics = currentQuiz!!.questions
                    .filter { !it.isCorrect }
                    .map { it.weakConcept.ifBlank { it.question.take(30) } }
                    .distinct()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(if (accuracy >= 60) ZoyaEmeraldGlass else ZoyaAmberGlass, CircleShape)
                            .border(2.dp, if (accuracy >= 60) ZoyaEmerald else ZoyaAmber, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EmojiEvents,
                            contentDescription = "Trophy",
                            tint = if (accuracy >= 60) ZoyaEmerald else ZoyaAmber,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (accuracy >= 80) "Mastery Achieved!" else if (accuracy >= 50) "Good Effort!" else "Revision Recommended",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )

                    Text(
                        text = "${currentQuiz!!.topic} • ${currentProfile.boardName}",
                        fontSize = 13.sp,
                        color = TextCyanSub
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Stats Scorecard
                    GlassmorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NovaBorderGlow, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "$score / $totalQ", fontSize = 22.sp, fontWeight = FontWeight.Black, color = ZoyaCyanBright)
                                    Text(text = "Final Score", fontSize = 11.sp, color = TextMuted)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "$accuracy%", fontSize = 22.sp, fontWeight = FontWeight.Black, color = if (accuracy >= 60) ZoyaEmerald else ZoyaAmber)
                                    Text(text = "Accuracy", fontSize = 11.sp, color = TextMuted)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "$wrongCount", fontSize = 22.sp, fontWeight = FontWeight.Black, color = ZoyaCoral)
                                    Text(text = "Incorrect", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                        }
                    }

                    if (weakTopics.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "TOPICS NEEDING REINFORCEMENT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZoyaAmber,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            weakTopics.forEach { weak ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(NovaDarkElevated)
                                        .border(1.dp, ZoyaAmberGlass, RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.Warning, contentDescription = "Weak", tint = ZoyaAmber, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = weak, fontSize = 12.sp, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { startQuiz(currentQuiz!!.topic) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = ZoyaVioletBright),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = NovaObsidian)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry Quiz", color = NovaObsidian, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (weakTopics.isNotEmpty()) {
                                    startQuiz(weakTopics.first())
                                } else {
                                    startQuiz(currentQuiz!!.topic)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = ZoyaCyanGlass),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Practice Weak Areas", color = ZoyaCyanBright, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            } else {
                // ACTIVE QUESTION SLIDER
                val questions = currentQuiz!!.questions
                val question = questions[currentIndex]

                AnimatedContent(
                    targetState = currentIndex,
                    transitionSpec = {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    },
                    label = "quiz_slide"
                ) { qIndex ->
                    val curQ = questions[qIndex]

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Question Card
                        GlassmorphicCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, NovaBorderGlow, RoundedCornerShape(18.dp))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = curQ.type.displayName.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ZoyaCyanBright,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "${curQ.weakConcept}",
                                        fontSize = 11.sp,
                                        color = TextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = curQ.question,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    lineHeight = 22.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Options / Answer area
                        if (curQ.type == QuestionType.MULTIPLE_CHOICE || curQ.type == QuestionType.TRUE_FALSE) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                curQ.options.forEachIndexed { optIdx, optText ->
                                    val isSelected = selectedOptionIndex == optIdx
                                    val isCorrectOpt = optIdx == curQ.correctIndex

                                    val bg = when {
                                        isAnswerLocked && isCorrectOpt -> ZoyaEmeraldGlass
                                        isAnswerLocked && isSelected && !isCorrectOpt -> ZoyaCoral.copy(alpha = 0.25f)
                                        else -> NovaDarkElevated
                                    }

                                    val borderCol = when {
                                        isAnswerLocked && isCorrectOpt -> ZoyaEmerald
                                        isAnswerLocked && isSelected && !isCorrectOpt -> ZoyaCoral
                                        else -> NovaBorderGlow
                                    }

                                    val textCol = when {
                                        isAnswerLocked && isCorrectOpt -> ZoyaEmerald
                                        isAnswerLocked && isSelected && !isCorrectOpt -> ZoyaCoral
                                        else -> TextPrimary
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(bg)
                                            .border(1.dp, borderCol, RoundedCornerShape(14.dp))
                                            .clickable(enabled = !isAnswerLocked) {
                                                handleOptionSelected(curQ, optIdx)
                                            }
                                            .padding(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .background(if (isAnswerLocked && isCorrectOpt) ZoyaEmerald else NovaDarkSurface, CircleShape)
                                                        .border(1.dp, borderCol, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = ('A' + optIdx).toString(),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isAnswerLocked && isCorrectOpt) NovaObsidian else textCol
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = optText,
                                                    fontSize = 14.sp,
                                                    color = textCol,
                                                    fontWeight = if (isSelected || (isAnswerLocked && isCorrectOpt)) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }

                                            if (isAnswerLocked && isCorrectOpt) {
                                                Icon(Icons.Default.Check, contentDescription = "Correct", tint = ZoyaEmerald)
                                            } else if (isAnswerLocked && isSelected && !isCorrectOpt) {
                                                Icon(Icons.Default.Close, contentDescription = "Wrong", tint = ZoyaCoral)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Text-based Question (Fill in blanks / Short answer)
                            Column {
                                OutlinedTextField(
                                    value = textAnswerInput,
                                    onValueChange = { textAnswerInput = it },
                                    placeholder = { Text("Type your answer here...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !isAnswerLocked,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ZoyaCyan,
                                        unfocusedBorderColor = NovaBorderGlow,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        if (textAnswerInput.isNotBlank() && !isAnswerLocked) {
                                            isAnswerLocked = true
                                            isEvaluatingShortAnswer = true
                                            focusManager.clearFocus()
                                            scope.launch {
                                                val eval = repository.evaluateShortAnswer(curQ.question, curQ.correctAnswerText, textAnswerInput)
                                                isEvaluatingShortAnswer = false
                                                attemptedCount++
                                                eval.onSuccess { res ->
                                                    if (res.isCorrect) {
                                                        score++
                                                        correctCount++
                                                        isFeedbackCorrect = true
                                                        feedbackMessage = "Correct! ${res.explanation}"
                                                    } else {
                                                        wrongCount++
                                                        isFeedbackCorrect = false
                                                        feedbackMessage = "Incorrect. Expected: ${curQ.correctAnswerText}\nWhy? ${res.explanation}"
                                                    }
                                                    showFeedback = true
                                                }.onFailure {
                                                    // Fallback check
                                                    val simpleMatch = textAnswerInput.trim().equals(curQ.correctAnswerText.trim(), true)
                                                    if (simpleMatch) {
                                                        score++
                                                        correctCount++
                                                        isFeedbackCorrect = true
                                                        feedbackMessage = "Correct! Great recall."
                                                    } else {
                                                        wrongCount++
                                                        isFeedbackCorrect = false
                                                        feedbackMessage = "Expected answer: ${curQ.correctAnswerText}\n${curQ.explanation}"
                                                    }
                                                    showFeedback = true
                                                }
                                            }
                                        }
                                    },
                                    enabled = textAnswerInput.isNotBlank() && !isAnswerLocked,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = ZoyaCyan),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isEvaluatingShortAnswer) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = NovaObsidian, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Evaluating...", color = NovaObsidian)
                                    } else {
                                        Text("SUBMIT ANSWER", color = NovaObsidian, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // FEEDBACK CARD (Animated visibility)
                        AnimatedVisibility(visible = showFeedback) {
                            Column(modifier = Modifier.padding(top = 16.dp)) {
                                GlassmorphicCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            1.dp,
                                            if (isFeedbackCorrect) ZoyaEmerald else ZoyaCoral,
                                            RoundedCornerShape(16.dp)
                                        )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isFeedbackCorrect) Icons.Default.Check else Icons.Default.Close,
                                                contentDescription = "Status",
                                                tint = if (isFeedbackCorrect) ZoyaEmerald else ZoyaCoral,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (isFeedbackCorrect) "Correct!" else "Explanation",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isFeedbackCorrect) ZoyaEmerald else ZoyaCoral
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = feedbackMessage,
                                            fontSize = 13.sp,
                                            color = TextPrimary,
                                            lineHeight = 18.sp
                                        )

                                        if (!isFeedbackCorrect) {
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Button(
                                                onClick = { handleNextAfterWrong() },
                                                colors = ButtonDefaults.buttonColors(containerColor = ZoyaVioletBright),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("CONTINUE TO NEXT QUESTION", color = NovaObsidian, fontWeight = FontWeight.Black)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", tint = NovaObsidian)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatBadge(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Black, color = color)
    }
}
