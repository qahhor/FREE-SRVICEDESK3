import { 
  WidgetConfig, 
  WidgetState, 
  WidgetMessage, 
  PreChatFormData,
  WebSocketEvent,
  AgentInfo,
  Translations
} from './types';
import { ApiService } from './services/ApiService';
import { WebSocketService } from './services/WebSocketService';
import { StorageService } from './services/StorageService';
import { LauncherButton } from './components/LauncherButton';
import { ChatWindow } from './components/ChatWindow';
import { createElement, injectStyles } from './utils/dom';
import { getTranslations } from './utils/formatters';

// Import styles
import './styles/widget.scss';

const DEFAULT_CONFIG: Partial<WidgetConfig> = {
  position: 'bottom-right',
  primaryColor: '#1976d2',
  greeting: 'Hi! How can we help you?',
  placeholder: 'Type your message...',
  offlineMessage: 'We are currently offline. Leave a message!',
  showAgentAvatar: true,
  soundEnabled: true,
  language: 'en',
  zIndex: 999999
};

const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
const ALLOWED_FILE_TYPES = [
  'image/*',
  'application/pdf',
  '.doc', '.docx',
  '.xls', '.xlsx',
  '.txt',
  '.zip', '.rar'
];

/**
 * Main Widget class
 */
export class Widget {
  private config: WidgetConfig;
  private state: WidgetState;
  private translations: Translations;
  private container: HTMLElement;
  private launcher: LauncherButton;
  private chatWindow: ChatWindow;
  private apiService: ApiService;
  private wsService: WebSocketService;
  private storageService: StorageService;
  private typingTimeout: ReturnType<typeof setTimeout> | null = null;
  private notificationSound: HTMLAudioElement | null = null;
  private isInitialized = false;

  constructor(config: WidgetConfig) {
    this.config = { ...DEFAULT_CONFIG, ...config } as WidgetConfig;
    this.translations = this.getTranslationsForConfig();
    
    this.state = {
      isOpen: false,
      isMinimized: false,
      isLoading: false,
      isConnected: false,
      isAgentTyping: false,
      unreadCount: 0,
      session: null,
      messages: [],
      agent: null,
      error: null
    };

    // Initialize services
    this.apiService = new ApiService(this.config.apiUrl);
    this.wsService = new WebSocketService(this.getWsUrl());
    this.storageService = new StorageService(this.config.projectKey);

    // Create UI components
    this.launcher = new LauncherButton({
      onClick: () => this.toggleWidget()
    });

    this.chatWindow = new ChatWindow({
      translations: this.translations,
      greeting: this.config.greeting || this.translations.greeting,
      showAgentAvatar: this.config.showAgentAvatar ?? true,
      maxFileSize: MAX_FILE_SIZE,
      allowedFileTypes: ALLOWED_FILE_TYPES,
      onClose: () => this.closeWidget(),
      onMinimize: () => this.handleMinimize(),
      onSendMessage: (msg) => this.sendMessage(msg),
      onSendAttachment: (file) => this.sendAttachment(file),
      onTyping: () => this.sendTypingIndicator(),
      onStartSession: (data) => this.startSession(data)
    });

    // Create container
    this.container = this.createContainer();

    // Set up WebSocket handlers
    this.setupWebSocketHandlers();
  }

  /**
   * Initialize the widget
   */
  async init(): Promise<void> {
    if (this.isInitialized) {
      return;
    }

    // Inject custom CSS variables
    this.injectCustomStyles();

    // Check for existing session
    const existingSession = this.storageService.getSession();
    if (existingSession && existingSession.status === 'ACTIVE') {
      try {
        // Verify session is still valid
        const session = await this.apiService.getSession(existingSession.id);
        if (session.status === 'ACTIVE') {
          this.state.session = session;
          this.apiService.setSessionToken(session.sessionToken);
          await this.loadMessages();
          this.connectWebSocket();
          this.chatWindow.setSession(session);
        } else {
          this.storageService.clearSession();
          this.chatWindow.showPreChatForm();
        }
      } catch (e) {
        console.warn('[ServiceDesk Widget] Failed to restore session:', e);
        this.storageService.clearSession();
        this.chatWindow.showPreChatForm();
      }
    } else {
      this.chatWindow.showPreChatForm();
    }

    // Restore unread count
    const unreadCount = this.storageService.getUnreadCount();
    if (unreadCount > 0) {
      this.state.unreadCount = unreadCount;
      this.launcher.setBadge(unreadCount);
    }

    // Add to DOM
    document.body.appendChild(this.container);
    
    this.isInitialized = true;
    console.log('[ServiceDesk Widget] Initialized');
  }

