package com.example.ai

import android.content.Context
import com.example.data.audio.AudioPlaybackManager
import com.example.data.audio.AudioRecorderManager
import com.example.data.gemini.GeminiLiveWebSocketClient
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
    val audioRecorder = AudioRecorderManager(context, scope)
    val audioPlayback = AudioPlaybackManager(context, scope)
    private val liveWsClient = GeminiLiveWebSocketClient(scope)

    private val _zoyaState = MutableStateFlow<ZoyaState>(ZoyaState.Disconnected)
    val zoyaState: StateFlow<ZoyaState> = _zoyaState.asStateFlow()

    private val _chatTranscript = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatTranscript: StateFlow<List<ChatMessage>> = _chatTranscript.asStateFlow()

    private val _isLiveSessionActive = MutableStateFlow(false)
    val isLiveSessionActive: StateFlow<Boolean> = _isLiveSessionActive.asStateFlow()

    private var currentModelStreamingText = StringBuilder()
    private var restFallbackJob: Job? = null

    init {
        setupListeners()
    }

    private fun setupListeners() {
        // Audio Recorder -> WebSocket / Waveform
        audioRecorder.onAudioChunkCaptured = { chunk ->
            if (liveWsClient.isLive()) {
                liveWsClient.sendAudioChunk(chunk)
            }
        }

        audioRecorder.onAmplitudeChanged = { amp ->
            if (_zoyaState.value is ZoyaState.Listening) {
                _zoyaState.value = ZoyaState.Listening(amp)
            }
            // Barge-in check: if Zoya is speaking and user mic detects prominent speech amplitude (> 0.25)
            if (_zoyaState.value is ZoyaState.Speaking && amp > 0.25f) {
                handleBargeInInterruption()
            }
        }

        // Audio Playback -> Zoya Speaking State
        audioPlayback.onPlaybackStarted = {
            _zoyaState.value = ZoyaState.Speaking(0.5f)
        }

        audioPlayback.onPlaybackAmplitude = { amp ->
            if (_zoyaState.value is ZoyaState.Speaking) {
                _zoyaState.value = ZoyaState.Speaking(amp)
            }
        }

        audioPlayback.onPlaybackCompleted = {
            if (_isLiveSessionActive.value) {
                _zoyaState.value = ZoyaState.Idle
            }
        }

        // Live WebSocket -> Audio Playback & Transcript
        liveWsClient.onConnectionStateChanged = { connected, error ->
            if (connected) {
                _zoyaState.value = ZoyaState.Idle
                _isLiveSessionActive.value = true
            } else {
                _isLiveSessionActive.value = false
                _zoyaState.value = if (error != null) ZoyaState.Error(error) else ZoyaState.Disconnected
            }
        }

        liveWsClient.onPcmAudioReceived = { pcm ->
            audioPlayback.playPcmChunk(pcm)
        }

        liveWsClient.onTranscriptReceived = { text, isFinal ->
            currentModelStreamingText.append(text)
            updateOrAppendZoyaMessage(currentModelStreamingText.toString(), !isFinal)
            if (isFinal) {
                currentModelStreamingText.clear()
            }
        }

        liveWsClient.onInterrupted = {
            handleBargeInInterruption()
        }
    }

    fun startLiveSession() {
        val apiKey = repository.getApiKey()
        if (apiKey.isBlank()) {
            _zoyaState.value = ZoyaState.Error("Gemini API Key missing. Please connect your key in Settings.")
            return
        }

        _zoyaState.value = ZoyaState.Thinking
        liveWsClient.connect(apiKey)

        // Add welcome message if chat is empty
        if (_chatTranscript.value.isEmpty()) {
            addMessage(
                ChatMessage(
                    sender = Speaker.ZOYA,
                    text = "Hey! I'm Zoya, your study companion. What concept or problem are we mastering today?"
                )
            )
        }
    }

    fun stopLiveSession() {
        audioRecorder.stopRecording()
        audioPlayback.interrupt()
        liveWsClient.disconnect()
        _isLiveSessionActive.value = false
        _zoyaState.value = ZoyaState.Disconnected
    }

    fun toggleMicrophone(on: Boolean? = null) {
        val shouldRecord = on ?: !audioRecorder.isCurrentlyRecording()
        if (shouldRecord) {
            // Stop any ongoing playback on user mic activation
            audioPlayback.interrupt()
            val started = audioRecorder.startRecording(16000)
            if (started) {
                _zoyaState.value = ZoyaState.Listening(0f)
            } else {
                _zoyaState.value = ZoyaState.Error("Microphone permission required.")
            }
        } else {
            audioRecorder.stopRecording()
            if (_isLiveSessionActive.value) {
                _zoyaState.value = ZoyaState.Idle
            }
        }
    }

    /**
     * Instant Barge-in / Interruption:
     * Immediately stops output playback, flushes audio buffers, switches to Listening.
     */
    fun handleBargeInInterruption() {
        audioPlayback.interrupt()
        currentModelStreamingText.clear()
        if (_isLiveSessionActive.value && audioRecorder.isCurrentlyRecording()) {
            _zoyaState.value = ZoyaState.Listening(0f)
        } else {
            _zoyaState.value = ZoyaState.Idle
        }
    }

    /**
     * Ask Zoya via text with voice response synthesis (fallback / quiet mode)
     */
    fun sendTextQuestion(question: String) {
        if (question.isBlank()) return
        handleBargeInInterruption()

        addMessage(ChatMessage(sender = Speaker.USER, text = question))
        _zoyaState.value = ZoyaState.Thinking

        if (liveWsClient.isLive()) {
            liveWsClient.sendTextMessage(question)
        } else {
            // REST Fallback with TTS
            restFallbackJob?.cancel()
            restFallbackJob = scope.launch(Dispatchers.Main) {
                val result = repository.askZoya(question, _chatTranscript.value)
                result.onSuccess { responseText ->
                    addMessage(ChatMessage(sender = Speaker.ZOYA, text = responseText))
                    _zoyaState.value = ZoyaState.Speaking(0.5f)
                    audioPlayback.speakText(responseText) {
                        _zoyaState.value = ZoyaState.Idle
                    }
                }.onFailure { err ->
                    _zoyaState.value = ZoyaState.Error(err.localizedMessage ?: "Failed to connect to Gemini")
                    addMessage(ChatMessage(sender = Speaker.ZOYA, text = "Oops, I ran into an issue connecting to Gemini. Check your network or API key!"))
                }
            }
        }
    }

    private fun addMessage(message: ChatMessage) {
        _chatTranscript.value = _chatTranscript.value + message
    }

    private fun updateOrAppendZoyaMessage(text: String, isStreaming: Boolean) {
        val current = _chatTranscript.value.toMutableList()
        val lastIdx = current.indexOfLast { it.sender == Speaker.ZOYA }
        if (lastIdx != -1 && current[lastIdx].isStreaming) {
            current[lastIdx] = current[lastIdx].copy(text = text, isStreaming = isStreaming)
        } else {
            current.add(ChatMessage(sender = Speaker.ZOYA, text = text, isStreaming = isStreaming))
        }
        _chatTranscript.value = current
    }

    fun clearChat() {
        _chatTranscript.value = emptyList()
    }

    fun release() {
        stopLiveSession()
        audioPlayback.release()
    }
}
