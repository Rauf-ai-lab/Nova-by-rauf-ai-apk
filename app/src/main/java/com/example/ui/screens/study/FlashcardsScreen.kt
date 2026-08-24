package com.example.ui.screens.study

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.FlashcardItem
import com.example.domain.repository.StudyRepository
import com.example.ui.components.FuturisticButton
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
fun FlashcardsScreen(
    repository: StudyRepository,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var topicInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var activeDeck by remember { mutableStateOf<List<FlashcardItem>>(emptyList()) }
    var currentCardIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val savedEntities by repository.allFlashcards.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Flip animation
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "card_flip"
    )

    val quickTopics = listOf("Cell Biology", "Organic Reactions", "Newtonian Mechanics", "World War II", "Algorithms")

    fun generateDeck(topic: String) {
        if (topic.isBlank()) return
        isLoading = true
        errorMessage = null
        scope.launch {
            val res = repository.generateFlashcards(topic, 5)
            isLoading = false
            res.onSuccess {
                activeDeck = it
                currentCardIndex = 0
                isFlipped = false
                repository.logSession(topic, "Flashcards", "Reviewed ${it.size} cards", 120)
            }.onFailure {
                errorMessage = it.localizedMessage ?: "Failed to generate cards"
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
                        imageVector = Icons.Default.Style,
                        contentDescription = null,
                        tint = ZoyaCyanBright,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Active Recall Flashcards",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.testTag("close_flashcards")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Deck Generator
            OutlinedTextField(
                value = topicInput,
                onValueChange = { topicInput = it },
                modifier = Modifier.fillMaxWidth().testTag("flashcard_topic_input"),
                placeholder = { Text("Generate cards for topic (e.g. Periodic Table)...", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ZoyaCyanBright,
                    unfocusedBorderColor = ZoyaCyan.copy(alpha = 0.3f),
                    focusedContainerColor = NovaDarkElevated,
                    unfocusedContainerColor = NovaDarkElevated,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                trailingIcon = {
                    IconButton(
                        onClick = { generateDeck(topicInput) },
                        enabled = !isLoading && topicInput.isNotBlank(),
                        modifier = Modifier.testTag("gen_flashcards_btn")
                    ) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = "Generate", tint = ZoyaCyanBright)
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick picks
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(quickTopics) { pick ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NovaCardGlass)
                            .clickable {
                                topicInput = pick
                                generateDeck(pick)
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
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = ZoyaCyanBright)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Zoya is extracting high-yield flashcard concepts...", style = MaterialTheme.typography.bodyMedium.copy(color = ZoyaCyanBright))
                    }
                }
            }

            errorMessage?.let { err ->
                GlassmorphicCard(modifier = Modifier.fillMaxWidth(), borderColor = ZoyaCoral.copy(alpha = 0.4f)) {
                    Text(text = err, color = ZoyaCoral, modifier = Modifier.padding(16.dp))
                }
            }

            val currentDeck = if (activeDeck.isNotEmpty()) {
                activeDeck
            } else {
                savedEntities.map {
                    FlashcardItem(it.id, it.subject, it.topic, it.frontQuestion, it.backAnswer, it.difficulty, it.masteryLevel)
                }
            }

            if (currentDeck.isNotEmpty()) {
                val safeIndex = currentCardIndex.coerceIn(0, currentDeck.size - 1)
                val card = currentDeck[safeIndex]

                // Card counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Card ${safeIndex + 1} of ${currentDeck.size} • ${card.topic}",
                        style = MaterialTheme.typography.labelMedium.copy(color = ZoyaCyan, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Tap card to flip",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Flippable Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12f * density
                        }
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                if (rotation > 90f) {
                                    listOf(NovaDarkElevated, Color(0xFF0F1E28))
                                } else {
                                    listOf(NovaCardGlass, NovaDarkElevated)
                                }
                            )
                        )
                        .border(
                            1.5.dp,
                            if (rotation > 90f) ZoyaEmerald.copy(alpha = 0.6f) else ZoyaCyan.copy(alpha = 0.4f),
                            RoundedCornerShape(24.dp)
                        )
                        .clickable { isFlipped = !isFlipped }
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (rotation <= 90f) {
                        // Front (Question)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "QUESTION / TERM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ZoyaCyan,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = card.question,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    } else {
                        // Back (Answer) - rotated 180 so it's readable
                        Column(
                            modifier = Modifier.graphicsLayer { rotationY = 180f },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "ANSWER / EXPLANATION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = ZoyaEmerald,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = card.answer,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation and Mastery Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (currentCardIndex > 0) {
                                currentCardIndex--
                                isFlipped = false
                            }
                        },
                        enabled = safeIndex > 0,
                        modifier = Modifier.background(NovaCardGlass, CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Previous", tint = TextPrimary)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                isFlipped = false
                                if (safeIndex < currentDeck.size - 1) currentCardIndex++
                            },
                            modifier = Modifier
                                .background(ZoyaAmber.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .border(1.dp, ZoyaAmber, RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Outlined.Replay, contentDescription = "Review Later", tint = ZoyaAmber)
                        }

                        IconButton(
                            onClick = {
                                isFlipped = false
                                if (safeIndex < currentDeck.size - 1) currentCardIndex++
                            },
                            modifier = Modifier
                                .background(ZoyaEmerald.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .border(1.dp, ZoyaEmerald, RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Mastered", tint = ZoyaEmerald)
                        }
                    }

                    IconButton(
                        onClick = {
                            if (safeIndex < currentDeck.size - 1) {
                                currentCardIndex++
                                isFlipped = false
                            }
                        },
                        enabled = safeIndex < currentDeck.size - 1,
                        modifier = Modifier.background(NovaCardGlass, CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = TextPrimary)
                    }
                }
            } else if (!isLoading) {
                // Empty State
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Flashcards Active",
                            style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Type any subject or topic above and Zoya will build an instant active-recall flashcard deck for you!",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, textAlign = TextAlign.Center)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
