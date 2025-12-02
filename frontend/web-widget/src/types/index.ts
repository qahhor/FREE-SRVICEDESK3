/**
 * Widget configuration options
 */
export interface WidgetConfig {
  /** Project key from Service Desk */
  projectKey: string;
  /** API base URL */
  apiUrl: string;
  /** WebSocket URL (optional, defaults to derived from apiUrl) */
  wsUrl?: string;
  /** Widget position on screen */
  position?: 'bottom-right' | 'bottom-left';
  /** Primary theme color */
  primaryColor?: string;
  /** Initial greeting message */
  greeting?: string;
  /** Placeholder text for input field */
  placeholder?: string;
  /** Message shown when agents are offline */
  offlineMessage?: string;
  /** Show agent avatar in messages */
  showAgentAvatar?: boolean;
  /** Enable sound notifications */
  soundEnabled?: boolean;
  /** Widget language */
  language?: 'en' | 'ru' | 'uz';
  /** Custom CSS class prefix */
  cssPrefix?: string;
  /** Z-index for widget */
  zIndex?: number;
}

/**
 * Widget session data
 */
export interface WidgetSession {
  id: string;
  sessionToken: string;
  visitorName?: string;
  visitorEmail?: string;
  ticketId?: string;
  projectId: string;
  status: SessionStatus;
  createdAt: string;
  updatedAt: string;
}

/**
 * Session status enum
 */
export type SessionStatus = 'ACTIVE' | 'CLOSED';

/**
 * Message sender type
 */
export type SenderType = 'VISITOR' | 'AGENT' | 'SYSTEM';

/**
 * Message type
 */
export type MessageType = 'TEXT' | 'FILE' | 'IMAGE';

/**
 * Widget message
 */
export interface WidgetMessage {
  id: string;
  sessionId: string;
  senderType: SenderType;
  senderId?: string;
  senderName?: string;
  content: string;
  messageType: MessageType;
  attachmentId?: string;
  attachmentUrl?: string;
  attachmentName?: string;
  readAt?: string;
  createdAt: string;
}

/**
 * Pre-chat form data
 */
export interface PreChatFormData {
  name: string;
  email: string;
  message?: string;
}

/**
 * Attachment upload result
 */
export interface AttachmentResult {
  id: string;
  filename: string;
  contentType: string;
  size: number;
  url: string;
}

/**
 * WebSocket event types
 */
export type WebSocketEventType = 
  | 'MESSAGE_RECEIVED'
  | 'AGENT_TYPING'
  | 'AGENT_JOINED'
  | 'SESSION_CLOSED'
  | 'CONNECTED'
  | 'DISCONNECTED'
  | 'ERROR';

/**
 * WebSocket event payload
 */
export interface WebSocketEvent {
  type: WebSocketEventType;
  payload?: WidgetMessage | AgentInfo | string;
  timestamp: string;
}

/**
 * Agent information
 */
export interface AgentInfo {
  id: string;
  name: string;
  avatar?: string;
}

/**
 * Widget state
 */
export interface WidgetState {
  isOpen: boolean;
  isMinimized: boolean;
  isLoading: boolean;
  isConnected: boolean;
  isAgentTyping: boolean;
  unreadCount: number;
  session: WidgetSession | null;
  messages: WidgetMessage[];
  agent: AgentInfo | null;
  error: string | null;
}

/**
 * Event handler callback type
 */
export type EventCallback<T = unknown> = (data: T) => void;

/**
 * Widget event emitter interface
 */
export interface EventEmitter {
  on(event: string, callback: EventCallback): void;
  off(event: string, callback: EventCallback): void;
  emit(event: string, data?: unknown): void;
}

/**
 * API response wrapper
 */
export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data?: T;
  error?: string;
  timestamp: string;
}

/**
 * Session start request
 */
export interface SessionStartRequest {
  projectKey: string;
  visitorName?: string;
  visitorEmail?: string;
  visitorMetadata?: Record<string, string>;
}

/**
 * Send message request
 */
export interface SendMessageRequest {
  content: string;
  messageType?: MessageType;
  attachmentId?: string;
}

/**
 * Translations interface
 */
export interface Translations {
  greeting: string;
  placeholder: string;
  offlineMessage: string;
  sendButton: string;
  attachFile: string;
  startChat: string;
  endChat: string;
  minimize: string;
  close: string;
  nameLabel: string;
  emailLabel: string;
  messageLabel: string;
  namePlaceholder: string;
  emailPlaceholder: string;
  messagePlaceholder: string;
  submitForm: string;
  connecting: string;
  connected: string;
  disconnected: string;
  reconnecting: string;
  agentTyping: string;
  agentJoined: string;
  sessionClosed: string;
  fileUploadError: string;
  fileTooLarge: string;
  invalidFileType: string;
  sending: string;
  sent: string;
  delivered: string;
  read: string;
  today: string;
  yesterday: string;
}
