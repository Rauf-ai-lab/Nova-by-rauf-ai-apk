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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.WarningAmber
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ConceptExplanation
import com.example.domain.repository.StudyRepository
import com.example.ui.components.FuturisticButton
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.NovaCardGlass
import com.example.ui.theme.NovaDarkElevated
import com.example.ui.theme.NovaDarkSurface
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
fun ConceptExplainerSheet(
    repository: StudyRepository,
    onClose: () -> Unit,
    onSpeakText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var topicInput by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf("School Level") }
    var isLoading by remember { mutableStateOf(false) }
    var explanation by remember { mutableStateOf<ConceptExplanation?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val levels = listOf("Beginner", "Simple", "School Level", "Exam Level", "Advanced")
    val quickPicks = listOf("Photosynthesis", "Newton's 2nd Law", "Quantum Superposition", "Mitochondria ATP", "Calculus Derivative")

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    fun loadExplanation(topic: String) {
        if (topic.isBlank()) return
        isLoading = true
        errorMessage = null
        scope.launch {
            val res = repository.explainConcept(topic, selectedLevel)
            isLoading = false
            res.onSuccess {
                explanation = it
                repository.logSession(topic, "Concept Explainer", it.quickSummary, 120)
            }.onFailure {
                errorMessage = it.localizedMessage ?: "Failed to generate concept explanation"
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
                        imageVector = Icons.Outlined.Lightbulb,
                        contentDescription = null,
                        tint = ZoyaCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Concept Explainer",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_concept_explainer")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Level Selector Chips
            Text(
                text = "EXPLANATION DEPTH",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, letterSpacing = 1.sp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(levels) { lvl ->
                    val isSelected = lvl == selectedLevel
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) ZoyaCyan else NovaDarkElevated)
                            .border(1.dp, if (isSelected) ZoyaCyan else ZoyaCyan.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .clickable { selectedLevel = lvl }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = lvl,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NovaObsidian else TextPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Topic Input
            OutlinedTextField(
                value = topicInput,
                onValueChange = { topicInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("concept_topic_input"),
                placeholder = { Text("Enter concept (e.g. Doppler Effect, Mitosis...)", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ZoyaCyan,
                    unfocusedBorderColor = ZoyaCyan.copy(alpha = 0.3f),
                    focusedContainerColor = NovaDarkElevated,
                    unfocusedContainerColor = NovaDarkElevated,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                trailingIcon = {
                    IconButton(
                        onClick = { loadExplanation(topicInput) },
                        enabled = !isLoading && topicInput.isNotBlank(),
                        modifier = Modifier.testTag("submit_concept_btn")
                    ) {
                        Icon(imageVector = Icons.Outlined.AutoAwesome, contentDescription = "Explain", tint = ZoyaCyan)
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Topic picks
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(quickPicks) { pick ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NovaCardGlass)
                            .clickable {
                                topicInput = pick
                                loadExplanation(pick)
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(text = pick, style = MaterialTheme.typography.labelSmall.copy(color = ZoyaCyanBright))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = ZoyaCyan)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Zoya is deconstructing the concept...", style = MaterialTheme.typography.bodyMedium.copy(color = ZoyaCyanBright))
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

            explanation?.let { exp ->
                // Summary Card
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = ZoyaCyan.copy(alpha = 0.4f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ZOYA'S CORE SUMMARY",
                                style = MaterialTheme.typography.labelSmall.copy(color = ZoyaCyan, fontWeight = FontWeight.Bold)
                            )
                            IconButton(
                                onClick = { onSpeakText("${exp.quickSummary}. Here is why: ${exp.deepExplanation}") },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Listen", tint = ZoyaCyanBright, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = exp.quickSummary,
                            style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Analogy Card
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = ZoyaVioletBright.copy(alpha = 0.4f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Psychology, contentDescription = null, tint = ZoyaVioletBright, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "REAL-WORLD ANALOGY",
                                style = MaterialTheme.typography.labelSmall.copy(color = ZoyaVioletBright, fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = exp.realWorldAnalogy, style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Deep Explanation
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "DETAILED MECHANISM",
                            style = MaterialTheme.typography.labelSmall.copy(color = ZoyaElectricBlue, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = exp.deepExplanation, style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, lineHeight = 22.sp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Key Points
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "KEY POINTS & FORMULAS",
                            style = MaterialTheme.typography.labelSmall.copy(color = ZoyaEmerald, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        exp.keyPointsAndFormulas.forEach { pt ->
                            Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Outlined.Check, contentDescription = null, tint = ZoyaEmerald, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = pt, style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Common Traps & Mistakes
                GlassmorphicCard(modifier = Modifier.fillMaxWidth(), borderColor = ZoyaAmber.copy(alpha = 0.4f)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = ZoyaAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "COMMON EXAM TRAPS",
                                style = MaterialTheme.typography.labelSmall.copy(color = ZoyaAmber, fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        exp.commonMistakes.forEach { mist ->
                            Text(text = "• $mist", style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary), modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Practice doubt
                GlassmorphicCard(modifier = Modifier.fillMaxWidth(), borderColor = ZoyaCyanBright.copy(alpha = 0.3f)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "ZOYA'S REFLECTION CHALLENGE",
                            style = MaterialTheme.typography.labelSmall.copy(color = ZoyaCyanBright, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = exp.quickPracticeDoubt, style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontWeight = FontWeight.Medium))
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
