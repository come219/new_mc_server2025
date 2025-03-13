#!/bin/bash

# Script to view and attach to the mc_server screen session

SESSION_NAME="mc_server"

# Check if the screen session exists
SCREEN_ID=$(screen -ls | grep "$SESSION_NAME" | awk '{print $1}')

if [ -n "$SCREEN_ID" ]; then
    echo "Attaching to screen session: $SESSION_NAME..."
    screen -r "$SESSION_NAME"
else
    echo "No active screen session named $SESSION_NAME found."
    screen -ls  # List all available sessions
fi
