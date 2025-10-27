#!/bin/bash

# Start Ollama server in the background
/bin/ollama serve &

# Wait for Ollama to be ready
echo "Waiting for Ollama server to start..."
sleep 10

# Pull the gpt-oss:20b model
echo "Pulling gpt-oss:20b model..."
/bin/ollama pull gpt-oss:20b

echo "Model pull complete. Ollama is ready."

# Keep the container running
wait