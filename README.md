# QueueCord

An Android app that queues messages to send to Discord when offline, automatically sending them when WiFi becomes available.

## Features

- **Offline Queueing**: Add messages to a queue even when offline
- **Auto-Send**: Messages automatically send when WiFi connection is detected
- **Immediate Send**: Messages send immediately when already connected
- **Cancel Messages**: Remove messages from the queue before they're sent
- **Persistent Queue**: Queue survives app restarts using DataStore
- **Clean UI**: Simple, intuitive Material 3 design

## Setup

1. **Get a Discord Webhook URL**:
   - Go to your Discord server settings → Integrations → Webhooks
   - Create a new webhook for your desired channel
   - Copy the webhook URL

2. **Configure the App**:
   - On first launch, enter your Discord webhook URL
   - You can edit the webhook URL anytime using the "Edit Webhook" button

## Usage

1. Type your message in the text field
2. Tap "Add to Queue"
3. If offline, the message will queue and send automatically when WiFi connects
4. If online, the message sends immediately
5. Cancel any queued message before sending starts

## Architecture

- **MVVM Architecture**: Clean separation of UI, business logic, and data
- **Jetpack Compose**: Modern declarative UI
- **StateFlow**: Reactive state management
- **DataStore**: Persistent storage for queue and settings
- **OkHttp**: HTTP client for Discord API calls
- **Coroutines**: Asynchronous operations

## Project Structure

```
app/src/main/java/com/raytonc/queuecord/
├── model/          # Data models
├── repository/     # Data access layer
├── service/        # Discord API service
├── ui/            # Compose UI components
├── util/          # Utilities (network monitoring)
├── viewmodel/     # ViewModels for state management
└── MainActivity.kt
```

## Requirements

- Android SDK 34+
- WiFi connectivity for automatic sending
- Discord webhook URL

## License

This project is open source and available under standard licensing terms.
