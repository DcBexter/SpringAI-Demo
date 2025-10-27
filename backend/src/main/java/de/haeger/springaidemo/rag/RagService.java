package de.haeger.springaidemo.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class RagService {

    private final VectorStore vectorStore;
    private final ChatMemory chatMemory;

    public RagService(VectorStore vectorStore, ChatMemory chatMemory) {
        this.vectorStore = vectorStore;
        this.chatMemory = chatMemory;
    }

    /**
     * Query the RAG system with a specific model.
     * Creates a fresh ChatClient with the provided model for each query.
     * 
     * @param question the question to ask
     * @param conversationId the conversation identifier for memory persistence
     * @param chatModel the model to use for this query
     * @return the answer generated using RAG
     */
    public String query(String question, String conversationId, ChatModel chatModel) {
        ChatClient ragChatClient = ChatClient.builder(chatModel)
                .defaultSystem("You are a helpful AI assistant. Answer questions based on the provided context from the document store. " +
                        "If the context doesn't contain relevant information, you can still answer using your general knowledge.")
                .defaultAdvisors(
                    QuestionAnswerAdvisor.builder(vectorStore).build(),
                    MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
        
        return ragChatClient.prompt()
                .user(question)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }
}
