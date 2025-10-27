package de.haeger.springaidemo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import de.haeger.springaidemo.service.ModelSwitchService;
import de.haeger.springaidemo.tools.WeatherTool;

/**
 * Configuration class for ChatClient with tool integration and memory.
 * Supports dynamic model switching through ModelSwitchService.
 */
@Configuration
public class ChatClientConfig {

    private final ChatMemory chatMemory;
    private final WeatherTool weatherTool;
    private final ModelSwitchService modelSwitchService;

    public ChatClientConfig(
            ChatMemory chatMemory,
            WeatherTool weatherTool,
            ModelSwitchService modelSwitchService) {
        this.chatMemory = chatMemory;
        this.weatherTool = weatherTool;
        this.modelSwitchService = modelSwitchService;
    }

    /**
     * Creates a ChatClient bean using the current model from ModelSwitchService.
     * This bean is prototype-scoped to allow fresh instances with the current model.
     */
    @Bean
    @Scope("prototype")
    public ChatClient chatClient() {
        return createChatClient(modelSwitchService.getCurrentModel());
    }

    /**
     * Creates a ChatClient with a specific ChatModel.
     * Used by controllers to create clients with conversation-specific models.
     * 
     * @param model The ChatModel to use for this client
     * @return A configured ChatClient instance
     */
    public ChatClient createChatClient(ChatModel model) {
        return ChatClient.builder(model)
                .defaultSystem("You are a helpful AI assistant. You can answer any questions the user asks. " +
                        "You have access to tools that you can use when appropriate, but you are not limited to only using tools. " +
                        "Answer questions directly using your knowledge, and only call tools when they would be helpful.")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(weatherTool)
                .build();
    }
}