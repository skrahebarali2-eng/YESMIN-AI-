package com.yesmin.ai.automation

object ActionType {
    const val OPEN_APP = "OPEN_APP"
    const val OPEN_URL = "OPEN_URL"
    const val SEARCH_YOUTUBE = "SEARCH_YOUTUBE"
    const val GLOBAL_BACK = "GLOBAL_BACK"
    const val GLOBAL_HOME = "GLOBAL_HOME"
    const val GLOBAL_NOTIFICATIONS = "GLOBAL_NOTIFICATIONS"
    const val TAKE_SCREENSHOT = "TAKE_SCREENSHOT"
    const val TAP_SCREEN = "TAP_SCREEN"
    const val SWIPE_SCREEN = "SWIPE_SCREEN"
    const val SCROLL_SCREEN = "SCROLL_SCREEN"
    const val TYPE_TEXT = "TYPE_TEXT"
    const val PLAY_MEDIA = "PLAY_MEDIA"
    const val PAUSE_MEDIA = "PAUSE_MEDIA"
    const val SET_VOLUME = "SET_VOLUME"
    const val SAVE_MEMORY = "SAVE_MEMORY"
    const val TRANSLATE_TEXT = "TRANSLATE_TEXT"
    const val CALCULATE = "CALCULATE"
    const val CODING_ASSIST = "CODING_ASSIST"
}

data class ActionExecutionResult(
    val success: Boolean,
    val message: String,
    val actionType: String,
    val details: Map<String, Any>? = null
)
