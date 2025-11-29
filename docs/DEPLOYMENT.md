# Service Desk Platform - Deployment Guide

## Table of Contents

- [Prerequisites](#prerequisites)
- [Production Deployment](#production-deployment)
  - [Docker Compose Deployment](#docker-compose-deployment)
  - [Kubernetes Deployment](#kubernetes-deployment)
- [Database Setup](#database-setup)
- [SSL/TLS Configuration](#ssltls-configuration)
- [Monitoring & Logging](#monitoring--logging)
- [Backup & Recovery](#backup--recovery)
- [Security Hardening](#security-hardening)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Hardware Requirements

**Minimum (Development/Small Team):**
- CPU: 2 cores
- RAM: 4GB
- Storage: 20GB SSD

**Recommended (Production):**
- CPU: 4+ cores
- RAM: 8GB+
- Storage: 100GB+ SSD

### Software Requirements

- Docker 24.0+
- Docker Compose 2.20+
- OR Kubernetes 1.28+
- PostgreSQL 14+ (if not using Docker)
- Redis 7+ (if not using Docker)
- Java 17+ (for local builds)
- Maven 3.8+ (for local builds)

---

## Production Deployment

### Option 1: Docker Compose Deployment

#### Step 1: Clone Repository

```bash
git clone https://github.com/qahhor/FREE-SRVICEDESK3.git
cd FREE-SRVICEDESK3
```

#### Step 2: Configure Environment

```bash
# Copy and edit environment file
cp .env.example .env

# Edit .env and set secure passwords
nano .env
```

**Important: Change all default passwords!**

#### Step 3: Build Application

```bash
# Build backend
cd backend
mvn clean package -DskipTests

# Build frontend
cd ../frontend/agent-app
npm install
npm run build --prod
```

#### Step 4: Build Docker Images

```bash
cd ../..

# Build ticket-service image
docker build -t servicedesk/ticket-service:0.1.0 -f backend/ticket-service/Dockerfile backend

# Or use Docker Compose
docker-compose -f docker-compose.prod.yml build
```

#### Step 5: Start Services

```bash
# Start all services
docker-compose -f docker-compose.prod.yml up -d

# Check logs
docker-compose -f docker-compose.prod.yml logs -f
```

#### Step 6: Verify Deployment

```bash
# Check service health
curl http://localhost:8080/actuator/health

# Check database migration
docker-compose -f docker-compose.prod.yml exec ticket-service \
  sh -c 'wget -qO- http://localhost:8080/actuator/flyway'
```

#### Step 7: Create Admin User

Admin user is automatically created by Flyway migration with credentials:
```
Email: admin@servicedesk.io
Password: admin123
```

⚠️ **IMPORTANT: Change admin password immediately!**

---

### Option 2: Kubernetes Deployment

#### Prerequisites

- Kubernetes cluster (1.28+)
- kubectl configured
- Helm 3+ installed

#### Step 1: Create Namespace

```bash
kubectl create namespace servicedesk
```

#### Step 2: Create Secrets

```bash
# Create database secret
kubectl create secret generic postgres-secret \
  --from-literal=password='your-secure-password' \
  -n servicedesk

# Create JWT secret
kubectl create secret generic jwt-secret \
  --from-literal=secret='your-256-bit-jwt-secret-key' \
  -n servicedesk

# Create Redis secret
kubectl create secret generic redis-secret \
  --from-literal=password='your-redis-password' \
  -n servicedesk
```

#### Step 3: Deploy with Helm

```bash
# Add Helm repository (when available)
helm repo add servicedesk https://charts.servicedesk.io
helm repo update

# Install
helm install servicedesk servicedesk/servicedesk-platform \
  --namespace servicedesk \
  --set image.tag=0.1.0 \
  --set ingress.enabled=true \
  --set ingress.host=servicedesk.yourdomain.com

# Or use local Helm chart
cd infrastructure/kubernetes/helm-charts
helm install servicedesk ./servicedesk-platform \
  --namespace servicedesk \
  --values values-prod.yaml
```

#### Step 4: Verify Deployment

```bash
# Check pods
kubectl get pods -n servicedesk

# Check services
kubectl get svc -n servicedesk

# Check logs
kubectl logs -f deployment/ticket-service -n servicedesk
```

---

## Database Setup

### Initial Setup

Database schema is automatically created by Flyway migrations on first startup.

### Manual Migration

If you need to run migrations manually:

```bash
# Using Docker
docker-compose -f docker-compose.prod.yml exec ticket-service \
  java -jar app.jar --spring.flyway.migrate

# Using kubectl
kubectl exec -it deployment/ticket-service -n servicedesk -- \
  java -jar app.jar --spring.flyway.migrate
```

### Database Backup

```bash
# Create backup
docker-compose -f docker-compose.prod.yml exec postgres \
  pg_dump -U servicedesk servicedesk > backup_$(date +%Y%m%d_%H%M%S).sql

# Restore backup
cat backup_20240101_120000.sql | \
  docker-compose -f docker-compose.prod.yml exec -T postgres \
  psql -U servicedesk servicedesk
```

---

## SSL/TLS Configuration

### Using Let's Encrypt (Recommended)

```bash
# Install certbot
sudo apt-get install certbot python3-certbot-nginx

# Obtain certificate
sudo certbot --nginx -d servicedesk.yourdomain.com

# Auto-renewal is configured automatically
```

### Using Custom Certificate

1. Place certificate files in `infrastructure/nginx/ssl/`:
   - `cert.pem`
   - `key.pem`
   - `chain.pem`

2. Update nginx configuration:

```nginx
server {
    listen 443 ssl http2;
    server_name servicedesk.yourdomain.com;

    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;
    ssl_trusted_certificate /etc/nginx/ssl/chain.pem;

    # Modern SSL configuration
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
}
```

---

## Monitoring & Logging

### Prometheus Metrics

Metrics are exposed at: `http://localhost:8080/actuator/prometheus`

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'servicedesk'
    static_configs:
      - targets: ['ticket-service:8080']
    metrics_path: '/actuator/prometheus'
```

### Grafana Dashboard

Import pre-configured dashboard from `infrastructure/monitoring/grafana-dashboard.json`

### Centralized Logging

Using ELK Stack:

```bash
# Start ELK
docker-compose -f infrastructure/monitoring/docker-compose-elk.yml up -d

# View logs in Kibana
# Open http://localhost:5601
```

### Application Logs

```bash
# Docker Compose
docker-compose -f docker-compose.prod.yml logs -f ticket-service

# Kubernetes
kubectl logs -f deployment/ticket-service -n servicedesk

# Local file (inside container)
tail -f /var/log/servicedesk/ticket-service.log
```

---

## Backup & Recovery

### Automated Backup Script

```bash
#!/bin/bash
# backup.sh

BACKUP_DIR="/backup"
DATE=$(date +%Y%m%d_%H%M%S)

# Backup database
docker-compose exec -T postgres pg_dump -U servicedesk servicedesk | \
  gzip > $BACKUP_DIR/db_backup_$DATE.sql.gz

# Backup uploaded files (when implemented)
tar -czf $BACKUP_DIR/files_backup_$DATE.tar.gz /var/servicedesk/uploads

# Keep only last 30 days
find $BACKUP_DIR -name "*.gz" -mtime +30 -delete

echo "Backup completed: $DATE"
```

Setup cron job:

```bash
# Edit crontab
crontab -e

# Add daily backup at 2 AM
0 2 * * * /opt/servicedesk/backup.sh >> /var/log/servicedesk-backup.log 2>&1
```

### Disaster Recovery

1. **Stop services:**
   ```bash
   docker-compose -f docker-compose.prod.yml stop
   ```

2. **Restore database:**
   ```bash
   gunzip < db_backup_20240101_120000.sql.gz | \
     docker-compose exec -T postgres psql -U servicedesk servicedesk
   ```

3. **Restore files:**
   ```bash
   tar -xzf files_backup_20240101_120000.tar.gz -C /
   ```

4. **Start services:**
   ```bash
   docker-compose -f docker-compose.prod.yml up -d
   ```

---

## Security Hardening

### 1. Change Default Passwords

```bash
# Generate secure passwords
openssl rand -base64 32

# Update .env file
nano .env
```

### 2. Firewall Configuration

```bash
# Allow only necessary ports
sudo ufw allow 22/tcp   # SSH
sudo ufw allow 80/tcp   # HTTP
sudo ufw allow 443/tcp  # HTTPS
sudo ufw enable
```

### 3. Database Security

```sql
-- Revoke public access
REVOKE ALL ON DATABASE servicedesk FROM PUBLIC;

-- Create read-only user for reporting
CREATE USER servicedesk_readonly WITH PASSWORD 'secure_password';
GRANT CONNECT ON DATABASE servicedesk TO servicedesk_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO servicedesk_readonly;
```

### 4. Rate Limiting

Configure nginx rate limiting:

```nginx
limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;

server {
    location /api/ {
        limit_req zone=api burst=20 nodelay;
        proxy_pass http://ticket-service:8080;
    }
}
```

### 5. Security Headers

```nginx
add_header X-Frame-Options "SAMEORIGIN" always;
add_header X-Content-Type-Options "nosniff" always;
add_header X-XSS-Protection "1; mode=block" always;
add_header Referrer-Policy "no-referrer-when-downgrade" always;
add_header Content-Security-Policy "default-src 'self';" always;
```

---

## Troubleshooting

### Service Won't Start

**Check logs:**
```bash
docker-compose -f docker-compose.prod.yml logs ticket-service
```

**Common issues:**
- Database connection refused → Check PostgreSQL is running
- Port already in use → Change port in .env
- Permission denied → Check file permissions

### Database Migration Failed

**View migration status:**
```bash
docker-compose exec postgres psql -U servicedesk -d servicedesk \
  -c "SELECT * FROM flyway_schema_history;"
```

**Repair migrations:**
```bash
docker-compose exec ticket-service \
  java -jar app.jar --spring.flyway.repair
```

### High Memory Usage

**Check Java heap:**
```bash
docker stats servicedesk-ticket-service-prod
```

**Adjust JVM options in Dockerfile:**
```dockerfile
ENV JAVA_OPTS="-Xms512m -Xmx2048m"
```

### WebSocket Connection Fails

**Check nginx websocket configuration:**
```nginx
location /ws {
    proxy_pass http://ticket-service:8080;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
}
```

---

## Performance Tuning

### Database Optimization

```sql
-- Create indexes for frequently queried fields
CREATE INDEX idx_tickets_status ON tickets(status) WHERE NOT deleted;
CREATE INDEX idx_tickets_assignee ON tickets(assignee_id) WHERE NOT deleted;
CREATE INDEX idx_tickets_created ON tickets(created_at DESC);

-- Analyze tables
ANALYZE tickets;
ANALYZE users;
```

### Application Tuning

```yaml
# application-prod.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

### Redis Caching

Enable caching for frequently accessed data:

```java
@Cacheable("tickets")
public TicketDTO getTicket(UUID id) {
    // ...
}
```

---

## Maintenance

### Rolling Updates

```bash
# Docker Compose
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d --no-deps ticket-service

# Kubernetes
kubectl set image deployment/ticket-service \
  ticket-service=servicedesk/ticket-service:0.2.0 \
  -n servicedesk
```

### Health Checks

```bash
# Manual health check
curl http://localhost:8080/actuator/health

# Automated monitoring
watch -n 5 'curl -s http://localhost:8080/actuator/health | jq'
```

---

## Support

For issues and questions:

- **GitHub Issues**: https://github.com/qahhor/FREE-SRVICEDESK3/issues
- **Documentation**: https://docs.servicedesk.io
- **Email**: support@greenwhite.uz

---

**Last Updated**: January 2024
**Version**: 0.1.0
