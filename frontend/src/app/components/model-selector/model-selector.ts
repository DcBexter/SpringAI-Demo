import { Component, OnInit, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ModelService } from '../../services/model';
import { DebugService } from '../../services/debug';
import { ModelInfo } from '../../models';

@Component({
  selector: 'app-model-selector',
  imports: [
    CommonModule,
    MatSelectModule,
    MatIconModule,
    MatTooltipModule,
    MatFormFieldModule,
    MatSnackBarModule
  ],
  templateUrl: './model-selector.html',
  styleUrl: './model-selector.scss'
})
export class ModelSelectorComponent implements OnInit {
  availableModels: ModelInfo[] = [];
  selectedModel: string = 'gemini';
  
  constructor(
    private modelService: ModelService,
    private debugService: DebugService,
    private snackBar: MatSnackBar
  ) {}
  
  ngOnInit(): void {
    this.loadModels();
    this.modelService.currentModel$.subscribe(model => {
      this.selectedModel = model;
    });
  }
  
  loadModels(): void {
    this.modelService.getAvailableModels().subscribe({
      next: (models) => {
        this.availableModels = models;
      },
      error: (error) => {
        console.error('Error loading models:', error);
      }
    });
  }
  
  onModelChange(event: any): void {
    const modelName = event.value;
    const conversationId = this.debugService.getConversationId();
    
    this.modelService.switchModel(modelName, conversationId).subscribe({
      next: (response) => {
        if (response.success) {
          this.snackBar.open(`Switched to ${modelName}`, 'Close', { duration: 2000 });
        } else {
          this.snackBar.open(response.message, 'Close', { duration: 3000 });
          this.selectedModel = response.currentModel;
        }
      },
      error: (error) => {
        console.error('Error switching model:', error);
        this.snackBar.open('Failed to switch model', 'Close', { duration: 3000 });
      }
    });
  }
  
  getStatusIcon(status: string): string {
    switch (status.toLowerCase()) {
      case 'online': return 'check_circle';
      case 'offline': return 'cancel';
      case 'loading': return 'hourglass_empty';
      default: return 'help';
    }
  }
}
