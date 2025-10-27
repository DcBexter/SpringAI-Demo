export interface ResponseDebugInfo {
  statusCode: number;
  timestamp: Date;
  duration: number; // milliseconds
  body?: any;
}
