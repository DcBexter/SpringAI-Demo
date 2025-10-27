package de.haeger.springaidemo.dto;

/**
 * DTO for vector store statistics.
 * Used by debug endpoints to expose vector store information.
 */
public class VectorStoreStatsDto {
    
    private long documentCount;
    private String storageType;
    private boolean isInitialized;

    public VectorStoreStatsDto() {
    }

    public VectorStoreStatsDto(long documentCount, String storageType, boolean isInitialized) {
        this.documentCount = documentCount;
        this.storageType = storageType;
        this.isInitialized = isInitialized;
    }

    public long getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(long documentCount) {
        this.documentCount = documentCount;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setInitialized(boolean initialized) {
        isInitialized = initialized;
    }
}
