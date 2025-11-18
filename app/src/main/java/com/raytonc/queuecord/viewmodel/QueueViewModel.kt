package com.raytonc.queuecord.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.raytonc.queuecord.model.QueuedMessage
import com.raytonc.queuecord.repository.MessageRepository
import com.raytonc.queuecord.service.DiscordService
import com.raytonc.queuecord.util.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Represents the current status of the application
 */
enum class AppStatus {
    /** Offline with messages waiting to be sent */
    OFFLINE_WITH_QUEUE,

    /** Currently sending queued messages */
    SENDING,

    /** No messages in queue */
    EMPTY,

    /** Online and ready to send */
    ONLINE_READY
}

/**
 * UI state for the QueueCord app
 */
data class UiState(
    val queuedMessages: List<QueuedMessage> = emptyList(),
    val isWifiConnected: Boolean = false,
    val isSending: Boolean = false,
    val webhookUrl: String? = null,
    val showWebhookDialog: Boolean = false,
    val errorMessage: String? = null,
    val showEditWebhookDialog: Boolean = false
) {
    val status: AppStatus
        get() = when {
            isSending -> AppStatus.SENDING
            queuedMessages.isEmpty() && isWifiConnected -> AppStatus.ONLINE_READY
            queuedMessages.isEmpty() -> AppStatus.EMPTY
            !isWifiConnected -> AppStatus.OFFLINE_WITH_QUEUE
            else -> AppStatus.ONLINE_READY
        }
}

/**
 * ViewModel for managing the message queue and application state.
 *
 * Handles:
 * - Adding and removing messages from the queue
 * - Monitoring WiFi connectivity
 * - Auto-sending messages when online
 * - Managing webhook URL configuration
 */
class QueueViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MessageRepository(application)
    private val networkMonitor = NetworkMonitor(application)

    private val _isSending = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _showEditWebhookDialog = MutableStateFlow(false)

    private data class MainState(
        val messages: List<QueuedMessage>,
        val wifi: Boolean,
        val sending: Boolean,
        val webhook: String?
    )

    private data class DialogState(
        val error: String?,
        val showEdit: Boolean
    )

    val uiState: StateFlow<UiState> = combine(
        combine(
            repository.queuedMessages,
            networkMonitor.isWifiConnected,
            _isSending,
            repository.webhookUrl
        ) { messages, wifi, sending, webhook ->
            MainState(messages, wifi, sending, webhook)
        },
        combine(
            _errorMessage,
            _showEditWebhookDialog
        ) { error, showEdit ->
            DialogState(error, showEdit)
        }
    ) { main, dialog ->
        UiState(
            queuedMessages = main.messages,
            isWifiConnected = main.wifi,
            isSending = main.sending,
            webhookUrl = main.webhook,
            showWebhookDialog = main.webhook.isNullOrBlank() && !dialog.showEdit,
            errorMessage = dialog.error,
            showEditWebhookDialog = dialog.showEdit
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState()
    )

    init {
        // Auto-send when WiFi becomes available
        viewModelScope.launch {
            combine(
                repository.queuedMessages,
                networkMonitor.isWifiConnected,
                repository.webhookUrl
            ) { messages, wifi, webhook ->
                Triple(messages, wifi, webhook)
            }.collect { (messages, wifi, webhook) ->
                if (wifi && messages.isNotEmpty() && !webhook.isNullOrBlank() && !_isSending.value) {
                    sendQueuedMessages()
                }
            }
        }
    }

    fun addMessage(content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            val message = QueuedMessage(content = content.trim())
            repository.addMessage(message)

            // Trigger immediate send if already online
            sendQueuedMessages()
        }
    }

    fun cancelMessage(messageId: String) {
        viewModelScope.launch {
            repository.removeMessage(messageId)
        }
    }

    fun saveWebhookUrl(url: String) {
        viewModelScope.launch {
            repository.saveWebhookUrl(url.trim())
            _showEditWebhookDialog.value = false
        }
    }

    fun showEditWebhookDialog() {
        _showEditWebhookDialog.value = true
    }

    fun hideEditWebhookDialog() {
        _showEditWebhookDialog.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private suspend fun sendQueuedMessages() {
        val state = uiState.value
        val webhookUrl = state.webhookUrl
        if (webhookUrl.isNullOrBlank() || state.queuedMessages.isEmpty()) return

        // Don't attempt to send if not connected to WiFi
        if (!state.isWifiConnected) return

        _isSending.value = true
        _errorMessage.value = null

        val discordService = DiscordService(webhookUrl)
        val messages = state.queuedMessages.toList()

        for (message in messages) {
            val result = discordService.sendMessage(message.content)
            if (result.isSuccess) {
                repository.removeMessage(message.id)
            } else {
                // Only show error if it's not a network-related issue
                val errorMessage = result.exceptionOrNull()?.message ?: ""
                val isNetworkError =
                    errorMessage.contains("Unable to resolve host", ignoreCase = true) ||
                            errorMessage.contains(
                                "No address associated with hostname",
                                ignoreCase = true
                            ) ||
                            errorMessage.contains("Network is unreachable", ignoreCase = true) ||
                            errorMessage.contains("Connection refused", ignoreCase = true)

                if (!isNetworkError && state.isWifiConnected) {
                    _errorMessage.value = errorMessage.ifBlank { "Failed to send message" }
                }
                break
            }
        }

        _isSending.value = false
    }
}
