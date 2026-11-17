package com.yesmin.ai.ai

import com.google.gson.Gson
import com.yesmin.ai.ai.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class GroqApiClient {

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    companion object {
        const val BASE_URL = "https://api.groq.com/openai/v1"
        const val DEFAULT_MODEL = "llama-3.3-70b-versatile"
        val FALLBACK_MODELS = listOf(
            "llama-3.3-70b-versatile",
            "llama-3.1-8b-instant",
            "mixtral-8x7b-32768",
            "gemma2-9b-it"
        )
    }

    suspend fun testConnection(apiKey: String): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Groq API Key cannot be blank"))
        }

        try {
            val request = Request.Builder()
                .url(BASE_URL + "/models")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val modelsResponse = gson.fromJson(body, GroqModelsResponse::class.java)
                    val activeCount = modelsResponse.data.size
                    Result.success("Connected successfully! (" + activeCount + " models available)")
                } else {
                    val err = response.body?.string() ?: response.message
                    Result.failure(IOException("Groq HTTP " + response.code + ": " + err))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchModels(apiKey: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(BASE_URL + "/models")
                .header("Authorization", "Bearer " + apiKey)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val parsed = gson.fromJson(body, GroqModelsResponse::class.java)
                    val models = parsed.data
                        .filter { it.active }
                        .map { it.id }
                        .sorted()
                    Result.success(if (models.isNotEmpty()) models else FALLBACK_MODELS)
                } else {
                    Result.success(FALLBACK_MODELS)
                }
            }
        } catch (e: Exception) {
            Result.success(FALLBACK_MODELS)
        }
    }

    suspend fun sendChat(
        apiKey: String,
        model: String,
        messages: List<ChatMessage>,
        asJson: Boolean = true
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Groq API Key is not configured"))
        }

        try {
            val chosenModel = if (model.isNotBlank()) model else DEFAULT_MODEL
            val payload = GroqChatRequest(
                model = chosenModel,
                messages = messages,
                temperature = 0.75,
                maxTokens = 1200,
                responseFormat = if (asJson) ResponseFormat("json_object") else null
            )

            val jsonBody = gson.toJson(payload)
            val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(BASE_URL + "/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val responseString = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val groqResponse = gson.fromJson(responseString, GroqChatResponse::class.java)
                    val content = groqResponse.choices?.firstOrNull()?.message?.content
                    if (content != null) {
                        Result.success(content)
                    } else {
                        Result.failure(IOException("Empty completion response from Groq"))
                    }
                } else {
                    Result.failure(IOException("Groq error [" + response.code + "]: " + responseString))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
