package de.haeger.springaidemo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing the AI model associated with a conversation.
 * Stores which model (Gemini, Ollama, etc.) is being used for each conversation.
 */
@Entity
@Table(name = "conversation_model")
public class ConversationModel {

    @Id
    @Column(name = "conversation_id", nullable = false)
    private String conversationId;

    @Column(name = "model_name", nullable = false)
    private String modelName;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public ConversationModel() {
    }

    public ConversationModel(String conversationId, String modelName) {
        this.conversationId = conversationId;
        this.modelName = modelName;
        this.updatedAt = LocalDateTime.now();
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
