import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { environment } from '../../../environments/environment';

export interface TicketEvent {
  type: 'CREATED' | 'UPDATED' | 'ASSIGNED' | 'STATUS_CHANGED' | 'COMMENTED' | 'DELETED';
  ticket: any;
  message: string;
  timestamp: string;
}

export interface NotificationEvent {
  type: 'NOTIFICATION_CREATED' | 'NOTIFICATION_READ' | 'NOTIFICATION_COUNT_UPDATE';
  notification: any;
  message: string;
  timestamp: string;
}

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private client: Client | null = null;
  private ticketEvents$ = new Subject<TicketEvent>();
  private notificationEvents$ = new Subject<NotificationEvent>();
  private notificationCountEvents$ = new Subject<number>();
  private connected = false;
  private userId: string | null = null;

  connect(userId?: string): void {
    if (this.connected) {
      return;
    }

    this.userId = userId || null;

    this.client = new Client({
      webSocketFactory: () => new SockJS(environment.wsUrl) as any,
      debug: (str) => {
        console.log('[WebSocket]', str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = () => {
      console.log('[WebSocket] Connected');
      this.connected = true;

      // Subscribe to ticket events (public topic)
      this.client?.subscribe('/topic/tickets', (message: IMessage) => {
        const event = JSON.parse(message.body) as TicketEvent;
        this.ticketEvents$.next(event);
      });

      // Subscribe to user-specific notification events
      if (this.userId) {
        this.client?.subscribe(`/user/${this.userId}/queue/notifications`, (message: IMessage) => {
          const event = JSON.parse(message.body) as NotificationEvent;
          this.notificationEvents$.next(event);
        });

        this.client?.subscribe(`/user/${this.userId}/queue/notifications/count`, (message: IMessage) => {
          const event = JSON.parse(message.body) as NotificationEvent;
          if (event.message) {
            this.notificationCountEvents$.next(parseInt(event.message, 10));
          }
        });
      }
    };

    this.client.onDisconnect = () => {
      console.log('[WebSocket] Disconnected');
      this.connected = false;
    };

    this.client.onStompError = (frame) => {
      console.error('[WebSocket] Error:', frame);
    };

    this.client.activate();
  }

  disconnect(): void {
    if (this.client) {
      this.client.deactivate();
      this.connected = false;
      this.userId = null;
    }
  }

  getTicketEvents(): Observable<TicketEvent> {
    return this.ticketEvents$.asObservable();
  }

  getNotificationEvents(): Observable<NotificationEvent> {
    return this.notificationEvents$.asObservable();
  }

  getNotificationCountEvents(): Observable<number> {
    return this.notificationCountEvents$.asObservable();
  }

  isConnected(): boolean {
    return this.connected;
  }
}
