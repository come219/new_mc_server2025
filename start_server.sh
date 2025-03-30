#!/bin/sh

# java -Xms#G -Xmx#G -XX:+UseG1GC -jar spigot.jar nogui
# java -Xms1G -Xmx2G -XX:+UseG1GC -jar spigot.jar nogui


# echo "3gb - Server starting at: $(date +"%T")"
# java -Xms1G -Xmx3G -XX:+UseG1GC -jar spigot.jar nogui
# echo "3gb - Server stopped at: $(date +"%T")"


# echo "4GB - Server starting at: $(date +"%T")"
# java -Xms2G -Xmx4G -XX:+UseG1GC -jar spigot.jar nogui
# echo "4GB - Server stopped at: $(date +"%T")"

echo "6GB - Server starting at: $(date +"%T")"
java -Xms4G -Xmx6G -XX:+UseG1GC -jar spigot.jar nogui
echo "6GB - Server stopped at: $(date +"%T")"

# start server command
# ./start_server.sh
