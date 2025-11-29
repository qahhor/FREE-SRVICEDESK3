# Service Desk Platform - Architecture Documentation

## System Architecture Overview

Service Desk Platform is built as a **microservices-based** system with a **modular monolith** approach for MVP, designed to scale into fully distributed microservices.

```
┌─────────────────────────────────────────────────────────────────┐
│                         Frontend Layer                          │
├─────────────────────────────────────────────────────────────────┤
│  Angular Agent App  │  Customer Portal  │  Admin App  │ Widget │
└──────────────┬──────────────────────────────────────────────────┘
               │
               │ HTTPS / WebSocket
               ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API Gateway / Nginx                        │
└──────────────┬──────────────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Application Services                         │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌───────────────┐  ┌──────────────┐        │
│  │Ticket Service│  │Channel Service│  │ AI Service   │        │
│  │   (Core)     │  │ (Omnichannel) │  │   (RAG/LLM)  │        │
│  └──────┬───────┘  └───────┬───────┘  └──────┬───────┘        │
│         │                   │                  │                │
│  ┌──────▼───────┐  ┌───────▼───────┐  ┌──────▼───────┐       │
│  │Telephony Svc │  │Analytics  Svc │  │Knowledge Svc │       │
│  └──────────────┘  └───────────────┘  └──────────────┘       │
└──────────────┬──────────────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Data & Cache Layer                         │
├─────────────────────────────────────────────────────────────────┤
│  PostgreSQL  │    Redis     │ Elasticsearch │   RabbitMQ      │
│  (Primary DB)│   (Cache)    │   (Search)    │  (Message Bus)  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Technology Stack

### Backend

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| **Language** | Java | 17+ | Main programming language |
| **Framework** | Spring Boot | 3.2+ | Application framework |
| **Security** | Spring Security | 6.2+ | Authentication & Authorization |
| **Data Access** | Spring Data JPA | 3.2+ | ORM layer |
| **Migration** | Flyway | 10.4+ | Database versioning |
| **Validation** | Hibernate Validator | 8.0+ | Bean validation |
| **Mapping** | MapStruct | 1.5+ | DTO mapping |
| **API Docs** | Springdoc OpenAPI | 2.3+ | Swagger/OpenAPI |
| **WebSocket** | Spring WebSocket | 3.2+ | Real-time communication |

### Frontend

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| **Framework** | Angular | 17+ | SPA framework |
| **UI Library** | Angular Material | 17+ | Material Design components |
| **State Management** | Signals | Built-in | Reactive state |
| **HTTP Client** | HttpClient | Built-in | API communication |
| **WebSocket** | STOMP.js + SockJS | 7.0 | Real-time updates |
| **Build Tool** | Angular CLI | 17+ | Build & tooling |

### Data Stores

| Store | Purpose | Persistence |
|-------|---------|-------------|
| **PostgreSQL** | Primary data store | Persistent |
| **Redis** | Cache & sessions | In-memory |
| **Elasticsearch** | Full-text search | Persistent |
| **RabbitMQ** | Message queue | Persistent |

---

## Architectural Patterns

### 1. Layered Architecture

```
┌─────────────────────────────────────┐
│        Presentation Layer           │
│   (Controllers, WebSocket, REST)    │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│         Service Layer               │
│   (Business Logic, Validation)      │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│      Repository Layer               │
│   (Data Access, JPA, Queries)       │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│        Database Layer               │
│        (PostgreSQL)                 │
└─────────────────────────────────────┘
```

### 2. Domain-Driven Design (DDD)

**Bounded Contexts:**
- **Ticket Management**: Core domain for ticket CRUD
- **User Management**: Authentication & authorization
- **Channel Management**: Omnichannel communication
- **Knowledge Base**: Self-service content
- **Analytics**: Reporting & metrics

**Aggregates:**
- **Ticket**: Root aggregate (Ticket + Comments)
- **User**: User + Teams
- **Project**: Project + Teams

### 3. Event-Driven Architecture

```
┌──────────────┐
│Ticket Service│
└──────┬───────┘
       │
       │ WebSocket Event
       │ {type: "CREATED", ticket: {...}}
       ▼
┌──────────────────┐
│  Message Broker  │
│   (WebSocket)    │
└──────┬───────────┘
       │
       │ Broadcast
       ▼
