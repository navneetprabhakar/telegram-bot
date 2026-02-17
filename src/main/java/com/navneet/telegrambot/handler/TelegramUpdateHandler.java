package com.navneet.telegrambot.handler;

import com.navneet.telegrambot.config.RateLimitConfig;
import com.navneet.telegrambot.dto.ChatMessage;
import com.navneet.telegrambot.service.LlmService;
import com.navneet.telegrambot.util.TelegramResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class TelegramUpdateHandler implements LongPollingSingleThreadUpdateConsumer {

    private static final Logger log = LoggerFactory.getLogger(TelegramUpdateHandler.class);

    private final TelegramClient telegramClient;
    private final LlmService llmService;
    private final RateLimitConfig rateLimitConfig;
    private final ToolCallbackProvider toolCallbackProvider;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public TelegramUpdateHandler(TelegramClient telegramClient,
                                  LlmService llmService,
                                  RateLimitConfig rateLimitConfig,
                                  ToolCallbackProvider toolCallbackProvider) {
        this.telegramClient = telegramClient;
        this.llmService = llmService;
        this.rateLimitConfig = rateLimitConfig;
        this.toolCallbackProvider = toolCallbackProvider;
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();
        Integer messageId = update.getMessage().getMessageId();
        User from = update.getMessage().getFrom();
        String username = from != null ? from.getFirstName() : "User";

        log.info("[RECEIVED] chatId={}, user={}, messageId={}, text=\"{}\"", chatId, username, messageId, text);

        // Handle /start command
        if (text.equals("/start")) {
            sendText(chatId, "Hello! I'm your AI assistant. Lets do some business.");
            return;
        }

        // Handle /clear command
        if (text.equals("/clear")) {
            llmService.clearHistory(chatId);
            sendText(chatId, "Conversation history cleared.");
            return;
        }

        // Handle /tools command
        if (text.equals("/tools")) {
            handleToolsCommand(chatId);
            return;
        }

        // Rate limit check
        if (!rateLimitConfig.resolveBucket(chatId).tryConsume(1)) {
            log.warn("[RATE-LIMITED] chatId={}, user={}", chatId, username);
            sendText(chatId, "You're sending messages too fast. Please wait a moment.");
            return;
        }

        // Process message asynchronously with virtual threads
        executor.submit(() -> processMessage(chatId, text, messageId, username));
    }

    private void processMessage(Long chatId, String text, Integer messageId, String username) {
        try {
            // Send typing indicator
            sendTypingAction(chatId);

            // Normalize incoming message to DTO
            ChatMessage userMessage = ChatMessage.userMessage(text, chatId, messageId, username);

            // Get LLM response
            String response = llmService.chat(chatId, userMessage);

            // Split and send response (Telegram 4096 char limit)
            List<String> chunks = TelegramResponseUtil.chunkMessage(response);
            log.info("[LLM-RESPONSE] chatId={}, responseLength={}, chunks={}", chatId, response.length(), chunks.size());

            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                String formatted = TelegramResponseUtil.formatForTelegram(chunk);
                SendMessage message = SendMessage.builder()
                        .chatId(chatId)
                        .text(formatted)
                        .parseMode("Markdown")
                        .build();
                try {
                    telegramClient.execute(message);
                    log.info("[SENT] chatId={}, chunk={}/{}, length={}", chatId, i + 1, chunks.size(), formatted.length());
                } catch (TelegramApiException e) {
                    // Fallback: send without markdown if parsing fails
                    log.warn("[SENT-FALLBACK] chatId={}, chunk={}/{}, markdown failed, sending plain text", chatId, i + 1, chunks.size());
                    SendMessage plainMessage = SendMessage.builder()
                            .chatId(chatId)
                            .text(chunk)
                            .build();
                    telegramClient.execute(plainMessage);
                    log.info("[SENT] chatId={}, chunk={}/{}, length={} (plain text)", chatId, i + 1, chunks.size(), chunk.length());
                }
            }
        } catch (Exception e) {
            log.error("Error processing message for chat {}", chatId, e);
            sendText(chatId, "Sorry, something went wrong. Please try again.");
        }
    }

    private void handleToolsCommand(Long chatId) {
        ToolCallback[] tools = toolCallbackProvider.getToolCallbacks();
        if (tools.length == 0) {
            sendText(chatId, "No MCP tools are currently available. Ensure MCP servers are running.");
            return;
        }
        StringBuilder sb = new StringBuilder("Available MCP Tools:\n\n");
        for (int i = 0; i < tools.length; i++) {
            ToolCallback tool = tools[i];
            sb.append(i + 1).append(". *").append(tool.getToolDefinition().name()).append("*\n");
            String desc = tool.getToolDefinition().description();
            if (desc != null && !desc.isEmpty()) {
                sb.append("   ").append(desc).append("\n");
            }
            sb.append("\n");
        }
        sendText(chatId, sb.toString().trim());
    }

    private void sendTypingAction(Long chatId) {
        try {
            telegramClient.execute(SendChatAction.builder()
                    .chatId(chatId)
                    .action("typing")
                    .build());
        } catch (TelegramApiException e) {
            log.warn("Failed to send typing action to chat {}", chatId, e);
        }
    }

    private void sendText(Long chatId, String text) {
        try {
            telegramClient.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .build());
            log.info("[SENT] chatId={}, length={}, text=\"{}\"", chatId, text.length(), text);
        } catch (TelegramApiException e) {
            log.error("[SEND-FAILED] chatId={}, text=\"{}\"", chatId, text, e);
        }
    }
}
