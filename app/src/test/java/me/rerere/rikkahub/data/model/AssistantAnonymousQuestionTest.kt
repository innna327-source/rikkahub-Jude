package me.rerere.rikkahub.data.model

import kotlinx.serialization.json.Json
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
}
