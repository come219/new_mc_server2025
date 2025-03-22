#!/bin/bash

# Server directory and script file
MCSERVER_DIR="/root/sebi/minecraft_server/new_mc_server2025"  # Change as needed
START_SCRIPT="start_server.sh"  # The script to start the server
LOG_FILE="$MCSERVER_DIR/mc_server.log"
SCREEN_NAME="mc_server"

# Kill any running Minecraft server process safely
if screen -list | grep -q "$SCREEN_NAME"; then
    echo "$(date) - Stopping Minecraft server..." >> "$LOG_FILE"
    screen -X -S "$SCREEN_NAME" quit
    sleep 5  # Wait for proper shutdown
fi

# Restart the Minecraft server in a screen session
cd "$MCSERVER_DIR" || exit 1

echo "$(date) - Starting Minecraft server..." >> "$LOG_FILE"

# Start the server in a detached screen session
screen -dmS "$SCREEN_NAME" bash "$START_SCRIPT" >> "$LOG_FILE" 2>&1

echo "$(date) - Minecraft server restarted using $START_SCRIPT in screen session: $SCREEN_NAME" >> "$LOG_FILE"

