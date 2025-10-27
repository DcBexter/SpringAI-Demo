export interface ConversationSummary {
  conversationId: string;
  messageCount: number;
  lastUpdated: Date | string;
  title?: string;
}
