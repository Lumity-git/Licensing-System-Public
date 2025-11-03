#!/bin/bash

# DeadZone Licensing System - Automated Deployment Script
# This script builds and deploys the application to your production server

set -e  # Exit on any error

# Configuration
SERVER_HOST="${SERVER_HOST:-your.server.address}"
SERVER_PORT="${SERVER_PORT:-2222}"
SERVER_USER="${SERVER_USER:-root}"
REMOTE_PATH="${REMOTE_PATH:-/opt/deadzone-license}"
PROJECT_NAME="${PROJECT_NAME:-deadzone-license}"
API_DOMAIN="${API_DOMAIN:-api.your-domain.com}"
ADMIN_DOMAIN="${ADMIN_DOMAIN:-admin.your-domain.com}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check dependencies
check_dependencies() {
    print_status "Checking dependencies..."
    
    if ! command_exists docker; then
        print_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    
    if ! command_exists docker-compose; then
        print_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi
    
    if ! command_exists ssh; then
        print_error "SSH is not installed. Please install OpenSSH client first."
        exit 1
    fi
    
    if ! command_exists scp; then
        print_error "SCP is not installed. Please install OpenSSH client first."
        exit 1
    fi
    
    print_success "All dependencies are installed."
}

# Test SSH connection
test_ssh_connection() {
    print_status "Testing SSH connection to $SERVER_HOST:$SERVER_PORT..."
    
    if ssh -o ConnectTimeout=10 -o BatchMode=yes -p $SERVER_PORT $SERVER_USER@$SERVER_HOST "echo 'SSH connection successful'" 2>/dev/null; then
        print_success "SSH connection test passed."
    else
        print_error "SSH connection failed. Please check:"
        echo "  1. Server is accessible at $SERVER_HOST:$SERVER_PORT"
        echo "  2. SSH key is properly configured"
        echo "  3. User $SERVER_USER has access to the server"
        exit 1
    fi
}

# Build the application
build_application() {
    print_status "Building the application..."
    
    # Create production environment file
    if [ ! -f .env ]; then
        print_warning ".env file not found. Creating from .env.example..."
        cp .env.example .env
        print_warning "Please update the .env file with your production values before continuing."
        read -p "Press Enter when you've updated the .env file..."
    fi
    
    # Build Docker images
    print_status "Building Docker images..."
    docker-compose build --no-cache
    
    # Export images as tar files
    print_status "Exporting Docker images..."
    mkdir -p ./dist
    docker save deadzone-license-backend:latest | gzip > ./dist/deadzone-backend.tar.gz
    docker save deadzone-license-frontend:latest | gzip > ./dist/deadzone-frontend.tar.gz
    
    print_success "Application built successfully."
}

# Create deployment package
create_deployment_package() {
    print_status "Creating deployment package..."
    
    # Create deployment directory
    mkdir -p ./dist/deploy
    
    # Copy necessary files
    cp docker-compose.yml ./dist/deploy/
    cp Caddyfile ./dist/deploy/
    cp .env ./dist/deploy/
    
    # Create deployment scripts
    cat > ./dist/deploy/deploy.sh << 'EOF'
#!/bin/bash
set -e

echo "Starting DeadZone Licensing System deployment..."

# Load Docker images
echo "Loading Docker images..."
docker load < deadzone-backend.tar.gz
docker load < deadzone-frontend.tar.gz

# Stop existing containers
echo "Stopping existing containers..."
docker-compose down || true

# Start new containers
echo "Starting new containers..."
docker-compose up -d

# Wait for services to be healthy
echo "Waiting for services to start..."
sleep 30

# Check service health
echo "Checking service health..."
docker-compose ps

echo "Deployment completed successfully!"
echo "API: https://${API_DOMAIN}"
echo "Admin: https://${ADMIN_DOMAIN}"
EOF

    chmod +x ./dist/deploy/deploy.sh
    
    # Create update script
    cat > ./dist/deploy/update.sh << 'EOF'
#!/bin/bash
set -e

echo "Updating DeadZone Licensing System..."

# Pull latest images and restart
docker-compose pull
docker-compose up -d --force-recreate

echo "Update completed successfully!"
EOF

    chmod +x ./dist/deploy/update.sh
    
    print_success "Deployment package created."
}

