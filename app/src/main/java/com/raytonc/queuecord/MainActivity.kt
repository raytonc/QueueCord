package com.raytonc.queuecord

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raytonc.queuecord.ui.QueueCordApp
import com.raytonc.queuecord.ui.theme.QueueCordTheme
import com.raytonc.queuecord.viewmodel.QueueViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QueueCordTheme {
                val viewModel: QueueViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()

                QueueCordApp(
                    uiState = uiState,
                    onAddMessage = viewModel::addMessage,
                    onCancelMessage = viewModel::cancelMessage,
                    onSaveWebhookUrl = viewModel::saveWebhookUrl,
                    onClearError = viewModel::clearError,
                    onShowEditWebhook = viewModel::showEditWebhookDialog,
                    onHideEditWebhook = viewModel::hideEditWebhookDialog
                )
            }
        }
    }
}