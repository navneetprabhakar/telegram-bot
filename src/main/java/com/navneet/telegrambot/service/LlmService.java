package com.navneet.telegrambot.service;

import com.navneet.telegrambot.dto.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);
    private static final String CONVERSATION_ID_KEY = "chat_memory_conversation_id";

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public LlmService(ChatClient chatClient, ChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
    }

    public String chat(Long chatId, ChatMessage userMessage) {
        String conversationId = String.valueOf(chatId);

        try {
            String response = chatClient.prompt()
                    .user(userMessage.content())
                    .advisors(advisor -> advisor
                            .param(CONVERSATION_ID_KEY, conversationId))
                    .call()
                    .content();

            log.info("LLM response generated for chat {} ({} chars)", chatId, response != null ? response.length() : 0);
            return response != null ? response : "I couldn't generate a response. Please try again.";

        } catch (Exception e) {
            log.error("LLM call failed for chat {}", chatId, e);
            throw new RuntimeException("Failed to get response from LLM", e);
        }
    }

    public void clearHistory(Long chatId) {
        String conversationId = String.valueOf(chatId);
        log.info("Clearing chat history for chat {}", chatId);
        chatMemory.clear(conversationId);
    }
}
