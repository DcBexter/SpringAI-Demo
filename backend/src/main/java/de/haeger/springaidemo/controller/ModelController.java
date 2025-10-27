package de.haeger.springaidemo.controller;

import de.haeger.springaidemo.dto.CurrentModelResponse;
import de.haeger.springaidemo.dto.ModelInfo;
import de.haeger.springaidemo.dto.SwitchModelRequest;
import de.haeger.springaidemo.dto.SwitchModelResponse;
import de.haeger.springaidemo.service.ModelSwitchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing AI model selection and switching.
 * Provides endpoints to list available models, get current model, and switch models.
 */
@RestController
@RequestMapping("/api/models")
@CrossOrigin(origins = "*")
public class ModelController {

    private final ModelSwitchService modelSwitchService;

    public ModelController(ModelSwitchService modelSwitchService) {
        this.modelSwitchService = modelSwitchService;
    }

    /**
     * Get a list of all available AI models with their status.
     * 
     * @return List of ModelInfo objects containing model details
     */
    @GetMapping
    public ResponseEntity<List<ModelInfo>> getAvailableModels() {
        return ResponseEntity.ok(modelSwitchService.getAvailableModels());
    }

    /**
     * Get the currently active model name.
     * If a conversationId is provided, returns the model for that conversation.
     * 
     * @param conversationId Optional conversation ID
     * @return CurrentModelResponse containing the model name
     */
    @GetMapping("/current")
    public ResponseEntity<CurrentModelResponse> getCurrentModel(
            @RequestParam(required = false) String conversationId) {
        String modelName;
        if (conversationId != null && !conversationId.isBlank()) {
            modelName = modelSwitchService.getModelNameForConversation(conversationId);
        } else {
            modelName = modelSwitchService.getCurrentModelName();
        }
        return ResponseEntity.ok(new CurrentModelResponse(modelName));
    }

    /**
     * Switch to a different AI model.
     * If a conversationId is provided, the switch is specific to that conversation.
     * Otherwise, switches the global default model.
     * 
     * @param request SwitchModelRequest containing modelName and optional conversationId
     * @return SwitchModelResponse indicating success or failure
     */
    @PostMapping("/switch")
    public ResponseEntity<SwitchModelResponse> switchModel(@RequestBody SwitchModelRequest request) {
        try {
            if (request.conversationId() != null && !request.conversationId().isBlank()) {
                // Switch model for specific conversation
                modelSwitchService.switchModelForConversation(request.conversationId(), request.modelName());
            } else {
                // Switch global model
                modelSwitchService.switchModel(request.modelName());
            }
            
            return ResponseEntity.ok(new SwitchModelResponse(
                true,
                "Switched to " + request.modelName(),
                request.modelName()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new SwitchModelResponse(
                false,
                e.getMessage(),
                modelSwitchService.getCurrentModelName()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new SwitchModelResponse(
                false,
                "Failed to switch model: " + e.getMessage(),
                modelSwitchService.getCurrentModelName()
            ));
        }
    }
}
