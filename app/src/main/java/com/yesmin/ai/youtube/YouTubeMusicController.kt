package com.yesmin.ai.youtube

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.yesmin.ai.automation.ActionExecutionResult
import java.net.URLEncoder

class YouTubeMusicController(private val context: Context) {

    fun searchAndPlay(query: String): ActionExecutionResult {
        if (query.isBlank()) {
            return ActionExecutionResult(false, "No song query provided", "SEARCH_YOUTUBE")
        }

        val cleanQuery = cleanSongQuery(query)
        val encodedQuery = URLEncoder.encode(cleanQuery, "UTF-8")

        val appIntent = Intent(Intent.ACTION_SEARCH).apply {
            setPackage("com.google.android.youtube")
            putExtra("query", cleanQuery)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return try {
            if (appIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(appIntent)
                ActionExecutionResult(true, "Playing " + cleanQuery + " in YouTube app", "SEARCH_YOUTUBE")
            } else {
                val webUrl = "https://www.youtube.com/results?search_query=" + encodedQuery
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                ActionExecutionResult(true, "Opened YouTube search for " + cleanQuery, "SEARCH_YOUTUBE")
            }
        } catch (e: Exception) {
            ActionExecutionResult(false, "Failed to launch YouTube: " + e.message, "SEARCH_YOUTUBE")
        }
    }

    private fun cleanSongQuery(raw: String): String {
        return raw
            .replace("chalao", "", ignoreCase = true)
            .replace("play", "", ignoreCase = true)
            .replace("gana", "", ignoreCase = true)
            .trim()
    }
}
