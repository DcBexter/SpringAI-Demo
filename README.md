# Spring AI RAG Tools Demo with Angular Frontend

A full-stack demonstration application showcasing Spring AI's RAG (Retrieval-Augmented Generation) capabilities with persistent memory, AI tools integration, and a modern Angular Material UI frontend.

## Features

- **Chat with Persistent Memory**: Conversational AI that remembers context across messages
- **RAG System**: Upload .txt files and ask questions using semantic search
- **AI Tools Integration**: Weather tool for external API calls
- **Vector Database**: PostgreSQL with pgvector extension for semantic search
- **Modern UI**: Angular application with Material Design components
- **Debug Panel**: Developer-friendly debug information panel
- **Containerized**: All services run in Docker containers

## Project Structure

```
project-root/
├── backend/                    # Spring Boot application
│   ├── src/                   # Java source code
│   ├── pom.xml               # Maven dependencies
│   ├── Dockerfile            # Backend container
│   └── init/                 # Database initialization scripts
├── frontend/                  # Angular application
│   ├── src/                  # TypeScript/Angular source code
│   ├── package.json          # NPM dependencies
│   ├── Dockerfile            # Frontend container
│   └── nginx.conf            # Nginx configuration
├── docker-compose.yml         # Multi-service orchestration
├── .env                      # Environment variables
└── README.md                 # This file
```

## Setup

### Prerequisites

- Docker and Docker Compose
- Google Cloud Platform account with Vertex AI API enabled

### 1. Configure Google Cloud Credentials

Create your GCP service account credentials file:

```bash
# Use the example file as a reference
cat gcp-credentials.json.example

# Follow the detailed instructions in the example file to:
# - Create a GCP service account
# - Enable Vertex AI API
# - Download the JSON key file
# - Save it as gcp-credentials.json in the project root
```

The `gcp-credentials.json.example` file contains step-by-step instructions for creating your service account in Google Cloud Console.

**Important**: Never commit `gcp-credentials.json` to version control. It should be in `.gitignore`.

### 2. Configure Environment Variables

Create your environment configuration file:

```bash
# Copy the example file
cp .env.example .env

# Edit .env and replace the placeholder values:
# - GEMINI_PROJECT_ID: Your GCP project ID
# - GEMINI_LOCATION: Your preferred GCP region (e.g., europe-west1, us-central1)
```

The `.env.example` file includes instructions on how to find your GCP project ID and choose an appropriate region.

### 3. Build & Run

Start all services (database, backend, frontend):

```bash
docker compose up --build
```

Start in background:

```bash
docker compose up -d
```

Stop all services:

```bash
docker compose down
```

View logs:

```bash
# All services
docker compose logs -f

# Specific service
docker compose logs -f backend
docker compose logs -f frontend
```

### 4. Access the Application

- **Frontend UI**: http://localhost:4200
- **Backend API**: http://localhost:8080
- **Database**: localhost:5432 (PostgreSQL with pgvector)

## User Interface

The Angular frontend provides a clean, responsive interface with three main sections:

### Chat Interface (Left Panel)
- Send messages and receive AI responses
- Conversation history with distinct styling for user/AI messages
- Persistent memory across messages in the same session
- Auto-generated conversation ID for session tracking
- Loading indicators during API calls

### RAG Interface (Right Panel)
- **Upload Text File**: Upload .txt files to index content for RAG queries
- **Ask Questions**: Query indexed text with context-aware answers
- File validation (.txt only, 5MB max)
- Simple text-based knowledge base for demonstration purposes
- Success/error notifications

### Debug Panel (Bottom)
- **Conversation ID**: Current session identifier
- **Chat Memory**: View conversation history with timestamps
- **Model Settings**: AI model configuration (name, temperature)
- **API Endpoints**: URLs being used for backend communication
- **Request/Response Details**: Status codes, timing, payloads
- **Vector Store Stats**: Document count and indexing information
- **Toggle Debug Mode**: Show/hide debug information
- **Copy to Clipboard**: Export debug info as JSON

