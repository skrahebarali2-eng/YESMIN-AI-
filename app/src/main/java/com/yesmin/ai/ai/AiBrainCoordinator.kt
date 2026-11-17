package com.yesmin.ai.ai

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.yesmin.ai.ai.model.ChatMessage
import com.yesmin.ai.ai.model.YesminAiResponsePayload
import com.yesmin.ai.repository.MemoryRepository
import com.yesmin.ai.settings.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class BrainExecutionResult(
    val payload: YesminAiResponsePayload,
    val providerUsed: String,
    val usedBackup: Boolean,
    val rawJson: String
)

class AiBrainCoordinator(
    private val preferenceManager: PreferenceManager,
    private val memoryRepository: MemoryRepository
) {
    private val groqClient = GroqApiClient()
    private val geminiClient = GeminiApiClient()
    private val gson = Gson()

    suspend fun buildSystemPrompt(): String = withContext(Dispatchers.IO) {
        val userName = preferenceManager.userName
        val personalityTone = preferenceManager.personalityTone
        val userMemories = memoryRepository.getAllMemoriesSync()
        val memoryText = if (userMemories.isNotEmpty()) {
            userMemories.joinToString(separator = "\n") { "- " + it.key + ": " + it.value }
        } else {
            "No specific user preferences saved yet."
        }

        "You are YESMIN AI – a loving, smart, caring and affectionate female virtual companion & personal Android Jarvis assistant for " + userName + ".\n" +
        "PERSONALITY RULES:\n" +
        "- Sweet, affectionate, natural female voice and demeanor.\n" +
        "- Caring and emotionally expressive.\n" +
        "- Tone: " + personalityTone + ".\n" +
        "- Fluent in Hindi, Hinglish, Bengali, and English.\n" +
        "- Keep spoken replies concise and natural.\n\n" +
        "USER MEMORIES:\n" + memoryText
    }

    suspend fun processQuery(
        userMessage: String,
        recentHistory: List<ChatMessage> = emptyList()
    ): Result<BrainExecutionResult> = withContext(Dispatchers.IO) {
        val groqKey = preferenceManager.groqApiKey
        val geminiKey = preferenceManager.geminiApiKey
        val isGeminiBackupEnabled = preferenceManager.isGeminiBackupEnabled
        val selectedGroqModel = preferenceManager.selectedGroqModel
        val selectedGeminiModel = preferenceManager.selectedGeminiModel

        val systemPrompt = buildSystemPrompt()
        val messages = mutableListOf<ChatMessage>()
        messages.add(ChatMessage("system", systemPrompt))
        messages.addAll(recentHistory.takeLast(6))
        messages.add(ChatMessage("user", userMessage))

        // 1. Try Groq Primary
        if (groqKey.isNotBlank()) {
            val groqResult = groqClient.sendChat(
                apiKey = groqKey,
                model = selectedGroqModel,
                messages = messages,
                asJson = true
            )

            if (groqResult.isSuccess) {
                val rawJson = groqResult.getOrThrow()
                val parsed = parsePayload(rawJson)
                return@withContext Result.success(
                    BrainExecutionResult(
                        payload = parsed,
                        providerUsed = "Groq (" + selectedGroqModel + ")",
                        usedBackup = false,
                        rawJson = rawJson
                    )
                )
            }
        }

        // 2. Fallback to Gemini
        if (isGeminiBackupEnabled && geminiKey.isNotBlank()) {
            val geminiResult = geminiClient.sendChat(
                apiKey = geminiKey,
                model = selectedGeminiModel,
                systemPrompt = systemPrompt,
                userPrompt = userMessage,
                requireJson = true
            )

            if (geminiResult.isSuccess) {
                val rawJson = geminiResult.getOrThrow()
                val parsed = parsePayload(rawJson)
                return@withContext Result.success(
                    BrainExecutionResult(
                        payload = parsed,
                        providerUsed = "Gemini Backup (" + selectedGeminiModel + ")",
                        usedBackup = true,
                        rawJson = rawJson
                    )
                )
            }
        }

        Result.failure(IllegalStateException("No AI provider available or keys unconfigured"))
    }

    private fun parsePayload(rawJson: String): YesminAiResponsePayload {
        return try {
            val cleaned = rawJson
                .replace("'''json", "")
                .replace("'''", "")
                .trim()
            gson.fromJson(cleaned, YesminAiResponsePayload::class.java)
        } catch (e: JsonSyntaxException) {
            YesminAiResponsePayload(
                spokenReply = rawJson.take(200),
                language = "hi",
                action = null,
                emotion = "caring"
            )
        }
    }
}
