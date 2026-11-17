package com.yesmin.ai.ai

import android.graphics.Bitmap
import android.util.Base64
import com.google.gson.Gson
import com.yesmin.ai.ai.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiApiClient {

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        const val DEFAULT_MODEL = "gemini-2.5-flash"
    }

    suspend fun testConnection(apiKey: String, model: String = DEFAULT_MODEL): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Gemini API Key is blank"))
        }

        try {
            val url = BASE_URL + "/" + model + ":generateContent?key=" + apiKey
            val requestPayload = GeminiGenerateContentRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = "Hello, reply with OK"))
                    )
                )
            )

            val body = gson.toJson(requestPayload).toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success("Gemini connected successfully (" + model + " ready)")
                } else {
                    val err = response.body?.string() ?: response.message
                    Result.failure(IOException("Gemini HTTP " + response.code + ": " + err))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendChat(
        apiKey: String,
        model: String,
        systemPrompt: String,
        userPrompt: String,
        requireJson: Boolean = true
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val selectedModel = if (model.isNotBlank()) model else DEFAULT_MODEL
            val url = BASE_URL + "/" + selectedModel + ":generateContent?key=" + apiKey

            val payload = GeminiGenerateContentRequest(
                systemInstruction = GeminiSystemInstruction(listOf(GeminiPart(text = systemPrompt))),
                contents = listOf(
                    GeminiContent(role = "user", parts = listOf(GeminiPart(text = userPrompt)))
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.7,
                    responseMimeType = if (requireJson) "application/json" else null
                )
            )

            val body = gson.toJson(payload).toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()

            client.newCall(request).execute().use { response ->
                val respStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val parsed = gson.fromJson(respStr, GeminiGenerateContentResponse::class.java)
                    val text = parsed.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (text != null) {
                        Result.success(text)
                    } else {
                        Result.failure(IOException("Gemini returned empty candidate"))
                    }
                } else {
                    Result.failure(IOException("Gemini error " + response.code + ": " + respStr))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
