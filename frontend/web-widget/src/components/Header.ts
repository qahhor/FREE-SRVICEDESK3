import { createElement, createSvgIcon, IconPaths } from '../utils/dom';
import { Translations, AgentInfo } from '../types';

export interface HeaderOptions {
  title: string;
  translations: Translations;
  onMinimize: () => void;
  onClose: () => void;
}

/**
 * Widget header component
 */
export class Header {
  private element: HTMLElement;
  private statusElement: HTMLElement;
  private titleElement: HTMLElement;
  private avatarElement: HTMLElement;
  private translations: Translations;
  private isMinimized = false;

  constructor(options: HeaderOptions) {
    this.translations = options.translations;
    this.element = this.render(options);
    this.statusElement = this.element.querySelector('.sd-widget-header-status') as HTMLElement;
    this.titleElement = this.element.querySelector('.sd-widget-header-title') as HTMLElement;
    this.avatarElement = this.element.querySelector('.sd-widget-header-avatar') as HTMLElement;
  }

  /**
   * Get the DOM element
   */
  getElement(): HTMLElement {
    return this.element;
  }

  /**
   * Update connection status
   */
  setConnected(connected: boolean): void {
    if (this.statusElement) {
      this.statusElement.textContent = connected 
        ? this.translations.connected 
        : this.translations.disconnected;
      this.statusElement.classList.toggle('is-offline', !connected);
    }
  }

  /**
   * Set reconnecting status
   */
  setReconnecting(): void {
    if (this.statusElement) {
      this.statusElement.textContent = this.translations.reconnecting;
      this.statusElement.classList.add('is-offline');
    }
  }

  /**
   * Update agent info
   */
  setAgent(agent: AgentInfo): void {
    if (this.titleElement) {
      this.titleElement.textContent = agent.name;
    }
    if (this.avatarElement && agent.avatar) {
      this.avatarElement.innerHTML = '';
      const img = createElement('img', {
        src: agent.avatar,
        alt: agent.name
      });
      this.avatarElement.appendChild(img);
    }
  }

  /**
   * Update minimize button state
   */
  setMinimized(minimized: boolean): void {
    this.isMinimized = minimized;
    const minimizeBtn = this.element.querySelector('.sd-widget-minimize-btn');
    if (minimizeBtn) {
      minimizeBtn.setAttribute('aria-label', minimized ? 'Maximize' : this.translations.minimize);
    }
  }

  /**
   * Render the header
   */
  private render(options: HeaderOptions): HTMLElement {
    const header = createElement('div', { className: 'sd-widget-header' });

    // Info section
    const info = createElement('div', { className: 'sd-widget-header-info' });
    
    // Avatar
    const avatar = createElement('div', { className: 'sd-widget-header-avatar' });
    avatar.appendChild(createSvgIcon(IconPaths.user, 18));
    info.appendChild(avatar);
    
    // Text
    const text = createElement('div', { className: 'sd-widget-header-text' });
    const title = createElement('div', { className: 'sd-widget-header-title' }, [options.title]);
    const status = createElement('div', { className: 'sd-widget-header-status' }, [
      options.translations.connecting
    ]);
    text.appendChild(title);
    text.appendChild(status);
    info.appendChild(text);
    
    header.appendChild(info);

    // Actions
    const actions = createElement('div', { className: 'sd-widget-header-actions' });
    
    // Minimize button
    const minimizeBtn = createElement('button', {
      className: 'sd-widget-header-btn sd-widget-minimize-btn',
      'aria-label': options.translations.minimize,
      type: 'button'
    });
    minimizeBtn.appendChild(createSvgIcon(IconPaths.minimize, 18));
    minimizeBtn.addEventListener('click', options.onMinimize);
    actions.appendChild(minimizeBtn);
    
    // Close button
    const closeBtn = createElement('button', {
      className: 'sd-widget-header-btn sd-widget-close-btn',
      'aria-label': options.translations.close,
      type: 'button'
    });
    closeBtn.appendChild(createSvgIcon(IconPaths.close, 18));
    closeBtn.addEventListener('click', options.onClose);
    actions.appendChild(closeBtn);
    
    header.appendChild(actions);

    return header;
  }
}
