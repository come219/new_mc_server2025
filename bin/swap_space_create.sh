# Remove any existing swap file
sudo swapoff /swapfile
sudo sed -i '/\/swapfile/d' /etc/fstab
sudo rm /swapfile

# Create a 4GB -> 8GB swap file
sudo fallocate -l 8G /swapfile
# Or use dd if fallocate is not available
# sudo dd if=/dev/zero of=/swapfile bs=1M count=4096

# Set the correct permissions
sudo chmod 600 /swapfile

# Format the swap file
sudo mkswap /swapfile

# Enable the swap file
sudo swapon /swapfile

# Verify swap space
sudo swapon --show

# Make swap permanent
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Verify memory and swap usage
free -h

