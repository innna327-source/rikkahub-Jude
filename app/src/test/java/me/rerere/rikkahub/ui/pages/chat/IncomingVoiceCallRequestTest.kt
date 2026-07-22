package me.rerere.rikkahub.ui.pages.chat

import me.rerere.ai.core.MessageRole
import me.rerere.ai.ui.ToolApprovalState
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessagePart
import me.rerere.rikkahub.data.ai.tools.REQUEST_VOICE_CALL_TOOL_NAME
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IncomingVoiceCallRequestTest {
    @Test
    fun returnsLatestPendingVoiceCall() {
        val older = voiceCallMessage(toolCallId = "older", reason = "旧来电")
        val latest = voiceCallMessage(toolCallId = "latest", reason = "想听听你的声音")

        val request = listOf(older, latest).pendingIncomingVoiceCall()

        assertEquals("latest", request?.toolCallId)
        assertEquals(latest.id.toString(), request?.assistantMessageId)
        assertEquals("想听听你的声音", request?.reason)
    }

    @Test
    fun ignoresVoiceCallThatIsNotPending() {
        val message = voiceCallMessage(
            toolCallId = "approved",
            reason = "聊一会儿",
            approvalState = ToolApprovalState.Approved,
        )

        assertNull(listOf(message).pendingIncomingVoiceCall())
    }

    @Test
    fun usesNaturalFallbackForBlankReason() {
        val request = listOf(
            voiceCallMessage(toolCallId = "blank", reason = "   ")
        ).pendingIncomingVoiceCall()

        assertEquals("想和你聊聊", request?.reason)
    }

    private fun voiceCallMessage(
        toolCallId: String,
        reason: String,
        approvalState: ToolApprovalState = ToolApprovalState.Pending,
    ): UIMessage {
        return UIMessage(
            role = MessageRole.ASSISTANT,
            parts = listOf(
                UIMessagePart.Tool(
                    toolCallId = toolCallId,
                    toolName = REQUEST_VOICE_CALL_TOOL_NAME,
                    input = "{\"reason\":\"$reason\"}",
                    approvalState = approvalState,
                )
            ),
        )
    }
}
