import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import {
  ModelInfo,
  CurrentModelResponse,
  SwitchModelRequest,
  SwitchModelResponse
} from '../models';

@Injectable({
  providedIn: 'root'
})
export class ModelService {
  private apiUrl = environment.apiUrl;
  private currentModelSubject = new BehaviorSubject<string>('gemini');
  public currentModel$ = this.currentModelSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadCurrentModel();
  }

  getAvailableModels(): Observable<ModelInfo[]> {
    return this.http.get<ModelInfo[]>(`${this.apiUrl}/api/models`);
  }

  getCurrentModel(): Observable<CurrentModelResponse> {
    return this.http.get<CurrentModelResponse>(`${this.apiUrl}/api/models/current`);
  }

  switchModel(modelName: string, conversationId?: string): Observable<SwitchModelResponse> {
    const request: SwitchModelRequest = {
      modelName,
      conversationId
    };
    
    return this.http.post<SwitchModelResponse>(
      `${this.apiUrl}/api/models/switch`,
      request
    ).pipe(
      tap(response => {
        if (response.success) {
          this.currentModelSubject.next(response.currentModel);
          localStorage.setItem('selectedModel', response.currentModel);
        }
      })
    );
  }

  private loadCurrentModel(): void {
    const stored = localStorage.getItem('selectedModel');
    if (stored) {
      this.currentModelSubject.next(stored);
    }
    
    this.getCurrentModel().subscribe({
      next: (response) => {
        this.currentModelSubject.next(response.modelName);
      },
      error: (error) => {
        console.error('Error loading current model:', error);
      }
    });
  }
}
