export interface ModelInfo {
  name: string;
  displayName: string;
  type: 'cloud' | 'local';
  status: 'ONLINE' | 'OFFLINE' | 'LOADING';
}

export interface CurrentModelResponse {
  modelName: string;
}

export interface SwitchModelRequest {
  modelName: string;
  conversationId?: string;
}

export interface SwitchModelResponse {
  success: boolean;
  message: string;
  currentModel: string;
}
