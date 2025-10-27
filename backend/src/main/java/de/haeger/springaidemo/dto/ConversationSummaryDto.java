package de.haeger.springaidemo.dto;

import java.time.LocalDateTime;

/**
 * DTO for conversation summary information.
 * Used to list all conversations with basic metadata.
 */
public class ConversationSummaryDto {
    
    private String conversationId;
    private int messageCount;
    private LocalDateTime lastUpdated;
    private String title;

    public ConversationSummaryDto() {
    }

    public ConversationSummaryDto(String conversationId, int messageCount, LocalDateTime lastUpdated, String title) {
        this.conversationId = conversationId;
        this.messageCount = messageCount;
        this.lastUpdated = lastUpdated;
        this.title = title;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
