package com.example.ui.screens.oneshot

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.Lightbulb
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.OneShotLecture
import com.example.domain.model.StudyMode
import com.example.domain.repository.StudyRepository
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
import com.example.ui.theme.ZoyaEmerald
import com.example.ui.theme.ZoyaEmeraldGlass
import com.example.ui.theme.ZoyaPurpleGlass
import com.example.ui.theme.ZoyaViolet
import com.example.ui.theme.ZoyaVioletBright
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OneShotLectureScreen(
    repository: StudyRepository,
    onClose: () -> Unit,
    onStartQuiz: (String) -> Unit,
    onNavigateMode: (StudyMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentProfile by repository.studentProfile.collectAsState()

    var chapterInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var activeLecture by remember { mutableStateOf<OneShotLecture?>(null) }
    var currentSectionIndex by remember { mutableIntStateOf(0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val suggestedChapters = remember(currentProfile.subject) {
        when (currentProfile.subject.lowercase()) {
            "physics" -> listOf("Optics & Light", "Electricity & Magnetism", "Laws of Motion", "Work, Energy & Power", "Thermodynamics", "Gravitation")
            "chemistry" -> listOf("Chemical Reactions & Equations", "Acids, Bases & Salts", "Periodic Classification", "Carbon & its Compounds", "Structure of Atom")
            "biology" -> listOf("Life Processes", "Control & Coordination", "Genetics & Evolution", "Our Environment", "Cell Biology")
            "mathematics", "math" -> listOf("Quadratic Equations", "Triangles & Trigonometry", "Coordinate Geometry", "Calculus & Derivatives", "Probability & Statistics")
            else -> listOf("Chapter 1: Core Fundamentals", "Chapter 2: Principles & Mechanisms", "Chapter 3: Problem Solving & Applications", "Chapter 4: Exam Mastery")
        }
    }

    fun startLecture(topic: String) {
        if (topic.isBlank()) return
        isLoading = true
        errorMessage = null
        scope.launch {
            val result = repository.generateOneShotLecture(topic)
            isLoading = false
            result.onSuccess { lecture ->
                activeLecture = lecture
                currentSectionIndex = 0
            }.onFailure { err ->
                errorMessage = err.localizedMessage ?: "Failed to generate One-Shot Lecture."
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NovaObsidian)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (activeLecture != null) {
                            activeLecture = null
                        } else {
                            onClose()
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(NovaDarkElevated, CircleShape)
                        .testTag("oneshot_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ZOYA ONE-SHOT LECTURES",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = ZoyaVioletBright,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${currentProfile.boardName} • ${currentProfile.classLevel} • ${currentProfile.subject}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            if (activeLecture == null) {
                // Topic Selector & Launcher
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    GlassmorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ZoyaViolet.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
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
                                        imageVector = Icons.Default.ElectricBolt,
                                        contentDescription = "Bolt",
                                        tint = ZoyaVioletBright,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Understand in One Session",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Complete 14-part curriculum master breakdown",
                                        fontSize = 12.sp,
                                        color = TextCyanSub
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            OutlinedTextField(
                                value = chapterInput,
                                onValueChange = { chapterInput = it },
                                placeholder = { Text("Enter Chapter / Topic (e.g. Light Reflection)") },
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

                            Button(
                                onClick = { startLecture(chapterInput) },
                                enabled = chapterInput.isNotBlank() && !isLoading,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = ZoyaVioletBright),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = NovaObsidian,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generating 14-Part Lecture...", color = NovaObsidian, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = NovaObsidian)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("START ONE-SHOT LECTURE", color = NovaObsidian, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = errorMessage!!, color = ZoyaCoral, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "SUGGESTED CHAPTERS FOR ${currentProfile.subject.uppercase()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        suggestedChapters.forEach { chapter ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(NovaDarkElevated)
                                    .border(1.dp, NovaBorderGlow, RoundedCornerShape(14.dp))
                                    .clickable {
                                        chapterInput = chapter
                                        startLecture(chapter)
                                    }
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.Lightbulb,
                                            contentDescription = "Chapter",
                                            tint = ZoyaCyanBright,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = chapter,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Start",
                                        tint = TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            } else {
                // Active 14-Section Lecture Viewer
                val lecture = activeLecture!!
                val sections = lecture.sections

                Column(modifier = Modifier.fillMaxSize()) {
                    // Section Selector Tabs
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NovaDarkElevated)
                            .padding(vertical = 10.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(sections) { idx, sec ->
                            val isSelected = idx == currentSectionIndex
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) ZoyaVioletBright else NovaDarkSurface)
                                    .border(
                                        1.dp,
                                        if (isSelected) ZoyaVioletBright else NovaBorderGlow,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { currentSectionIndex = idx }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${sec.sectionNumber}. ${sec.title}",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) NovaObsidian else TextSecondary
                                )
                            }
                        }
                    }

                    // Section Content Area
                    val currentSec = sections.getOrElse(currentSectionIndex) { sections.first() }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Section Title Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "PART ${currentSec.sectionNumber} OF ${sections.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ZoyaVioletBright,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = lecture.topic,
                                fontSize = 12.sp,
                                color = TextCyanSub,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = currentSec.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Body Content
                        GlassmorphicCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, NovaBorderGlow, RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = currentSec.body,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    lineHeight = 22.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Navigation buttons between sections
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (currentSectionIndex > 0) {
                                Button(
                                    onClick = { currentSectionIndex-- },
                                    colors = ButtonDefaults.buttonColors(containerColor = NovaDarkElevated),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev", tint = TextPrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Previous Part", color = TextPrimary)
                                }
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            if (currentSectionIndex < sections.size - 1) {
                                Button(
                                    onClick = { currentSectionIndex++ },
                                    colors = ButtonDefaults.buttonColors(containerColor = ZoyaVioletBright),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Next Part", color = NovaObsidian, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", tint = NovaObsidian)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        onStartQuiz(lecture.topic)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ZoyaEmerald),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.SportsEsports, contentDescription = "Quiz", tint = NovaObsidian)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Test with Quiz", color = NovaObsidian, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Quick Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    onStartQuiz(lecture.topic)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = ZoyaPurpleGlass),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Quiz on Chapter", color = ZoyaVioletBright, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        repository.generateFlashcards(lecture.topic, 5)
                                        onNavigateMode(StudyMode.FlashcardDeck)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = ZoyaCyanGlass),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Make Flashcards", color = ZoyaCyanBright, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}
