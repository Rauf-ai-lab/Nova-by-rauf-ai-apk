package com.example.data.gemini

import android.util.Base64
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class GeminiLiveWebSocketClient(
    private val scope: CoroutineScope
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val clientAdapter = moshi.adapter(LiveClientMessage::class.java)
    private val serverAdapter = moshi.adapter(LiveServerMessage::class.java)

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // Keep alive
        .writeTimeout(15, TimeUnit.SECONDS)
        .pingInterval(10, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var isConnected = false
    private var isSessionConfigured = false

    var onConnectionStateChanged: ((Boolean, String?) -> Unit)? = null
    var onPcmAudioReceived: ((ByteArray) -> Unit)? = null
    var onTranscriptReceived: ((String, Boolean) -> Unit)? = null // text, isFinal
    var onInterrupted: (() -> Unit)? = null

    companion object {
        private const val LIVE_MODEL = "models/gemini-2.5-flash-native-audio-preview-12-2025"
        private const val BASE_WS_URL = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent"
    }

    fun connect(apiKey: String) {
        if (isConnected) return
        val url = "$BASE_WS_URL?key=$apiKey"
        val request = Request.Builder().url(url).build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                isConnected = true
                isSessionConfigured = false
                scope.launch(Dispatchers.Main) {
                    onConnectionStateChanged?.invoke(true, null)
                }
                sendSetupConfig()
            }

            override fun onMessage(ws: WebSocket, text: String) {
                handleServerMessage(text)
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                isConnected = false
                isSessionConfigured = false
                scope.launch(Dispatchers.Main) {
                    onConnectionStateChanged?.invoke(false, "Session closing: $reason")
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                isSessionConfigured = false
                scope.launch(Dispatchers.Main) {
                    onConnectionStateChanged?.invoke(false, t.localizedMessage ?: "WebSocket failure")
                }
            }
        })
    }

    private fun sendSetupConfig() {
        val setupMsg = LiveClientMessage(
            setup = LiveSetupConfig(
                model = LIVE_MODEL,
                generationConfig = LiveGenerationConfig(
                    responseModalities = listOf("AUDIO"),
                    speechConfig = LiveSpeechConfig(
                        voiceConfig = LiveVoiceConfig(
                            prebuiltVoiceConfig = LivePrebuiltVoiceConfig(voiceName = "Aoede")
                        )
                    )
                ),
                systemInstruction = RestContent(
                    parts = listOf(RestPart(text = GeminiRestService.ZOYA_SYSTEM_PROMPT))
                )
            )
        )
        try {
            val json = clientAdapter.toJson(setupMsg)
            webSocket?.send(json)
            isSessionConfigured = true
        } catch (_: Exception) {}
    }

    fun sendAudioChunk(pcmChunk: ByteArray) {
        if (!isConnected || webSocket == null) return
        val base64Data = Base64.encodeToString(pcmChunk, Base64.NO_WRAP)
        val audioMsg = LiveClientMessage(
            realtimeInput = LiveRealtimeInput(
                mediaChunks = listOf(
                    LiveMediaChunk(
                        mimeType = "audio/pcm;rate=16000",
                        data = base64Data
                    )
                )
            )
        )
        try {
            val json = clientAdapter.toJson(audioMsg)
            webSocket?.send(json)
        } catch (_: Exception) {}
    }

    fun sendTextMessage(text: String) {
        if (!isConnected || webSocket == null) return
        val textMsg = LiveClientMessage(
            clientContent = LiveClientContent(
                turns = listOf(
                    RestContent(role = "user", parts = listOf(RestPart(text = text)))
                ),
                turnComplete = true
            )
        )
        try {
            val json = clientAdapter.toJson(textMsg)
            webSocket?.send(json)
        } catch (_: Exception) {}
    }

    private fun handleServerMessage(json: String) {
        try {
            val msg = serverAdapter.fromJson(json) ?: return
            val serverContent = msg.serverContent

            if (serverContent?.interrupted == true) {
                scope.launch(Dispatchers.Main) {
                    onInterrupted?.invoke()
                }
                return
            }

            val parts = serverContent?.modelTurn?.parts
            if (parts != null) {
                for (part in parts) {
                    // Audio PCM playback
                    if (part.inlineData != null && part.inlineData.mimeType.contains("audio/pcm")) {
                        val pcmBytes = Base64.decode(part.inlineData.data, Base64.NO_WRAP)
                        onPcmAudioReceived?.invoke(pcmBytes)
                    }
                    // Transcript text
                    if (!part.text.isNullOrBlank()) {
                        scope.launch(Dispatchers.Main) {
                            onTranscriptReceived?.invoke(part.text, serverContent.turnComplete == true)
                        }
                    }
                }
            }
        } catch (_: Exception) {}
    }

    fun disconnect() {
        isConnected = false
        isSessionConfigured = false
        try {
            webSocket?.close(1000, "Client disconnect")
        } catch (_: Exception) {}
        webSocket = null
    }

    fun isLive(): Boolean = isConnected
}
