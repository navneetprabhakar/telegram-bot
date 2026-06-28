# Telegram Bot (LLM + MCP)

A Spring Boot Telegram bot powered by an LLM via Spring AI, with Model Context Protocol (MCP) client support so the assistant can call external tools.

## Overview

This bot listens for Telegram updates over long polling, forwards user messages to an Anthropic (Claude) chat model through Spring AI, and replies in the chat. It maintains per-chat conversation memory, enforces per-user rate limiting, and connects to one or more MCP servers as a client — exposing their tools to the model so it can fulfil requests that require external actions (for example, querying a trading API).

## Features

- **Telegram integration** — receives and responds to messages using Telegram long polling
- **LLM responses** — chat powered by Anthropic (Claude) via Spring AI's `ChatClient`
- **MCP client** — connects to MCP servers over streamable HTTP and registers their tools as callable tools for the model
- **Per-chat memory** — conversation history kept per chat with configurable max messages and TTL-based eviction
- **Rate limiting** — per-user request limiting (Bucket4j)
- **Message chunking & Markdown** — long replies are split into Telegram-sized chunks, with Markdown formatting and a plain-text fallback
- **Bot commands** — `/start`, `/clear` (reset conversation history), and `/tools` (list available MCP tools)

## Tech Stack

- Java 21
- Spring Boot 3.5.10
- Spring AI 1.1.2 (Anthropic chat model + MCP client starter)
- Telegram Bots 7.11.0 (longpolling + client)
- Bucket4j (rate limiting)
- Caffeine (chat-memory TTL eviction)
- Lombok
- Gradle (wrapper included)

## Getting Started

### Prerequisites

- Java 21
- A Telegram bot token (from [@BotFather](https://t.me/BotFather))
- An Anthropic API key
- (Optional) A running MCP server to connect to

### Build

```bash
./gradlew build
```

### Run

```bash
./gradlew bootRun
```

The application starts on port `8083` by default and begins polling Telegram.

## Configuration

Configuration lives in `src/main/resources/application.yml` and reads from environment variables. Set the following (use your own values):

```bash
export TELEGRAM_BOT_TOKEN=<your-telegram-bot-token>
export TELEGRAM_BOT_USERNAME=<your-bot-username>
export ANTHROPIC_API_KEY=<your-anthropic-api-key>
export MCP_SERVER_URL=<your-mcp-server-url>   # e.g. http://localhost:8082/
```

Key configuration properties:

| Property | Description |
|----------|-------------|
| `spring.ai.anthropic.api-key` | Anthropic API key (`ANTHROPIC_API_KEY`) |
| `spring.ai.anthropic.chat.options.model` | Claude model id |
| `spring.ai.mcp.client.streamable-http.connections.*.url` | MCP server URL (`MCP_SERVER_URL`) |
| `telegram.bot.token` | Telegram bot token (`TELEGRAM_BOT_TOKEN`) |
| `telegram.bot.username` | Telegram bot username (`TELEGRAM_BOT_USERNAME`) |
| `chat.context.max-messages` | Max messages retained per chat |
| `chat.context.ttl-minutes` | Conversation TTL in minutes |
| `rate-limit.requests-per-minute` | Per-user request limit |
| `server.port` | HTTP port (default `8083`) |

## Project Structure

```
src/main/java/com/navneet/telegrambot/
├── TelegramBotApplication.java
├── config/   # LLM/ChatClient, chat memory, rate limit, and Telegram bot configuration
├── handler/  # Telegram update handler (commands, rate limiting, dispatch)
├── service/  # LlmService — chat, history, and tool integration
├── dto/      # ChatMessage model
└── util/     # Telegram response formatting helpers
```

## Disclaimer

This project is for educational and personal use. Keep API keys and bot tokens out of source control.
