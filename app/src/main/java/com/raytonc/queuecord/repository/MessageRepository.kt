package com.raytonc.queuecord.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.raytonc.queuecord.model.QueuedMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "queuecord_prefs")

/**
 * Repository for managing message queue and app settings.
 *
 * Uses DataStore for persistent storage of:
 * - Queued messages
 * - Discord webhook URL
 */
class MessageRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val QUEUED_MESSAGES_KEY = stringPreferencesKey("queued_messages")
        private val WEBHOOK_URL_KEY = stringPreferencesKey("webhook_url")
    }

    val queuedMessages: Flow<List<QueuedMessage>> = context.dataStore.data
        .map { preferences ->
            val jsonString = preferences[QUEUED_MESSAGES_KEY] ?: "[]"
            try {
                json.decodeFromString<List<QueuedMessage>>(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        }

    val webhookUrl: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[WEBHOOK_URL_KEY]
        }

    suspend fun addMessage(message: QueuedMessage) {
        context.dataStore.edit { preferences ->
            val currentList = try {
                val jsonString = preferences[QUEUED_MESSAGES_KEY] ?: "[]"
                json.decodeFromString<List<QueuedMessage>>(jsonString)
            } catch (e: Exception) {
                emptyList()
            }

            val updatedList = currentList + message
            preferences[QUEUED_MESSAGES_KEY] = json.encodeToString(updatedList)
        }
    }

    suspend fun removeMessage(messageId: String) {
        context.dataStore.edit { preferences ->
            val currentList = try {
                val jsonString = preferences[QUEUED_MESSAGES_KEY] ?: "[]"
                json.decodeFromString<List<QueuedMessage>>(jsonString)
            } catch (e: Exception) {
                emptyList()
            }

            val updatedList = currentList.filter { it.id != messageId }
            preferences[QUEUED_MESSAGES_KEY] = json.encodeToString(updatedList)
        }
    }

    suspend fun clearAllMessages() {
        context.dataStore.edit { preferences ->
            preferences[QUEUED_MESSAGES_KEY] = "[]"
        }
    }

    suspend fun saveWebhookUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[WEBHOOK_URL_KEY] = url
        }
    }
}
