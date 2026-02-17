package com.navneet.telegrambot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class LlmConfig {

    private static final Logger log = LoggerFactory.getLogger(LlmConfig.class);

    @Bean
    public ChatClient chatClient(AnthropicChatModel anthropicChatModel,
                                  ToolCallbackProvider toolCallbackProvider,
                                  ChatMemory chatMemory) {
        // Log all registered MCP tools
        Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .forEach(t -> log.info("[MCP] Tool found: {}", t.getToolDefinition()));

        return ChatClient.builder(anthropicChatModel)
                .defaultSystem("You are a helpful AI assistant in a Telegram chat. " +
                        "Keep responses concise and well-formatted for mobile reading. " +
                        "Use Telegram-compatible markdown when helpful. " +
                        "You have access to external tools via MCP servers. " +
                        "Use them when the user's request can be fulfilled by a tool.")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultToolCallbacks(toolCallbackProvider)
                .build();
    }
}
