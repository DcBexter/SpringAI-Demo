package de.haeger.springaidemo.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for indexing text content into the vector store.
 * Converts text into embeddings and stores them for RAG queries.
 */
@Service
public class DocumentIndexer {

    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;

    public DocumentIndexer(EmbeddingModel embeddingModel, VectorStore vectorStore) {
        this.embeddingModel = embeddingModel;
        this.vectorStore = vectorStore;
    }

    /**
     * Index text content into the vector store.
     * 
     * @param text the text content to index
     */
    public void indexText(String text) {
        Document document = new Document(text, Map.of("source", "text-input"));
        vectorStore.add(List.of(document));
    }
}
