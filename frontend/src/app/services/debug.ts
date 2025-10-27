import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ChatMessage } from '../models/chat-message.model';
import { ModelSettings } from '../models/model-settings.model';
import { RequestDebugInfo } from '../models/request-debug-info.model';
import { ResponseDebugInfo } from '../models/response-debug-info.model';
import { ApiEndpoints } from '../models/api-endpoints.model';

@Injectable({
  providedIn: 'root'
})
export class DebugService {
  private apiUrl = environment.apiUrl;
  private debugEnabled = new BehaviorSubject<boolean>(false);
  private conversationId = new BehaviorSubject<string>('');
  private chatHistory = new BehaviorSubject<ChatMessage[]>([]);
  private lastRequest = new BehaviorSubject<RequestDebugInfo | null>(null);
  private lastResponse = new BehaviorSubject<ResponseDebugInfo | null>(null);

  constructor(private http: HttpClient) {}

  // Getters
  getConversationId(): string {
    return this.conversationId.value;
  }

  setConversationId(id: string): void {
    this.conversationId.next(id);
  }

  getChatHistory(): ChatMessage[] {
    return this.chatHistory.value;
  }

  addChatMessage(message: ChatMessage): void {
    if (this.debugEnabled.value) {
      const history = [...this.chatHistory.value, message];
      this.chatHistory.next(history);
    }
  }

  clearChatHistory(): void {
    this.chatHistory.next([]);
  }

  getModelSettings(): Observable<ModelSettings> {
    return this.http.get<ModelSettings>(`${this.apiUrl}/info/model`);
  }

  getChatMemory(conversationId: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/info/memory/${conversationId}`);
  }

  getVectorStoreStats(): Observable<any> {
    return this.http.get(`${this.apiUrl}/info/vector-store`);
  }

  getLastRequest(): RequestDebugInfo | null {
    return this.lastRequest.value;
  }

  setLastRequest(request: RequestDebugInfo): void {
    if (this.debugEnabled.value) {
      this.lastRequest.next(request);
    }
  }

  getLastResponse(): ResponseDebugInfo | null {
    return this.lastResponse.value;
  }

  setLastResponse(response: ResponseDebugInfo): void {
    if (this.debugEnabled.value) {
      this.lastResponse.next(response);
    }
  }

  getApiEndpoints(): ApiEndpoints {
    return {
      chat: `${this.apiUrl}/chat`,
      ragAddText: `${this.apiUrl}/rag/addText`,
      ragAsk: `${this.apiUrl}/rag/ask`,
      infoModel: `${this.apiUrl}/info/model`,
      infoMemory: `${this.apiUrl}/info/memory/{conversationId}`,
      infoVectorStore: `${this.apiUrl}/info/vector-store`
    };
  }

  isDebugEnabled(): boolean {
    return this.debugEnabled.value;
  }

  toggleDebug(): void {
    const newState = !this.debugEnabled.value;
    this.debugEnabled.next(newState);
    
    // Clear debug data when disabling
    if (!newState) {
      this.lastRequest.next(null);
      this.lastResponse.next(null);
      this.chatHistory.next([]);
    }
  }

  exportDebugInfo(): string {
    const debugInfo = {
      conversationId: this.getConversationId(),
      chatHistory: this.getChatHistory(),
      lastRequest: this.getLastRequest(),
      lastResponse: this.getLastResponse(),
      apiEndpoints: this.getApiEndpoints(),
      timestamp: new Date().toISOString()
    };
    return JSON.stringify(debugInfo, null, 2);
  }
}
