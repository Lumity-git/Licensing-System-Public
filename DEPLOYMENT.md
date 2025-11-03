# DeadZone Licensing System - Deployment Guide

This guide will help you deploy the DeadZone Licensing System to your production server using Docker and Caddy.

## 🚀 Quick Deployment

### Prerequisites

- A server with Ubuntu 20.04+ or similar Linux distribution
- SSH access to your server (port 2222 in this setup)
- DNS records pointing to your server:
  - `api.your-domain.com` → Your server IP
  - `admin.your-domain.com` → Your server IP

> ℹ️ Replace placeholder values such as `<SERVER_HOST>` and `<POSTGRES_PASSWORD>` with real deployment details before running the commands below.

### Step 1: Prepare Your Server

Upload and run the server setup script on your server:

```bash
# Upload the setup script to your server
scp -P 2222 server-setup.sh <DEPLOY_USER>@<SERVER_HOST>:/tmp/

# SSH into your server and run the setup
ssh -p 2222 <DEPLOY_USER>@<SERVER_HOST>
sudo bash /tmp/server-setup.sh
```

This script will install:
- Docker & Docker Compose
- Caddy web server
- Configure firewall (UFW)
- Create project directories
- Set up log rotation

### Step 2: Configure Environment Variables

Create a `.env` file from the example:

```bash
cp .env.example .env
```

Edit the `.env` file with your production values:

```env
# PostgreSQL Database Configuration
POSTGRES_PASSWORD=<POSTGRES_PASSWORD>

# JWT Configuration (Generate a secure 256-bit key)
JWT_SECRET=your-super-secure-jwt-secret-key-that-should-be-at-least-256-bits

# Admin User Configuration
ADMIN_USERNAME=admin
ADMIN_PASSWORD=your_secure_admin_password
ADMIN_EMAIL=admin@yourdomain.com

# Next.js Configuration
NEXT_PUBLIC_API_URL=https://api.your-domain.com

# Server Configuration
SERVER_PORT=8080
```

### Step 3: Deploy to Production

Run the automated deployment script:

```bash
./deploy.sh
```

The script will:
1. ✅ Build Docker images for backend and frontend
2. ✅ Package the application for deployment
3. ✅ Upload everything to your server via SSH/SCP
4. ✅ Deploy containers using Docker Compose
5. ✅ Configure Caddy reverse proxy
6. ✅ Start all services

## 🔧 Manual Deployment (Alternative)

If you prefer to deploy manually:

### Build Images

```bash
docker-compose build
```

### Deploy to Server

```bash
# Create deployment package
mkdir -p dist
docker save deadzone-license-backend:latest | gzip > dist/deadzone-backend.tar.gz
docker save deadzone-license-frontend:latest | gzip > dist/deadzone-frontend.tar.gz

# Upload to server
scp -P 2222 -r dist/* <SERVER_USER>@<SERVER_HOST>:/opt/deadzone-license/
scp -P 2222 docker-compose.yml <SERVER_USER>@<SERVER_HOST>:/opt/deadzone-license/
scp -P 2222 Caddyfile <SERVER_USER>@<SERVER_HOST>:/opt/deadzone-license/
scp -P 2222 .env <SERVER_USER>@<SERVER_HOST>:/opt/deadzone-license/

# Deploy on server
ssh -p 2222 <SERVER_USER>@<SERVER_HOST> "cd /opt/deadzone-license && docker load < deadzone-backend.tar.gz && docker load < deadzone-frontend.tar.gz && docker-compose up -d"
```

## 🌐 Services

After successful deployment, your services will be available at:

- **API Backend**: https://api.your-domain.com
- **Admin Dashboard**: https://admin.your-domain.com

### API Endpoints

- `GET /api/health` - Health check
- `POST /api/auth/login` - Admin login
- `GET /api/licenses` - List licenses
- `POST /api/licenses` - Create license
- `POST /api/validate` - Validate license (for Minecraft plugin)

## 📊 Monitoring & Management

### Check Service Status

```bash
ssh -p 2222 <SERVER_USER>@<SERVER_HOST> "cd /opt/deadzone-license && docker-compose ps"
```

### View Logs

```bash
# All services
ssh -p 2222 <SERVER_USER>@<SERVER_HOST> "cd /opt/deadzone-license && docker-compose logs -f"

# Specific service
ssh -p 2222 <SERVER_USER>@<SERVER_HOST> "cd /opt/deadzone-license && docker-compose logs -f deadzone-backend"

# Caddy logs
ssh -p 2222 <SERVER_USER>@<SERVER_HOST> "tail -f /var/log/caddy/api.your-domain.com.log"
```

### Restart Services

```bash
ssh -p 2222 <SERVER_USER>@<SERVER_HOST> "cd /opt/deadzone-license && docker-compose restart"
```

### Update Application

```bash
# Run the deployment script again
./deploy.sh

# Or update manually
ssh -p 2222 <SERVER_USER>@<SERVER_HOST> "cd /opt/deadzone-license && ./update.sh"
```

## 🔒 Security Features

- **HTTPS Only**: All traffic is encrypted via Caddy's automatic HTTPS
- **JWT Authentication**: Secure token-based authentication
- **CORS Protection**: Configured for your domains only
- **Security Headers**: HSTS, XSS protection, content type options
- **Firewall**: UFW configured to allow only necessary ports
- **Non-root Containers**: All containers run as non-root users

## 🗄️ Database

- **Development**: H2 in-memory database
- **Production**: PostgreSQL with persistent storage
- **Backups**: Located in Docker volume `deadzone_postgres_data`

### Database Backup

```bash
ssh -p 2222 <SERVER_USER>@<SERVER_HOST> "docker exec deadzone-postgres pg_dump -U deadzone deadzone_license > /opt/deadzone-license/backup-$(date +%Y%m%d).sql"
```

## 🔄 Updates & Maintenance

### Update Dependencies

Rebuild and redeploy when updating dependencies:

```bash
./deploy.sh
```

### SSL Certificate Renewal

Caddy automatically renews SSL certificates. Check status:

```bash
ssh -p 2222 <SERVER_USER>@<SERVER_HOST> "systemctl status caddy"
```

## 🆘 Troubleshooting

### Common Issues

1. **Services not starting**: Check logs and ensure all environment variables are set
2. **SSL issues**: Verify DNS records point to your server
3. **Database connection**: Ensure PostgreSQL container is healthy
4. **CORS errors**: Check `CORS_ALLOWED_ORIGINS` in environment variables

### Debug Commands

```bash
# Check container health
docker-compose ps

# Inspect container logs
docker-compose logs [service-name]

# Test connectivity
curl -k https://api.your-domain.com/api/health
curl -k https://admin.your-domain.com/api/health

# Verify Caddy config
caddy validate --config /etc/caddy/Caddyfile
```

## 📋 Default Credentials

- **Admin Username**: admin (or value from `ADMIN_USERNAME`)
- **Admin Password**: admin123 (or value from `ADMIN_PASSWORD`)

**⚠️ Important**: Change the default admin password after first login!

## 🎯 Production Checklist

- [ ] Server setup completed
- [ ] DNS records configured
- [ ] Environment variables set
- [ ] SSL certificates working
- [ ] Admin credentials changed
- [ ] Database backups configured
- [ ] Monitoring set up
- [ ] Log rotation working

## 🤝 Support

If you encounter issues:

1. Check the logs for error messages
2. Verify all environment variables are correctly set
3. Ensure DNS records are properly configured
4. Check firewall settings on your server

The deployment script includes comprehensive error checking and will guide you through any issues that arise during deployment.
