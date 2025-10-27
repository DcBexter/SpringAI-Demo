package de.haeger.springaidemo.controller;

import de.haeger.springaidemo.rag.DocumentIndexer;
import de.haeger.springaidemo.rag.RagService;
import de.haeger.springaidemo.service.ModelSwitchService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for RAG (Retrieval-Augmented Generation) functionality.
 * Provides endpoints for document indexing and question-answering using vector
 * store.
 * Supports per-conversation model selection.
 */
@RestController
@RequestMapping("/rag")
public class RagController {

    private final DocumentIndexer documentIndexer;
    private final RagService ragService;
    private final ModelSwitchService modelSwitchService;

    public RagController(
            DocumentIndexer documentIndexer,
            RagService ragService,
            ModelSwitchService modelSwitchService) {
        this.documentIndexer = documentIndexer;
        this.ragService = ragService;
        this.modelSwitchService = modelSwitchService;
    }

    /**
     * Add text content to the vector store for RAG queries.
     * 
     * @param text the text content to index
     * @return success message
     */
    @PostMapping("/addText")
    public ResponseEntity<String> addText(@RequestBody String text) {
        try {
            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Text content cannot be null or empty");
            }

            documentIndexer.indexText(text);
            return ResponseEntity.ok("Text successfully indexed");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error indexing text: " + e.getMessage());
        }
    }

    /**
     * Ask a question using RAG to retrieve relevant context from indexed documents.
     * Now supports conversation memory for context-aware responses.
     * Uses the model associated with the conversation, or the current global model if none is set.
     * 
     * @param question the question to ask
     * @param conversationId the conversation identifier for memory persistence
     * @return the answer generated using RAG
     */
    @GetMapping("/ask")
    public ResponseEntity<String> ask(
            @RequestParam String question,
            @RequestParam String conversationId) {
        try {
            if (question == null || question.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Question cannot be null or empty");
            }

            if (conversationId == null || conversationId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Conversation ID cannot be null or empty");
            }

            // Get the model for this conversation
            ChatModel model = modelSwitchService.getModelForConversation(conversationId);
            
            String answer = ragService.query(question, conversationId, model);
            return ResponseEntity.ok(answer);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing question: " + e.getMessage());
        }
    }
}
