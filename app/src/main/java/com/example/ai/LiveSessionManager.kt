package com.example.ai

import android.content.Context
import com.example.data.audio.AudioPlaybackManager
import com.example.data.audio.SpeechInputManager
import com.example.domain.model.ChatMessage
import com.example.domain.model.Speaker
import com.example.domain.model.ZoyaState
import com.example.domain.repository.StudyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LiveSessionManager(
    private val context: Context,
    private val scope: CoroutineScope,
    private val repository: StudyRepository
) {
    val audioPlayback = AudioPlaybackManager(context, scope)
    val speechInput = SpeechInputManager(context)

    private val _zoyaState = MutableStateFlow<ZoyaState>(ZoyaState.Idle)
    val zoyaState: StateFlow<ZoyaState> = _zoyaState.asStateFlow()

    private val _chatTranscript = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatTranscript: StateFlow<List<ChatMessage>> = _chatTranscript.asStateFlow()

    private val _isLiveSessionActive = MutableStateFlow(true)
    val isLiveSessionActive: StateFlow<Boolean> = _isLiveSessionActive.asStateFlow()

    private var activeJob: Job? = null

    init {
        setupListeners()
    }

    private fun setupListeners() {
        speechInput.onSpeechResult = { recognizedText ->
            if (recognizedText.isNotBlank()) {
                sendTextQuestion(recognizedText)
            }
        }

        speechInput.onErrorOccurred = { errorMsg ->
            _zoyaState.value = ZoyaState.Error(errorMsg)
        }

        audioPlayback.onPlaybackStarted = {
            _zoyaState.value = ZoyaState.Speaking(0.5f)
        }

        audioPlayback.onPlaybackAmplitude = { amp ->
            if (_zoyaState.value is ZoyaState.Speaking) {
                _zoyaState.value = ZoyaState.Speaking(amp)
            }
        }

        audioPlayback.onPlaybackCompleted = {
            _zoyaState.value = ZoyaState.Idle
        }
    }

    fun startLiveSession() {
        _isLiveSessionActive.value = true
        _zoyaState.value = ZoyaState.Idle

        // Add welcome message if chat is empty
        if (_chatTranscript.value.isEmpty()) {
            val profile = repository.getCurrentProfile()
            addMessage(
                ChatMessage(
                    sender = Speaker.ZOYA,
                    text = "Hey! I'm Zoya, your AI Study Companion for ${profile.boardName} (${profile.classLevel} ${profile.subject}). What concept, doubt, or problem are we solving today?"
                )
            )
        }
    }

    fun stopLiveSession() {
        speechInput.stopListening()
        audioPlayback.interrupt()
        activeJob?.cancel()
        _isLiveSessionActive.value = false
        _zoyaState.value = ZoyaState.Disconnected
    }

    fun toggleMicrophone(on: Boolean? = null) {
        val shouldListen = on ?: !speechInput.isListening.value
        if (shouldListen) {
            audioPlayback.interrupt()
            _zoyaState.value = ZoyaState.Listening(0f)
            speechInput.startListening()
        } else {
            speechInput.stopListening()
            _zoyaState.value = ZoyaState.Idle
        }
    }

    /**
     * Send student question to Zoya:
     * Voice is for User Input, Text is for Zoya Output.
     * Generates structured text and displays it directly in chat without auto-playing voice.
     */
    fun sendTextQuestion(question: String) {
        if (question.isBlank()) return
        audioPlayback.interrupt()
        speechInput.stopListening()

        addMessage(ChatMessage(sender = Speaker.USER, text = question.trim()))
        _zoyaState.value = ZoyaState.Thinking

        activeJob?.cancel()
        activeJob = scope.launch(Dispatchers.Main) {
            val result = repository.askZoya(question.trim(), _chatTranscript.value)
            result.onSuccess { responseText ->
                addMessage(ChatMessage(sender = Speaker.ZOYA, text = responseText))
                _zoyaState.value = ZoyaState.Idle
            }.onFailure { err ->
                val errorMsg = err.localizedMessage ?: "Connection error. Please check your API key or internet connection."
                _zoyaState.value = ZoyaState.Error(errorMsg)
                addMessage(
                    ChatMessage(
                        sender = Speaker.ZOYA,
                        text = "I encountered an issue connecting to the AI engine: $errorMsg. Please check your network or API key in Settings."
                    )
                )
            }
        }
    }

    fun addMessage(message: ChatMessage) {
        _chatTranscript.value = _chatTranscript.value + message
    }

    fun clearChat() {
        _chatTranscript.value = emptyList()
    }

    fun release() {
        stopLiveSession()
        speechInput.release()
        audioPlayback.release()
    }
}
