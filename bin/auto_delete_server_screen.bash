#!/bin/bash

# Script to stop and delete the mc_server screen session

### 
# Windows env 
### 

# NONE 

### 
# CentOS env 
### 

# Check if the screen session exists
SESSION_NAME="mc_server"
SCREEN_ID=$(screen -ls | grep "$SESSION_NAME" | awk '{print $1}')

if [ -n "$SCREEN_ID" ]; then
    echo "Stopping and deleting the screen session: $SESSION_NAME..."
    screen -S "$SESSION_NAME" -X quit
    echo "Screen session $SESSION_NAME closed."
else
    echo "No active screen session named $SESSION_NAME found."
fi
