import { WidgetSession } from '../types';

const STORAGE_PREFIX = 'servicedesk_widget_';

/**
 * Storage service for widget session persistence
 */
export class StorageService {
  private prefix: string;

  constructor(projectKey: string) {
    this.prefix = `${STORAGE_PREFIX}${projectKey}_`;
  }

  /**
   * Save session to local storage
   */
  saveSession(session: WidgetSession): void {
    try {
      const data = JSON.stringify(session);
      localStorage.setItem(this.getKey('session'), data);
    } catch (e) {
      console.warn('[ServiceDesk Widget] Failed to save session to localStorage:', e);
    }
  }

  /**
   * Get session from local storage
   */
  getSession(): WidgetSession | null {
    try {
      const data = localStorage.getItem(this.getKey('session'));
      if (data) {
        return JSON.parse(data) as WidgetSession;
      }
    } catch (e) {
      console.warn('[ServiceDesk Widget] Failed to get session from localStorage:', e);
    }
    return null;
  }

  /**
   * Clear session from local storage
   */
  clearSession(): void {
    try {
      localStorage.removeItem(this.getKey('session'));
    } catch (e) {
      console.warn('[ServiceDesk Widget] Failed to clear session from localStorage:', e);
    }
  }

  /**
   * Save session token
   */
  saveSessionToken(token: string): void {
    try {
      localStorage.setItem(this.getKey('token'), token);
    } catch (e) {
      console.warn('[ServiceDesk Widget] Failed to save token to localStorage:', e);
    }
  }

  /**
   * Get session token
   */
  getSessionToken(): string | null {
    try {
      return localStorage.getItem(this.getKey('token'));
    } catch (e) {
      console.warn('[ServiceDesk Widget] Failed to get token from localStorage:', e);
    }
    return null;
  }

  /**
   * Clear session token
   */
  clearSessionToken(): void {
    try {
      localStorage.removeItem(this.getKey('token'));
    } catch (e) {
      console.warn('[ServiceDesk Widget] Failed to clear token from localStorage:', e);
    }
  }

  /**
   * Save visitor info
   */
  saveVisitorInfo(name: string, email: string): void {
    try {
      const data = JSON.stringify({ name, email });
      localStorage.setItem(this.getKey('visitor'), data);
    } catch (e) {
      console.warn('[ServiceDesk Widget] Failed to save visitor info to localStorage:', e);
    }
  }

  /**
   * Get visitor info
   */
  getVisitorInfo(): { name: string; email: string } | null {
    try {
      const data = localStorage.getItem(this.getKey('visitor'));
      if (data) {
        return JSON.parse(data);
      }
    } catch (e) {
      console.warn('[ServiceDesk Widget] Failed to get visitor info from localStorage:', e);
    }
    return null;
  }

  /**
   * Save unread count
   */
  saveUnreadCount(count: number): void {
    try {
      localStorage.setItem(this.getKey('unread'), count.toString());
    } catch (e) {
      console.warn('[ServiceDesk Widget] Failed to save unread count to localStorage:', e);
    }
  }

  /**
   * Get unread count
   */
  getUnreadCount(): number {
    try {
      const data = localStorage.getItem(this.getKey('unread'));
      if (data) {
        return parseInt(data, 10) || 0;
      }
    } catch (e) {
      console.warn('[ServiceDesk Widget] Failed to get unread count from localStorage:', e);
    }
    return 0;
  }

  /**
   * Clear all widget data
   */
  clearAll(): void {
    try {
      const keys = Object.keys(localStorage).filter(key => key.startsWith(this.prefix));
      keys.forEach(key => localStorage.removeItem(key));
    } catch (e) {
      console.warn('[ServiceDesk Widget] Failed to clear all widget data from localStorage:', e);
    }
  }

  /**
   * Get storage key with prefix
   */
  private getKey(key: string): string {
    return `${this.prefix}${key}`;
  }
}