### Responsive Design
- **Desktop**: Two-column layout (chat left, RAG right)
- **Mobile**: Single-column stacked layout
- Material Design components throughout
- Smooth animations and transitions

## API Endpoints

### Chat Endpoints

```bash
# Send a chat message with memory
GET http://localhost:8080/chat?msg=Remember:%20My%20name%20is%20Alex&conversationId=u1

# Retrieve what the AI remembers
GET http://localhost:8080/chat?msg=What%20is%20my%20name?&conversationId=u1

# Use AI tools (weather)
GET http://localhost:8080/chat?msg=What%20is%20the%20weather%20in%20Berlin?&conversationId=u1

# View conversation history
GET http://localhost:8080/chat/memory/{conversationId}

# Clear conversation memory
DELETE http://localhost:8080/chat/memory/{conversationId}
```

### RAG Endpoints

```bash
# Add text content to vector store
POST http://localhost:8080/rag/addText
Content-Type: application/json
Body: { "text": "Your content here" }

# Ask a question using RAG
GET http://localhost:8080/rag/ask?question=What%20is%20the%20main%20topic?
```

### Debug/Info Endpoints

```bash
# Get current model settings
GET http://localhost:8080/info/model

# Get chat memory for a conversation
GET http://localhost:8080/info/memory/{conversationId}

# Get vector store statistics
GET http://localhost:8080/info/vector-store
```

## Development

### Backend Development

```bash
cd backend

# Compile
mvn clean compile

# Run tests
mvn test

# Package
mvn package

# Run locally (requires database)
mvn spring-boot:run
```

### Frontend Development

```bash
cd frontend

# Install dependencies
npm install

# Run development server
npm start

# Build for production
npm run build

# Run tests
npm test

# Run linter
npm run lint
```

### Database Access

Connect to PostgreSQL:

```bash
docker compose exec db psql -U postgres -d postgres
```

Query vector store:

```sql
SELECT * FROM vector_store;
```

## Technology Stack

### Backend
- **Java 21** - Latest LTS version
- **Spring Boot 3.3.3** - Application framework
- **Spring AI 1.0.3** - AI integration
- **Maven** - Build tool
- **PostgreSQL + pgvector** - Vector database

### Frontend
- **Angular 17+** - Frontend framework
- **Angular Material** - UI component library
- **TypeScript** - Type-safe JavaScript
- **RxJS** - Reactive programming
- **Nginx** - Production web server

### Infrastructure
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration

## Architecture

```
┌─────────────────────────────────────────┐
│         Browser (Client)                │
└─────────────────────────────────────────┘
                  │
                  │ HTTP
                  ▼
┌─────────────────────────────────────────┐
│   Frontend Container (nginx:80)         │
│   Angular + Material UI                 │
└─────────────────────────────────────────┘
                  │
                  │ REST API (CORS)
                  ▼
┌─────────────────────────────────────────┐
│   Backend Container (Spring Boot:8080)  │
│   - Chat Controller                     │
│   - RAG Controller                      │
│   - Info Controller                     │
│   - Vector Store                        │
└─────────────────────────────────────────┘
                  │
                  ▼
┌─────────────────────────────────────────┐
│   Database Container (PostgreSQL:5432)  │
│   - pgvector extension                  │
│   - Chat memory                         │
│   - Vector embeddings                   │
└─────────────────────────────────────────┘
```

## Troubleshooting

### Backend won't start
- Check if `gcp-credentials.json` exists in project root
- Verify `.env` file has correct GCP project ID and location
- Check database container is running: `docker compose ps`

### Frontend can't connect to backend
- Verify backend is running: `curl http://localhost:8080/actuator/health`
- Check CORS configuration in backend `WebConfig.java`
- Check browser console for CORS errors

### Vector store queries return no results
- Verify text content has been added: `GET /info/vector-store`
- Check pgvector extension is installed in database
- Verify embeddings are being generated (check backend logs)
- Add some text content first before asking questions

## License

This is a demonstration project for educational purposes.

## Contributing

This is a demo project. Feel free to fork and modify for your own use cases.
