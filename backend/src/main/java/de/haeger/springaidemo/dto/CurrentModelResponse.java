package de.haeger.springaidemo.dto;

/**
 * DTO for the response containing the current model information.
 * 
 * @param modelName The name of the currently active model
 */
public record CurrentModelResponse(
    String modelName
) {}
