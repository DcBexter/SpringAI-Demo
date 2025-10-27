package de.haeger.springaidemo.dto;

/**
 * DTO for the response to a model switch request.
 * 
 * @param success Whether the model switch was successful
 * @param message A message describing the result of the switch operation
 * @param currentModel The name of the currently active model after the operation
 */
public record SwitchModelResponse(
    boolean success,
    String message,
    String currentModel
) {}
