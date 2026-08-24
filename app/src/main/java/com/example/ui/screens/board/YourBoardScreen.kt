package com.example.ui.screens.board

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.BoardInfo
import com.example.domain.model.ExamModePack
import com.example.domain.model.StudentProfile
import com.example.domain.model.StudyMode
import com.example.domain.model.allSupportedBoards
import com.example.domain.model.defaultClasses
import com.example.domain.model.defaultLanguages
import com.example.domain.model.defaultStates
import com.example.domain.model.defaultSubjectsForClass
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
import com.example.ui.theme.ZoyaElectricBlue
import com.example.ui.theme.ZoyaEmerald
import com.example.ui.theme.ZoyaEmeraldGlass
import com.example.ui.theme.ZoyaPurpleGlass
import com.example.ui.theme.ZoyaViolet
import com.example.ui.theme.ZoyaVioletBright
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun YourBoardScreen(
    repository: StudyRepository,
    onNavigateMode: (StudyMode) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentProfile by repository.studentProfile.collectAsState()
    var showProfileSheet by remember { mutableStateOf(false) }

    // Exam Mode Sheet state
    var showExamPackSheet by remember { mutableStateOf(false) }
    var examPackTopic by remember { mutableStateOf("") }
    var isGeneratingExamPack by remember { mutableStateOf(false) }
    var examPackResult by remember { mutableStateOf<ExamModePack?>(null) }
    var examPackError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val availableSubjects = remember(currentProfile.classLevel) {
        defaultSubjectsForClass(currentProfile.classLevel)
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
                .padding(bottom = 32.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(40.dp)
                            .background(NovaDarkElevated, CircleShape)
                            .testTag("board_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "YOUR BOARD",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = ZoyaCyanBright,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(ZoyaCyanGlass, RoundedCornerShape(6.dp))
                                    .border(1.dp, ZoyaCyanGlow, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = currentProfile.boardName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ZoyaCyanBright
                                )
                            }
                        }
                        Text(
                            text = "Your study space, built around your board",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = { showProfileSheet = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(NovaDarkElevated, CircleShape)
                        .testTag("edit_board_profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = ZoyaCyan
                    )
                }
            }

            // Student Profile Active Card
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Brush.horizontalGradient(listOf(ZoyaCyanGlow, ZoyaPurpleGlass)), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentProfile.boardName,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${currentProfile.state} • ${currentProfile.classLevel}",
                                    fontSize = 13.sp,
                                    color = TextCyanSub
                                )
                            }
                            Button(
                                onClick = { showProfileSheet = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ZoyaCyanGlass),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Change",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ZoyaCyanBright
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Subjects horizontal list
                        Text(
                            text = "ACTIVE SUBJECT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(availableSubjects) { sub ->
                                val isSelected = sub.equals(currentProfile.subject, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) ZoyaCyan else NovaDarkElevated)
                                        .border(
                                            1.dp,
                                            if (isSelected) ZoyaCyanBright else NovaBorderGlow,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            repository.updateStudentProfile(currentProfile.copy(subject = sub))
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = sub,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) NovaObsidian else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // EXAMS ARE CLOSE SECTION
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Exams",
                        tint = ZoyaCoral,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "IF YOUR EXAMS ARE CLOSE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = ZoyaCoral,
                        letterSpacing = 1.sp
                    )
                }

                // 1. One-Shot Lectures Card
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ZoyaVioletBright.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        .clickable {
                            onNavigateMode(StudyMode.OneShotLecture)
                        }
                        .testTag("one_shot_lecture_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(ZoyaPurpleGlass, RoundedCornerShape(14.dp))
                                .border(1.dp, ZoyaViolet, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = "One-Shot",
                                tint = ZoyaVioletBright,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ZOYA ONE-SHOT LECTURES",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Understand an entire chapter in one powerful, structured 14-section master session.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Exam Mode Pack Card
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ZoyaAmber.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        .clickable {
                            showExamPackSheet = true
                        }
                        .testTag("exam_mode_pack_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(ZoyaAmberGlass, RoundedCornerShape(14.dp))
                                .border(1.dp, ZoyaAmber, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Exam Pack",
                                tint = ZoyaAmber,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "EXAM MODE PACK",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "High-priority concepts, most expected questions, common exam traps & formula cheat sheets.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // BOARD STUDY MODULES GRID
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "BOARD STUDY TOOLS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                val modules = listOf(
                    BoardModuleItem("Concept Explainer", "Exam-level conceptual breakdown", Icons.Outlined.Lightbulb, ZoyaCyanGlass, ZoyaCyanBright, StudyMode.ConceptExplainer),
                    BoardModuleItem("Step-by-Step Solver", "Math, Physics & Chem solutions", Icons.Default.Calculate, ZoyaEmeraldGlass, ZoyaEmerald, StudyMode.StepByStepSolver),
                    BoardModuleItem("Quiz Arena", "Score tracking & active AI recall", Icons.Default.SportsEsports, ZoyaPurpleGlass, ZoyaVioletBright, StudyMode.QuizArena),
                    BoardModuleItem("Active Flashcards", "Spaced repetition term review", Icons.Default.Style, ZoyaAmberGlass, ZoyaAmber, StudyMode.FlashcardDeck),
                    BoardModuleItem("Rapid Revision", "Formula sheets & high-yield facts", Icons.Default.AutoAwesome, ZoyaCyanGlass, ZoyaCyanBright, StudyMode.QuickRevision)
                )

                modules.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { item ->
                            Box(modifier = Modifier.weight(1f)) {
                                GlassmorphicCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
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
                                                .size(38.dp)
                                                .background(item.bgTint, RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.title,
                                                tint = item.tint,
                                                modifier = Modifier.size(20.dp)
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
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // Profile Customizer Bottom Sheet
        if (showProfileSheet) {
            ModalBottomSheet(
                onDismissRequest = { showProfileSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = NovaDarkSurface
            ) {
                StudentProfileCustomizerContent(
                    currentProfile = currentProfile,
                    onSave = { updated ->
                        repository.updateStudentProfile(updated)
                        showProfileSheet = false
                    },
                    onClose = { showProfileSheet = false }
                )
            }
        }

        // Exam Mode Pack Sheet
        if (showExamPackSheet) {
            ModalBottomSheet(
                onDismissRequest = { showExamPackSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = NovaDarkSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EXAM MODE PACK",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = ZoyaAmber
                        )
                        IconButton(onClick = { showExamPackSheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    Text(
                        text = "${currentProfile.boardName} • ${currentProfile.classLevel} • ${currentProfile.subject}",
                        fontSize = 12.sp,
                        color = TextCyanSub
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = examPackTopic,
                        onValueChange = { examPackTopic = it },
                        label = { Text("Chapter or Topic (e.g. Optics, Electricity)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZoyaAmber,
                            unfocusedBorderColor = NovaBorderGlow,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (examPackTopic.isNotBlank()) {
                                isGeneratingExamPack = true
                                examPackError = null
                                scope.launch {
                                    val res = repository.generateExamModePack(examPackTopic)
                                    isGeneratingExamPack = false
                                    res.onSuccess {
                                        examPackResult = it
                                    }.onFailure {
                                        examPackError = it.localizedMessage ?: "Failed to generate exam pack."
                                    }
                                }
                            }
                        },
                        enabled = examPackTopic.isNotBlank() && !isGeneratingExamPack,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ZoyaAmber),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isGeneratingExamPack) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = NovaObsidian, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Assembling Exam Pack...", color = NovaObsidian, fontWeight = FontWeight.Bold)
                        } else {
                            Text("GENERATE EXAM PACK", color = NovaObsidian, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (examPackError != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = examPackError!!, color = ZoyaCoral, fontSize = 12.sp)
                    }

                    examPackResult?.let { pack ->
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(text = "🎯 High-Priority Concepts", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ZoyaCyanBright)
                        pack.highPriorityConcepts.forEach { Text(text = "• $it", fontSize = 12.sp, color = TextPrimary, modifier = Modifier.padding(vertical = 2.dp)) }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(text = "❓ Most Expected Questions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ZoyaAmber)
                        pack.mostExpectedQuestions.forEach { Text(text = "• $it", fontSize = 12.sp, color = TextPrimary, modifier = Modifier.padding(vertical = 2.dp)) }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(text = "⚠️ Common Traps & Mistakes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ZoyaCoral)
                        pack.commonExamTraps.forEach { Text(text = "• $it", fontSize = 12.sp, color = TextPrimary, modifier = Modifier.padding(vertical = 2.dp)) }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(text = "📐 Formula Cheat Sheet", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ZoyaEmerald)
                        pack.formulaCheatSheet.forEach { Text(text = "• $it", fontSize = 12.sp, color = TextPrimary, modifier = Modifier.padding(vertical = 2.dp)) }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
fun StudentProfileCustomizerContent(
    currentProfile: StudentProfile,
    onSave: (StudentProfile) -> Unit,
    onClose: () -> Unit
) {
    var selectedState by remember { mutableStateOf(currentProfile.state) }
    var selectedBoard by remember { mutableStateOf(allSupportedBoards().firstOrNull { it.id == currentProfile.boardId } ?: allSupportedBoards().first()) }
    var selectedClass by remember { mutableStateOf(currentProfile.classLevel) }
    var selectedSubject by remember { mutableStateOf(currentProfile.subject) }
    var selectedLanguage by remember { mutableStateOf(currentProfile.language) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredBoards = remember(selectedState, searchQuery) {
        allSupportedBoards().filter { board ->
            val matchesState = if (selectedState == "National (All India)") true else board.state == selectedState || board.isNational
            val matchesSearch = if (searchQuery.isBlank()) true else board.name.contains(searchQuery, true) || board.fullName.contains(searchQuery, true)
            matchesState && matchesSearch
        }
    }

    val availableSubjects = remember(selectedClass) {
        defaultSubjectsForClass(selectedClass)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CUSTOMIZE STUDY PROFILE",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = ZoyaCyanBright,
                letterSpacing = 1.sp
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
            }
        }

        Text(
            text = "ZOYA optimizes explanation depth and exam questions according to your curriculum.",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. STATE SELECTION
        Text(text = "1. State / Region", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(defaultStates()) { st ->
                val isSelected = st == selectedState
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) ZoyaCyan else NovaDarkElevated)
                        .border(1.dp, if (isSelected) ZoyaCyanBright else NovaBorderGlow, RoundedCornerShape(10.dp))
                        .clickable { selectedState = st }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = st,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) NovaObsidian else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. BOARD SELECTION
        Text(text = "2. Educational Board", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search Board (e.g. JKBOSE, CBSE, ICSE)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ZoyaCyan,
                unfocusedBorderColor = NovaBorderGlow,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            filteredBoards.take(6).forEach { board ->
                val isSelected = board.id == selectedBoard.id
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) ZoyaCyanGlass else NovaDarkElevated)
                        .border(1.dp, if (isSelected) ZoyaCyanBright else NovaBorderGlow, RoundedCornerShape(12.dp))
                        .clickable { selectedBoard = board }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = board.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = board.fullName, fontSize = 11.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = ZoyaCyanBright, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. CLASS SELECTION
        Text(text = "3. Class / Grade", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(defaultClasses()) { cls ->
                val isSelected = cls == selectedClass
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) ZoyaCyan else NovaDarkElevated)
                        .border(1.dp, if (isSelected) ZoyaCyanBright else NovaBorderGlow, RoundedCornerShape(10.dp))
                        .clickable {
                            selectedClass = cls
                            if (!defaultSubjectsForClass(cls).contains(selectedSubject)) {
                                selectedSubject = defaultSubjectsForClass(cls).first()
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cls,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) NovaObsidian else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. SUBJECT SELECTION
        Text(text = "4. Subject", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(availableSubjects) { sub ->
                val isSelected = sub == selectedSubject
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) ZoyaCyan else NovaDarkElevated)
                        .border(1.dp, if (isSelected) ZoyaCyanBright else NovaBorderGlow, RoundedCornerShape(10.dp))
                        .clickable { selectedSubject = sub }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = sub,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) NovaObsidian else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. PREFERRED LANGUAGE
        Text(text = "5. Language Preference", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(defaultLanguages()) { lang ->
                val isSelected = lang == selectedLanguage
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) ZoyaCyan else NovaDarkElevated)
                        .border(1.dp, if (isSelected) ZoyaCyanBright else NovaBorderGlow, RoundedCornerShape(10.dp))
                        .clickable { selectedLanguage = lang }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = lang,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) NovaObsidian else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SAVE BUTTON
        Button(
            onClick = {
                onSave(
                    StudentProfile(
                        boardId = selectedBoard.id,
                        boardName = selectedBoard.name,
                        state = selectedState,
                        classLevel = selectedClass,
                        subject = selectedSubject,
                        language = selectedLanguage
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ZoyaCyan),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("SAVE & UPDATE STUDY SPACE", color = NovaObsidian, fontWeight = FontWeight.Black, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

data class BoardModuleItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val bgTint: Color,
    val tint: Color,
    val mode: StudyMode
)
