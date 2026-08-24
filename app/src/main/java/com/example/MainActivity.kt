package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.domain.model.StudyMode
import com.example.ui.screens.main.MainStudyScreen
import com.example.ui.screens.onboarding.ApiKeyOnboardingScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.study.ConceptExplainerSheet
import com.example.ui.screens.study.FlashcardsScreen
import com.example.ui.screens.study.QuickRevisionScreen
import com.example.ui.screens.study.QuizArenaScreen
import com.example.ui.screens.study.StepByStepSolverSheet
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as NovaApplication
        val repository = app.studyRepository
        val liveSessionManager = app.liveSessionManager
        val deviceTools = app.deviceToolsManager

        setContent {
            MyApplicationTheme {
                var hasValidKey by remember {
                    mutableStateOf(repository.hasValidApiKey() && repository.getApiKey().isNotBlank())
                }
                var currentMode by remember { mutableStateOf<StudyMode?>(null) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BoxModifierWrapper(modifier = Modifier.padding(innerPadding)) {
                        if (!hasValidKey) {
                            ApiKeyOnboardingScreen(
                                repository = repository,
                                onKeyValidated = {
                                    hasValidKey = true
                                    liveSessionManager.startLiveSession()
                                }
                            )
                        } else {
                            BackHandler(enabled = currentMode != null) {
                                currentMode = null
                            }

                            AnimatedContent(
                                targetState = currentMode,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "screen_nav"
                            ) { mode ->
                                when (mode) {
                                    null -> MainStudyScreen(
                                        liveSessionManager = liveSessionManager,
                                        repository = repository,
                                        onNavigateMode = { selected ->
                                            currentMode = selected
                                        }
                                    )
                                    StudyMode.ConceptExplainer -> ConceptExplainerSheet(
                                        repository = repository,
                                        onClose = { currentMode = null },
                                        onSpeakText = { text ->
                                            liveSessionManager.audioPlayback.speakText(text)
                                        }
                                    )
                                    StudyMode.StepByStepSolver -> StepByStepSolverSheet(
                                        repository = repository,
                                        onClose = { currentMode = null },
                                        onSpeakText = { text ->
                                            liveSessionManager.audioPlayback.speakText(text)
                                        }
                                    )
                                    StudyMode.QuizArena -> QuizArenaScreen(
                                        repository = repository,
                                        onClose = { currentMode = null },
                                        onSpeakText = { text ->
                                            liveSessionManager.audioPlayback.speakText(text)
                                        }
                                    )
                                    StudyMode.FlashcardDeck -> FlashcardsScreen(
                                        repository = repository,
                                        onClose = { currentMode = null }
                                    )
                                    StudyMode.QuickRevision -> QuickRevisionScreen(
                                        repository = repository,
                                        onClose = { currentMode = null },
                                        onSpeakText = { text ->
                                            liveSessionManager.audioPlayback.speakText(text)
                                        }
                                    )
                                    StudyMode.Settings -> SettingsScreen(
                                        repository = repository,
                                        deviceTools = deviceTools,
                                        onClose = { currentMode = null },
                                        onKeyCleared = {
                                            hasValidKey = false
                                            currentMode = null
                                            liveSessionManager.stopLiveSession()
                                        }
                                    )
                                    else -> MainStudyScreen(
                                        liveSessionManager = liveSessionManager,
                                        repository = repository,
                                        onNavigateMode = { selected ->
                                            currentMode = selected
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BoxModifierWrapper(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Box(modifier = modifier) {
        content()
    }
}

