package de.haeger.springaidemo.dto;

/**
 * DTO for requesting a model switch.
 * 
 * @param modelName The name of the model to switch to (e.g., "gemini", "ollama")
 * @param conversationId The ID of the conversation for which to switch the model
 */
public record SwitchModelRequest(
    String modelName,
    String conversationId
) {}
