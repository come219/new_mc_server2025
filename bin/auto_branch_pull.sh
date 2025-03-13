#!/bin/bash

# Navigate to the repository (optional, adjust as needed)
# cd /path/to/your/repo

# Fetch all updates from remote
echo "Fetching updates from remote..."
git fetch --all

# Get current date in YYYY-MM-DD format
CURRENT_DATE=$(date +"%Y-%m-%d")

# Create a backup branch of the current main state
BACKUP_BRANCH="old_backup_main_$CURRENT_DATE"
echo "Creating backup branch: $BACKUP_BRANCH"
git branch "$BACKUP_BRANCH"

# Hard reset main branch to origin/main
echo "Resetting local main branch to match origin/main..."
git reset --hard origin/main

echo "Done!"
