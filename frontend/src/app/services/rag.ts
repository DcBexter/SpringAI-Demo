import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class RagService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  addText(text: string): Observable<string> {
    return this.http.post(`${this.apiUrl}/rag/addText`, 
      text, 
      { 
        responseType: 'text',
        headers: {
          'Content-Type': 'text/plain'
        }
      }
    );
  }

  askQuestion(question: string, conversationId: string): Observable<string> {
    const params = new HttpParams()
      .set('question', question)
      .set('conversationId', conversationId);

    return this.http.get(`${this.apiUrl}/rag/ask`, {
      params,
      responseType: 'text'
    });
  }
}
