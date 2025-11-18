package com.raytonc.queuecord.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents a message queued for sending to Discord.
 *
 * @property id Unique identifier for the message
 * @property content The message text to send
 * @property timestamp When the message was queued (milliseconds since epoch)
 */
@Serializable
data class QueuedMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
