# Service Desk Platform

<div align="center">

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green.svg)
![Angular](https://img.shields.io/badge/Angular-17+-red.svg)

**Open-source Service Desk Platform with Omnichannel Support, AI Integration, and Telephony**

[Features](#features) • [Quick Start](#quick-start) • [Documentation](#documentation) • [Contributing](#contributing) • [License](#license)

</div>

---

## 🎯 Overview

Service Desk Platform is a modern, open-source customer support solution designed for enterprises in Central Asia and beyond. Built with Java Spring Boot and Angular, it provides comprehensive ticket management, omnichannel communication, AI-powered automation, and telephony integration.

### Why Service Desk Platform?

- **Open Source**: MIT licensed, fully customizable
- **Omnichannel**: Email, Telegram, WhatsApp, Phone, Web Widget
- **AI-Powered**: Smart ticket routing, auto-responses, sentiment analysis
- **Enterprise-Ready**: Multi-project support, SLA management, advanced analytics
- **Developer-Friendly**: Well-documented API, modern tech stack
- **Multi-Language**: Support for English, Russian, Uzbek, Kazakh, Arabic

---

## ✨ Features

### Core Features (MVP)
- ✅ **Ticket Management**: Create, assign, track, and resolve tickets
- ✅ **User Management**: Role-based access control (Admin, Manager, Agent, Customer)
- ✅ **Multi-Project Support**: Manage multiple departments or products
- ✅ **Team Collaboration**: Shared inbox, internal notes, @mentions
- 🚧 **Omnichannel Communication**:
  - Email (SMTP/IMAP)
  - Telegram Bot
  - WhatsApp Business API
  - Web Widget
  - Phone (VoIP/SIP)
- 🚧 **Automation & Workflows**: Auto-assignment, triggers, SLA management
- 🚧 **Knowledge Base**: Self-service portal with full-text search
- 🚧 **Analytics & Reporting**: Real-time metrics, CSAT/NPS surveys

### Advanced Features (Roadmap)
- 🔮 **AI Agent**: RAG-powered auto-responses, intent detection
- 🔮 **Telephony**: WebRTC calls, IVR, call recording
- 🔮 **Advanced Analytics**: Predictive analytics, ML models
- 🔮 **Integrations**: Zapier, Slack, Microsoft Teams
- 🔮 **Mobile Apps**: iOS and Android native apps

Legend: ✅ Completed | 🚧 In Progress | 🔮 Planned

---

## 🚀 Quick Start

### Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **Node.js 18+** (for frontend)
- **Docker & Docker Compose** (recommended)
- **PostgreSQL 14+** (if not using Docker)
- **Redis 7+** (if not using Docker)

### Option 1: Docker Compose (Recommended)

```bash
# Clone the repository
git clone https://github.com/qahhor/FREE-SRVICEDESK3.git
cd FREE-SRVICEDESK3

# Start infrastructure services (PostgreSQL, Redis, Elasticsearch, RabbitMQ)
docker-compose -f docker-compose.dev.yml up -d

# Build and run backend
cd backend
mvn clean install
cd ticket-service
mvn spring-boot:run

# Access the application
# API: http://localhost:8080/api/v1
# Swagger UI: http://localhost:8080/swagger-ui.html
```

### Option 2: Manual Setup

1. **Database Setup**
```bash
# Create PostgreSQL database
createdb servicedesk

# Start Redis
redis-server
```

2. **Backend Setup**
```bash
cd backend
mvn clean install
cd ticket-service
mvn spring-boot:run
```

3. **Frontend Setup** (Coming soon)
```bash
cd frontend/agent-app
npm install
ng serve
```

### Default Credentials

```
Email: admin@servicedesk.io
Password: admin123
```

⚠️ **Security Warning**: Change the default password immediately in production!

---

## 📚 Architecture

### Technology Stack

**Backend:**
- Java 17
- Spring Boot 3.2
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Redis
- Flyway (migrations)
- MapStruct (DTO mapping)

**Frontend:**
- Angular 17+
- Angular Material / PrimeNG
- NgRx (state management)
- WebSocket (real-time updates)

**Infrastructure:**
- Docker & Docker Compose
- Kubernetes (Helm charts)
- Elasticsearch (search)
- RabbitMQ (messaging)

### Project Structure

```
servicedesk-platform/
├── backend/
│   ├── common-lib/           # Shared utilities
│   ├── ticket-service/        # Core ticket management
│   ├── channel-service/       # Omnichannel adapter
│   ├── ai-service/            # AI/ML features
│   ├── telephony-service/     # VoIP integration
│   ├── analytics-service/     # Metrics & reporting
│   ├── knowledge-service/     # Knowledge base
│   └── notification-service/  # Email/Push notifications
├── frontend/
│   ├── agent-app/            # Agent interface
│   ├── admin-app/            # Admin panel
│   ├── customer-portal/      # Self-service portal
│   └── web-widget/           # Embeddable chat widget
├── infrastructure/
│   ├── docker/
│   └── kubernetes/
├── docs/                     # Documentation
└── scripts/                  # Utility scripts
```

---

## 🔌 API Documentation

### Authentication

```bash
# Login
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "admin@servicedesk.io",
  "password": "admin123"
}

# Response
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "user": {
      "id": "uuid",
      "email": "admin@servicedesk.io",
      "firstName": "System",
      "lastName": "Administrator",
      "role": "ADMIN"
    }
  }
}
```

### Tickets

```bash
# Create ticket
POST /api/v1/tickets
Authorization: Bearer {token}
Content-Type: application/json

{
  "subject": "Cannot login to the system",
  "description": "I'm getting 'Invalid credentials' error",
  "priority": "HIGH",
  "channel": "EMAIL",
  "projectId": "uuid"
}

# Get all tickets
GET /api/v1/tickets?page=0&size=20
Authorization: Bearer {token}

# Get ticket by ID
GET /api/v1/tickets/{id}
Authorization: Bearer {token}

# Update ticket status
PATCH /api/v1/tickets/{id}/status?status=RESOLVED
Authorization: Bearer {token}

# Assign ticket
PATCH /api/v1/tickets/{id}/assign?assigneeId={uuid}
Authorization: Bearer {token}
```

Full API documentation: [API.md](docs/api/API.md)

---

## 🛠️ Development

### Build from Source

```bash
# Backend
cd backend
mvn clean install

# Run tests
mvn test

# Run with dev profile
cd ticket-service
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Database Migrations

We use Flyway for database version control. Migrations are located in:
```
backend/ticket-service/src/main/resources/db/migration/
```

Create a new migration:
```bash
# Format: V{version}__{description}.sql
# Example: V2__add_custom_fields.sql
```

### Code Style

- Java: Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use Lombok for boilerplate reduction
- Write meaningful commit messages

---

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run integration tests
mvn verify -Pintegration-tests

# Run with coverage
mvn clean test jacoco:report
```

---

## 🌐 Internationalization

Supported languages:
- **English** (en)
- **Русский** (ru)
- **Oʻzbekcha** (uz)
- **Қазақша** (kk)
- **العربية** (ar) - RTL support

Add translations in:
```
backend/*/src/main/resources/i18n/messages_{locale}.properties
```

---

## 📊 Monitoring

Access monitoring dashboards:

- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **Prometheus**: http://localhost:8080/actuator/prometheus
- **RabbitMQ Management**: http://localhost:15672 (servicedesk/servicedesk)
- **Elasticsearch**: http://localhost:9200

---

## 🤝 Contributing

We welcome contributions! Please read our [Contributing Guide](CONTRIBUTING.md) for details.

### Development Workflow

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code of Conduct

This project adheres to a [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

---

## 📝 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- Developed by **Green White Solutions** team
- Inspired by modern service desk platforms
- Built for the Central Asia region with ❤️

---

## 📧 Contact & Support

- **Website**: https://greenwhite.uz
- **Issues**: https://github.com/qahhor/FREE-SRVICEDESK3/issues
- **Discussions**: https://github.com/qahhor/FREE-SRVICEDESK3/discussions
- **Email**: support@greenwhite.uz

---

## 🗺️ Roadmap

See our [ROADMAP.md](ROADMAP.md) for planned features and timeline.

### Milestone 1: Core Foundation ✅ (Completed)
- [x] Project structure
- [x] Database schema
- [x] JWT authentication
- [x] REST API for tickets
- [x] Docker Compose setup

### Milestone 2: Omnichannel Integration 🚧 (In Progress)
- [ ] Email integration
- [ ] Telegram Bot
- [ ] WhatsApp Business API
- [ ] Web Widget

### Milestone 3: Frontend & UX 🔮 (Planned)
- [ ] Angular agent app
- [ ] Customer portal
- [ ] Real-time updates (WebSocket)
- [ ] Dark theme

### Milestone 4: Advanced Features 🔮 (Planned)
- [ ] AI integration
- [ ] Knowledge base
- [ ] Analytics dashboard
- [ ] Telephony

---

<div align="center">

**[⬆ back to top](#service-desk-platform)**

Made with ❤️ by Green White Solutions

</div>
