# Telegram MCP Client Bot

A Spring Boot-based Telegram bot that integrates with a Large Language Model (LLM) and uses Model Context Protocol (MCP) client features to enable AI-native processing of chat updates.

This README follows the structure of the provided template while being tailored to this project.

## 🚀 Features

- **Telegram Integration**: Receives Telegram updates and sends responses using the Telegram Bot API
- **LLM Integration**: Responses powered by a configurable LLM (Anthropic/OpenAI-style) via `LlmService`
- **MCP Client Support**: Configured MCP client for streamable HTTP and tool callbacks
- **Chat Context Management**: In-memory chat context with configurable max messages and TTL
- **Rate Limiting**: Per-minute request rate limiting to prevent abuse
- **Config-Driven**: All behavior is configurable via `application.yml` and environment variables
- **Extensible Handlers & Services**: Clear separation between handlers, services, config, DTOs, and utilities

## 📋 Prerequisites

- Java 21 (project compiled with Java 21)
- Gradle (wrapper included)
- Telegram bot token (from BotFather)
- LLM provider API key (if using LLM features)

## 🛠️ Technology Stack

- **Framework**: Spring Boot
- **AI Integration**: Spring AI / custom LLM integration classes
- **Build Tool**: Gradle (wrapper included)
- **HTTP Client**: Spring WebClient / RestTemplate (as used by services)
- **In-Memory Context**: Custom chat context configuration
- **Caching**: Optional caching hooks in configuration

## ⚙️ Configuration

### Environment Variables

Set the following environment variables (bash/zsh):

```bash
# Telegram
export TELEGRAM_BOT_TOKEN="<your-telegram-token>"
export TELEGRAM_BOT_USERNAME="<your-bot-username>"

# LLM (Anthropic example)
export ANTHROPIC_API_KEY="<your-anthropic-api-key>"

# MCP server URL (if using remote MCP service)
export MCP_SERVER_URL="http://localhost:8082/"
```

### `application.yml` (key excerpts)

This project is configured with sensible defaults in `src/main/resources/application.yml`. Relevant sections:

```yaml
spring:
  application:
    name: telegram-bot
  ai:
    anthropic:
      api-key: ${ANTHROPIC_API_KEY:your-api-key-here}
      chat:
        options:
          model: claude-sonnet-4-5-20250929
          max-tokens: 4096
          temperature: 0.7
    mcp:
      client:
        name: telegram-bot-mcp-client
        version: 1.0.0
        toolcallback:
          enabled: true
        streamable-http:
          connections:
            server1:
              url: ${MCP_SERVER_URL:http://localhost:8082/}

telegram:
  bot:
    token: ${TELEGRAM_BOT_TOKEN:your-bot-token-here}
    username: ${TELEGRAM_BOT_USERNAME:your-bot-username}

chat:
  context:
    max-messages: 50
    ttl-minutes: 30

rate-limit:
  requests-per-minute: 20

server:
  port: 8083
```

Adjust values as needed for your environment.

## 🗂️ Project structure

Actual repository layout (top-level + important files):

```
telegram-bot/
├── build.gradle
├── gradlew
├── settings.gradle
├── LICENSE
├── README.md
└── src/
    ├── main/
    │   ├── java/com/navneet/telegrambot/
    │   │   ├── TelegramBotApplication.java
    │   │   ├── config/
    │   │   │   ├── ChatMemoryConfig.java
    │   │   │   ├── LlmConfig.java
    │   │   │   ├── RateLimitConfig.java
    │   │   │   └── TelegramBotConfig.java
    │   │   ├── handler/
    │   │   │   └── TelegramUpdateHandler.java
    │   │   ├── service/
    │   │   │   └── LlmService.java
    │   │   ├── dto/
    │   │   │   └── ChatMessage.java
    │   │   └── util/
    │   │       └── TelegramResponseUtil.java
    │   └── resources/
    │       └── application.yml
    └── test/
        └── java/com/navneet/telegrambot/
```

## 🤖 MCP Client Architecture

This project acts as an MCP *client* and can connect to MCP servers for tool invocations and streamable communication. Key points:

- **MCP Client**: Configured via `spring.ai.mcp.client` properties in `application.yml`
- **Tool Callbacks**: Tool callback support (`toolcallback.enabled`) allows the bot to register and receive tool invocations
- **Streamable HTTP**: Streamable HTTP connections configured to communicate with MCP servers

## 📚 Domain Models

Key data models in the codebase:

- `ChatMessage` — represents messages in a chat with optional metadata
- `LlmService` request/response models — used when calling the configured LLM
- Rate limit and chat-context configuration objects

## 🧪 Testing

Run unit tests with Gradle:

```bash
./gradlew test
```

For integration tests that require external services (Telegram, LLM), mock external HTTP calls or run tests with a test profile that uses local mocks.

## 📦 Build & Run

Build the project:

```bash
./gradlew clean build
```

Run with Gradle wrapper:

```bash
./gradlew bootRun
```

Or run the produced JAR (adjust name if different):

```bash
java -jar build/libs/telegram-bot-0.0.1-SNAPSHOT.jar
```

The server listens on the port configured in `application.yml` (default 8083).

## 🔒 Security & Secrets

- Keep API keys and tokens out of source control. Use environment variables or a secrets manager.
- For production, secure the application with appropriate network and application-level protections.

## 👤 Author

Navneet Prabhakar

GitHub: https://github.com/navneetprabhakar

Email: navneet@example.com  <!-- replace or remove if you prefer not to publish an email -->

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/awesome`) 
3. Commit your changes (`git commit -m "Add awesome feature"`)
4. Push and open a Pull Request

## 📞 Support

Open an issue on the repository for bugs or feature requests.

## 📝 License

See the `LICENSE` file at the repository root for license details.

---

*This README is based on the Trade MCP Server template and adapted for the Telegram LLM/MCP client bot in this repository.*
