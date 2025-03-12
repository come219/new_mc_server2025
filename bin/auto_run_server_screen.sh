 #!/bin/bash

# cat run_backend_impl.sh

###
# windows env
###

# NONE


###
# centos env
###

# Navigate to the mc server directory
cd /root/sebi/minecraft_server/new_mc_server2025

# Start the mc server in a screen session
echo "Starting the mc server in a screen session..."
screen -dmS mc_server bash start_server.sh

# python3 /root/sebi/backend-lstand/src/server.py

# Get the process ID of the backend server
BACKEND_PID=$(screen -ls | grep mc_server | awk '{print $1}')
echo "mc_server server started with screen session ID: $BACKEND_PID"




# chmod +x run_backend.sh
# ./run_backend.sh

# screen -r backend_server



# step by step:

# screen -S server_session

# ./run_backend_impl.sh
# bash run_backend_impl.sh

# screen -ls

# screen -r server_session

# [root@centos-s-1vcpu-1gb-lon1-01 bin]#