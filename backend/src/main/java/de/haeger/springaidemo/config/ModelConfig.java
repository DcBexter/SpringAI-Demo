package de.haeger.springaidemo.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ModelConfig {

    @Bean
    @Primary
    @Qualifier("gemini")
    public ChatModel geminiChatModel(VertexAiGeminiChatModel vertexAiGeminiChatModel) {
        return vertexAiGeminiChatModel;
    }

    @Bean
    @Qualifier("ollama")
    public ChatModel ollamaChatModel(OllamaChatModel ollamaChatModel) {
        return ollamaChatModel;
    }
}
