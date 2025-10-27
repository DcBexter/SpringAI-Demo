import { Component, OnInit, Output, EventEmitter, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ChatService } from '../../services/chat';
import { ConversationSummary } from '../../models/conversation-summary.model';

@Component({
  selector: 'app-conversation-list',
  imports: [
    CommonModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
    MatDividerModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './conversation-list.html',
  styleUrl: './conversation-list.scss'
})
export class ConversationListComponent implements OnInit {
  @Input() currentConversationId: string = '';
  @Output() conversationSelected = new EventEmitter<string>();
  @Output() newConversation = new EventEmitter<void>();

  conversations: ConversationSummary[] = [];
  isLoading: boolean = false;

  constructor(private chatService: ChatService) {}

  ngOnInit(): void {
    this.loadConversations();
  }

  loadConversations(): void {
    this.isLoading = true;
    this.chatService.getConversations().subscribe({
      next: (conversations) => {
        this.conversations = conversations;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading conversations:', error);
        this.isLoading = false;
      }
    });
  }

  selectConversation(conversationId: string): void {
    this.conversationSelected.emit(conversationId);
  }

  createNewConversation(): void {
    this.newConversation.emit();
  }

  deleteConversation(conversationId: string, event: Event): void {
    event.stopPropagation();
    
    if (confirm('Are you sure you want to delete this conversation?')) {
      this.chatService.deleteConversation(conversationId).subscribe({
        next: () => {
          this.chatService.removeStoredConversationId(conversationId);
          this.loadConversations();
          
          // If deleted conversation was active, create new one
          if (this.currentConversationId === conversationId) {
            this.createNewConversation();
          }
        },
        error: (error) => {
          console.error('Error deleting conversation:', error);
        }
      });
    }
  }

  formatDate(date: Date | string): string {
    const d = typeof date === 'string' ? new Date(date) : date;
    const now = new Date();
    const diffMs = now.getTime() - d.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    
    return d.toLocaleDateString();
  }

  getConversationPreview(conversationId: string): string {
    return conversationId.substring(0, 8);
  }
}
