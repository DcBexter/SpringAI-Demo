CREATE EXTENSION IF NOT EXISTS vector;

-- Vector store table for PgVectorStore
CREATE TABLE IF NOT EXISTS vector_store (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content TEXT,
    metadata JSONB,
    embedding vector(768)
);

-- Create index for vector search
CREATE INDEX IF NOT EXISTS vector_store_embedding_idx ON vector_store 
USING hnsw (embedding vector_cosine_ops);

-- Conversation model table for storing model per conversation
CREATE TABLE IF NOT EXISTS conversation_model (
    conversation_id VARCHAR(255) PRIMARY KEY,
    model_name VARCHAR(50) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);