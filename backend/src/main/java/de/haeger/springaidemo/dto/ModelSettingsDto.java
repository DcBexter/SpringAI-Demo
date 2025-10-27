package de.haeger.springaidemo.dto;

/**
 * DTO for model settings information.
 * Used by debug endpoints to expose current AI model configuration.
 */
public class ModelSettingsDto {
    
    private String modelName;
    private Double temperature;
    private String projectId;
    private String location;

    public ModelSettingsDto() {
    }

    public ModelSettingsDto(String modelName, Double temperature, String projectId, String location) {
        this.modelName = modelName;
        this.temperature = temperature;
        this.projectId = projectId;
        this.location = location;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
