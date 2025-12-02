import { createElement, createSvgIcon, IconPaths, scrollToBottom } from '../utils/dom';
import { formatTime, formatDate } from '../utils/formatters';
import { WidgetMessage, Translations } from '../types';

export interface MessageListOptions {
  translations: Translations;
  showAgentAvatar: boolean;
}

/**
 * Message list component
 */
export class MessageList {
  private element: HTMLElement;
  private translations: Translations;
  private showAgentAvatar: boolean;
  private messages: WidgetMessage[] = [];
  private lastDateShown: string | null = null;

  constructor(options: MessageListOptions) {
    this.translations = options.translations;
    this.showAgentAvatar = options.showAgentAvatar;
    this.element = this.render();
  }

  /**
   * Get the DOM element
   */
  getElement(): HTMLElement {
    return this.element;
  }

  /**
   * Set messages and re-render
   */
  setMessages(messages: WidgetMessage[]): void {
    this.messages = messages;
    this.lastDateShown = null;
    this.element.innerHTML = '';
    messages.forEach(msg => this.renderMessage(msg));
    this.scrollToBottom();
  }

  /**
   * Add a new message
   */
  addMessage(message: WidgetMessage): void {
    this.messages.push(message);
    this.renderMessage(message);
    this.scrollToBottom();
  }

  /**
   * Show typing indicator
   */
  showTyping(): void {
    // Remove existing typing indicator
    this.hideTyping();
    
    const typing = createElement('div', { className: 'sd-widget-typing', 'data-typing': 'true' });
    
    const dots = createElement('div', { className: 'sd-widget-typing-dots' });
    dots.appendChild(createElement('span'));
    dots.appendChild(createElement('span'));
    dots.appendChild(createElement('span'));
    typing.appendChild(dots);
    
    const text = createElement('span', { className: 'sd-widget-typing-text' }, [
      this.translations.agentTyping
    ]);
    typing.appendChild(text);
    
    this.element.appendChild(typing);
    this.scrollToBottom();
  }

  /**
   * Hide typing indicator
   */
  hideTyping(): void {
    const typing = this.element.querySelector('[data-typing="true"]');
    if (typing) {
      typing.remove();
    }
  }

  /**
   * Update message read status
   */
  updateMessageStatus(messageId: string, _readAt: string): void {
    const messageEl = this.element.querySelector(`[data-message-id="${messageId}"]`);
    if (messageEl) {
      const statusEl = messageEl.querySelector('.sd-widget-message-status');
      if (statusEl) {
        statusEl.innerHTML = '';
        statusEl.appendChild(createSvgIcon(IconPaths.checkDouble, 12));
        statusEl.setAttribute('title', this.translations.read);
      }
    }
  }

  /**
   * Clear all messages
   */
  clear(): void {
    this.messages = [];
    this.lastDateShown = null;
    this.element.innerHTML = '';
  }

  /**
   * Scroll to bottom
   */
  scrollToBottom(): void {
    scrollToBottom(this.element);
  }

  /**
   * Render the message list container
   */
  private render(): HTMLElement {
    return createElement('div', { className: 'sd-widget-body' });
  }

  /**
   * Render a single message
   */
  private renderMessage(message: WidgetMessage): void {
    // Check if we need a date separator
    const messageDate = new Date(message.createdAt);
    const dateKey = messageDate.toDateString();
    
    if (this.lastDateShown !== dateKey) {
      this.lastDateShown = dateKey;
      this.renderDateSeparator(messageDate);
    }

    const isVisitor = message.senderType === 'VISITOR';
    const isSystem = message.senderType === 'SYSTEM';
    
    const messageWrapper = createElement('div', {
      className: `sd-widget-message ${isVisitor ? 'is-visitor' : ''} ${isSystem ? 'is-system' : 'is-agent'}`,
      'data-message-id': message.id
    });

    // Message content wrapper
    const contentWrapper = createElement('div', { className: 'sd-widget-message-wrapper' });

    // Agent avatar (only for agent messages if enabled)
    if (!isVisitor && !isSystem && this.showAgentAvatar) {
      const avatar = createElement('div', { className: 'sd-widget-message-avatar' });
      avatar.appendChild(createSvgIcon(IconPaths.user, 14));
      contentWrapper.appendChild(avatar);
    }

    // Message bubble
    const bubble = createElement('div', { className: 'sd-widget-message-bubble' });
    bubble.textContent = message.content;
    
    // Attachment
    if (message.attachmentId && message.attachmentUrl) {
      const attachment = this.renderAttachment(message);
      bubble.appendChild(attachment);
    }
    
    contentWrapper.appendChild(bubble);
    messageWrapper.appendChild(contentWrapper);

    // Time and status
    const timeWrapper = createElement('div', { className: 'sd-widget-message-time' });
    timeWrapper.appendChild(document.createTextNode(formatTime(message.createdAt)));
    
    // Read status (only for visitor messages)
    if (isVisitor) {
      const status = createElement('span', { className: 'sd-widget-message-status' });
      if (message.readAt) {
        status.appendChild(createSvgIcon(IconPaths.checkDouble, 12));
        status.setAttribute('title', this.translations.read);
      } else {
        status.appendChild(createSvgIcon(IconPaths.check, 12));
        status.setAttribute('title', this.translations.sent);
      }
      timeWrapper.appendChild(status);
    }
    
    messageWrapper.appendChild(timeWrapper);
    this.element.appendChild(messageWrapper);
  }

  /**
   * Render date separator
   */
  private renderDateSeparator(date: Date): void {
    const separator = createElement('div', { className: 'sd-widget-date-separator' }, [
      formatDate(date, this.translations)
    ]);
    this.element.appendChild(separator);
  }

  /**
   * Render attachment link
   */
  private renderAttachment(message: WidgetMessage): HTMLElement {
    const link = createElement('a', {
      className: 'sd-widget-message-attachment',
      href: message.attachmentUrl || '#',
      target: '_blank',
      rel: 'noopener noreferrer'
    });
    
    link.appendChild(createSvgIcon(IconPaths.attach, 20));
    
    const name = createElement('span', { className: 'sd-widget-message-attachment-name' }, [
      message.attachmentName || 'Attachment'
    ]);
    link.appendChild(name);
    
    return link;
  }
}