┌──────────────────┐
│Connected Clients │
└──────────────────┘
```

**Event Types:**
- `TICKET_CREATED`
- `TICKET_UPDATED`
- `TICKET_ASSIGNED`
- `TICKET_STATUS_CHANGED`
- `COMMENT_ADDED`

---

## Database Schema

### Core Tables

#### `users`
```sql
id              UUID PRIMARY KEY
email           VARCHAR(255) UNIQUE NOT NULL
password        VARCHAR(255) NOT NULL
first_name      VARCHAR(100) NOT NULL
last_name       VARCHAR(100) NOT NULL
role            VARCHAR(20) NOT NULL
active          BOOLEAN DEFAULT true
created_at      TIMESTAMP NOT NULL
updated_at      TIMESTAMP NOT NULL
```

#### `tickets`
```sql
id              UUID PRIMARY KEY
ticket_number   VARCHAR(50) UNIQUE NOT NULL
subject         VARCHAR(500) NOT NULL
description     TEXT
status          VARCHAR(20) NOT NULL
priority        VARCHAR(20) NOT NULL
channel         VARCHAR(20) NOT NULL
project_id      UUID REFERENCES projects(id)
requester_id    UUID REFERENCES users(id)
assignee_id     UUID REFERENCES users(id)
team_id         UUID REFERENCES teams(id)
created_at      TIMESTAMP NOT NULL
updated_at      TIMESTAMP NOT NULL
```

#### `comments`
```sql
id              UUID PRIMARY KEY
ticket_id       UUID REFERENCES tickets(id)
author_id       UUID REFERENCES users(id)
content         TEXT NOT NULL
is_internal     BOOLEAN DEFAULT false
created_at      TIMESTAMP NOT NULL
```

**Indexes:**
- `idx_tickets_status` on `tickets(status)`
- `idx_tickets_assignee` on `tickets(assignee_id)`
- `idx_tickets_created` on `tickets(created_at DESC)`

---

## API Design

### RESTful Principles

- **Resource-based URLs**: `/api/v1/tickets`, `/api/v1/users`
- **HTTP Methods**: GET, POST, PUT, PATCH, DELETE
- **Status Codes**: 200, 201, 400, 401, 403, 404, 500
- **Versioning**: URL-based (`/api/v1/`)

### Standard Response Format

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {...},
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Error Response Format

```json
{
  "success": false,
  "error": "Resource not found",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Pagination

```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 5,
  "size": 20,
  "number": 0
}
```

---

## Security Architecture

### Authentication Flow

```
┌──────┐                              ┌──────────┐
│Client│                              │  Server  │
└───┬──┘                              └────┬─────┘
    │                                      │
    │ POST /api/v1/auth/login             │
    │ {email, password}                   │
    ├────────────────────────────────────►│
    │                                      │
    │                  Validate Credentials│
    │                  Generate JWT Token  │
    │                                      │
    │ 200 OK                              │
    │ {accessToken, user}                 │
    │◄────────────────────────────────────┤
    │                                      │
    │ GET /api/v1/tickets                 │
    │ Authorization: Bearer {token}       │
    ├────────────────────────────────────►│
    │                                      │
    │                  Validate JWT        │
    │                  Extract User Info   │
    │                                      │
    │ 200 OK {data}                       │
    │◄────────────────────────────────────┤
```

### Role-Based Access Control (RBAC)

| Role | Permissions |
|------|------------|
| **ADMIN** | Full system access, user management |
| **MANAGER** | Team management, reports, all tickets |
| **AGENT** | Assigned tickets, create tickets, respond |
| **CUSTOMER** | View own tickets, create tickets |

### Security Features

- ✅ **JWT Authentication**: Stateless authentication
- ✅ **BCrypt Password Hashing**: Secure password storage
- ✅ **CORS Protection**: Configurable origins
- ✅ **CSRF Protection**: Token-based
- ✅ **SQL Injection Prevention**: Prepared statements
- ✅ **XSS Protection**: Content Security Policy
- ✅ **Rate Limiting**: API throttling (planned)

---

## WebSocket Architecture

### Connection Flow

```
Client                          Server
  │                               │
  │ Connect to /ws               │
  ├──────────────────────────────►│
  │                               │
  │ STOMP CONNECT                │
  ├──────────────────────────────►│
  │                               │
  │ CONNECTED                    │
  │◄──────────────────────────────┤
  │                               │
  │ SUBSCRIBE /topic/tickets     │
  ├──────────────────────────────►│
  │                               │
  │ ... ticket events ...        │
  │◄──────────────────────────────┤
```

### Message Format

```json
{
  "type": "CREATED",
  "ticket": {
    "id": "uuid",
    "ticketNumber": "DESK-123",
    "subject": "Issue with login"
  },
  "message": "New ticket created: DESK-123",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## Performance Considerations

### Caching Strategy

```
Level 1: HTTP Cache (Browser)
    ↓
Level 2: Redis Cache (Server)
    ↓
Level 3: JPA 2nd Level Cache
    ↓
Database
```

### Query Optimization

- **Pagination**: Always paginate large result sets
- **Indexes**: Strategic indexes on frequently queried fields
- **N+1 Problem**: Use `@EntityGraph` for eager loading
- **Connection Pooling**: HikariCP with optimal settings

### Scalability

**Horizontal Scaling:**
- Stateless services (JWT)
- Load balancer (Nginx/HAProxy)
- Multiple service instances
- Shared Redis cache

**Vertical Scaling:**
- JVM tuning (`-Xms`, `-Xmx`)
- Database optimization
- Connection pool sizing

---

## Monitoring & Observability

### Health Checks

- **Liveness**: `/actuator/health/liveness` - Is app running?
- **Readiness**: `/actuator/health/readiness` - Is app ready for traffic?

### Metrics

- **JVM Metrics**: Memory, GC, threads
- **HTTP Metrics**: Request count, latency, errors
- **Database Metrics**: Connection pool, query time
- **Custom Metrics**: Ticket count, response time

### Distributed Tracing

```
Request ID: abc-123-def

┌─────────────┐   ┌──────────────┐   ┌──────────────┐
│   Nginx     │──►│Ticket Service│──►│  PostgreSQL  │
└─────────────┘   └──────────────┘   └──────────────┘
     100ms             50ms                 30ms
├──────────────────────────────────────────────────────┤
                   Total: 180ms
```

---

## Deployment Architecture

### Docker Compose (Development/Small Production)

```
┌────────────────────────────────────────┐
│           Docker Host                  │
│  ┌────────────────────────────────┐   │
│  │  Nginx (Reverse Proxy)         │   │
│  └────────┬───────────────────────┘   │
│           │                            │
│  ┌────────▼───────────────────────┐   │
│  │  Ticket Service (Spring Boot)  │   │
│  └────────┬───────────────────────┘   │
│           │                            │
│  ┌────────▼────┐  ┌────────────┐     │
│  │ PostgreSQL  │  │   Redis    │     │
│  └─────────────┘  └────────────┘     │
└────────────────────────────────────────┘
```

### Kubernetes (Production at Scale)

```
┌────────────────────────────────────────────────────┐
│              Kubernetes Cluster                    │
│  ┌────────────────────────────────────────────┐   │
│  │           Ingress Controller               │   │
│  └────────┬───────────────────────────────────┘   │
│           │                                        │
│  ┌────────▼────────────────────────────────┐     │
│  │  Ticket Service (3 replicas)            │     │
│  │  ┌────────┐ ┌────────┐ ┌────────┐      │     │
│  │  │ Pod 1  │ │ Pod 2  │ │ Pod 3  │      │     │
│  │  └────────┘ └────────┘ └────────┘      │     │
│  └────────┬────────────────────────────────┘     │
│           │                                        │
│  ┌────────▼─────────┐  ┌──────────────┐         │
│  │  StatefulSet      │  │ StatefulSet  │         │
│  │  PostgreSQL       │  │    Redis     │         │
│  └───────────────────┘  └──────────────┘         │
└────────────────────────────────────────────────────┘
```

---

## Future Architecture

### Microservices Evolution

```
Current: Modular Monolith
    ↓
Phase 1: Extract Channel Service
    ↓
Phase 2: Extract AI Service
    ↓
Phase 3: Extract Telephony Service
    ↓
Final: Full Microservices
```

### Event Sourcing (Planned)

```
Command         Event Stream        Projection
┌──────┐       ┌──────────┐       ┌─────────┐
│CREATE│──────►│ CREATED  │──────►│ Ticket  │
│TICKET│       │ ASSIGNED │       │  View   │
└──────┘       │ RESOLVED │       └─────────┘
               └──────────┘
```

---

## Design Decisions

### Why Spring Boot?

- ✅ Mature ecosystem
- ✅ Excellent documentation
- ✅ Large community
- ✅ Production-ready features
- ✅ Easy deployment

### Why PostgreSQL?

- ✅ ACID compliance
- ✅ JSON support
- ✅ Full-text search
- ✅ Proven reliability
- ✅ Strong community

### Why Angular?

- ✅ TypeScript support
- ✅ Material Design
- ✅ Strong CLI
- ✅ Enterprise-ready
- ✅ Good for large teams

### Why WebSocket?

- ✅ Real-time updates
- ✅ Bidirectional communication
- ✅ Low latency
- ✅ Standard protocol

---

## Performance Benchmarks

### Target Metrics

| Metric | Target | Current |
|--------|--------|---------|
| **API Response Time (p95)** | <200ms | TBD |
| **WebSocket Latency** | <100ms | TBD |
| **Concurrent Users** | 1000+ | TBD |
| **Tickets per Second** | 100+ | TBD |

---

## Conclusion

This architecture provides:

1. **Scalability**: Horizontal scaling ready
2. **Maintainability**: Clear separation of concerns
3. **Testability**: Layered architecture
4. **Security**: Multiple security layers
5. **Observability**: Comprehensive monitoring

---

**Document Version**: 1.0
**Last Updated**: January 2024
**Next Review**: March 2024
