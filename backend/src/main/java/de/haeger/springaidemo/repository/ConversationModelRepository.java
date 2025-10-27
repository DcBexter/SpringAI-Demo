package de.haeger.springaidemo.repository;

import de.haeger.springaidemo.entity.ConversationModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing ConversationModel entities.
 * Provides data access operations for conversation-specific model settings.
 */
@Repository
public interface ConversationModelRepository extends JpaRepository<ConversationModel, String> {
    
    /**
     * Find the model associated with a specific conversation.
     * 
     * @param conversationId The conversation ID
     * @return Optional containing the ConversationModel if found
     */
    Optional<ConversationModel> findByConversationId(String conversationId);
}
