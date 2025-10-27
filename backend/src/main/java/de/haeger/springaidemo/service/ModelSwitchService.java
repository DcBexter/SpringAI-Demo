package de.haeger.springaidemo.service;

import de.haeger.springaidemo.dto.ModelInfo;
import de.haeger.springaidemo.dto.ModelStatus;
import de.haeger.springaidemo.entity.ConversationModel;
import de.haeger.springaidemo.repository.ConversationModelRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ModelSwitchService {

    private final Map<String, ChatModel> models;
    private final ConversationModelRepository conversationModelRepository;
    private String currentModelName = "gemini";

    public ModelSwitchService(
            @Qualifier("gemini") ChatModel geminiModel,
            @Qualifier("ollama") ChatModel ollamaModel,
            ConversationModelRepository conversationModelRepository) {
        this.models = Map.of(
            "gemini", geminiModel,
            "ollama", ollamaModel
        );
        this.conversationModelRepository = conversationModelRepository;
    }

    /**
     * Get the currently active model for the global context.
     */
    public ChatModel getCurrentModel() {
        return models.get(currentModelName);
    }

    /**
     * Get the name of the currently active model.
     */
    public String getCurrentModelName() {
        return currentModelName;
    }

    /**
     * Get the model for a specific conversation.
     * If no model is stored for the conversation, returns the current global model.
     */
    public ChatModel getModelForConversation(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return getCurrentModel();
        }
        
        Optional<ConversationModel> conversationModel = conversationModelRepository.findByConversationId(conversationId);
        if (conversationModel.isPresent()) {
            String modelName = conversationModel.get().getModelName();
            return models.getOrDefault(modelName, getCurrentModel());
        }
        
        return getCurrentModel();
    }

    /**
     * Get the model name for a specific conversation.
     */
    public String getModelNameForConversation(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return currentModelName;
        }
        
        return conversationModelRepository.findByConversationId(conversationId)
            .map(ConversationModel::getModelName)
            .orElse(currentModelName);
    }

    /**
     * Switch the global model.
     */
    public synchronized void switchModel(String modelName) {
        if (!models.containsKey(modelName)) {
            throw new IllegalArgumentException("Unknown model: " + modelName);
        }
        this.currentModelName = modelName;
    }

    /**
     * Switch the model for a specific conversation and persist the choice.
     */
    public synchronized void switchModelForConversation(String conversationId, String modelName) {
        if (!models.containsKey(modelName)) {
            throw new IllegalArgumentException("Unknown model: " + modelName);
        }
        
        if (conversationId == null || conversationId.isBlank()) {
            // If no conversation ID, just switch globally
            switchModel(modelName);
            return;
        }
        
        // Save or update the conversation model preference
        Optional<ConversationModel> existing = conversationModelRepository.findByConversationId(conversationId);
        if (existing.isPresent()) {
            ConversationModel conversationModel = existing.get();
            conversationModel.setModelName(modelName);
            conversationModelRepository.save(conversationModel);
        } else {
            ConversationModel conversationModel = new ConversationModel(conversationId, modelName);
            conversationModelRepository.save(conversationModel);
        }
    }

    /**
     * Get a list of all available models with their status.
     */
    public List<ModelInfo> getAvailableModels() {
        return List.of(
            new ModelInfo("gemini", "Gemini 2.5 Flash", "cloud", checkModelStatus("gemini")),
            new ModelInfo("ollama", "GPT-OSS 20B", "local", checkModelStatus("ollama"))
        );
    }

    /**
     * Check the status of a model.
     */
    private ModelStatus checkModelStatus(String modelName) {
        try {
            ChatModel model = models.get(modelName);
            if (model == null) {
                return ModelStatus.OFFLINE;
            }
            return ModelStatus.ONLINE;
        } catch (Exception e) {
            return ModelStatus.OFFLINE;
        }
    }
}
