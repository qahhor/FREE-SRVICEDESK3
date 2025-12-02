/**
 * Service Desk Widget - Embeddable Chat Widget
 * 
 * Usage:
 *   ServiceDeskWidget.init({
 *     projectKey: 'DESK',
 *     apiUrl: 'https://api.servicedesk.io'
 *   });
 */

import { WidgetConfig } from './types';
import { Widget } from './widget';

let widgetInstance: Widget | null = null;

/**
 * Service Desk Widget API
 */
const ServiceDeskWidget = {
  /**
   * Initialize the widget with configuration
   */
  init(config: WidgetConfig): void {
    if (!config.projectKey) {
      console.error('[ServiceDesk Widget] projectKey is required');
      return;
    }
    if (!config.apiUrl) {
      console.error('[ServiceDesk Widget] apiUrl is required');
      return;
    }

    // Destroy existing instance if any
    if (widgetInstance) {
      widgetInstance.destroy();
    }

    widgetInstance = new Widget(config);
    widgetInstance.init().catch(err => {
      console.error('[ServiceDesk Widget] Initialization failed:', err);
    });
  },

  /**
   * Open the chat widget
   */
  open(): void {
    if (widgetInstance) {
      widgetInstance.open();
    } else {
      console.warn('[ServiceDesk Widget] Widget not initialized. Call init() first.');
    }
  },

  /**
   * Close the chat widget
   */
  close(): void {
    if (widgetInstance) {
      widgetInstance.close();
    }
  },

  /**
   * Destroy the widget instance
   */
  destroy(): void {
    if (widgetInstance) {
      widgetInstance.destroy();
      widgetInstance = null;
    }
  }
};

// Export for ES modules
export default ServiceDeskWidget;

// Make available globally for script tag usage
if (typeof window !== 'undefined') {
  (window as unknown as { ServiceDeskWidget: typeof ServiceDeskWidget }).ServiceDeskWidget = ServiceDeskWidget;
}
