import { Component, OnInit, OnDestroy, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatBadgeModule } from '@angular/material/badge';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Subscription } from 'rxjs';

import { NotificationService, Notification } from '../../../core/services/notification.service';
import { WebSocketService } from '../../../core/services/websocket.service';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    MatButtonModule,
    MatBadgeModule,
    MatMenuModule,
    MatDividerModule,
    MatTooltipModule
  ],
  template: `
    <button mat-icon-button 
            [matMenuTriggerFor]="notificationMenu" 
            [matBadge]="unreadCount()" 
            [matBadgeHidden]="unreadCount() === 0"
            matBadgeColor="warn"
            matBadgeSize="small"
            matTooltip="Notifications">
      <mat-icon>notifications</mat-icon>
    </button>

    <mat-menu #notificationMenu="matMenu" class="notification-menu">
      <div class="notification-header">
        <span class="notification-title">Notifications</span>
        @if (unreadCount() > 0) {
          <button mat-button color="primary" (click)="markAllRead($event)">
            Mark all read
          </button>
        }
      </div>
      <mat-divider></mat-divider>
      
      @if (notifications().length === 0) {
        <div class="notification-empty">
          <mat-icon>notifications_none</mat-icon>
          <p>No notifications</p>
        </div>
      } @else {
        <div class="notification-list">
          @for (notification of notifications(); track notification.id) {
            <div class="notification-item" 
                 [class.unread]="!notification.read"
                 (click)="handleNotificationClick(notification, $event)">
              <div class="notification-icon">
                <mat-icon [color]="getIconColor(notification.category)">
                  {{ getCategoryIcon(notification.category) }}
                </mat-icon>
              </div>
              <div class="notification-content">
                <div class="notification-title-text">{{ notification.title }}</div>
                <div class="notification-message">{{ notification.message }}</div>
                <div class="notification-time">{{ formatTime(notification.createdAt) }}</div>
              </div>
              @if (!notification.read) {
                <div class="unread-indicator"></div>
              }
            </div>
          }
        </div>
        <mat-divider></mat-divider>
        <div class="notification-footer">
          <button mat-button color="primary" routerLink="/notifications">
            View all notifications
          </button>
        </div>
      }
    </mat-menu>
  `,
  styles: [`
    :host {
      display: inline-block;
    }

    ::ng-deep .notification-menu {
      max-width: 400px;
      min-width: 320px;
    }

    .notification-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
    }

    .notification-title {
      font-weight: 500;
      font-size: 16px;
    }

    .notification-empty {
      padding: 32px 16px;
      text-align: center;
      color: #666;
    }

    .notification-empty mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      opacity: 0.5;
    }

    .notification-empty p {
      margin-top: 8px;
      margin-bottom: 0;
    }

    .notification-list {
      max-height: 400px;
      overflow-y: auto;
    }

    .notification-item {
      display: flex;
      padding: 12px 16px;
      cursor: pointer;
      position: relative;
    }

    .notification-item:hover {
      background-color: #f5f5f5;
    }

    .notification-item.unread {
      background-color: #e3f2fd;
    }

    .notification-item.unread:hover {
      background-color: #bbdefb;
    }

    .notification-icon {
      margin-right: 12px;
      display: flex;
      align-items: flex-start;
      padding-top: 2px;
    }

    .notification-content {
      flex: 1;
      min-width: 0;
    }

    .notification-title-text {
      font-weight: 500;
      font-size: 14px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .notification-message {
      font-size: 13px;
      color: #666;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      margin-top: 2px;
    }

    .notification-time {
      font-size: 12px;
      color: #999;
      margin-top: 4px;
    }

    .unread-indicator {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background-color: #1976d2;
      margin-left: 8px;
      flex-shrink: 0;
      align-self: center;
    }

    .notification-footer {
      padding: 8px 16px;
      text-align: center;
    }
  `]
})
export class NotificationBellComponent implements OnInit, OnDestroy {
  private subscriptions = new Subscription();

  // Use signals from service
  readonly unreadCount = computed(() => this.notificationService.unreadCount());
  readonly notifications = computed(() => this.notificationService.notifications().slice(0, 5));

  constructor(
    private notificationService: NotificationService,
    private websocketService: WebSocketService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Load initial notification count
    this.notificationService.getUnreadCount().subscribe();
    
    // Load recent notifications
    this.notificationService.getNotifications(0, 5).subscribe();

    // Subscribe to WebSocket notification events
    this.subscriptions.add(
      this.websocketService.getNotificationEvents().subscribe(event => {
        if (event.type === 'NOTIFICATION_CREATED' && event.notification) {
          this.notificationService.addNotification(event.notification);
        }
      })
    );

    this.subscriptions.add(
      this.websocketService.getNotificationCountEvents().subscribe(count => {
        this.notificationService.updateUnreadCount(count);
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  handleNotificationClick(notification: Notification, event: Event): void {
    event.stopPropagation();
    
    // Mark as read if unread
    if (!notification.read) {
      this.notificationService.markAsRead(notification.id).subscribe();
    }

    // Navigate to referenced item
    if (notification.referenceType && notification.referenceId) {
      switch (notification.referenceType) {
        case 'TICKET':
          this.router.navigate(['/tickets', notification.referenceId]);
          break;
        case 'COMMENT':
          // Navigate to ticket with comment
          if (notification.data && notification.data['ticketId']) {
            this.router.navigate(['/tickets', notification.data['ticketId']]);
          }
          break;
      }
    }
  }

  markAllRead(event: Event): void {
    event.stopPropagation();
    this.notificationService.markAllAsRead().subscribe();
  }

  getCategoryIcon(category: string): string {
    switch (category) {
      case 'TICKET':
        return 'confirmation_number';
      case 'COMMENT':
        return 'comment';
      case 'SLA':
        return 'schedule';
      case 'SYSTEM':
        return 'settings';
      default:
        return 'notifications';
    }
  }

  getIconColor(category: string): string {
    switch (category) {
      case 'SLA':
        return 'warn';
      default:
        return 'primary';
    }
  }

  formatTime(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) {
      return 'Just now';
    } else if (minutes < 60) {
      return `${minutes}m ago`;
    } else if (hours < 24) {
      return `${hours}h ago`;
    } else if (days < 7) {
      return `${days}d ago`;
    } else {
      return date.toLocaleDateString();
    }
  }
}
