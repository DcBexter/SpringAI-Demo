package de.haeger.springaidemo.controller;

import de.haeger.springaidemo.config.ChatClientConfig;
import de.haeger.springaidemo.service.ModelSwitchService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for chat functionality with persistent memory.
 * Provides endpoints for conversational AI with memory persistence per
 * conversation ID.
 * Uses Spring AI's official ChatMemory implementation with
 * MessageChatMemoryAdvisor.
 * Supports per-conversation model selection.
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatClientConfig chatClientConfig;
    private final ChatMemory chatMemory;
    private final ModelSwitchService modelSwitchService;

    public ChatController(
            ChatClientConfig chatClientConfig,
            ChatMemory chatMemory,
            ModelSwitchService modelSwitchService) {
        this.chatClientConfig = chatClientConfig;
        this.chatMemory = chatMemory;
        this.modelSwitchService = modelSwitchService;
    }

    /**
     * Chat endpoint with persistent memory support using Spring AI's ChatMemory.
     * The ChatClient is configured with MessageChatMemoryAdvisor to automatically
     * manage conversation history.
     * Uses the model associated with the conversation, or the current global model if none is set.
     * 
     * @param message        the user message
     * @param conversationId the conversation identifier for memory persistence
     * @return the AI response
     */
    @GetMapping
    public ResponseEntity<String> chat(
            @RequestParam String msg,
            @RequestParam String conversationId) {

        try {
            if (msg == null || msg.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Message cannot be null or empty");
            }

            if (conversationId == null || conversationId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Conversation ID cannot be null or empty");
            }

            // Get the model for this conversation
            ChatModel model = modelSwitchService.getModelForConversation(conversationId);
            
            // Create a fresh ChatClient with the conversation-specific model
            ChatClient chatClient = chatClientConfig.createChatClient(model);

            // Generate response with memory
            // The MessageChatMemoryAdvisor automatically manages conversation history
            // Tools are already configured as defaultTools in the ChatClient bean
            String response = chatClient.prompt()
                    .user(msg)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .call()
                    .content();

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing chat: " + e.getMessage());
        }
    }

    /**
     * Clear conversation memory for a specific conversation ID.
     * 
     * @param conversationId the conversation identifier
     * @return success message
     */
    @DeleteMapping("/memory/{conversationId}")
    public ResponseEntity<String> clearMemory(@PathVariable String conversationId) {
        try {
            if (conversationId == null || conversationId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Conversation ID cannot be null or empty");
            }

            chatMemory.clear(conversationId);
            return ResponseEntity.ok("Memory cleared for conversation: " + conversationId);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error clearing memory: " + e.getMessage());
        }
    }

    /**
     * Get conversation history for a specific conversation ID.
     * 
     * @param conversationId the conversation identifier
     * @return the conversation history
     */
    @GetMapping("/memory/{conversationId}")
    public ResponseEntity<String> getMemory(@PathVariable String conversationId) {
        try {
            if (conversationId == null || conversationId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Conversation ID cannot be null or empty");
            }

            var messages = chatMemory.get(conversationId);
            StringBuilder history = new StringBuilder();

            for (var message : messages) {
                history.append(message.getMessageType().name())
                        .append(": ")
                        .append(message.getText())
                        .append("\n\n");
            }

            return ResponseEntity.ok(history.toString());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving memory: " + e.getMessage());
        }
    }
}