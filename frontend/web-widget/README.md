# Service Desk Web Widget

An embeddable JavaScript chat widget for integrating with the Service Desk platform.

## Features

- 🚀 Single-file bundle (~50KB)
- 💬 Real-time messaging via WebSocket
- 📎 File attachments with drag & drop
- 🎨 Customizable appearance
- 🌐 Multi-language support (en, ru, uz)
- 📱 Responsive design for mobile and desktop
- 💾 Persistent sessions across page refreshes
- 🔔 Sound notifications for new messages

## Installation

### Option 1: CDN (Recommended)

```html
<script src="https://cdn.servicedesk.io/widget.js"></script>
<script>
  ServiceDeskWidget.init({
    projectKey: 'DESK',
    apiUrl: 'https://api.servicedesk.io'
  });
</script>
```

### Option 2: npm

```bash
npm install servicedesk-widget
```

```javascript
import ServiceDeskWidget from 'servicedesk-widget';

ServiceDeskWidget.init({
  projectKey: 'DESK',
  apiUrl: 'https://api.servicedesk.io'
});
```

## Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `projectKey` | string | *required* | Project key from Service Desk |
| `apiUrl` | string | *required* | API base URL |
| `wsUrl` | string | derived | WebSocket URL (derived from apiUrl if not provided) |
| `position` | string | `'bottom-right'` | Widget position: `'bottom-right'` or `'bottom-left'` |
| `primaryColor` | string | `'#1976d2'` | Primary theme color |
| `greeting` | string | `'Hi! How can we help you?'` | Initial greeting message |
| `placeholder` | string | `'Type your message...'` | Input field placeholder |
| `offlineMessage` | string | `'We are currently offline...'` | Message when agents are offline |
| `showAgentAvatar` | boolean | `true` | Show agent avatar in messages |
| `soundEnabled` | boolean | `true` | Enable notification sounds |
| `language` | string | `'en'` | Widget language: `'en'`, `'ru'`, or `'uz'` |
| `zIndex` | number | `999999` | CSS z-index for the widget |

## API Methods

### `ServiceDeskWidget.init(config)`

Initialize the widget with configuration options.

### `ServiceDeskWidget.open()`

Programmatically open the chat window.

### `ServiceDeskWidget.close()`

Programmatically close the chat window.

### `ServiceDeskWidget.destroy()`

Remove the widget from the page completely.

## Development

### Prerequisites

- Node.js 18+
- npm 9+

### Setup

```bash
cd frontend/web-widget
npm install
```

### Development Build

```bash
npm run dev
```

### Production Build

```bash
npm run build
```

The compiled widget will be output to `dist/servicedesk-widget.js`.

### Testing

```bash
npm test
```

## File Structure

```
frontend/web-widget/
├── package.json
├── tsconfig.json
├── webpack.config.js
├── src/
│   ├── index.ts                    # Main entry point
│   ├── widget.ts                   # Widget initialization
│   ├── components/
│   │   ├── ChatWindow.ts           # Main chat window
│   │   ├── MessageList.ts          # Message list component
│   │   ├── MessageInput.ts         # Input field with send button
│   │   ├── Header.ts               # Widget header with close button
│   │   ├── LauncherButton.ts       # Floating button to open chat
│   │   └── FileUpload.ts           # File attachment component
│   ├── services/
│   │   ├── WebSocketService.ts     # Real-time communication
│   │   ├── ApiService.ts           # REST API calls
│   │   └── StorageService.ts       # Local storage for session
│   ├── styles/
│   │   ├── widget.scss             # Main styles
│   │   ├── variables.scss          # Theme variables
│   │   └── animations.scss         # CSS animations
│   ├── types/
│   │   └── index.ts                # TypeScript interfaces
│   └── utils/
│       ├── dom.ts                  # DOM utilities
│       └── formatters.ts           # Date/time formatters
└── dist/
    └── servicedesk-widget.js       # Compiled widget (single file)
```

## Browser Support

- Chrome 80+
- Firefox 75+
- Safari 13+
- Edge 80+
- iOS Safari 13+
- Chrome for Android 80+

## License

MIT License - See LICENSE file for details.
