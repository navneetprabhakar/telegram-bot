package com.navneet.telegrambot.dto;

import java.time.Instant;

public record ChatMessage(
        String role,
        String content,
        Instant timestamp,
        MessageMetadata metadata
) {

    public record MessageMetadata(
            Long chatId,
            Integer messageId,
            String messageType,
            String username
    ) {}

    public static ChatMessage userMessage(String content, Long chatId, Integer messageId, String username) {
        return new ChatMessage(
                "user",
                content,
                Instant.now(),
                new MessageMetadata(chatId, messageId, messageType(content), username)
        );
    }

    public static ChatMessage assistantMessage(String content, Long chatId) {
        return new ChatMessage(
                "assistant",
                content,
                Instant.now(),
                new MessageMetadata(chatId, null, "text", null)
        );
    }

    private static String messageType(String content) {
        return "text";
    }
}
