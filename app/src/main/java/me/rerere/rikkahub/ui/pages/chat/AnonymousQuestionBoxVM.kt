package me.rerere.rikkahub.ui.pages.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.rerere.ai.core.MessageRole
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessagePart
import me.rerere.rikkahub.data.ai.GenerationChunk
import me.rerere.rikkahub.data.ai.GenerationHandler
import me.rerere.rikkahub.data.datastore.SettingsStore
import me.rerere.rikkahub.data.datastore.findModelById
import me.rerere.rikkahub.data.model.Assistant
import me.rerere.rikkahub.data.repository.AnonymousQuestion
import me.rerere.rikkahub.data.repository.AnonymousQuestionAuthor
import me.rerere.rikkahub.data.repository.AnonymousQuestionEntry
import me.rerere.rikkahub.data.repository.AnonymousQuestionReply
import me.rerere.rikkahub.data.repository.AnonymousQuestionReplyStatus
import me.rerere.rikkahub.data.repository.AnonymousQuestionRepository
import me.rerere.rikkahub.data.repository.MemoryRepository
import kotlin.uuid.Uuid

class AnonymousQuestionBoxVM(
    private val settingsStore: SettingsStore,
    private val repository: AnonymousQuestionRepository,
    private val memoryRepository: MemoryRepository,
    private val generationHandler: GenerationHandler,
) : ViewModel() {
    private val _processing = MutableStateFlow(false)
    val processing: StateFlow<Boolean> = _processing.asStateFlow()
    private val processingIds = mutableSetOf<String>()

    fun observeQuestions(scopeId: Uuid): Flow<List<AnonymousQuestionEntry>> = repository.observeQuestions(scopeId)
    fun observeProfile(scopeId: Uuid) = repository.observeProfile(scopeId)
    fun observeHasUnread(scopeId: Uuid) = repository.observeHasUnread(scopeId)

    fun markViewed(scopeId: Uuid) {
        viewModelScope.launch { repository.markViewed(scopeId) }
    }

    fun postUserQuestion(scopeId: Uuid, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch { repository.postUserQuestion(scopeId, content) }
    }

    fun addUserAnswer(question: AnonymousQuestion, content: String) {
        if (content.isBlank() || question.author != AnonymousQuestionAuthor.ASSISTANT) return
        viewModelScope.launch { repository.addUserAnswer(question.id, content) }
    }

    fun processDue(scopeId: Uuid, assistant: Assistant, conversationSystemPrompt: String?) {
        viewModelScope.launch {
            if (_processing.value) return@launch
            _processing.value = true
            try {
                val now = System.currentTimeMillis()
                repository.getDueQuestions(scopeId, now, AnonymousQuestionRepository.MAX_PROCESSING_ITEMS)
                    .forEach { processQuestion(it, assistant, conversationSystemPrompt) }
                repository.getDueAnswers(scopeId, now, AnonymousQuestionRepository.MAX_PROCESSING_ITEMS)
                    .forEach { processAnswer(it, assistant, conversationSystemPrompt) }
            } finally {
                _processing.value = false
            }
        }
    }

    private suspend fun processQuestion(question: AnonymousQuestion, assistant: Assistant, conversationSystemPrompt: String?) {
        val key = "question:${question.id}"
        if (!processingIds.add(key)) return
        try {
            val answer = generateAnonymousAnswer(question.content, assistant, conversationSystemPrompt) ?: return
            repository.addAssistantAnswer(question.id, answer)
            repository.updateQuestion(question.copy(replyStatus = AnonymousQuestionReplyStatus.DONE))
        } finally {
            processingIds.remove(key)
        }
    }

    private suspend fun processAnswer(reply: AnonymousQuestionReply, assistant: Assistant, conversationSystemPrompt: String?) {
        val key = "answer:${reply.id}"
        if (!processingIds.add(key)) return
        try {
            val question = repository.getQuestion(reply.questionId) ?: return
            val comment = generateAnonymousComment(question.content, reply.content, assistant, conversationSystemPrompt) ?: return
            repository.addAssistantComment(question.id, comment)
            repository.updateReply(reply.copy(replyStatus = AnonymousQuestionReplyStatus.DONE))
        } finally {
            processingIds.remove(key)
        }
    }

    private suspend fun generateAnonymousAnswer(question: String, assistant: Assistant, conversationSystemPrompt: String?): String? {
        return generateText(
            assistant = assistant,
            conversationSystemPrompt = conversationSystemPrompt,
            prompt = """
                Answer an anonymous question in the assistant's anonymous question box.
                You do not know who asked it. Do not infer, identify, name, or expose the asker.
                Return only a natural, concise answer with no markdown and no identity claims.
                Keep the complete answer within 200 characters and finish it naturally before reaching the limit.

                Anonymous question:
                $question
            """.trimIndent(),
            maxTokens = 240,
        )
    }

    private suspend fun generateAnonymousComment(question: String, answer: String, assistant: Assistant, conversationSystemPrompt: String?): String? {
        return generateText(
            assistant = assistant,
            conversationSystemPrompt = conversationSystemPrompt,
            prompt = """
                Write a short natural comment on an anonymous question-box answer.
                The question and answer are anonymous. Do not infer who wrote either one.
                Do not reveal that the assistant authored the question or mention any identity.
                Return only the comment text, with no markdown.
                Keep the complete comment within 200 characters and finish it naturally before reaching the limit.

                Anonymous question:
                $question

                Anonymous answer:
                $answer
            """.trimIndent(),
            maxTokens = 180,
        )
    }

    private suspend fun generateText(
        assistant: Assistant,
        conversationSystemPrompt: String?,
        prompt: String,
        maxTokens: Int,
    ): String? {
        val settings = settingsStore.settingsFlow.value
        val model = settings.findModelById(assistant.chatModelId ?: settings.chatModelId) ?: return null
        val memories = if (assistant.useGlobalMemory) {
            memoryRepository.getGlobalMemories()
        } else {
            memoryRepository.getMemoriesOfAssistant(assistant.id.toString())
        }
        var output = ""
        generationHandler.generateText(
            settings = settings,
            model = model,
            messages = listOf(UIMessage(role = MessageRole.USER, parts = listOf(UIMessagePart.Text(prompt)))),
            assistant = assistant.copy(streamOutput = false),
            conversationSystemPrompt = conversationSystemPrompt,
            memories = memories,
            tools = emptyList(),
            maxSteps = 1,
            maxTokensOverride = maxTokens,
        ).collect { chunk ->
            if (chunk is GenerationChunk.Messages) {
                output = chunk.messages.lastOrNull()?.parts?.joinToString("\n") { part ->
                    if (part is UIMessagePart.Text) part.text else ""
                }.orEmpty()
            }
        }
        return output.trim().takeIf { it.isNotBlank() }
    }
}
