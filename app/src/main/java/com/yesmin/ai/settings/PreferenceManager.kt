package com.yesmin.ai.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class PreferenceManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            "yesmin_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        context.getSharedPreferences("yesmin_secure_prefs_fallback", Context.MODE_PRIVATE)
    }

    var groqApiKey: String
        get() = prefs.getString("groq_api_key", "") ?: ""
        set(value) = prefs.edit().putString("groq_api_key", value.trim()).apply()

    var geminiApiKey: String
        get() = prefs.getString("gemini_api_key", "") ?: ""
        set(value) = prefs.edit().putString("gemini_api_key", value.trim()).apply()

    var isGeminiBackupEnabled: Boolean
        get() = prefs.getBoolean("gemini_backup_enabled", true)
        set(value) = prefs.edit().putBoolean("gemini_backup_enabled", value).apply()

    var selectedGroqModel: String
        get() = prefs.getString("selected_groq_model", "llama-3.3-70b-versatile") ?: "llama-3.3-70b-versatile"
        set(value) = prefs.edit().putString("selected_groq_model", value).apply()

    var selectedGeminiModel: String
        get() = prefs.getString("selected_gemini_model", "gemini-2.5-flash") ?: "gemini-2.5-flash"
        set(value) = prefs.edit().putString("selected_gemini_model", value).apply()

    var userName: String
        get() = prefs.getString("user_name", "Jaan") ?: "Jaan"
        set(value) = prefs.edit().putString("user_name", value).apply()

    var personalityTone: String
        get() = prefs.getString("personality_tone", "Caring, sweet, playful and romantic") ?: "Caring, sweet, playful and romantic"
        set(value) = prefs.edit().putString("personality_tone", value).apply()
}