  /**
   * Open the widget
   */
  open(): void {
    this.state.isOpen = true;
    this.launcher.setOpen(true);
    this.chatWindow.show();
    
    // Clear unread count
    this.state.unreadCount = 0;
    this.launcher.setBadge(0);
    this.storageService.saveUnreadCount(0);
  }

  /**
   * Close the widget
   */
  close(): void {
    this.closeWidget();
  }

  /**
   * Destroy the widget
   */
  destroy(): void {
    this.wsService.disconnect();
    this.container.remove();
    this.isInitialized = false;
    console.log('[ServiceDesk Widget] Destroyed');
  }

  /**
   * Toggle widget open/closed
   */
  private toggleWidget(): void {
    if (this.state.isOpen) {
      this.closeWidget();
    } else {
      this.open();
    }
  }

  /**
   * Close the widget
   */
  private closeWidget(): void {
    this.state.isOpen = false;
    this.launcher.setOpen(false);
    this.chatWindow.hide();
  }

  /**
   * Handle minimize
   */
  private handleMinimize(): void {
    this.state.isMinimized = !this.state.isMinimized;
  }

  /**
   * Start a new session
   */
  private async startSession(data: PreChatFormData): Promise<void> {
    try {
      this.state.isLoading = true;
      
      const session = await this.apiService.startSession({
        projectKey: this.config.projectKey,
        visitorName: data.name,
        visitorEmail: data.email
      });

      this.state.session = session;
      this.storageService.saveSession(session);
      this.storageService.saveVisitorInfo(data.name, data.email);
      
      this.chatWindow.setSession(session);
      this.connectWebSocket();

      // Send initial message if provided
      if (data.message) {
        await this.sendMessage(data.message);
      }
    } catch (e) {
      console.error('[ServiceDesk Widget] Failed to start session:', e);
      this.state.error = 'Failed to start session';
    } finally {
      this.state.isLoading = false;
    }
  }

  /**
   * Load message history
   */
  private async loadMessages(): Promise<void> {
    if (!this.state.session) return;

    try {
      const messages = await this.apiService.getMessages(this.state.session.id);
      this.state.messages = messages;
      this.chatWindow.setMessages(messages);
    } catch (e) {
      console.error('[ServiceDesk Widget] Failed to load messages:', e);
    }
  }

  /**
   * Send a message
   */
  private async sendMessage(content: string): Promise<void> {
    if (!this.state.session || !content.trim()) return;

    this.chatWindow.setSending(true);

    try {
      const message = await this.apiService.sendMessage(this.state.session.id, {
        content: content.trim(),
        messageType: 'TEXT'
      });

      this.state.messages.push(message);
      this.chatWindow.addMessage(message);
      this.chatWindow.clearInput();
    } catch (e) {
      console.error('[ServiceDesk Widget] Failed to send message:', e);
    } finally {
      this.chatWindow.setSending(false);
    }
  }

  /**
   * Send file attachment
   */
  private async sendAttachment(file: File): Promise<void> {
    if (!this.state.session) return;

    this.chatWindow.setSending(true);

    try {
      const attachment = await this.apiService.uploadAttachment(this.state.session.id, file);
      
      // Send message with attachment reference
      const message = await this.apiService.sendMessage(this.state.session.id, {
        content: `Attached file: ${attachment.filename}`,
        messageType: file.type.startsWith('image/') ? 'IMAGE' : 'FILE',
        attachmentId: attachment.id
      });

      this.state.messages.push(message);
      this.chatWindow.addMessage(message);
    } catch (e) {
      console.error('[ServiceDesk Widget] Failed to send attachment:', e);
    } finally {
      this.chatWindow.setSending(false);
    }
  }

  /**
   * Send typing indicator
   */
  private sendTypingIndicator(): void {
    if (this.wsService.isConnected()) {
      this.wsService.sendTyping();
    }
  }

  /**
   * Connect to WebSocket
   */
  private connectWebSocket(): void {
    if (!this.state.session) return;
    
    this.wsService.connect(this.state.session.id, this.state.session.sessionToken);
  }

