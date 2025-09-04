#!/bin/bash

# Server Setup Script for DeadZone Licensing System
# Run this script on your server to prepare it for the DeadZone deployment

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

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

# Update system
update_system() {
    print_status "Updating system packages..."
    apt update && apt upgrade -y
    print_success "System updated."
}

# Install Docker
install_docker() {
    if command -v docker >/dev/null 2>&1; then
        print_warning "Docker is already installed."
        return
    fi
    
    print_status "Installing Docker..."
    
    # Install prerequisites
    apt install -y apt-transport-https ca-certificates curl gnupg lsb-release
    
    # Add Docker GPG key
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
    
    # Add Docker repository
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
    
    # Install Docker
    apt update
    apt install -y docker-ce docker-ce-cli containerd.io
    
    # Start and enable Docker
    systemctl start docker
    systemctl enable docker
    
    print_success "Docker installed and started."
}

# Install Docker Compose
install_docker_compose() {
    if command -v docker-compose >/dev/null 2>&1; then
        print_warning "Docker Compose is already installed."
        return
    fi
    
    print_status "Installing Docker Compose..."
    
    # Install Docker Compose
    curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    
    print_success "Docker Compose installed."
}

# Install and configure Caddy
install_caddy() {
    if command -v caddy >/dev/null 2>&1; then
        print_warning "Caddy is already installed."
        return
    fi
    
    print_status "Installing Caddy..."
    
    # Install Caddy
    apt install -y debian-keyring debian-archive-keyring apt-transport-https
    curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' | gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
    curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' | tee /etc/apt/sources.list.d/caddy-stable.list
    apt update
    apt install -y caddy
    
    # Create Caddy directories
    mkdir -p /etc/caddy /var/log/caddy
    chown -R caddy:caddy /var/log/caddy
    
    # Enable Caddy service
    systemctl enable caddy
    
    print_success "Caddy installed and configured."
}

# Configure firewall
configure_firewall() {
    print_status "Configuring firewall..."
    
    # Install UFW if not present
    apt install -y ufw
    
    # Reset UFW rules
    ufw --force reset
    
    # Set default policies
    ufw default deny incoming
    ufw default allow outgoing
    
    # Allow SSH
    ufw allow 2222/tcp comment 'SSH'
    
    # Allow HTTP and HTTPS
    ufw allow 80/tcp comment 'HTTP'
    ufw allow 443/tcp comment 'HTTPS'
    
    # Enable firewall
    ufw --force enable
    
    print_success "Firewall configured."
}

# Create project directory
create_project_directory() {
    print_status "Creating project directory..."
    
    mkdir -p /opt/deadzone-license
    chown -R root:root /opt/deadzone-license
    
    print_success "Project directory created at /opt/deadzone-license."
}

# Configure log rotation
configure_log_rotation() {
    print_status "Configuring log rotation..."
    
    cat > /etc/logrotate.d/deadzone-license << EOF
/var/log/caddy/*.log {
    daily
    missingok
    rotate 52
    compress
    delaycompress
    notifempty
    create 644 caddy caddy
    postrotate
        systemctl reload caddy
    endscript
}

/opt/deadzone-license/logs/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 root root
}
EOF
    
    print_success "Log rotation configured."
}

# Main setup function
main() {
    echo "========================================"
    echo "DeadZone Licensing System Server Setup"
    echo "========================================"
    echo ""
    
    # Check if running as root
    if [ "$EUID" -ne 0 ]; then
        print_error "This script must be run as root (use sudo)."
        exit 1
    fi
    
    print_status "Starting server setup..."
    
    update_system
    install_docker
    install_docker_compose
    install_caddy
    configure_firewall
    create_project_directory
    configure_log_rotation
    
    echo ""
    echo "========================================"
    print_success "Server setup completed successfully!"
    echo "========================================"
    echo ""
    echo "Your server is now ready for DeadZone Licensing System deployment."
    echo ""
    echo "Next steps:"
    echo "  1. Run the deployment script from your local machine:"
    echo "     ./deploy.sh"
    echo ""
    echo "  2. The deployment will automatically:"
    echo "     - Build and upload the application"
    echo "     - Configure Docker containers"
    echo "     - Set up Caddy reverse proxy"
    echo "     - Start all services"
    echo ""
    echo "Services installed:"
    echo "  ✓ Docker & Docker Compose"
    echo "  ✓ Caddy web server"
    echo "  ✓ UFW firewall (ports 80, 443, 2222)"
    echo "  ✓ Log rotation"
    echo ""
}

# Run main function
main "$@"
