import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { Subscription, interval } from 'rxjs';
import { DebugService } from '../../services/debug';
import { ModelSettings } from '../../models/model-settings.model';
import { ChatMessage } from '../../models/chat-message.model';
import { RequestDebugInfo } from '../../models/request-debug-info.model';
import { ResponseDebugInfo } from '../../models/response-debug-info.model';
import { ApiEndpoints } from '../../models/api-endpoints.model';

@Component({
  selector: 'app-debug-panel',
  imports: [
    CommonModule,
    FormsModule,
    MatExpansionModule,
    MatListModule,
    MatDividerModule,
    MatChipsModule,
    MatIconModule,
    MatButtonModule,
    MatSnackBarModule,
    MatSlideToggleModule
  ],
  templateUrl: './debug-panel.html',
  styleUrl: './debug-panel.scss',
})
export class DebugPanel implements OnInit, OnDestroy {
  conversationId: string = '';
  chatHistory: ChatMessage[] = [];
  modelSettings: ModelSettings | null = null;
  apiEndpoints: ApiEndpoints | null = null;
  lastRequest: RequestDebugInfo | null = null;
  lastResponse: ResponseDebugInfo | null = null;
  vectorStoreStats: any = null;
  chatMemory: any = null;
  debugEnabled: boolean = false;

  private refreshSubscription?: Subscription;

  constructor(
    private debugService: DebugService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadDebugInfo();
  }

  ngOnDestroy(): void {
  }

  loadDebugInfo(): void {
    this.debugEnabled = this.debugService.isDebugEnabled();

    if (!this.debugEnabled) {
      return;
    }

    this.conversationId = this.debugService.getConversationId();
    this.apiEndpoints = this.debugService.getApiEndpoints();
    this.lastRequest = this.debugService.getLastRequest();
    this.lastResponse = this.debugService.getLastResponse();

    this.debugService.getModelSettings().subscribe({
      next: (settings) => {
        this.modelSettings = settings;
      },
      error: (error) => {
        console.error('Failed to load model settings:', error);
      }
    });

    if (this.conversationId) {
      this.debugService.getChatMemory(this.conversationId).subscribe({
        next: (memory) => {
          this.chatMemory = memory;
          
          // Update chat history from backend memory
          if (memory.messages && Array.isArray(memory.messages)) {
            this.chatHistory = memory.messages.map((msg: any) => ({
              role: msg.role.toLowerCase() === 'user' ? 'user' : 'ai',
              content: msg.content,
              timestamp: new Date(msg.timestamp || Date.now())
            }));
          } else {
            this.chatHistory = [];
          }
        },
        error: (error) => {
          console.error('Failed to load chat memory:', error);
          this.chatHistory = [];
        }
      });
    } else {
      this.chatHistory = [];
    }

    this.debugService.getVectorStoreStats().subscribe({
      next: (stats) => {
        this.vectorStoreStats = stats;
      },
      error: (error) => {
        console.error('Failed to load vector store stats:', error);
      }
    });
  }

  toggleDebug(): void {
    this.debugService.toggleDebug();
    this.debugEnabled = this.debugService.isDebugEnabled();
    
    if (this.debugEnabled) {
      this.loadDebugInfo();
    }
  }

  copyDebugInfo(): void {
    const debugInfo = this.debugService.exportDebugInfo();
    navigator.clipboard.writeText(debugInfo).then(() => {
      // Success - no notification needed
    }).catch((error) => {
      console.error('Failed to copy debug info:', error);
      this.snackBar.open('Failed to copy debug info', 'Close', { duration: 3000 });
    });
  }

  refresh(): void {
    this.loadDebugInfo();
  }

  formatTimestamp(timestamp: Date | string): string {
    if (!timestamp) return 'N/A';
    const date = typeof timestamp === 'string' ? new Date(timestamp) : timestamp;
    return date.toLocaleString();
  }

  formatDuration(duration: number): string {
    if (!duration) return 'N/A';
    return `${duration}ms`;
  }
}
