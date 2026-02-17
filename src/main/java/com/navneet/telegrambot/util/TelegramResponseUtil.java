package com.navneet.telegrambot.util;

import java.util.ArrayList;
import java.util.List;

public final class TelegramResponseUtil {

    private static final int TELEGRAM_MAX_MESSAGE_LENGTH = 4096;

    private TelegramResponseUtil() {}

    /**
     * Split a long message into chunks that fit Telegram's 4096 char limit.
     * Tries to split at newlines for cleaner breaks.
     */
    public static List<String> chunkMessage(String message) {
        List<String> chunks = new ArrayList<>();
        if (message == null || message.isEmpty()) {
            chunks.add("(empty response)");
            return chunks;
        }

        if (message.length() <= TELEGRAM_MAX_MESSAGE_LENGTH) {
            chunks.add(message);
            return chunks;
        }

        int start = 0;
        while (start < message.length()) {
            int end = Math.min(start + TELEGRAM_MAX_MESSAGE_LENGTH, message.length());

            if (end < message.length()) {
                // Try to find a newline to break at
                int newlinePos = message.lastIndexOf('\n', end);
                if (newlinePos > start) {
                    end = newlinePos + 1;
                }
            }

            chunks.add(message.substring(start, end));
            start = end;
        }

        return chunks;
    }

    /**
     * Convert standard markdown to Telegram-compatible markdown.
     * Telegram supports a subset of markdown: bold, italic, code, links.
     */
    public static String formatForTelegram(String text) {
        if (text == null) return "";

        // Telegram Markdown V1 supports: *bold*, _italic_, `code`, [link](url)
        // Most standard markdown passes through fine.
        // Main issue: triple backticks for code blocks need to use single backtick pairs in V1
        // or we use MarkdownV2 which requires escaping special chars.

        // For simplicity, use Markdown V1 which is more forgiving
        return text;
    }
}
