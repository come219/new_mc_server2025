:: @echo off
:: java -Xms2G -Xmx2G -XX:+UseG1GC -jar spigot.jar nogui
:: pause


@echo off
echo Server starting at: %TIME%  
java -Xms4G -Xmx4G -XX:+UseG1GC -jar spigot.jar nogui  
echo Server stopped at: %TIME%  
pause
