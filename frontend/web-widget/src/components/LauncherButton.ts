import { createElement, createSvgIcon, IconPaths } from '../utils/dom';

export interface LauncherButtonOptions {
  onClick: () => void;
}

/**
 * Floating launcher button component
 */
export class LauncherButton {
  private element: HTMLButtonElement;
  private badgeElement: HTMLElement;
  private isOpen = false;

  constructor(options: LauncherButtonOptions) {
    this.element = this.render(options);
    this.badgeElement = this.element.querySelector('.sd-widget-badge') as HTMLElement;
  }

  /**
   * Get the DOM element
   */
  getElement(): HTMLButtonElement {
    return this.element;
  }

  /**
   * Set open/closed state
   */
  setOpen(open: boolean): void {
    this.isOpen = open;
    this.element.classList.toggle('is-open', open);
    
    // Clear badge when opening
    if (open) {
      this.setBadge(0);
    }
  }

  /**
   * Set unread badge count
   */
  setBadge(count: number): void {
    if (this.badgeElement) {
      this.badgeElement.textContent = count > 0 ? (count > 99 ? '99+' : count.toString()) : '';
      this.badgeElement.style.display = count > 0 ? 'flex' : 'none';
    }
  }

  /**
   * Show the launcher
   */
  show(): void {
    this.element.style.display = 'flex';
  }

  /**
   * Hide the launcher
   */
  hide(): void {
    this.element.style.display = 'none';
  }

  /**
   * Animate the launcher
   */
  animate(): void {
    this.element.classList.add('sd-animate-bounce');
    setTimeout(() => {
      this.element.classList.remove('sd-animate-bounce');
    }, 500);
  }

  /**
   * Render the launcher button
   */
  private render(options: LauncherButtonOptions): HTMLButtonElement {
    const button = createElement('button', {
      className: 'sd-widget-launcher',
      'aria-label': 'Open chat',
      type: 'button'
    }) as HTMLButtonElement;

    // Chat icon
    button.appendChild(createSvgIcon(IconPaths.chat, 28));
    
    // Badge
    const badge = createElement('span', { className: 'sd-widget-badge' });
    badge.style.display = 'none';
    button.appendChild(badge);
    
    button.addEventListener('click', options.onClick);

    return button;
  }
}
