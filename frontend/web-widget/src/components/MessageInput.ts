import { createElement, createSvgIcon, IconPaths, focusElement } from '../utils/dom';
import { debounce } from '../utils/formatters';
import { Translations } from '../types';

export interface MessageInputOptions {
  translations: Translations;
  onSend: (message: string) => void;
  onAttach: () => void;
  onTyping: () => void;
}

/**
 * Message input component
 */
export class MessageInput {
  private element: HTMLElement;
  private textareaElement: HTMLTextAreaElement;
  private sendButton: HTMLButtonElement;
  private attachButton: HTMLButtonElement;
  private onSend: (message: string) => void;
  private translations: Translations;

  constructor(options: MessageInputOptions) {
    this.onSend = options.onSend;
    this.translations = options.translations;
    this.element = this.render(options);
    this.textareaElement = this.element.querySelector('textarea') as HTMLTextAreaElement;
    this.sendButton = this.element.querySelector('.sd-widget-send-btn') as HTMLButtonElement;
    this.attachButton = this.element.querySelector('.sd-widget-attach-btn') as HTMLButtonElement;
    
    // Set up typing indicator with debounce
    const debouncedTyping = debounce(options.onTyping, 500);
    this.textareaElement.addEventListener('input', () => {
      this.autoResize();
      debouncedTyping();
    });
  }

  /**
   * Get the DOM element
   */
  getElement(): HTMLElement {
    return this.element;
  }

  /**
   * Focus the input
   */
  focus(): void {
    focusElement(this.textareaElement);
  }

  /**
   * Get current message text
   */
  getValue(): string {
    return this.textareaElement.value.trim();
  }

  /**
   * Clear the input
   */
  clear(): void {
    this.textareaElement.value = '';
    this.autoResize();
  }

  /**
   * Enable/disable the input
   */
  setEnabled(enabled: boolean): void {
    this.textareaElement.disabled = !enabled;
    this.sendButton.disabled = !enabled;
    this.attachButton.disabled = !enabled;
  }

  /**
   * Show sending state
   */
  setSending(sending: boolean): void {
    this.sendButton.disabled = sending;
    if (sending) {
      this.sendButton.innerHTML = '';
      const spinner = createElement('div', { className: 'sd-widget-spinner' });
      this.sendButton.appendChild(spinner);
    } else {
      this.sendButton.innerHTML = '';
      this.sendButton.appendChild(createSvgIcon(IconPaths.send, 20));
    }
  }

  /**
   * Auto-resize textarea based on content
   */
  private autoResize(): void {
    this.textareaElement.style.height = 'auto';
    this.textareaElement.style.height = Math.min(this.textareaElement.scrollHeight, 120) + 'px';
  }

  /**
   * Handle send action
   */
  private handleSend(): void {
    const message = this.getValue();
    if (message) {
      this.onSend(message);
    }
  }

  /**
   * Render the input component
   */
  private render(options: MessageInputOptions): HTMLElement {
    const input = createElement('div', { className: 'sd-widget-input' });

    // Attach button
    const attachBtn = createElement('button', {
      className: 'sd-widget-input-btn sd-widget-attach-btn',
      'aria-label': options.translations.attachFile,
      type: 'button'
    }) as HTMLButtonElement;
    attachBtn.appendChild(createSvgIcon(IconPaths.attach, 20));
    attachBtn.addEventListener('click', options.onAttach);
    input.appendChild(attachBtn);

    // Textarea
    const textarea = createElement('textarea', {
      className: 'sd-widget-input-textarea',
      placeholder: options.translations.placeholder,
      rows: '1'
    }) as HTMLTextAreaElement;
    
    textarea.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        this.handleSend();
      }
    });
    input.appendChild(textarea);

    // Send button
    const sendBtn = createElement('button', {
      className: 'sd-widget-input-btn sd-widget-send-btn is-primary',
      'aria-label': options.translations.sendButton,
      type: 'button'
    }) as HTMLButtonElement;
    sendBtn.appendChild(createSvgIcon(IconPaths.send, 20));
    sendBtn.addEventListener('click', () => this.handleSend());
    input.appendChild(sendBtn);

    return input;
  }
}
