import { createElement, addClass, removeClass, toggleClass } from '../utils/dom';
import { Translations, WidgetSession, WidgetMessage, AgentInfo, PreChatFormData } from '../types';
import { Header } from './Header';
import { MessageList } from './MessageList';
import { MessageInput } from './MessageInput';
import { FileUpload } from './FileUpload';
import { isValidEmail } from '../utils/formatters';

export interface ChatWindowOptions {
  translations: Translations;
  greeting: string;
  showAgentAvatar: boolean;
  maxFileSize: number;
  allowedFileTypes: string[];
  onClose: () => void;
  onMinimize: () => void;
  onSendMessage: (message: string) => void;
  onSendAttachment: (file: File) => void;
  onTyping: () => void;
  onStartSession: (data: PreChatFormData) => void;
}

/**
 * Main chat window component
 */
export class ChatWindow {
  private element: HTMLElement;
  private header: Header;
  private messageList: MessageList;
  private messageInput: MessageInput;
  private fileUpload: FileUpload;
  private preChatForm: HTMLElement | null = null;
  private options: ChatWindowOptions;
  private isVisible = false;
  private isMinimized = false;
  private hasSession = false;

  constructor(options: ChatWindowOptions) {
    this.options = options;
    
    // Create components
    this.header = new Header({
      title: 'Support',
      translations: options.translations,
      onMinimize: () => this.handleMinimize(),
      onClose: options.onClose
    });

    this.messageList = new MessageList({
      translations: options.translations,
      showAgentAvatar: options.showAgentAvatar
    });

    this.messageInput = new MessageInput({
      translations: options.translations,
      onSend: options.onSendMessage,
      onAttach: () => this.fileUpload.openFileDialog(),
      onTyping: options.onTyping
    });

    this.fileUpload = new FileUpload({
      translations: options.translations,
      maxFileSize: options.maxFileSize,
      allowedTypes: options.allowedFileTypes,
      onFileSelect: options.onSendAttachment,
      onError: (error) => console.error('[ServiceDesk Widget] File error:', error)
    });

    this.element = this.render();
    
    // Set up drop zone
    this.fileUpload.setupDropZone(this.element);
  }

  /**
   * Get the DOM element
   */
  getElement(): HTMLElement {
    return this.element;
  }

  /**
   * Show the chat window
   */
  show(): void {
    this.isVisible = true;
    addClass(this.element, 'is-visible');
    
    if (this.hasSession) {
      this.messageInput.focus();
      this.messageList.scrollToBottom();
    }
  }

  /**
   * Hide the chat window
   */
  hide(): void {
    this.isVisible = false;
    removeClass(this.element, 'is-visible');
  }

  /**
   * Check if visible
   */
  isOpen(): boolean {
    return this.isVisible;
  }

  /**
   * Set session and switch to chat view
   */
  setSession(session: WidgetSession): void {
    this.hasSession = true;
    this.showChatView();
    this.messageInput.setEnabled(true);
  }

  /**
   * Show pre-chat form
   */
  showPreChatForm(): void {
    this.hasSession = false;
    
    // Remove chat view elements if present
    const body = this.element.querySelector('.sd-widget-body');
    const input = this.element.querySelector('.sd-widget-input');
    if (body) body.remove();
    if (input) input.remove();
    
    // Create and show pre-chat form
    if (!this.preChatForm) {
      this.preChatForm = this.createPreChatForm();
    }
    this.element.appendChild(this.preChatForm);
  }

  /**
   * Show chat view (messages + input)
   */
  showChatView(): void {
    // Remove pre-chat form if present
    if (this.preChatForm && this.preChatForm.parentNode) {
      this.preChatForm.remove();
    }
    
    // Add chat view elements
    const existingBody = this.element.querySelector('.sd-widget-body');
    if (!existingBody) {
      this.element.appendChild(this.messageList.getElement());
      this.element.appendChild(this.messageInput.getElement());
      this.element.appendChild(this.fileUpload.getFileInput());
    }
    
    this.messageInput.focus();
  }

  /**
   * Set messages
   */
  setMessages(messages: WidgetMessage[]): void {
    this.messageList.setMessages(messages);
  }

  /**
   * Add a new message
   */
  addMessage(message: WidgetMessage): void {
    this.messageList.addMessage(message);
  }

  /**
   * Show typing indicator
   */
  showTyping(): void {
    this.messageList.showTyping();
  }

  /**
   * Hide typing indicator
   */
  hideTyping(): void {
    this.messageList.hideTyping();
  }

