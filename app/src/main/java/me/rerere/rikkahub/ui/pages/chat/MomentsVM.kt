package me.rerere.rikkahub.ui.pages.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.rerere.ai.core.MessageRole
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessagePart
import me.rerere.rikkahub.data.ai.GenerationChunk
import me.rerere.rikkahub.data.ai.GenerationHandler
import me.rerere.rikkahub.data.datastore.SettingsStore
import me.rerere.rikkahub.data.datastore.findModelById
import me.rerere.rikkahub.data.files.FilesManager
import me.rerere.rikkahub.data.model.Assistant
import me.rerere.rikkahub.data.repository.ConversationRepository
import me.rerere.rikkahub.data.repository.MemoryRepository
import me.rerere.rikkahub.data.repository.Moment
import me.rerere.rikkahub.data.repository.MomentAuthor
import me.rerere.rikkahub.data.repository.MomentComment
import me.rerere.rikkahub.data.repository.MomentEntry
import me.rerere.rikkahub.data.repository.MomentProfile
import me.rerere.rikkahub.data.repository.MomentReplyStatus
import me.rerere.rikkahub.data.repository.MomentRepository
import me.rerere.rikkahub.utils.JsonInstant
import kotlin.uuid.Uuid

class MomentsVM(
    private val settingsStore: SettingsStore,
    private val momentRepository: MomentRepository,
    private val conversationRepository: ConversationRepository,
    private val memoryRepository: MemoryRepository,
    private val generationHandler: GenerationHandler,
    val filesManager: FilesManager,
) : ViewModel() {
    private val _processing = MutableStateFlow(false)
    val processing: StateFlow<Boolean> = _processing.asStateFlow()
    private val processingIds = mutableSetOf<String>()

    fun observeTimeline(assistantId: Uuid): Flow<List<MomentEntry>> = momentRepository.observeTimeline(assistantId)

    fun observeProfile(assistantId: Uuid): Flow<MomentProfile> = momentRepository.observeProfile(assistantId)

    fun observeHasUnread(assistantId: Uuid): Flow<Boolean> = momentRepository.observeHasUnread(assistantId)

    fun markViewed(assistantId: Uuid) {
        viewModelScope.launch {
            momentRepository.markViewed(assistantId)
        }
    }

    fun updateCover(assistantId: Uuid, coverUri: String) {
        viewModelScope.launch {
            momentRepository.updateCover(assistantId, coverUri)
        }
    }

    fun postUserMoment(assistantId: Uuid, content: String, imageUris: List<String>) {
        if (content.isBlank() && imageUris.isEmpty()) return
        viewModelScope.launch {
            momentRepository.postUserMoment(assistantId, content, imageUris)
        }
    }

    fun toggleUserLike(momentId: Uuid) {
        viewModelScope.launch {
            momentRepository.toggleUserLike(momentId)
        }
    }

    fun addComment(momentId: Uuid, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            momentRepository.addUserComment(momentId, content)
        }
    }

    fun processDue(
        assistantId: Uuid,
        assistant: Assistant,
        conversationSystemPrompt: String?,
    ) {
        viewModelScope.launch {
            if (_processing.value) return@launch
            _processing.value = true
            try {
                val now = System.currentTimeMillis()
                val dueMoments = momentRepository.getDueUserMoments(assistantId, now, limit = 3)
                dueMoments.forEach { moment ->
                    processDueMoment(moment, assistant, conversationSystemPrompt)
                }
                val dueComments = momentRepository.getDueUserComments(assistantId, now, limit = 3)
                dueComments.forEach { comment ->
                    processDueComment(comment, assistant, conversationSystemPrompt)
                }
            } finally {
                _processing.value = false
            }
        }
    }

    private suspend fun processDueMoment(
        moment: Moment,
        assistant: Assistant,
        conversationSystemPrompt: String?,
    ) {
        val key = "moment:${moment.id}"
        if (!processingIds.add(key)) return
        try {
            val reaction = generateMomentReaction(moment, assistant, conversationSystemPrompt)
                ?: MomentReaction(liked = true, comment = "")
            val parsed = parseImageDescriptionOutput(reaction.comment)
            momentRepository.updateMoment(
                moment.copy(
                    aiLiked = reaction.liked,
                    aiReplyContent = parsed.visibleText,
                    imageDescription = parsed.imageDescription ?: moment.imageDescription,
                    repliedAt = System.currentTimeMillis(),
                    replyStatus = MomentReplyStatus.DONE,
                )
            )
        } finally {
            processingIds.remove(key)
        }
    }

    private suspend fun processDueComment(
        comment: MomentComment,
        assistant: Assistant,
        conversationSystemPrompt: String?,
    ) {
        val key = "comment:${comment.id}"
        if (!processingIds.add(key)) return
        try {
            val moment = momentRepository.getMoment(comment.momentId) ?: return
            val comments = momentRepository.getComments(moment.id)
            val reply = generateCommentReply(moment, comments, comment, assistant, conversationSystemPrompt)
                .orEmpty()
                .trim()
            if (reply.isNotBlank()) {
                momentRepository.addAssistantComment(moment.id, reply)
            }
            momentRepository.updateComment(comment.copy(replyStatus = MomentReplyStatus.DONE))
        } finally {
            processingIds.remove(key)
        }
    }

    private suspend fun generateMomentReaction(
        moment: Moment,
        assistant: Assistant,
        conversationSystemPrompt: String?,
    ): MomentReaction? {
        val settings = settingsStore.settingsFlow.value
        val model = settings.findModelById(assistant.chatModelId ?: settings.chatModelId) ?: return null
        val memories = if (assistant.useGlobalMemory) {
            memoryRepository.getGlobalMemories()
        } else {
            memoryRepository.getMemoriesOfAssistant(assistant.id.toString())
        }
        val recentContext = buildRecentChatContext(assistant.id)
        val timelineContext = buildTimelineContext(moment.assistantId, excludeMomentId = moment.id)
        val imageInstruction = if (moment.imageUris.isNotEmpty() && moment.imageDescription.isBlank()) {
            """
            This moment includes images. After the visible JSON response, include an [image_desc]...[/image_desc]
            block with a concise objective visual description. Do not put image description text inside JSON.comment.
            """.trimIndent()
        } else {
            ""
        }
        val prompt = """
            Generate the assistant's private reaction to this user Moment.
            Output only JSON with optional fields: {"like": true|false, "comment": "short natural comment"}.
            The comment should be brief, intimate, and specific. It may be empty.

            Recent chat context:
            $recentContext

            Recent Moments context:
            $timelineContext

            Current Moment:
            ${moment.content}
            Images: ${moment.imageUris.size}
            Stored image description: ${moment.imageDescription.ifBlank { "(none yet)" }}

            $imageInstruction
        """.trimIndent()
        val parts = buildList {
            add(UIMessagePart.Text(prompt))
            if (moment.imageDescription.isBlank()) {
                moment.imageUris.take(MomentRepository.MAX_IMAGES).forEach { uri ->
                    add(UIMessagePart.Image(uri))
                }
            }
        }
        val text = generateText(
            settings = settings,
            assistant = assistant,
            model = model,
            memories = memories,
            messages = listOf(UIMessage(role = MessageRole.USER, parts = parts)),
            conversationSystemPrompt = conversationSystemPrompt,
            maxTokens = 360,
        )
        val parsed = parseMomentReaction(text)
        val imageParsed = parseImageDescriptionOutput(text)
        return parsed?.copy(comment = listOf(parsed.comment, imageParsed.imageDescription?.let {
            "[image_desc]$it[/image_desc]"
        }).filterNotNull().joinToString("\n"))
    }

    private suspend fun generateCommentReply(
        moment: Moment,
        comments: List<MomentComment>,
        target: MomentComment,
        assistant: Assistant,
        conversationSystemPrompt: String?,
    ): String? {
        val settings = settingsStore.settingsFlow.value
        val model = settings.findModelById(assistant.chatModelId ?: settings.chatModelId) ?: return null
        val memories = if (assistant.useGlobalMemory) {
            memoryRepository.getGlobalMemories()
        } else {
            memoryRepository.getMemoriesOfAssistant(assistant.id.toString())
        }
        val prompt = """
            Reply as the assistant in a Moments comment thread.
            Keep it natural and short. Return only the reply text, no JSON and no markdown.

            Original Moment:
            ${moment.content}
            Hidden context note:
            ${moment.contextNote.ifBlank { "(none)" }}
            Image description:
            ${moment.imageDescription.ifBlank { "(none)" }}

            Recent comments:
            ${comments.takeLast(10).joinToString("\n") { "${it.author.value}: ${it.content}" }}

            Reply to this user comment:
            ${target.content}
        """.trimIndent()
        return generateText(
            settings = settings,
            assistant = assistant,
            model = model,
            memories = memories,
            messages = listOf(UIMessage.user(prompt)),
            conversationSystemPrompt = conversationSystemPrompt,
            maxTokens = 180,
        ).trim()
    }

    private suspend fun buildRecentChatContext(assistantId: Uuid): String {
        val conversations = conversationRepository.getRecentConversations(assistantId, limit = 2)
        return conversations
            .flatMap { it.currentMessages.takeLast(8) }
            .takeLast(8)
            .joinToString("\n") { message ->
                "${message.role}: ${message.parts.asPlainText().take(160)}"
            }
            .ifBlank { "(none)" }
    }

    private suspend fun buildTimelineContext(assistantId: Uuid, excludeMomentId: Uuid): String {
        return momentRepository.getTimeline(assistantId)
            .map { it.moment }
            .filterNot { it.id == excludeMomentId }
            .take(3)
            .joinToString("\n") { moment ->
                "${moment.author.value}: ${moment.content.take(160)}"
            }
            .ifBlank { "(none)" }
    }

    private suspend fun generateText(
        settings: me.rerere.rikkahub.data.datastore.Settings,
        assistant: me.rerere.rikkahub.data.model.Assistant,
        model: me.rerere.ai.provider.Model,
        memories: List<me.rerere.rikkahub.data.model.AssistantMemory>,
        messages: List<UIMessage>,
        conversationSystemPrompt: String?,
        maxTokens: Int,
    ): String {
        var output = ""
        generationHandler.generateText(
            settings = settings,
            model = model,
            messages = messages,
            assistant = assistant.copy(streamOutput = false),
            conversationSystemPrompt = conversationSystemPrompt,
            memories = memories,
            tools = emptyList(),
            maxSteps = 1,
            maxTokensOverride = maxTokens,
        ).collect { chunk ->
            if (chunk is GenerationChunk.Messages) {
                output = chunk.messages.lastOrNull()?.parts?.asPlainText().orEmpty()
            }
        }
        return output
    }

    private fun parseMomentReaction(text: String): MomentReaction? {
        val jsonText = text.extractJsonObject() ?: return null
        return runCatching {
            val obj = JsonInstant.parseToJsonElement(jsonText).jsonObject
            MomentReaction(
                liked = obj["like"]?.jsonPrimitive?.booleanOrNull
                    ?: obj["liked"]?.jsonPrimitive?.booleanOrNull
                    ?: false,
                comment = obj["comment"]?.jsonPrimitive?.contentOrNull
                    ?: obj["reply_content"]?.jsonPrimitive?.contentOrNull
                    ?: "",
            )
        }.getOrNull()
    }

    private data class MomentReaction(
        val liked: Boolean,
        val comment: String,
    )

    private data class ParsedImageOutput(
        val visibleText: String,
        val imageDescription: String?,
    )

    private fun parseImageDescriptionOutput(text: String): ParsedImageOutput {
        val regex = Regex("\\[image_desc\\]([\\s\\S]*?)\\[/image_desc\\]", RegexOption.IGNORE_CASE)
        val imageDescription = regex.findAll(text).lastOrNull()?.groupValues?.getOrNull(1)?.trim()?.take(1000)
        val visible = text.replace(regex, "").trim()
        return ParsedImageOutput(visibleText = visible, imageDescription = imageDescription)
    }

    private fun String.extractJsonObject(): String? {
        val cleaned = replace("```json", "", ignoreCase = true)
            .replace("```", "")
            .trim()
        val start = cleaned.indexOf('{')
        val end = cleaned.lastIndexOf('}')
        return if (start >= 0 && end > start) cleaned.substring(start, end + 1) else null
    }

    private fun List<UIMessagePart>.asPlainText(): String {
        return joinToString("\n") { part ->
            when (part) {
                is UIMessagePart.Text -> part.text
                is UIMessagePart.Image -> "[image: ${part.url}]"
                is UIMessagePart.Video -> "[video: ${part.url}]"
                is UIMessagePart.Audio -> "[audio]"
                is UIMessagePart.Document -> "[document: ${part.fileName}]"
                else -> ""
            }
        }.trim()
    }
}
