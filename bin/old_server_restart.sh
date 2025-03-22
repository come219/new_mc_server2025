# Server directory and JAR file
MCSERVER_DIR="/root/sebi/minecraft_server/new_mc_server2025"  # Change this to your server directory
JAR_FILE="spigot.jar"  # Change this if using a different JAR file
MEMORY_MIN="2G"  # Adjust as needed
MEMORY_MAX="4G"  # Adjust as needed
LOG_FILE="$MCSERVER_DIR/mc_server.log"

# Kill any running Minecraft server process
pkill -f "$JAR_FILE"

# Wait a few seconds to ensure process is stopped
sleep 5

# Restart the Minecraft server
cd "$MCSERVER_DIR" || exit 1
nohup java -Xms$MEMORY_MIN -Xmx$MEMORY_MAX -jar "$JAR_FILE" nogui >> "$LOG_FILE" 2>&1 &

echo "$(date) - Minecraft server restarted." >> "$LOG_FILE"
