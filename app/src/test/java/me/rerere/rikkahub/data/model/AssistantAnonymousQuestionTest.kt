package me.rerere.rikkahub.data.model

import kotlinx.serialization.json.Json
import me.rerere.rikkahub.data.ai.tools.LocalToolOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AssistantAnonymousQuestionTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun missingFlagDefaultsToEnabled() {
        val assistant = json.decodeFromString<Assistant>("{\"name\":\"test\"}")
        assertTrue(assistant.anonymousQuestionBoxEnabled)
    }

    @Test
    fun flagRoundTrips() {
        val encoded = json.encodeToString(Assistant.serializer(), Assistant(name = "test", anonymousQuestionBoxEnabled = false))
        val decoded = json.decodeFromString<Assistant>(encoded)
        assertFalse(decoded.anonymousQuestionBoxEnabled)
    }

    @Test
    fun voiceCallToolOptionRoundTrips() {
        val assistant = Assistant(
            name = "test",
            localTools = listOf(LocalToolOption.VoiceCall),
        )

        val encoded = json.encodeToString(Assistant.serializer(), assistant)
        val decoded = json.decodeFromString<Assistant>(encoded)

        assertEquals(listOf(LocalToolOption.VoiceCall), decoded.localTools)
    }
}
