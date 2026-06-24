package me.rerere.tts.provider

import android.content.Context
import kotlinx.coroutines.flow.Flow
import me.rerere.tts.model.AudioChunk
import me.rerere.tts.model.TTSRequest
import me.rerere.tts.provider.providers.GeminiTTSProvider
import me.rerere.tts.provider.providers.GroqTTSProvider
import me.rerere.tts.provider.providers.MiMoTTSProvider
import me.rerere.tts.provider.providers.MiniMaxTTSProvider
import me.rerere.tts.provider.providers.OpenAITTSProvider
import me.rerere.tts.provider.providers.QwenTTSProvider
import me.rerere.tts.provider.providers.SystemTTSProvider
import me.rerere.tts.provider.providers.XAITTSProvider

class TTSManager(private val context: Context) {
    private val openAIProvider = OpenAITTSProvider()
    private val geminiProvider = GeminiTTSProvider()
    private val systemProvider = SystemTTSProvider()
    private val miniMaxProvider = MiniMaxTTSProvider()
    private val qwenProvider = QwenTTSProvider()
    private val groqProvider = GroqTTSProvider()
    private val xaiProvider = XAITTSProvider()
    private val miMoProvider = MiMoTTSProvider()

    fun generateSpeech(
        providerSetting: TTSProviderSetting,
        request: TTSRequest
    ): Flow<AudioChunk> {
        return when (val normalizedSetting = providerSetting.normalizeKnownProvider()) {
            is TTSProviderSetting.OpenAI -> openAIProvider.generateSpeech(context, normalizedSetting, request)
            is TTSProviderSetting.MiMo -> miMoProvider.generateSpeech(context, normalizedSetting, request)
            is TTSProviderSetting.Gemini -> geminiProvider.generateSpeech(context, normalizedSetting, request)
            is TTSProviderSetting.SystemTTS -> systemProvider.generateSpeech(context, normalizedSetting, request)
            is TTSProviderSetting.MiniMax -> miniMaxProvider.generateSpeech(context, normalizedSetting, request)
            is TTSProviderSetting.Qwen -> qwenProvider.generateSpeech(context, normalizedSetting, request)
            is TTSProviderSetting.Groq -> groqProvider.generateSpeech(context, normalizedSetting, request)
            is TTSProviderSetting.XAI -> xaiProvider.generateSpeech(context, normalizedSetting, request)
        }
    }

    private fun TTSProviderSetting.normalizeKnownProvider(): TTSProviderSetting {
        return when (this) {
            is TTSProviderSetting.OpenAI -> {
                if (baseUrl.contains("xiaomimimo.com", ignoreCase = true)) {
                    val openAIVoices = setOf("alloy", "echo", "fable", "onyx", "nova", "shimmer")
                    TTSProviderSetting.MiMo(
                        id = id,
                        name = name.ifBlank { "MiMo TTS" },
                        apiKey = apiKey,
                        baseUrl = baseUrl,
                        model = model,
                        voice = voice.takeUnless { it in openAIVoices } ?: "mimo_default"
                    )
                } else {
                    this
                }
            }

            else -> this
        }
    }
}
