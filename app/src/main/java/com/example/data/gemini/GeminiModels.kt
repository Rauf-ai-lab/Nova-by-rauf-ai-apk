package com.example.data.gemini

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ==================== REST MODELS ====================

@JsonClass(generateAdapter = true)
data class GeminiRestRequest(
    val contents: List<RestContent>,
    @Json(name = "system_instruction") val systemInstruction: RestContent? = null,
    @Json(name = "generationConfig") val generationConfig: RestGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class RestContent(
    val role: String? = "user",
    val parts: List<RestPart>
)

@JsonClass(generateAdapter = true)
data class RestPart(
    val text: String? = null,
    @Json(name = "inline_data") val inlineData: RestInlineData? = null
)

@JsonClass(generateAdapter = true)
data class RestInlineData(
    @Json(name = "mime_type") val mimeType: String,
    val data: String
)

@JsonClass(generateAdapter = true)
data class RestGenerationConfig(
    val temperature: Float? = 0.7f,
    val topP: Float? = 0.95f,
    val topK: Int? = 40,
    @Json(name = "response_mime_type") val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRestResponse(
    val candidates: List<RestCandidate>? = null,
    val error: RestError? = null
)

@JsonClass(generateAdapter = true)
data class RestCandidate(
    val content: RestContent? = null,
    val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class RestError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

// ==================== GEMINI LIVE WEBSOCKET PROTOCOL ====================

@JsonClass(generateAdapter = true)
data class LiveClientMessage(
    val setup: LiveSetupConfig? = null,
    @Json(name = "realtime_input") val realtimeInput: LiveRealtimeInput? = null,
    @Json(name = "client_content") val clientContent: LiveClientContent? = null
)

@JsonClass(generateAdapter = true)
data class LiveSetupConfig(
    val model: String,
    @Json(name = "generation_config") val generationConfig: LiveGenerationConfig? = null,
    @Json(name = "system_instruction") val systemInstruction: RestContent? = null
)

@JsonClass(generateAdapter = true)
data class LiveGenerationConfig(
    @Json(name = "response_modalities") val responseModalities: List<String> = listOf("AUDIO"),
    @Json(name = "speech_config") val speechConfig: LiveSpeechConfig? = null
)

@JsonClass(generateAdapter = true)
data class LiveSpeechConfig(
    @Json(name = "voice_config") val voiceConfig: LiveVoiceConfig? = null
)

@JsonClass(generateAdapter = true)
data class LiveVoiceConfig(
    @Json(name = "prebuilt_voice_config") val prebuiltVoiceConfig: LivePrebuiltVoiceConfig? = null
)

@JsonClass(generateAdapter = true)
data class LivePrebuiltVoiceConfig(
    @Json(name = "voice_name") val voiceName: String = "Aoede" // Bright, clear, youthful voice
)

@JsonClass(generateAdapter = true)
data class LiveRealtimeInput(
    @Json(name = "media_chunks") val mediaChunks: List<LiveMediaChunk>
)

@JsonClass(generateAdapter = true)
data class LiveMediaChunk(
    @Json(name = "mime_type") val mimeType: String = "audio/pcm;rate=16000",
    val data: String // Base64 encoded PCM16
)

@JsonClass(generateAdapter = true)
data class LiveClientContent(
    val turns: List<RestContent>,
    @Json(name = "turn_complete") val turnComplete: Boolean = true
)

@JsonClass(generateAdapter = true)
data class LiveServerMessage(
    @Json(name = "serverContent") val serverContent: LiveServerContent? = null,
    @Json(name = "toolCall") val toolCall: Any? = null,
    @Json(name = "toolCallCancellation") val toolCallCancellation: Any? = null
)

@JsonClass(generateAdapter = true)
data class LiveServerContent(
    @Json(name = "modelTurn") val modelTurn: RestContent? = null,
    @Json(name = "turnComplete") val turnComplete: Boolean? = null,
    val interrupted: Boolean? = null
)
