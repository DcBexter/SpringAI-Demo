import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ConversationSummary } from '../models/conversation-summary.model';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private apiUrl = environment.apiUrl;
  private readonly STORAGE_KEY = 'chat_conversations';

  constructor(private http: HttpClient) {}

  sendMessage(message: string, conversationId: string): Observable<string> {
    const params = new HttpParams()
      .set('msg', message)
      .set('conversationId', conversationId);

    // Save conversation ID to localStorage
    this.saveConversationId(conversationId);

    return this.http.get(`${this.apiUrl}/chat`, {
      params,
      responseType: 'text'
    });
  }

  getConversations(): Observable<ConversationSummary[]> {
    return this.http.get<ConversationSummary[]>(`${this.apiUrl}/info/conversations`);
  }

  getConversationMemory(conversationId: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/info/memory/${conversationId}`);
  }

  deleteConversation(conversationId: string): Observable<string> {
    return this.http.delete(`${this.apiUrl}/chat/memory/${conversationId}`, {
      responseType: 'text'
    });
  }

  // LocalStorage management
  private saveConversationId(conversationId: string): void {
    const conversations = this.getStoredConversationIds();
    if (!conversations.includes(conversationId)) {
      conversations.unshift(conversationId);
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(conversations));
    }
  }

  getStoredConversationIds(): string[] {
    const stored = localStorage.getItem(this.STORAGE_KEY);
    return stored ? JSON.parse(stored) : [];
  }

  removeStoredConversationId(conversationId: string): void {
    const conversations = this.getStoredConversationIds();
    const filtered = conversations.filter(id => id !== conversationId);
    localStorage.setItem(this.STORAGE_KEY, JSON.stringify(filtered));
  }
}
