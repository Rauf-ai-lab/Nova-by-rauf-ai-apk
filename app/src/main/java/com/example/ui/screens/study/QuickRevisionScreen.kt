package com.example.ui.screens.study

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bookmark
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.QuickRevisionGuide
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
fun QuickRevisionScreen(
    repository: StudyRepository,
    onClose: () -> Unit,
    onSpeakText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var topicInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var revisionGuide by remember { mutableStateOf<QuickRevisionGuide?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val quickTopics = listOf("Electromagnetism", "Genetics & DNA", "Organic Chemistry Reactions", "Macroeconomics", "Calculus Integrals")
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    fun generateRevision(topic: String) {
        if (topic.isBlank()) return
        isLoading = true
        errorMessage = null
        scope.launch {
            val res = repository.generateRevision(topic)
            isLoading = false
            res.onSuccess {
                revisionGuide = it
                repository.logSession(topic, "Quick Revision", "Generated ${it.coreFormulas.size} formulas & facts", 100)
            }.onFailure {
                errorMessage = it.localizedMessage ?: "Failed to generate revision guide"
            }
        }
    }

    Surface(modifier = modifier.fillMaxSize(), color = NovaObsidian) {
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
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = ZoyaAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Rapid Revision Guide",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.testTag("close_revision")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = topicInput,
                onValueChange = { topicInput = it },
                modifier = Modifier.fillMaxWidth().testTag("revision_topic_input"),
                placeholder = { Text("Topic for rapid formula/fact drill...", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ZoyaAmber,
                    unfocusedBorderColor = ZoyaCyan.copy(alpha = 0.3f),
                    focusedContainerColor = NovaDarkElevated,
                    unfocusedContainerColor = NovaDarkElevated,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                trailingIcon = {
                    IconButton(
                        onClick = { generateRevision(topicInput) },
                        enabled = !isLoading && topicInput.isNotBlank(),
                        modifier = Modifier.testTag("gen_revision_btn")
                    ) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = "Generate", tint = ZoyaAmber)
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(quickTopics) { pick ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NovaCardGlass)
                            .clickable {
                                topicInput = pick
                                generateRevision(pick)
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
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = ZoyaAmber)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Zoya is assembling high-yield formulas & definitions...", style = MaterialTheme.typography.bodyMedium.copy(color = ZoyaCyanBright))
                    }
                }
            }

            errorMessage?.let { err ->
                GlassmorphicCard(modifier = Modifier.fillMaxWidth(), borderColor = ZoyaCoral.copy(alpha = 0.4f)) {
                    Text(text = err, color = ZoyaCoral, modifier = Modifier.padding(16.dp))
                }
            }

            revisionGuide?.let { guide ->
                // Formulas Card
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = ZoyaAmber.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CORE FORMULAS & EQUATIONS",
                                style = MaterialTheme.typography.labelSmall.copy(color = ZoyaAmber, fontWeight = FontWeight.Bold)
                            )
                            IconButton(
                                onClick = { onSpeakText("Here are the key formulas for ${guide.topic}: " + guide.coreFormulas.joinToString(". ")) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Listen", tint = ZoyaAmber, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        guide.coreFormulas.forEach { formula ->
                            Text(
                                text = "• $formula",
                                style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // High Yield Facts
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = ZoyaEmerald.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "HIGH-YIELD EXAM FACTS",
                            style = MaterialTheme.typography.labelSmall.copy(color = ZoyaEmerald, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        guide.highYieldFacts.forEach { fact ->
                            Text(
                                text = "• $fact",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Definitions
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = ZoyaCyan.copy(alpha = 0.4f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "CRITICAL DEFINITIONS",
                            style = MaterialTheme.typography.labelSmall.copy(color = ZoyaCyan, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        guide.keyDefinitions.forEach { def ->
                            Text(
                                text = "• $def",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Weak Area Watchlist
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = ZoyaCoral.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = ZoyaCoral, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WEAK AREA WATCHLIST",
                                style = MaterialTheme.typography.labelSmall.copy(color = ZoyaCoral, fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        guide.weakAreaWatchlist.forEach { weak ->
                            Text(
                                text = "• $weak",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
