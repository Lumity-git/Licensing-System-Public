#!/bin/bash
# Simple upload script for DeadZone Licensing System
# Usage: ./upload.sh

# Config
TARGET_JAR="backend/target/license-backend-1.0.0.jar"
FRONTEND_BUILD_DIR="frontend/.next"
REMOTE_USER="l"
REMOTE_HOST="70.120.241.113"
REMOTE_PORT="2222"
REMOTE_DIR="/home/l/Licensing-Updated/"

# Upload backend jar
if [ -f "$TARGET_JAR" ]; then
    echo "Uploading backend jar to $REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR"
    scp -P $REMOTE_PORT "$TARGET_JAR" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR"
else
    echo "Backend jar not found: $TARGET_JAR"
fi

# Upload frontend build
if [ -d "$FRONTEND_BUILD_DIR" ]; then
    echo "Uploading frontend build to $REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR/frontend/.next"
    scp -P $REMOTE_PORT -r "$FRONTEND_BUILD_DIR" "$REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR/frontend/.next"
else
    echo "Frontend build directory not found: $FRONTEND_BUILD_DIR"
fi

echo "Upload complete."
