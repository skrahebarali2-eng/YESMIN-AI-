package com.yesmin.ai.ai.model

import com.google.gson.annotations.SerializedName

data class GroqChatRequest(
    @SerializedName("model") val model: String,
    @SerializedName("messages") val messages: List<ChatMessage>,
    @SerializedName("temperature") val temperature: Double = 0.7,
    @SerializedName("max_tokens") val maxTokens: Int = 1024,
    @SerializedName("response_format") val responseFormat: ResponseFormat? = null
)

data class ResponseFormat(
    @SerializedName("type") val type: String = "json_object"
)

data class ChatMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class GroqChatResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("choices") val choices: List<GroqChoice>?,
    @SerializedName("model") val model: String?,
    @SerializedName("usage") val usage: GroqUsage?
)

data class GroqChoice(
    @SerializedName("index") val index: Int,
    @SerializedName("message") val message: ChatMessage,
    @SerializedName("finish_reason") val finishReason: String?
)

data class GroqUsage(
    @SerializedName("prompt_tokens") val promptTokens: Int,
    @SerializedName("completion_tokens") val completionTokens: Int,
    @SerializedName("total_tokens") val totalTokens: Int
)

data class GroqModelsResponse(
    @SerializedName("data") val data: List<GroqModelItem>
)

data class GroqModelItem(
    @SerializedName("id") val id: String,
    @SerializedName("object") val objectType: String?,
    @SerializedName("owned_by") val ownedBy: String?,
    @SerializedName("active") val active: Boolean = true
)

data class GeminiGenerateContentRequest(
    @SerializedName("contents") val contents: List<GeminiContent>,
    @SerializedName("systemInstruction") val systemInstruction: GeminiSystemInstruction? = null,
    @SerializedName("generationConfig") val generationConfig: GeminiGenerationConfig? = null
)

data class GeminiSystemInstruction(
    @SerializedName("parts") val parts: List<GeminiPart>
)

data class GeminiContent(
    @SerializedName("role") val role: String = "user",
    @SerializedName("parts") val parts: List<GeminiPart>
)

data class GeminiPart(
    @SerializedName("text") val text: String? = null,
    @SerializedName("inlineData") val inlineData: GeminiInlineData? = null
)

data class GeminiInlineData(
    @SerializedName("mimeType") val mimeType: String,
    @SerializedName("data") val data: String
)

data class GeminiGenerationConfig(
    @SerializedName("temperature") val temperature: Double = 0.7,
    @SerializedName("maxOutputTokens") val maxOutputTokens: Int = 1024,
    @SerializedName("responseMimeType") val responseMimeType: String? = null
)

data class GeminiGenerateContentResponse(
    @SerializedName("candidates") val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    @SerializedName("content") val content: GeminiContent?
)

data class YesminAiResponsePayload(
    @SerializedName("spoken_reply") val spokenReply: String,
    @SerializedName("language") val language: String = "hi",
    @SerializedName("action") val action: YesminStructuredAction? = null,
    @SerializedName("emotion") val emotion: String = "caring"
)

data class YesminStructuredAction(
    @SerializedName("type") val type: String,
    @SerializedName("target") val target: String? = null,
    @SerializedName("params") val params: Map<String, Any>? = null,
    @SerializedName("requires_confirmation") val requiresConfirmation: Boolean = false,
    @SerializedName("confirmation_prompt") val confirmationPrompt: String? = null
)
