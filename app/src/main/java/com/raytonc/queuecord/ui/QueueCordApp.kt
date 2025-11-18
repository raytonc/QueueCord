package com.raytonc.queuecord.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.raytonc.queuecord.R
import com.raytonc.queuecord.model.QueuedMessage
import com.raytonc.queuecord.viewmodel.AppStatus
import com.raytonc.queuecord.viewmodel.UiState

@Composable
fun QueueCordApp(
    uiState: UiState,
    onAddMessage: (String) -> Unit,
    onCancelMessage: (String) -> Unit,
    onSaveWebhookUrl: (String) -> Unit,
    onClearError: () -> Unit,
    onShowEditWebhook: () -> Unit,
    onHideEditWebhook: () -> Unit
) {
    var messageInput by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error message in snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            onClearError()
        }
    }

    // Show webhook dialog if needed
    if (uiState.showWebhookDialog) {
        WebhookUrlDialog(
            currentUrl = null,
            onSave = { url ->
                onSaveWebhookUrl(url)
            },
            onDismiss = null
        )
    }

    // Show edit webhook dialog
    if (uiState.showEditWebhookDialog) {
        WebhookUrlDialog(
            currentUrl = uiState.webhookUrl,
            onSave = { url ->
                onSaveWebhookUrl(url)
            },
            onDismiss = onHideEditWebhook
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Input Section at the top
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        label = { Text(stringResource(R.string.message_label)) },
                        placeholder = { Text(stringResource(R.string.message_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (messageInput.isNotBlank()) {
                                onAddMessage(messageInput)
                                messageInput = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = messageInput.isNotBlank()
                    ) {
                        Text(stringResource(R.string.add_to_queue))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Queued Messages List in the middle
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.queuedMessages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_messages),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.queuedMessages, key = { it.id }) { message ->
                            MessageItem(
                                message = message,
                                onCancel = { onCancelMessage(message.id) },
                                enabled = !uiState.isSending
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status at the bottom
            StatusBar(
                status = uiState.status,
                messageCount = uiState.queuedMessages.size,
                onEditWebhook = onShowEditWebhook
            )
        }
    }
}

@Composable
fun MessageItem(
    message: QueuedMessage,
    onCancel: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message.content,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedButton(
                onClick = onCancel,
                enabled = enabled,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    }
}

@Composable
fun StatusBar(
    status: AppStatus,
    messageCount: Int,
    onEditWebhook: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusText = when (status) {
        AppStatus.OFFLINE_WITH_QUEUE -> stringResource(R.string.status_offline_with_queue)
        AppStatus.SENDING -> stringResource(R.string.status_sending)
        AppStatus.EMPTY -> stringResource(R.string.status_empty)
        AppStatus.ONLINE_READY -> stringResource(R.string.status_online_ready)
    }

    val statusDotColor = when (status) {
        AppStatus.OFFLINE_WITH_QUEUE -> Color(0xFF9E9E9E) // Gray
        AppStatus.SENDING -> Color(0xFFFFC107) // Amber
        AppStatus.EMPTY -> Color(0xFFBDBDBD) // Light Gray
        AppStatus.ONLINE_READY -> Color(0xFF4CAF50) // Green
    }

    val backgroundColor = when (status) {
        AppStatus.OFFLINE_WITH_QUEUE -> MaterialTheme.colorScheme.tertiaryContainer
        AppStatus.SENDING -> MaterialTheme.colorScheme.primaryContainer
        AppStatus.EMPTY -> MaterialTheme.colorScheme.surfaceVariant
        AppStatus.ONLINE_READY -> MaterialTheme.colorScheme.secondaryContainer
    }

    val textColor = when (status) {
        AppStatus.OFFLINE_WITH_QUEUE -> MaterialTheme.colorScheme.onTertiaryContainer
        AppStatus.SENDING -> MaterialTheme.colorScheme.onPrimaryContainer
        AppStatus.EMPTY -> MaterialTheme.colorScheme.onSurfaceVariant
        AppStatus.ONLINE_READY -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status dot and text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Status indicator dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(statusDotColor, CircleShape)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }

            // Edit webhook icon button
            IconButton(
                onClick = onEditWebhook,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.edit_webhook),
                    tint = textColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun WebhookUrlDialog(
    currentUrl: String?,
    onSave: (String) -> Unit,
    onDismiss: (() -> Unit)?
) {
    var webhookUrl by remember { mutableStateOf(currentUrl ?: "") }

    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
        title = { Text(stringResource(R.string.webhook_dialog_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.webhook_dialog_message),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = webhookUrl,
                    onValueChange = { webhookUrl = it },
                    label = { Text(stringResource(R.string.webhook_url_label)) },
                    placeholder = { Text(stringResource(R.string.webhook_url_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (webhookUrl.isNotBlank()) {
                        onSave(webhookUrl)
                    }
                },
                enabled = webhookUrl.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = onDismiss?.let {
            {
                TextButton(onClick = it) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    )
}
