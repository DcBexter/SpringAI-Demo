package de.haeger.springaidemo.controller;

import de.haeger.springaidemo.dto.ChatMemoryDto;
import de.haeger.springaidemo.dto.ConversationSummaryDto;
import de.haeger.springaidemo.dto.ModelSettingsDto;
import de.haeger.springaidemo.dto.VectorStoreStatsDto;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for debug and information endpoints.
 * Provides endpoints to inspect application state, model settings, and vector store statistics.
 */
@RestController
@RequestMapping("/info")
public class InfoController {

    private final ChatMemory chatMemory;
    private final JdbcTemplate jdbcTemplate;

    @Value("${spring.ai.vertex.ai.gemini.chat.options.model:unknown}")
    private String modelName;

    @Value("${spring.ai.vertex.ai.gemini.chat.options.temperature:0.7}")
    private Double temperature;

    @Value("${spring.ai.vertex.ai.gemini.project-id:unknown}")
    private String projectId;

    @Value("${spring.ai.vertex.ai.gemini.location:unknown}")
    private String location;

    public InfoController(ChatMemory chatMemory, JdbcTemplate jdbcTemplate) {
        this.chatMemory = chatMemory;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Get current AI model settings.
     * 
     * @return model configuration including name, temperature, project ID, and location
     */
    @GetMapping("/model")
    public ResponseEntity<ModelSettingsDto> getModelSettings() {
        try {
            ModelSettingsDto settings = new ModelSettingsDto(
                    modelName,
                    temperature,
                    projectId,
                    location
            );
            return ResponseEntity.ok(settings);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get chat memory for a specific conversation ID.
     * 
     * @param conversationId the conversation identifier
     * @return conversation history with messages and timestamps
     */
    @GetMapping("/memory/{conversationId}")
    public ResponseEntity<ChatMemoryDto> getMemory(@PathVariable String conversationId) {
        try {
            if (conversationId == null || conversationId.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            var messages = chatMemory.get(conversationId);
            List<ChatMemoryDto.MessageDto> messageDtos = new ArrayList<>();

            for (var message : messages) {
                ChatMemoryDto.MessageDto messageDto = new ChatMemoryDto.MessageDto(
                        message.getMessageType().name(),
                        message.getText(),
                        LocalDateTime.now() // Note: Spring AI Message doesn't have timestamp, using current time
                );
                messageDtos.add(messageDto);
            }

            ChatMemoryDto memoryDto = new ChatMemoryDto(conversationId, messageDtos);
            return ResponseEntity.ok(memoryDto);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get vector store statistics.
     * 
     * @return statistics about the vector store including document count
     */
    @GetMapping("/vector-store")
    public ResponseEntity<VectorStoreStatsDto> getVectorStoreStats() {
        try {
            // Query the vector_store table to get document count
            Long documentCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM vector_store",
                    Long.class
            );

            VectorStoreStatsDto stats = new VectorStoreStatsDto(
                    documentCount != null ? documentCount : 0,
                    "PgVector",
                    true
            );
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            // If table doesn't exist or query fails, return zero count
            VectorStoreStatsDto stats = new VectorStoreStatsDto(0, "PgVector", false);
            return ResponseEntity.ok(stats);
        }
    }

    /**
     * Get list of all conversation IDs with message counts.
     * 
     * @return list of conversation summaries
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationSummaryDto>> getConversations() {
        try {
            String sql = "SELECT conversation_id, COUNT(*) as message_count, MAX(timestamp) as last_updated, " +
                        "(SELECT content FROM spring_ai_chat_memory m2 " +
                        " WHERE m2.conversation_id = m1.conversation_id AND m2.type = 'USER' " +
                        " ORDER BY m2.timestamp ASC LIMIT 1) as first_message " +
                        "FROM spring_ai_chat_memory m1 " +
                        "GROUP BY conversation_id " +
                        "ORDER BY MAX(timestamp) DESC";
            
            List<ConversationSummaryDto> conversations = jdbcTemplate.query(sql, (rs, rowNum) -> {
                ConversationSummaryDto dto = new ConversationSummaryDto();
                dto.setConversationId(rs.getString("conversation_id"));
                dto.setMessageCount(rs.getInt("message_count"));
                dto.setLastUpdated(rs.getTimestamp("last_updated").toLocalDateTime());
                
                String firstMessage = rs.getString("first_message");
                if (firstMessage != null && !firstMessage.isEmpty()) {
                    dto.setTitle(firstMessage.length() > 50 ? firstMessage.substring(0, 50) + "..." : firstMessage);
                } else {
                    dto.setTitle("New Conversation");
                }
                
                return dto;
            });

            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
}