  /**
   * Set up WebSocket event handlers
   */
  private setupWebSocketHandlers(): void {
    this.wsService.on('CONNECTED', () => {
      this.state.isConnected = true;
      this.chatWindow.setConnected(true);
    });

    this.wsService.on('DISCONNECTED', () => {
      this.state.isConnected = false;
      this.chatWindow.setConnected(false);
    });

    this.wsService.on('MESSAGE_RECEIVED', (event: WebSocketEvent) => {
      const message = event.payload as WidgetMessage;
      
      // Add message if not already present
      if (!this.state.messages.find(m => m.id === message.id)) {
        this.state.messages.push(message);
        this.chatWindow.addMessage(message);
        
        // Hide typing indicator
        this.chatWindow.hideTyping();
        
        // Update unread count if widget is closed
        if (!this.state.isOpen) {
          this.state.unreadCount++;
          this.launcher.setBadge(this.state.unreadCount);
          this.storageService.saveUnreadCount(this.state.unreadCount);
          this.launcher.animate();
          this.playNotificationSound();
        }
      }
    });

    this.wsService.on('AGENT_TYPING', () => {
      this.state.isAgentTyping = true;
      this.chatWindow.showTyping();
      
      // Auto-hide after 3 seconds
      if (this.typingTimeout) {
        clearTimeout(this.typingTimeout);
      }
      this.typingTimeout = setTimeout(() => {
        this.state.isAgentTyping = false;
        this.chatWindow.hideTyping();
      }, 3000);
    });

    this.wsService.on('AGENT_JOINED', (event: WebSocketEvent) => {
      const agent = event.payload as AgentInfo;
      this.state.agent = agent;
      this.chatWindow.setAgent(agent);
    });

    this.wsService.on('SESSION_CLOSED', () => {
      this.state.session = null;
      this.storageService.clearSession();
      this.wsService.disconnect();
      
      // Add system message
      const systemMessage: WidgetMessage = {
        id: 'system-' + Date.now(),
        sessionId: '',
        senderType: 'SYSTEM',
        content: this.translations.sessionClosed,
        messageType: 'TEXT',
        createdAt: new Date().toISOString()
      };
      this.chatWindow.addMessage(systemMessage);
    });

    this.wsService.on('ERROR', () => {
      this.chatWindow.setReconnecting();
    });
  }

  /**
   * Play notification sound
   */
  private playNotificationSound(): void {
    if (!this.config.soundEnabled) return;

    if (!this.notificationSound) {
      // Create simple beep sound using Web Audio API
      try {
        const audioContext = new (window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext)();
        const oscillator = audioContext.createOscillator();
        const gainNode = audioContext.createGain();
        
        oscillator.connect(gainNode);
        gainNode.connect(audioContext.destination);
        
        oscillator.frequency.value = 800;
        oscillator.type = 'sine';
        gainNode.gain.setValueAtTime(0.1, audioContext.currentTime);
        gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.3);
        
        oscillator.start(audioContext.currentTime);
        oscillator.stop(audioContext.currentTime + 0.3);
      } catch (e) {
        // Silently fail if audio is not supported
      }
    }
  }

  /**
   * Get translations based on config
   */
  private getTranslationsForConfig(): Translations {
    const translations = getTranslations(this.config.language || 'en');
    
    // Override with custom values from config
    if (this.config.greeting) {
      translations.greeting = this.config.greeting;
    }
    if (this.config.placeholder) {
      translations.placeholder = this.config.placeholder;
    }
    if (this.config.offlineMessage) {
      translations.offlineMessage = this.config.offlineMessage;
    }
    
    return translations;
  }

  /**
   * Get WebSocket URL
   */
  private getWsUrl(): string {
    if (this.config.wsUrl) {
      return this.config.wsUrl;
    }
    
    // Derive from API URL
    const apiUrl = new URL(this.config.apiUrl);
    const wsProtocol = apiUrl.protocol === 'https:' ? 'wss:' : 'ws:';
    return `${wsProtocol}//${apiUrl.host}/ws`;
  }

  /**
   * Create main container
   */
  private createContainer(): HTMLElement {
    const container = createElement('div', {
      className: `sd-widget-container position-${this.config.position || 'bottom-right'}`
    });
    
    container.appendChild(this.chatWindow.getElement());
    container.appendChild(this.launcher.getElement());
    
    return container;
  }

  /**
   * Inject custom CSS variables
   */
  private injectCustomStyles(): void {
    const styles = `
      :root {
        --sd-primary-color: ${this.config.primaryColor || '#1976d2'};
        --sd-z-index: ${this.config.zIndex || 999999};
      }
    `;
    injectStyles(styles, 'sd-widget-custom-styles');
  }
}
