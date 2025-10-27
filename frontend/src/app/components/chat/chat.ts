import { Component, OnInit, AfterViewChecked, ViewChild, ElementRef, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule, FormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MarkdownModule } from 'ngx-markdown';
import { ChatService } from '../../services/chat';
import { RagService } from '../../services/rag';
import { DebugService } from '../../services/debug';
import { ModelService } from '../../services/model';
import { ModelSelectorComponent } from '../model-selector/model-selector';
import { ChatMessage } from '../../models/chat-message.model';

@Component({
  selector: 'app-chat',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatProgressSpinnerModule,
    MatSlideToggleModule,
    MatSnackBarModule,
    MatTooltipModule,
    MarkdownModule,
    ModelSelectorComponent
  ],
  templateUrl: './chat.html',
  styleUrl: './chat.scss'
})
export class ChatComponent implements OnInit, AfterViewChecked {
  @ViewChild('messageContainer') private messageContainer!: ElementRef;
  @Output() messageSent = new EventEmitter<void>();
  @Output() conversationChanged = new EventEmitter<void>();
  
  messageControl = new FormControl('', [Validators.required]);
  chatHistory: ChatMessage[] = [];
  conversationId: string = '';
  isLoading: boolean = false;
  private shouldScroll: boolean = false;
  
  // RAG functionality
  ragEnabled: boolean = false;
  hasIndexedDocuments: boolean = false;
  selectedFile: File | null = null;
  isUploadingFile: boolean = false;

  constructor(
    public chatService: ChatService,
    private ragService: RagService,
    private debugService: DebugService,
    private modelService: ModelService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.startNewConversation();
    this.checkForIndexedDocuments();
  }

  checkForIndexedDocuments(): void {
    // Check vector store stats instead of making a test query (avoids quota issues)
    this.debugService.getVectorStoreStats().subscribe({
      next: (stats) => {
        this.hasIndexedDocuments = stats.documentCount > 0;
      },
      error: () => {
        this.hasIndexedDocuments = false;
      }
    });
  }

  startNewConversation(): void {
    setTimeout(() => {
      this.conversationId = this.generateUUID();
      this.chatHistory = [];
      this.debugService.setConversationId(this.conversationId);
      this.debugService.clearChatHistory();
      this.conversationChanged.emit();
    });
  }

  loadConversation(conversationId: string): void {
    this.conversationId = conversationId;
    this.debugService.setConversationId(conversationId);
    
    // Load the model for this conversation
    this.modelService.getCurrentModelForConversation(conversationId).subscribe({
      next: (response) => {
        this.modelService.setCurrentModel(response.modelName);
      },
      error: (error) => {
        console.error('Error loading conversation model:', error);
      }
    });
    
    this.chatService.getConversationMemory(conversationId).subscribe({
      next: (memory) => {
        this.chatHistory = memory.messages.map((msg: any) => ({
          role: msg.role.toLowerCase() === 'user' ? 'user' : 'ai',
          content: msg.content,
          timestamp: new Date(msg.timestamp)
        }));
        this.debugService.clearChatHistory();
        this.chatHistory.forEach(msg => this.debugService.addChatMessage(msg));
        this.shouldScroll = true;
        this.conversationChanged.emit();
      },
      error: (error) => {
        console.error('Error loading conversation:', error);
        this.chatHistory = [];
      }
    });
  }

