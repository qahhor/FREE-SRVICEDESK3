# Service Desk - Agent Portal (Frontend)

Angular 17+ application for Service Desk agents.

## Features

- 🔐 JWT Authentication
- 📊 Real-time Dashboard
- 🎫 Ticket Management (List, Detail, Create)
- 🔄 WebSocket for live updates
- 🎨 Material Design UI
- 📱 Responsive layout

## Prerequisites

- Node.js 18+
- npm or yarn

## Installation

```bash
# Install dependencies
npm install

# Start development server
npm start

# Open browser at http://localhost:4200
```

## Build

```bash
# Production build
npm run build

# Output will be in dist/agent-app
```

## Project Structure

```
src/
├── app/
│   ├── core/                    # Core services and models
│   │   ├── guards/             # Route guards
│   │   ├── interceptors/       # HTTP interceptors
│   │   ├── models/             # TypeScript models
│   │   └── services/           # API services
│   ├── features/               # Feature modules
│   │   ├── auth/              # Authentication
│   │   ├── dashboard/         # Dashboard
│   │   ├── layout/            # Main layout
│   │   └── tickets/           # Ticket management
│   ├── app.component.ts       # Root component
│   └── app.routes.ts          # Application routes
├── environments/               # Environment configs
└── styles.scss                # Global styles
```

## Development

```bash
# Run tests
npm test

# Lint
npm run lint

# Format code
npm run format
```

## Default Credentials

```
Email: admin@servicedesk.io
Password: admin123
```

## API Configuration

Edit `src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1',
  wsUrl: 'http://localhost:8080/ws'
};
```

## Technologies

- Angular 17
- Angular Material
- RxJS
- STOMP WebSocket
- TypeScript 5.2
