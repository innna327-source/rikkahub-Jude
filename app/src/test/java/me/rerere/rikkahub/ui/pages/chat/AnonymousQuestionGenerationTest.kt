package me.rerere.rikkahub.ui.pages.chat

import me.rerere.ai.core.ReasoningLevel
import me.rerere.ai.ui.UIMessagePart
import me.rerere.rikkahub.data.model.Assistant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnonymousQuestionGenerationTest {
    @Test
    fun anonymousResponseBudgetLeavesRoomForTwoHundredChineseCharacters() {
        assertTrue(ANONYMOUS_RESPONSE_MAX_TOKENS >= 512)
    }

    @Test
    fun anonymousGenerationDoesNotUseStreamingOrReasoningBudget() {
        val assistant = Assistant(
            name = "test",
            streamOutput = true,
            reasoningLevel = ReasoningLevel.HIGH,
        )

        val generationAssistant = assistant.forAnonymousQuestionGeneration()

        assertFalse(generationAssistant.streamOutput)
        assertEquals(ReasoningLevel.OFF, generationAssistant.reasoningLevel)
        assertEquals(ReasoningLevel.HIGH, assistant.reasoningLevel)
    }

    @Test
    fun visibleTextIgnoresReasoningParts() {
        val visibleText = listOf(
            UIMessagePart.Reasoning("hidden reasoning"),
            UIMessagePart.Text("visible answer"),
            UIMessagePart.Reasoning("more hidden reasoning"),
            UIMessagePart.Text("second line"),
        ).anonymousQuestionVisibleText()

        assertEquals("visible answer\nsecond line", visibleText)
    }
}
