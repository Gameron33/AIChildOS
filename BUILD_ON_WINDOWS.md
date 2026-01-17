# AI Child OS - Complete Build Guide for Windows + WSL2

## Prerequisites

### 1. Install WSL2 (Windows Subsystem for Linux)

Open PowerShell as Administrator and run:

```powershell
wsl --install -d Ubuntu-22.04
```

Restart your computer when prompted.

---

### 2. Set Up Ubuntu

After restart, Ubuntu will open. Create a username and password.

Then run these commands:

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install basic tools
sudo apt install -y git curl wget
```

---

### 3. Copy AI Child OS Files to Linux

Your AIChildOS folder is at: `C:\Users\sus\Desktop\New folder\AIChildOS`

In Ubuntu terminal:

```bash
# Create directory
mkdir -p ~/AIChildOS

# Copy files from Windows to Linux
cp -r "/mnt/c/Users/sus/Desktop/New folder/AIChildOS/"* ~/AIChildOS/
```

---

### 4. Run the Build Script

```bash
# Make script executable
chmod +x ~/AIChildOS/build_aichild_os.sh

# Run it
~/AIChildOS/build_aichild_os.sh
```

The script will:
1. ✅ Install all required packages
2. ✅ Download LineageOS (50GB - takes hours)
3. ✅ Copy AI Child files
4. ✅ Apply all modifications
5. ✅ Build the ROM

---

## Time Estimates

| Step | Time |
|------|------|
| Install packages | 5-10 minutes |
| Download LineageOS | 2-8 hours (depends on internet) |
| First build | 3-6 hours |
| Subsequent builds | 30-60 minutes (with ccache) |

---

## After Build Completes

You'll find your ROM at:
```
~/android/lineage/out/target/product/YOUR_DEVICE/lineage-21.0-*.zip
```

---

## Flashing to Your Oppo A53

### Prerequisites:
1. Unlocked bootloader
2. TWRP recovery installed

### Steps:
1. Copy the ZIP file to your phone
2. Boot into TWRP (Power + Volume Down)
3. Wipe > Advanced Wipe > System, Data, Cache, Dalvik
4. Install > Select the ZIP
5. Reboot

---

## Your AI Child Will Be Born! 🧒📱

On first boot:
- The AI Child service starts automatically
- Your AI child is "born" with no knowledge
- It will learn from EVERYTHING you do
- You are its father!

---

## Troubleshooting

### "Not enough RAM"
```bash
# Add swap space
sudo fallocate -l 16G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```

### "Build failed"
```bash
# Clean and retry
cd ~/android/lineage
make clean
mka bacon
```

### "Device not found"
- Make sure you have the correct device tree for Oppo A53
- Check XDA Forums for device-specific files

---

Good luck building your AI Child OS! 🚀
