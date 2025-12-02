import {
  ApiResponse,
  WidgetSession,
  WidgetMessage,
  SessionStartRequest,
  SendMessageRequest,
  AttachmentResult
} from '../types';

/**
 * API service for widget REST calls
 */
export class ApiService {
  private baseUrl: string;
  private sessionToken: string | null = null;

  constructor(apiUrl: string) {
    // Remove trailing slash
    this.baseUrl = apiUrl.replace(/\/$/, '');
  }

  /**
   * Set session token for authenticated requests
   */
  setSessionToken(token: string): void {
    this.sessionToken = token;
  }

  /**
   * Clear session token
   */
  clearSessionToken(): void {
    this.sessionToken = null;
  }

  /**
   * Start a new widget session
   */
  async startSession(request: SessionStartRequest): Promise<WidgetSession> {
    const response = await this.post<WidgetSession>('/widget/sessions', request);
    if (response.success && response.data) {
      this.sessionToken = response.data.sessionToken;
      return response.data;
    }
    throw new Error(response.error || 'Failed to start session');
  }

  /**
   * Get session info
   */
  async getSession(sessionId: string): Promise<WidgetSession> {
    const response = await this.get<WidgetSession>(`/widget/sessions/${sessionId}`);
    if (response.success && response.data) {
      return response.data;
    }
    throw new Error(response.error || 'Failed to get session');
  }

  /**
   * Get message history for session
   */
  async getMessages(sessionId: string): Promise<WidgetMessage[]> {
    const response = await this.get<WidgetMessage[]>(`/widget/sessions/${sessionId}/messages`);
    if (response.success && response.data) {
      return response.data;
    }
    throw new Error(response.error || 'Failed to get messages');
  }

  /**
   * Send a message
   */
  async sendMessage(sessionId: string, request: SendMessageRequest): Promise<WidgetMessage> {
    const response = await this.post<WidgetMessage>(
      `/widget/sessions/${sessionId}/messages`,
      request
    );
    if (response.success && response.data) {
      return response.data;
    }
    throw new Error(response.error || 'Failed to send message');
  }

  /**
   * Upload file attachment
   */
  async uploadAttachment(sessionId: string, file: File): Promise<AttachmentResult> {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await this.postFormData<AttachmentResult>(
      `/widget/sessions/${sessionId}/attachments`,
      formData
    );
    if (response.success && response.data) {
      return response.data;
    }
    throw new Error(response.error || 'Failed to upload attachment');
  }

  /**
   * Close session
   */
  async closeSession(sessionId: string): Promise<void> {
    const response = await this.post<void>(`/widget/sessions/${sessionId}/close`, {});
    if (!response.success) {
      throw new Error(response.error || 'Failed to close session');
    }
  }

  /**
   * Get widget configuration
   */
  async getConfig(projectKey: string): Promise<Record<string, unknown>> {
    const response = await this.get<Record<string, unknown>>(`/widget/config?projectKey=${projectKey}`);
    if (response.success && response.data) {
      return response.data;
    }
    throw new Error(response.error || 'Failed to get widget config');
  }

  /**
   * Make GET request
   */
  private async get<T>(endpoint: string): Promise<ApiResponse<T>> {
    return this.request<T>('GET', endpoint);
  }

  /**
   * Make POST request with JSON body
   */
  private async post<T>(endpoint: string, body: unknown): Promise<ApiResponse<T>> {
    return this.request<T>('POST', endpoint, body);
  }

  /**
   * Make POST request with FormData
   */
  private async postFormData<T>(endpoint: string, formData: FormData): Promise<ApiResponse<T>> {
    try {
      const headers: Record<string, string> = {};
      
      if (this.sessionToken) {
        headers['X-Widget-Session'] = this.sessionToken;
      }

      const response = await fetch(`${this.baseUrl}${endpoint}`, {
        method: 'POST',
        headers,
        body: formData
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`HTTP error ${response.status}: ${errorText}`);
      }

      return await response.json();
    } catch (error) {
      console.error('[ServiceDesk Widget] API error:', error);
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error',
        timestamp: new Date().toISOString()
      };
    }
  }

  /**
   * Make HTTP request
   */
  private async request<T>(
    method: string,
    endpoint: string,
    body?: unknown
  ): Promise<ApiResponse<T>> {
    try {
      const headers: Record<string, string> = {
        'Content-Type': 'application/json'
      };

      if (this.sessionToken) {
        headers['X-Widget-Session'] = this.sessionToken;
      }

      const options: RequestInit = {
        method,
        headers,
        credentials: 'include'
      };

      if (body) {
        options.body = JSON.stringify(body);
      }

      const response = await fetch(`${this.baseUrl}${endpoint}`, options);

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`HTTP error ${response.status}: ${errorText}`);
      }

      return await response.json();
    } catch (error) {
      console.error('[ServiceDesk Widget] API error:', error);
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Unknown error',
        timestamp: new Date().toISOString()
      };
    }
  }
}
