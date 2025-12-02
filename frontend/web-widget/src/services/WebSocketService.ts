import { WebSocketEvent, WebSocketEventType } from '../types';

type EventHandler = (event: WebSocketEvent) => void;

/**
 * WebSocket service for real-time communication
 */
export class WebSocketService {
  private ws: WebSocket | null = null;
  private wsUrl: string;
  private sessionId: string | null = null;
  private sessionToken: string | null = null;
  private handlers: Map<WebSocketEventType, Set<EventHandler>> = new Map();
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000;
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null;
  private pingInterval: ReturnType<typeof setInterval> | null = null;
  private isConnecting = false;

  constructor(wsUrl: string) {
    this.wsUrl = wsUrl.replace(/\/$/, '');
  }

  /**
   * Connect to WebSocket
   */
  connect(sessionId: string, sessionToken: string): void {
    if (this.isConnecting || (this.ws && this.ws.readyState === WebSocket.OPEN)) {
      return;
    }

    this.sessionId = sessionId;
    this.sessionToken = sessionToken;
    this.isConnecting = true;

    try {
      const url = `${this.wsUrl}/widget/${sessionId}?token=${encodeURIComponent(sessionToken)}`;
      this.ws = new WebSocket(url);

      this.ws.onopen = () => {
        console.log('[ServiceDesk Widget] WebSocket connected');
        this.isConnecting = false;
        this.reconnectAttempts = 0;
        this.startPingInterval();
        this.emit('CONNECTED', {
          type: 'CONNECTED',
          timestamp: new Date().toISOString()
        });
      };

      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data) as WebSocketEvent;
          this.handleMessage(data);
        } catch (e) {
          console.warn('[ServiceDesk Widget] Failed to parse WebSocket message:', e);
        }
      };

      this.ws.onerror = (error) => {
        console.error('[ServiceDesk Widget] WebSocket error:', error);
        this.isConnecting = false;
        this.emit('ERROR', {
          type: 'ERROR',
          payload: 'WebSocket error',
          timestamp: new Date().toISOString()
        });
      };

      this.ws.onclose = (event) => {
        console.log('[ServiceDesk Widget] WebSocket closed:', event.code, event.reason);
        this.isConnecting = false;
        this.stopPingInterval();
        this.emit('DISCONNECTED', {
          type: 'DISCONNECTED',
          timestamp: new Date().toISOString()
        });
        
        // Attempt reconnect if not a clean close
        if (event.code !== 1000 && this.sessionId && this.sessionToken) {
          this.scheduleReconnect();
        }
      };
    } catch (e) {
      console.error('[ServiceDesk Widget] Failed to create WebSocket:', e);
      this.isConnecting = false;
      this.scheduleReconnect();
    }
  }

  /**
   * Disconnect WebSocket
   */
  disconnect(): void {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
    this.stopPingInterval();
    
    if (this.ws) {
      this.ws.close(1000, 'Client disconnect');
      this.ws = null;
    }
    
    this.sessionId = null;
    this.sessionToken = null;
    this.reconnectAttempts = 0;
    this.isConnecting = false;
  }

  /**
   * Send typing indicator
   */
  sendTyping(): void {
    this.send({
      type: 'VISITOR_TYPING',
      timestamp: new Date().toISOString()
    });
  }

  /**
   * Subscribe to event type
   */
  on(eventType: WebSocketEventType, handler: EventHandler): void {
    if (!this.handlers.has(eventType)) {
      this.handlers.set(eventType, new Set());
    }
    this.handlers.get(eventType)!.add(handler);
  }

  /**
   * Unsubscribe from event type
   */
  off(eventType: WebSocketEventType, handler: EventHandler): void {
    const handlers = this.handlers.get(eventType);
    if (handlers) {
      handlers.delete(handler);
    }
  }

  /**
   * Check if connected
   */
  isConnected(): boolean {
    return this.ws !== null && this.ws.readyState === WebSocket.OPEN;
  }

  /**
   * Handle incoming WebSocket message
   */
  private handleMessage(event: WebSocketEvent): void {
    console.log('[ServiceDesk Widget] WebSocket message:', event.type);
    
    switch (event.type) {
      case 'MESSAGE_RECEIVED':
        this.emit('MESSAGE_RECEIVED', event);
        break;
      case 'AGENT_TYPING':
        this.emit('AGENT_TYPING', event);
        break;
      case 'AGENT_JOINED':
        this.emit('AGENT_JOINED', event);
        break;
      case 'SESSION_CLOSED':
        this.emit('SESSION_CLOSED', event);
        break;
      default:
        console.log('[ServiceDesk Widget] Unknown event type:', event.type);
    }
  }

  /**
   * Emit event to handlers
   */
  private emit(eventType: WebSocketEventType, event: WebSocketEvent): void {
    const handlers = this.handlers.get(eventType);
    if (handlers) {
      handlers.forEach(handler => {
        try {
          handler(event);
        } catch (e) {
          console.error('[ServiceDesk Widget] Event handler error:', e);
        }
      });
    }
  }

  /**
   * Send message through WebSocket
   */
  private send(data: unknown): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data));
    }
  }

  /**
   * Schedule reconnection attempt
   */
  private scheduleReconnect(): void {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.log('[ServiceDesk Widget] Max reconnect attempts reached');
      return;
    }

    const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts);
    console.log(`[ServiceDesk Widget] Scheduling reconnect in ${delay}ms`);
    
    this.reconnectTimer = setTimeout(() => {
      this.reconnectAttempts++;
      if (this.sessionId && this.sessionToken) {
        this.connect(this.sessionId, this.sessionToken);
      }
    }, delay);
  }

  /**
   * Start ping interval to keep connection alive
   */
  private startPingInterval(): void {
    this.pingInterval = setInterval(() => {
      this.send({ type: 'PING', timestamp: new Date().toISOString() });
    }, 30000);
  }

  /**
   * Stop ping interval
   */
  private stopPingInterval(): void {
    if (this.pingInterval) {
      clearInterval(this.pingInterval);
      this.pingInterval = null;
    }
  }
}