# Deploy to server
deploy_to_server() {
    print_status "Deploying to server $SERVER_HOST:$SERVER_PORT..."
    
    # Create remote directory
    ssh -p $SERVER_PORT $SERVER_USER@$SERVER_HOST "mkdir -p $REMOTE_PATH"
    
    # Upload deployment package
    print_status "Uploading deployment package..."
    scp -P $SERVER_PORT -r ./dist/deploy/* $SERVER_USER@$SERVER_HOST:$REMOTE_PATH/
    
    # Upload Docker images
    print_status "Uploading Docker images..."
    scp -P $SERVER_PORT ./dist/deadzone-backend.tar.gz $SERVER_USER@$SERVER_HOST:$REMOTE_PATH/
    scp -P $SERVER_PORT ./dist/deadzone-frontend.tar.gz $SERVER_USER@$SERVER_HOST:$REMOTE_PATH/
    
    # Execute deployment script on server
    print_status "Executing deployment on server..."
    ssh -p $SERVER_PORT $SERVER_USER@$SERVER_HOST "cd $REMOTE_PATH && ./deploy.sh"
    
    print_success "Deployment completed successfully!"
}

# Update Caddy configuration
update_caddy_config() {
    print_status "Updating Caddy configuration..."
    
    ssh -p $SERVER_PORT $SERVER_USER@$SERVER_HOST << 'EOF'
# Copy Caddyfile to Caddy configuration directory
sudo mkdir -p /etc/caddy
sudo cp /opt/deadzone-license/Caddyfile /etc/caddy/Caddyfile

# Reload Caddy configuration
sudo systemctl reload caddy || sudo docker exec caddy caddy reload --config /etc/caddy/Caddyfile
EOF
    
    print_success "Caddy configuration updated."
}

# Cleanup local files
cleanup() {
    print_status "Cleaning up local files..."
    rm -rf ./dist
    print_success "Cleanup completed."
}

# Main deployment function
main() {
    echo "========================================"
    echo "DeadZone Licensing System Deployment"
    echo "========================================"
    echo ""
    
    print_status "Starting deployment process..."
    
    # Pre-flight checks
    check_dependencies
    test_ssh_connection
    
    # Build and package
    build_application
    create_deployment_package
    
    # Deploy
    deploy_to_server
    update_caddy_config
    
    # Cleanup
    if [ "${KEEP_BUILD_FILES}" != "true" ]; then
        cleanup
    fi
    
    echo ""
    echo "========================================"
    print_success "Deployment completed successfully!"
    echo "========================================"
    echo ""
    echo "Your DeadZone Licensing System is now running:"
    echo "  🌐 API:   https://${API_DOMAIN}"
    echo "  📱 Admin: https://${ADMIN_DOMAIN}"
    echo ""
    echo "To check the status:"
    echo "  ssh -p $SERVER_PORT $SERVER_USER@$SERVER_HOST 'cd $REMOTE_PATH && docker-compose ps'"
    echo ""
    echo "To view logs:"
    echo "  ssh -p $SERVER_PORT $SERVER_USER@$SERVER_HOST 'cd $REMOTE_PATH && docker-compose logs -f'"
    echo ""
}

# Handle script arguments
case "${1:-}" in
    --help|-h)
        echo "DeadZone Licensing System Deployment Script"
        echo ""
        echo "Usage: $0 [options]"
        echo ""
        echo "Options:"
        echo "  --help, -h     Show this help message"
        echo "  --build-only   Only build the application (don't deploy)"
        echo "  --deploy-only  Only deploy (assumes images are already built)"
        echo ""
        echo "Environment Variables:"
        echo "  KEEP_BUILD_FILES=true    Keep build files after deployment"
        echo ""
        exit 0
        ;;
    --build-only)
        check_dependencies
        build_application
        create_deployment_package
        print_success "Build completed. Files available in ./dist/"
        ;;
    --deploy-only)
        check_dependencies
        test_ssh_connection
        deploy_to_server
        update_caddy_config
        ;;
    "")
        main
        ;;
    *)
        print_error "Unknown option: $1"
        echo "Use --help for usage information."
        exit 1
        ;;
esac
