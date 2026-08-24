package com.example.ui.screens.study

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Functions
import androidx.compose.material.icons.outlined.Science
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.StepByStepSolution
import com.example.domain.repository.StudyRepository
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.NovaCardGlass
import com.example.ui.theme.NovaDarkElevated
import com.example.ui.theme.NovaObsidian
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ZoyaAmber
import com.example.ui.theme.ZoyaCoral
import com.example.ui.theme.ZoyaCyan
import com.example.ui.theme.ZoyaCyanBright
import com.example.ui.theme.ZoyaElectricBlue
import com.example.ui.theme.ZoyaEmerald
import com.example.ui.theme.ZoyaVioletBright
import kotlinx.coroutines.launch

@Composable
fun StepByStepSolverSheet(
    repository: StudyRepository,
    onClose: () -> Unit,
    onSpeakText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var problemInput by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("Mathematics") }
    var isLoading by remember { mutableStateOf(false) }
    var solution by remember { mutableStateOf<StepByStepSolution?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val subjects = listOf("Mathematics", "Physics", "Chemistry", "Logical Reasoning")
    val exampleProblems = listOf(
        "Find the derivative of f(x) = x^3 * sin(x)",
        "A 5kg block accelerates at 3 m/s^2 on a surface with friction coeff 0.2. Find applied force.",
        "Balance: Fe + O2 -> Fe2O3 and calculate moles needed for 10g of rust.",
        "Solve the recurrence relation: T(n) = 2T(n/2) + n"
    )

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    fun solveProblem(problem: String) {
        if (problem.isBlank()) return
        isLoading = true
        errorMessage = null
        scope.launch {
            val res = repository.solveProblem(problem, selectedSubject)
            isLoading = false
            res.onSuccess {
                solution = it
                repository.logSession(problem.take(30), "Problem Solver", it.finalAnswer, 180)
            }.onFailure {
                errorMessage = it.localizedMessage ?: "Failed to solve problem"
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = NovaObsidian
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        tint = ZoyaElectricBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Step-by-Step Solver",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_solver_sheet")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subject Selector
            Text(
                text = "ACADEMIC DOMAIN",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, letterSpacing = 1.sp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(subjects) { subj ->
                    val isSelected = subj == selectedSubject
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) ZoyaElectricBlue else NovaDarkElevated)
                            .border(1.dp, if (isSelected) ZoyaElectricBlue else ZoyaCyan.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .clickable { selectedSubject = subj }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = subj,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NovaObsidian else TextPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Problem Input
            OutlinedTextField(
                value = problemInput,
                onValueChange = { problemInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("problem_statement_input"),
                placeholder = { Text("Paste equation, problem or word question...", color = TextMuted) },
                minLines = 3,
                maxLines = 6,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ZoyaElectricBlue,
                    unfocusedBorderColor = ZoyaCyan.copy(alpha = 0.3f),
                    focusedContainerColor = NovaDarkElevated,
                    unfocusedContainerColor = NovaDarkElevated,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Try an example:", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                IconButton(
                    onClick = { solveProblem(problemInput) },
                    enabled = !isLoading && problemInput.isNotBlank(),
                    modifier = Modifier
                        .testTag("solve_problem_btn")
                        .background(ZoyaElectricBlue, CircleShape)
                        .size(38.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.AutoAwesome, contentDescription = "Solve", tint = NovaObsidian)
                }
            }

            // Quick Problem examples
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(exampleProblems) { example ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NovaCardGlass)
                            .clickable {
                                problemInput = example
                                solveProblem(example)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = example.take(35) + "...",
                            style = MaterialTheme.typography.labelSmall.copy(color = ZoyaCyanBright)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = ZoyaElectricBlue)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Zoya is analyzing equations and steps...", style = MaterialTheme.typography.bodyMedium.copy(color = ZoyaCyanBright))
                    }
                }
            }

            errorMessage?.let { err ->
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = ZoyaCoral.copy(alpha = 0.4f)
                ) {
                    Text(text = err, color = ZoyaCoral, modifier = Modifier.padding(16.dp))
                }
            }

            solution?.let { sol ->
                // Concept & Strategy
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = ZoyaElectricBlue.copy(alpha = 0.4f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "IDENTIFIED CONCEPT",
                            style = MaterialTheme.typography.labelSmall.copy(color = ZoyaElectricBlue, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = sol.identifiedConcept,
                            style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "STRATEGY & APPROACH",
                            style = MaterialTheme.typography.labelSmall.copy(color = ZoyaCyanBright, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = sol.strategyApproach,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Steps
                Text(
                    text = "STEP-BY-STEP REASONING",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, letterSpacing = 1.sp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                sol.steps.forEach { step ->
                    GlassmorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        borderColor = ZoyaVioletBright.copy(alpha = 0.3f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(ZoyaVioletBright, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${step.stepNumber}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NovaObsidian,
                                            fontWeight = FontWeight.Black
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = step.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = ZoyaVioletBright,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = step.calculationOrAction,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(NovaDarkElevated, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "Why: ${step.whyThisStep}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Final Answer Banner
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = ZoyaEmerald.copy(alpha = 0.6f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FINAL SOLUTION",
                                style = MaterialTheme.typography.labelSmall.copy(color = ZoyaEmerald, fontWeight = FontWeight.Black)
                            )
                            IconButton(
                                onClick = { onSpeakText("The final answer is ${sol.finalAnswer}. Here is the verification: ${sol.verificationNote}") },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Listen", tint = ZoyaEmerald, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = sol.finalAnswer,
                            style = MaterialTheme.typography.titleLarge.copy(color = ZoyaEmerald, fontWeight = FontWeight.Black)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Check: ${sol.verificationNote}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Practice Challenge
                GlassmorphicCard(modifier = Modifier.fillMaxWidth(), borderColor = ZoyaCyan.copy(alpha = 0.3f)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "PRACTICE CHALLENGE",
                            style = MaterialTheme.typography.labelSmall.copy(color = ZoyaCyan, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = sol.practiceChallenge, style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary))
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
