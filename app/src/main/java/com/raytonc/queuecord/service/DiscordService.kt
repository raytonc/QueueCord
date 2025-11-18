package com.raytonc.queuecord.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

/**
 * Payload for Discord webhook messages
 */
@Serializable
data class DiscordWebhookPayload(
    val content: String
)

/**
 * Service for sending messages to Discord via webhooks.
 *
 * @param webhookUrl The Discord webhook URL to send messages to
 */
class DiscordService(private val webhookUrl: String) {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun sendMessage(content: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val payload = DiscordWebhookPayload(content)
            val jsonBody = json.encodeToString(payload)

            val request = Request.Builder()
                .url(webhookUrl)
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(IOException("Failed to send message: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
