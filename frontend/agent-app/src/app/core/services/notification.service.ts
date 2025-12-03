import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface Notification {
  id: string;
  userId: string;
  type: 'EMAIL' | 'IN_APP' | 'PUSH';
  category: 'TICKET' | 'COMMENT' | 'SLA' | 'SYSTEM';
  title: string;
  message: string;
  data?: Record<string, any>;
  referenceType?: string;
  referenceId?: string;
  readAt?: string;
  sentAt?: string;
  createdAt: string;
  read: boolean;
}

export interface NotificationPreferences {
  id: string;
  userId: string;
  emailEnabled: boolean;
  inAppEnabled: boolean;
  pushEnabled: boolean;
  emailSettings: Record<string, boolean>;
  inAppSettings: Record<string, boolean>;
  quietHoursEnabled: boolean;
  quietHoursStart?: string;
  quietHoursEnd?: string;
  timezone: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  error?: string;
  timestamp: string;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = `${environment.apiUrl}/notifications`;
  
  // Reactive state using signals
  readonly unreadCount = signal<number>(0);
  readonly notifications = signal<Notification[]>([]);

  constructor(private http: HttpClient) {}

  /**
   * Get notifications with pagination
   */
  getNotifications(page: number = 0, size: number = 10): Observable<ApiResponse<PagedResponse<Notification>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<PagedResponse<Notification>>>(this.apiUrl, { params }).pipe(
      tap(response => {
        if (response.success && response.data) {
          this.notifications.set(response.data.content);
        }
      })
    );
  }

  /**
   * Get unread notifications
   */
  getUnreadNotifications(page: number = 0, size: number = 10): Observable<ApiResponse<PagedResponse<Notification>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<PagedResponse<Notification>>>(`${this.apiUrl}/unread`, { params });
  }

  /**
   * Get unread notification count
   */
  getUnreadCount(): Observable<ApiResponse<{ count: number }>> {
    return this.http.get<ApiResponse<{ count: number }>>(`${this.apiUrl}/unread-count`).pipe(
      tap(response => {
        if (response.success && response.data) {
          this.unreadCount.set(response.data.count);
        }
      })
    );
  }

  /**
   * Mark notification as read
   */
  markAsRead(notificationId: string): Observable<ApiResponse<Notification>> {
    return this.http.patch<ApiResponse<Notification>>(`${this.apiUrl}/${notificationId}/read`, {}).pipe(
      tap(response => {
        if (response.success) {
          // Update local state
          const current = this.notifications();
          const updated = current.map(n => 
            n.id === notificationId ? { ...n, read: true, readAt: new Date().toISOString() } : n
          );
          this.notifications.set(updated);
          this.unreadCount.update(count => Math.max(0, count - 1));
        }
      })
    );
  }

  /**
   * Mark all notifications as read
   */
  markAllAsRead(): Observable<ApiResponse<{ updated: number }>> {
    return this.http.patch<ApiResponse<{ updated: number }>>(`${this.apiUrl}/read-all`, {}).pipe(
      tap(response => {
        if (response.success) {
          // Update local state
          const current = this.notifications();
          const updated = current.map(n => ({ ...n, read: true, readAt: new Date().toISOString() }));
          this.notifications.set(updated);
          this.unreadCount.set(0);
        }
      })
    );
  }

  /**
   * Delete a notification
   */
  deleteNotification(notificationId: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${notificationId}`).pipe(
      tap(response => {
        if (response.success) {
          // Update local state
          const current = this.notifications();
          const notification = current.find(n => n.id === notificationId);
          const updated = current.filter(n => n.id !== notificationId);
          this.notifications.set(updated);
          if (notification && !notification.read) {
            this.unreadCount.update(count => Math.max(0, count - 1));
          }
        }
      })
    );
  }

  /**
   * Get notification preferences
   */
  getPreferences(): Observable<ApiResponse<NotificationPreferences>> {
    return this.http.get<ApiResponse<NotificationPreferences>>(`${this.apiUrl}/preferences`);
  }

  /**
   * Update notification preferences
   */
  updatePreferences(preferences: Partial<NotificationPreferences>): Observable<ApiResponse<NotificationPreferences>> {
    return this.http.put<ApiResponse<NotificationPreferences>>(`${this.apiUrl}/preferences`, preferences);
  }

  /**
   * Add a notification from WebSocket
   */
  addNotification(notification: Notification): void {
    const current = this.notifications();
    this.notifications.set([notification, ...current]);
    if (!notification.read) {
      this.unreadCount.update(count => count + 1);
    }
  }

  /**
   * Update unread count from WebSocket
   */
  updateUnreadCount(count: number): void {
    this.unreadCount.set(count);
  }
}
