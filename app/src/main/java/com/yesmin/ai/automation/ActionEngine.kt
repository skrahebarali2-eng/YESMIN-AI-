package com.yesmin.ai.automation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import com.yesmin.ai.accessibility.YesminAccessibilityService
import com.yesmin.ai.ai.model.YesminStructuredAction
import com.yesmin.ai.youtube.YouTubeMusicController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ActionEngine(private val context: Context) {

    private val youtubeController = YouTubeMusicController(context)

    suspend fun executeAction(action: YesminStructuredAction?): ActionExecutionResult = withContext(Dispatchers.Main) {
        if (action == null) {
            return@withContext ActionExecutionResult(true, "No action required", "NONE")
        }

        try {
            when (action.type) {
                ActionType.OPEN_APP -> openApplication(action.target ?: "")
                ActionType.SEARCH_YOUTUBE -> youtubeController.searchAndPlay(action.target ?: "")
                ActionType.OPEN_URL -> openUrl(action.target ?: "")
                ActionType.GLOBAL_BACK -> performAccessibilityAction { it.performGlobalBack() }
                ActionType.GLOBAL_HOME -> performAccessibilityAction { it.performGlobalHome() }
                ActionType.GLOBAL_NOTIFICATIONS -> performAccessibilityAction { it.performGlobalNotifications() }
                ActionType.TAKE_SCREENSHOT -> performAccessibilityAction { it.performTakeScreenshot() }
                else -> ActionExecutionResult(true, "Action processed: " + action.type, action.type)
            }
        } catch (e: Exception) {
            ActionExecutionResult(false, "Action failed: " + e.message, action.type)
        }
    }

    private fun openApplication(appName: String): ActionExecutionResult {
        val target = appName.lowercase().trim()
        val intent: Intent? = when {
            target.contains("youtube") -> context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com"))
            target.contains("whatsapp") -> context.packageManager.getLaunchIntentForPackage("com.whatsapp")
            target.contains("chrome") -> context.packageManager.getLaunchIntentForPackage("com.android.chrome")
                ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"))
            target.contains("camera") -> Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
            target.contains("settings") -> Intent(Settings.ACTION_SETTINGS)
            else -> context.packageManager.getLaunchIntentForPackage(target)
        }

        return if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            ActionExecutionResult(true, "Opened " + appName + " successfully", ActionType.OPEN_APP)
        } else {
            ActionExecutionResult(false, "App " + appName + " not found", ActionType.OPEN_APP)
        }
    }

    private fun openUrl(url: String): ActionExecutionResult {
        val safeUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://" + url else url
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ActionExecutionResult(true, "Opened URL: " + safeUrl, ActionType.OPEN_URL)
    }

    private fun performAccessibilityAction(block: (YesminAccessibilityService) -> Boolean): ActionExecutionResult {
        val service = YesminAccessibilityService.instance
        return if (service != null) {
            val ok = block(service)
            ActionExecutionResult(ok, if (ok) "Action executed via AccessibilityService" else "Could not execute", "ACCESSIBILITY")
        } else {
            ActionExecutionResult(false, "Accessibility Service is not enabled", "ACCESSIBILITY_DISABLED")
        }
    }
}
