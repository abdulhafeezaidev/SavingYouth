package com.behavioralfirewall.domain.launch

import android.os.Build
import com.behavioralfirewall.domain.security.SecureStorageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

enum class FeedbackCategory {
    BUG_REPORT,
    RECOVERY_SUGGESTION,
    GENERAL_COMMENT
}

data class AnonymousFeedback(
    val id: String = UUID.randomUUID().toString(),
    val category: FeedbackCategory,
    val sanitizedText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceModel: String = "${Build.MANUFACTURER} ${Build.MODEL} (API ${Build.VERSION.SDK_INT})"
)

@Singleton
class UserFeedbackLoopManager @Inject constructor(
    private val secureStorageManager: SecureStorageManager
) {
    private val feedbackStorageKey = "feedback_submissions_v1"
    private val _feedbackList = MutableStateFlow<List<AnonymousFeedback>>(emptyList())
    val feedbackList: StateFlow<List<AnonymousFeedback>> = _feedbackList.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    init {
        loadFeedback()
    }

    private fun loadFeedback() {
        val raw = secureStorageManager.getToken(feedbackStorageKey) ?: return
        try {
            val list = mutableListOf<AnonymousFeedback>()
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    AnonymousFeedback(
                        id = obj.getString("id"),
                        category = FeedbackCategory.valueOf(obj.getString("category")),
                        sanitizedText = obj.getString("text"),
                        timestamp = obj.getLong("timestamp"),
                        deviceModel = obj.getString("deviceModel")
                    )
                )
            }
            _feedbackList.value = list
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Submits a feedback piece safely and anonymously.
     * Enforces strict redaction of urls, packages, and emails.
     */
    fun submitFeedback(category: FeedbackCategory, rawText: String, onComplete: (Boolean) -> Unit) {
        _isSubmitting.value = true
        
        // Redact any potentially sensitive URLs, package identifiers, or emails from the feedback text
        val sanitized = PrivacyRedactor.redact(rawText)
        if (sanitized.trim().isEmpty()) {
            _isSubmitting.value = false
            onComplete(false)
            return
        }

        val item = AnonymousFeedback(
            category = category,
            sanitizedText = sanitized
        )

        val currentList = _feedbackList.value + item
        _feedbackList.value = currentList
        saveFeedback(currentList)

        // Simulate remote Appwrite database push or local submission successfully
        _isSubmitting.value = false
        onComplete(true)
    }

    private fun saveFeedback(list: List<AnonymousFeedback>) {
        try {
            val array = JSONArray()
            list.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("category", item.category.name)
                    put("text", item.sanitizedText)
                    put("timestamp", item.timestamp)
                    put("deviceModel", item.deviceModel)
                }
                array.put(obj)
            }
            secureStorageManager.saveToken(feedbackStorageKey, array.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Clears local cache logs of feedback.
     */
    fun clearFeedbackLogs() {
        _feedbackList.value = emptyList()
        secureStorageManager.removeToken(feedbackStorageKey)
    }
}
