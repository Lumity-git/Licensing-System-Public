# 🎮 DeadZone Minecraft Plugin Licensing System

[![Production Ready](https://img.shields.io/badge/Status-Production%20Ready-brightgreen)](https://github.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-15-black)](https://nextjs.org)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.0-blue)](https://www.typescriptlang.org)

A complete, production-ready licensing system for the DeadZone Minecraft plugin featuring a secure Spring Boot backend API and a modern Next.js admin dashboard.

## 🚀 **PROJECT STATUS: COMPLETED & PRODUCTION READY**

✅ **Backend API**: Fully implemented with all endpoints working  
✅ **Frontend Dashboard**: Complete admin interface with all pages  
✅ **Authentication**: JWT-based security system  
✅ **Database**: H2 for development, production config ready  
✅ **Documentation**: Complete setup and deployment guides  
✅ **Production Ready**: Docker, monitoring, security hardening  

## 🏃‍♂️ Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- Maven 3.6+

### Running the Application

1. **Start Backend** (Port 8080):
```bash
cd backend
mvn spring-boot:run
```

2. **Start Frontend** (Port 3000):
```bash
cd frontend
npm install
npm run dev
```

3. **Access the Application**:
   - **Admin Dashboard**: http://localhost:3000
   - **Backend API**: http://localhost:8080
   - **H2 Console**: http://localhost:8080/h2-console

### Default Login
- **Username**: `admin`
- **Password**: `admin123`

## Environment Variables

### Backend Configuration
```yaml
# Database
DB_URL=jdbc:mysql://localhost:3306/deadzone_licenses
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password

# JWT
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Admin Bootstrap
ADMIN_USERNAME=admin
ADMIN_PASSWORD=secure_password
ADMIN_EMAIL=admin@deadzone.com

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://your-domain.com
```

### Frontend Configuration
```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.6+
- Node.js 18+
- MySQL/PostgreSQL (or use H2 for development)

### Backend Setup
1. Navigate to `backend/` directory
2. Configure database connection in `application.yml`
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
4. The API will be available at `http://localhost:8080`

### Frontend Setup
1. Navigate to `frontend/` directory
2. Install dependencies:
   ```bash
   npm install
   ```
3. Configure API URL in `.env.local`:
   ```env
   NEXT_PUBLIC_API_URL=http://localhost:8080
   ```
4. Start development server:
   ```bash
   npm run dev
   ```
5. The dashboard will be available at `http://localhost:3000`

## Database Schema

### Licenses Table
- `id` - Primary key
- `license_key` - Unique license identifier
- `type` - License type (TRIAL, SINGLE_SERVER, MULTI_SERVER, LIFETIME)
- `expiry_date` - License expiration date
- `max_servers` - Maximum number of servers allowed
- `buyer_name` - License purchaser name
- `buyer_email` - License purchaser email
- `is_active` - License status
- `created_at` - Creation timestamp
- `updated_at` - Last update timestamp

### Server Instances Table
- `id` - Primary key
- `license_id` - Foreign key to licenses table
- `server_id` - Unique server identifier
- `ip_address` - Server IP address
- `plugin_version` - Plugin version
- `server_name` - Server name
- `server_port` - Server port
- `last_seen` - Last validation timestamp
- `first_seen` - First validation timestamp
- `validation_count` - Number of validations

### Admin Users Table
- `id` - Primary key
- `username` - Admin username
- `password` - Hashed password
- `email` - Admin email
- `is_enabled` - Account status
- `created_at` - Creation timestamp
- `last_login` - Last login timestamp

## Security Features

- **JWT Authentication**: Stateless authentication with access and refresh tokens
- **Password Hashing**: BCrypt for secure password storage
- **CORS Configuration**: Configurable cross-origin resource sharing
- **Request Validation**: Input validation for all API endpoints
- **Rate Limiting**: Protection against abuse (configurable)
- **Signature Validation**: Plugin request signature verification

## Plugin Integration

The license validation endpoint expects requests in this format:

```json
{
  "license": "LICENSE_KEY",
  "server": "SERVER_ID", 
  "plugin": "PLUGIN_ID",
  "signature": "REQUEST_SIGNATURE",
  "version": "PLUGIN_VERSION"
}
```

Response format:
```json
{
  "valid": true,
  "type": "SINGLE_SERVER",
  "expiry": "2025-12-31",
  "allowedServers": 1,
  "message": "License validation successful"
}
```

## Development

### Running with H2 Database (Development)
The application is configured to use H2 in-memory database by default for development:
- H2 Console: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (empty)

### Production Deployment
1. Configure production database (MySQL/PostgreSQL)
2. Set secure JWT secret
3. Configure proper CORS origins
4. Set strong admin credentials
5. Use HTTPS in production
6. Consider using Docker for containerized deployment

## License Types

- **TRIAL**: Limited time trial license
- **SINGLE_SERVER**: License for one server
- **MULTI_SERVER**: License for multiple servers (configurable limit)
- **LIFETIME**: Permanent license with no expiration

## Monitoring and Logging

- Server activity tracking
- License usage analytics
- Authentication attempt logging
- Validation request monitoring
- Suspicious activity detection