  ngAfterViewChecked(): void {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  sendMessage(): void {
    if (this.messageControl.invalid || this.isLoading) {
      return;
    }

    const userMessage = this.messageControl.value?.trim();
    if (!userMessage) {
      return;
    }

    // Check if RAG is enabled but no documents indexed
    if (this.ragEnabled && !this.hasIndexedDocuments) {
      this.snackBar.open('Please upload a document first to use RAG mode', 'Close', { duration: 3000 });
      return;
    }

    // Add user message to history
    const userMsg: ChatMessage = {
      role: 'user',
      content: userMessage,
      timestamp: new Date()
    };
    this.chatHistory.push(userMsg);
    this.debugService.addChatMessage(userMsg);

    this.messageControl.setValue('');
    this.setLoadingState(true);
    this.shouldScroll = true;

    const startTime = Date.now();

    // Use RAG or regular chat based on toggle
    if (this.ragEnabled) {
      // Track RAG request
      this.debugService.setLastRequest({
        method: 'GET',
        url: `/rag/ask?question=${encodeURIComponent(userMessage)}&conversationId=${this.conversationId}`,
        timestamp: new Date(),
        body: null
      });

      this.ragService.askQuestion(userMessage, this.conversationId).subscribe({
        next: (response) => {
          this.handleAIResponse(response, startTime);
        },
        error: (error) => {
          this.handleError(error, startTime);
        }
      });
    } else {
      // Track regular chat request
      this.debugService.setLastRequest({
        method: 'GET',
        url: `/chat?msg=${encodeURIComponent(userMessage)}&conversationId=${this.conversationId}`,
        timestamp: new Date(),
        body: null
      });

      this.chatService.sendMessage(userMessage, this.conversationId).subscribe({
        next: (response) => {
          this.handleAIResponse(response, startTime);
        },
        error: (error) => {
          this.handleError(error, startTime);
        }
      });
    }
  }

  private setLoadingState(loading: boolean): void {
    this.isLoading = loading;
    if (loading) {
      this.messageControl.disable();
    } else {
      this.messageControl.enable();
    }
  }

  private handleAIResponse(response: string, startTime: number): void {
    const aiMsg: ChatMessage = {
      role: 'ai',
      content: response,
      timestamp: new Date()
    };
    this.chatHistory.push(aiMsg);
    this.debugService.addChatMessage(aiMsg);

    this.debugService.setLastResponse({
      statusCode: 200,
      duration: Date.now() - startTime,
      timestamp: new Date(),
      body: response
    });

    this.setLoadingState(false);
    this.shouldScroll = true;
    
    this.messageSent.emit();
  }

  private handleError(error: any, startTime: number): void {
    console.error('Error:', error);
    const errorMsg: ChatMessage = {
      role: 'ai',
      content: 'Sorry, there was an error processing your message. Please try again.',
      timestamp: new Date()
    };
    this.chatHistory.push(errorMsg);
    this.debugService.addChatMessage(errorMsg);

    this.debugService.setLastResponse({
      statusCode: error.status || 500,
      duration: Date.now() - startTime,
      timestamp: new Date(),
      body: error.message
    });

    this.setLoadingState(false);
    this.shouldScroll = true;
  }

  private scrollToBottom(): void {
    try {
      if (this.messageContainer) {
        this.messageContainer.nativeElement.scrollTop = 
          this.messageContainer.nativeElement.scrollHeight;
      }
    } catch (err) {
      console.error('Error scrolling to bottom:', err);
    }
  }

  private generateUUID(): string {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
      const r = Math.random() * 16 | 0;
      const v = c === 'x' ? r : (r & 0x3 | 0x8);
      return v.toString(16);
    });
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];

      if (!file.name.endsWith('.txt') && file.type !== 'text/plain') {
        this.snackBar.open('Please select a .txt file', 'Close', { duration: 3000 });
        this.selectedFile = null;
        input.value = '';
        return;
      }

      const maxSize = 5 * 1024 * 1024;
      if (file.size > maxSize) {
        this.snackBar.open('File size must be less than 5MB', 'Close', { duration: 3000 });
        this.selectedFile = null;
        input.value = '';
        return;
      }

      this.selectedFile = file;
    }
  }

  uploadTextFile(): void {
    if (!this.selectedFile || this.isUploadingFile) {
      return;
    }

    this.isUploadingFile = true;

    const reader = new FileReader();
    reader.onload = (e) => {
      const text = e.target?.result as string;
      const startTime = Date.now();

      this.debugService.setLastRequest({
        method: 'POST',
        url: '/rag/addText',
        timestamp: new Date(),
        body: { text: text.substring(0, 100) + '...' }
      });

      this.ragService.addText(text).subscribe({
        next: (response) => {
          this.hasIndexedDocuments = true;
          this.checkForIndexedDocuments(); // Refresh document count
          this.snackBar.open('Document uploaded and indexed successfully!', 'Close', { duration: 3000 });

          this.debugService.setLastResponse({
            statusCode: 200,
            duration: Date.now() - startTime,
            timestamp: new Date(),
            body: response
          });

          this.selectedFile = null;
          const fileInput = document.getElementById('txtFileInput') as HTMLInputElement;
          if (fileInput) {
            fileInput.value = '';
          }
          this.isUploadingFile = false;
        },
        error: (error) => {
          console.error('Error uploading file:', error);
          this.snackBar.open(`Error uploading file: ${error.error || error.message}`, 'Close', { duration: 5000 });

          this.debugService.setLastResponse({
            statusCode: error.status || 500,
            duration: Date.now() - startTime,
            timestamp: new Date(),
            body: error.message
          });

          this.isUploadingFile = false;
        }
      });
    };

    reader.onerror = () => {
      this.snackBar.open('Error reading file', 'Close', { duration: 3000 });
      this.isUploadingFile = false;
    };

    reader.readAsText(this.selectedFile);
  }
}
