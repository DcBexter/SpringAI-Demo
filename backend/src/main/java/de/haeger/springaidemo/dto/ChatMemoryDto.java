package de.haeger.springaidemo.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for chat memory information.
 * Used by debug endpoints to expose conversation history.
 */
public class ChatMemoryDto {
    
    private String conversationId;
    private List<MessageDto> messages;
    private int messageCount;

    public ChatMemoryDto() {
    }

    public ChatMemoryDto(String conversationId, List<MessageDto> messages) {
        this.conversationId = conversationId;
        this.messages = messages;
        this.messageCount = messages != null ? messages.size() : 0;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public List<MessageDto> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageDto> messages) {
        this.messages = messages;
        this.messageCount = messages != null ? messages.size() : 0;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    /**
     * Inner DTO class for individual messages in the conversation.
     */
    public static class MessageDto {
        private String role;
        private String content;
        private LocalDateTime timestamp;

        public MessageDto() {
        }

        public MessageDto(String role, String content, LocalDateTime timestamp) {
            this.role = role;
            this.content = content;
            this.timestamp = timestamp;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }
}