  /**
   * Update connection status
   */
  setConnected(connected: boolean): void {
    this.header.setConnected(connected);
  }

  /**
   * Set reconnecting status
   */
  setReconnecting(): void {
    this.header.setReconnecting();
  }

  /**
   * Update agent info
   */
  setAgent(agent: AgentInfo): void {
    this.header.setAgent(agent);
  }

  /**
   * Clear input field
   */
  clearInput(): void {
    this.messageInput.clear();
  }

  /**
   * Set sending state
   */
  setSending(sending: boolean): void {
    this.messageInput.setSending(sending);
  }

  /**
   * Update message status
   */
  updateMessageStatus(messageId: string, readAt: string): void {
    this.messageList.updateMessageStatus(messageId, readAt);
  }

  /**
   * Handle minimize toggle
   */
  private handleMinimize(): void {
    this.isMinimized = !this.isMinimized;
    toggleClass(this.element, 'is-minimized', this.isMinimized);
    this.header.setMinimized(this.isMinimized);
    this.options.onMinimize();
  }

  /**
   * Render the chat window
   */
  private render(): HTMLElement {
    const window = createElement('div', { className: 'sd-widget-window' });
    
    window.appendChild(this.header.getElement());
    window.appendChild(this.messageList.getElement());
    window.appendChild(this.messageInput.getElement());
    window.appendChild(this.fileUpload.getFileInput());
    
    return window;
  }

  /**
   * Create pre-chat form
   */
  private createPreChatForm(): HTMLElement {
    const form = createElement('form', { className: 'sd-widget-prechat' }) as HTMLFormElement;
    
    // Greeting
    const greeting = createElement('div', { className: 'sd-widget-prechat-greeting' }, [
      this.options.greeting
    ]);
    form.appendChild(greeting);

    // Name field
    const nameGroup = createElement('div', { className: 'sd-widget-form-group' });
    const nameLabel = createElement('label', { 
      className: 'sd-widget-form-label',
      'for': 'sd-widget-name'
    }, [this.options.translations.nameLabel]);
    const nameInput = createElement('input', {
      className: 'sd-widget-form-input',
      id: 'sd-widget-name',
      type: 'text',
      placeholder: this.options.translations.namePlaceholder,
      required: 'true'
    }) as HTMLInputElement;
    nameGroup.appendChild(nameLabel);
    nameGroup.appendChild(nameInput);
    form.appendChild(nameGroup);

    // Email field
    const emailGroup = createElement('div', { className: 'sd-widget-form-group' });
    const emailLabel = createElement('label', { 
      className: 'sd-widget-form-label',
      'for': 'sd-widget-email'
    }, [this.options.translations.emailLabel]);
    const emailInput = createElement('input', {
      className: 'sd-widget-form-input',
      id: 'sd-widget-email',
      type: 'email',
      placeholder: this.options.translations.emailPlaceholder,
      required: 'true'
    }) as HTMLInputElement;
    emailGroup.appendChild(emailLabel);
    emailGroup.appendChild(emailInput);
    form.appendChild(emailGroup);

    // Message field (optional)
    const messageGroup = createElement('div', { className: 'sd-widget-form-group' });
    const messageLabel = createElement('label', { 
      className: 'sd-widget-form-label',
      'for': 'sd-widget-message'
    }, [this.options.translations.messageLabel]);
    const messageInput = createElement('textarea', {
      className: 'sd-widget-form-input sd-widget-form-textarea',
      id: 'sd-widget-message',
      placeholder: this.options.translations.messagePlaceholder,
      rows: '3'
    }) as HTMLTextAreaElement;
    messageGroup.appendChild(messageLabel);
    messageGroup.appendChild(messageInput);
    form.appendChild(messageGroup);

    // Submit button
    const submitBtn = createElement('button', {
      className: 'sd-widget-form-submit',
      type: 'submit'
    }, [this.options.translations.submitForm]) as HTMLButtonElement;
    form.appendChild(submitBtn);

    // Form submission
    form.addEventListener('submit', (e) => {
      e.preventDefault();
      
      const name = nameInput.value.trim();
      const email = emailInput.value.trim();
      const message = messageInput.value.trim();

      if (!name || !email) {
        return;
      }

      if (!isValidEmail(email)) {
        emailInput.classList.add('sd-animate-shake');
        setTimeout(() => emailInput.classList.remove('sd-animate-shake'), 500);
        return;
      }

      submitBtn.disabled = true;
      submitBtn.textContent = this.options.translations.connecting;

      this.options.onStartSession({ name, email, message });
    });

    return form;
  }
}
