package de.haeger.springaidemo.dto;

/**
 * DTO representing information about an available AI model.
 * 
 * @param name The internal name/identifier of the model (e.g., "gemini", "ollama")
 * @param displayName The user-friendly display name (e.g., "Gemini 2.5 Flash", "GPT-OSS 20B")
 * @param type The type of model - "cloud" for cloud-based models or "local" for local models
 * @param status The current status of the model (ONLINE, OFFLINE, LOADING)
 */
public record ModelInfo(
    String name,
    String displayName,
    String type,
    ModelStatus status
) {}
