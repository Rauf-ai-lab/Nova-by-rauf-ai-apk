package com.example.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.sqrt

class AudioPlaybackManager(
    private val context: Context,
    private val scope: CoroutineScope
) : TextToSpeech.OnInitListener {

    private var audioTrack: AudioTrack? = null
    private val pcmQueue = LinkedBlockingQueue<ByteArray>()
    private var playbackJob: Job? = null
    private var isPcmPlaying = false

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    var onPlaybackStarted: (() -> Unit)? = null
    var onPlaybackCompleted: (() -> Unit)? = null
    var onPlaybackAmplitude: ((Float) -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
        initAudioTrack(24000)
    }

    private fun initAudioTrack(sampleRate: Int) {
        try {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = (minBufSize * 2).coerceAtLeast(4096)

            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            val format = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(format)
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
            startPlaybackLoop()
        } catch (_: Exception) {}
    }

    private fun startPlaybackLoop() {
        playbackJob?.cancel()
        playbackJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                val chunk = pcmQueue.poll()
                if (chunk != null) {
                    if (!isPcmPlaying) {
                        isPcmPlaying = true
                        onPlaybackStarted?.invoke()
                    }
                    audioTrack?.write(chunk, 0, chunk.size)

                    // Calculate amplitude for speaking orb animation
                    var sum = 0.0
                    var i = 0
                    while (i < chunk.size - 1) {
                        val sample = (chunk[i].toInt() and 0xFF) or (chunk[i + 1].toInt() shl 8)
                        val shortSample = sample.toShort()
                        sum += shortSample * shortSample
                        i += 2
                    }
                    val rms = sqrt(sum / (chunk.size / 2.0)).toFloat()
                    val normalized = (rms / 32768.0f).coerceIn(0.1f, 1f)
                    onPlaybackAmplitude?.invoke(normalized)
                } else {
                    if (isPcmPlaying) {
                        isPcmPlaying = false
                        onPlaybackCompleted?.invoke()
                        onPlaybackAmplitude?.invoke(0f)
                    }
                    kotlinx.coroutines.delay(10)
                }
            }
        }
    }

    fun playPcmChunk(pcmData: ByteArray) {
        pcmQueue.offer(pcmData)
    }

    fun speakText(text: String, onFinished: (() -> Unit)? = null) {
        interrupt()
        if (isTtsReady && tts != null) {
            val utteranceId = "ZOYA_${System.currentTimeMillis()}"
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onPlaybackStarted?.invoke()
                }

                override fun onDone(utteranceId: String?) {
                    onPlaybackCompleted?.invoke()
                    onFinished?.invoke()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    onPlaybackCompleted?.invoke()
                    onFinished?.invoke()
                }
            })
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    /**
     * Instant Barge-in / Interruption handler:
     * Immediately stops AudioTrack, drops queue, stops TTS, and resets amplitude.
     */
    fun interrupt() {
        pcmQueue.clear()
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.play()
        } catch (_: Exception) {}
        try {
            tts?.stop()
        } catch (_: Exception) {}
        isPcmPlaying = false
        onPlaybackAmplitude?.invoke(0f)
        onPlaybackCompleted?.invoke()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            tts?.setPitch(1.15f) // Energetic, young voice for Zoya
            tts?.setSpeechRate(1.05f) // Confident, brisk pacing
            isTtsReady = true
        }
    }

    fun release() {
        playbackJob?.cancel()
        pcmQueue.clear()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
    }
}
