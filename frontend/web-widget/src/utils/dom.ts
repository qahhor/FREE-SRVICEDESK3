/**
 * DOM utilities for widget
 */

/**
 * Create an HTML element with optional attributes and children
 */
export function createElement<K extends keyof HTMLElementTagNameMap>(
  tag: K,
  attributes?: Record<string, string>,
  children?: (HTMLElement | string)[]
): HTMLElementTagNameMap[K] {
  const element = document.createElement(tag);
  
  if (attributes) {
    Object.entries(attributes).forEach(([key, value]) => {
      if (key === 'className') {
        element.className = value;
      } else if (key.startsWith('data-')) {
        element.setAttribute(key, value);
      } else {
        element.setAttribute(key, value);
      }
    });
  }
  
  if (children) {
    children.forEach(child => {
      if (typeof child === 'string') {
        element.appendChild(document.createTextNode(child));
      } else {
        element.appendChild(child);
      }
    });
  }
  
  return element;
}

/**
 * Add CSS class to element
 */
export function addClass(element: HTMLElement, className: string): void {
  element.classList.add(className);
}

/**
 * Remove CSS class from element
 */
export function removeClass(element: HTMLElement, className: string): void {
  element.classList.remove(className);
}

/**
 * Toggle CSS class on element
 */
export function toggleClass(element: HTMLElement, className: string, force?: boolean): void {
  element.classList.toggle(className, force);
}

/**
 * Check if element has CSS class
 */
export function hasClass(element: HTMLElement, className: string): boolean {
  return element.classList.contains(className);
}

/**
 * Set multiple styles on element
 */
export function setStyles(element: HTMLElement, styles: Partial<CSSStyleDeclaration>): void {
  Object.assign(element.style, styles);
}

/**
 * Query selector with type safety
 */
export function querySelector<T extends HTMLElement>(
  selector: string,
  parent: HTMLElement | Document = document
): T | null {
  return parent.querySelector<T>(selector);
}

/**
 * Query selector all with type safety
 */
export function querySelectorAll<T extends HTMLElement>(
  selector: string,
  parent: HTMLElement | Document = document
): NodeListOf<T> {
  return parent.querySelectorAll<T>(selector);
}

/**
 * Append element to parent
 */
export function appendTo(child: HTMLElement, parent: HTMLElement): void {
  parent.appendChild(child);
}

/**
 * Remove element from DOM
 */
export function removeElement(element: HTMLElement): void {
  element.parentNode?.removeChild(element);
}

/**
 * Set element inner HTML safely (escapes HTML entities)
 */
export function setTextContent(element: HTMLElement, text: string): void {
  element.textContent = text;
}

/**
 * Escape HTML entities to prevent XSS
 */
export function escapeHtml(text: string): string {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

/**
 * Create SVG icon element
 */
export function createSvgIcon(pathData: string, size: number = 24): SVGSVGElement {
  const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
  svg.setAttribute('width', size.toString());
  svg.setAttribute('height', size.toString());
  svg.setAttribute('viewBox', '0 0 24 24');
  svg.setAttribute('fill', 'none');
  svg.setAttribute('stroke', 'currentColor');
  svg.setAttribute('stroke-width', '2');
  svg.setAttribute('stroke-linecap', 'round');
  svg.setAttribute('stroke-linejoin', 'round');
  
  const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
  path.setAttribute('d', pathData);
  svg.appendChild(path);
  
  return svg;
}

/**
 * Common SVG icon paths
 */
export const IconPaths = {
  chat: 'M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z',
  close: 'M18 6L6 18M6 6l12 12',
  minimize: 'M20 12H4',
  send: 'M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z',
  attach: 'M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48',
  check: 'M20 6L9 17l-5-5',
  checkDouble: 'M18 7l-8.5 8.5-4-4M22 7l-8.5 8.5',
  user: 'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z'
};

/**
 * Scroll element to bottom
 */
export function scrollToBottom(element: HTMLElement, smooth: boolean = true): void {
  element.scrollTo({
    top: element.scrollHeight,
    behavior: smooth ? 'smooth' : 'auto'
  });
}

/**
 * Focus element
 */
export function focusElement(element: HTMLElement): void {
  element.focus();
}

/**
 * Check if element is visible in viewport
 */
export function isElementVisible(element: HTMLElement): boolean {
  const rect = element.getBoundingClientRect();
  return (
    rect.top >= 0 &&
    rect.left >= 0 &&
    rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&
    rect.right <= (window.innerWidth || document.documentElement.clientWidth)
  );
}

/**
 * Inject CSS styles into document head
 */
export function injectStyles(css: string, id?: string): HTMLStyleElement {
  const style = document.createElement('style');
  style.type = 'text/css';
  if (id) {
    style.id = id;
  }
  style.appendChild(document.createTextNode(css));
  document.head.appendChild(style);
  return style;
}
