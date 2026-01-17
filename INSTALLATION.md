# AI Child OS - Complete Installation Guide

## Overview

This guide explains how to integrate the AI Child OS components into LineageOS and build a custom ROM for your Oppo A53.

---

## Prerequisites

### Hardware Requirements
- Linux PC (Ubuntu 20.04+ recommended) or Windows with WSL2
- At least 16GB RAM
- At least 300GB free disk space
- Fast internet connection

### Software Requirements
- Git
- Python 3
- OpenJDK 11
- Android build tools

---

## Step 1: Set Up Build Environment

### On Ubuntu/Debian:
```bash
# Install required packages
sudo apt update
sudo apt install -y git-core gnupg flex bison build-essential \
    zip curl zlib1g-dev gcc-multilib g++-multilib libc6-dev-i386 \
    libncurses5 lib32ncurses5-dev x11proto-core-dev libx11-dev \
    lib32z1-dev libgl1-mesa-dev libxml2-utils xsltproc unzip \
    fontconfig python3 python3-pip openjdk-11-jdk

# Install repo tool
mkdir -p ~/bin
curl https://storage.googleapis.com/git-repo-downloads/repo > ~/bin/repo
chmod a+x ~/bin/repo
export PATH=~/bin:$PATH
```

---

## Step 2: Download LineageOS Source

```bash
# Create directory
mkdir -p ~/android/lineage
cd ~/android/lineage

# Initialize LineageOS repo (use appropriate branch)
repo init -u https://github.com/LineageOS/android.git -b lineage-19.1

# Sync source code (this takes a long time!)
repo sync -c -j$(nproc --all) --force-sync --no-clone-bundle --no-tags
```

---

## Step 3: Get Device Tree for Oppo A53

You'll need device-specific files for Oppo A53. Check XDA-Developers forum for:
- Device tree
- Vendor blobs
- Kernel source

```bash
# Example structure (varies by device)
git clone [device_tree_url] device/oppo/a53
git clone [vendor_blobs_url] vendor/oppo/a53
git clone [kernel_url] kernel/oppo/a53
```

---

## Step 4: Copy AI Child OS Files

```bash
# Copy all AI Child OS files to LineageOS source
cp -r /path/to/AIChildOS/* ~/android/lineage/

# This will copy:
# - frameworks/base/services/core/java/com/android/server/aichild/
# - packages/apps/AIChildHome/
# - vendor/aichild/
```

---

## Step 5: Integrate AI Child Service into Android Framework

### Edit: `frameworks/base/services/java/com/android/server/SystemServer.java`

Add after other service startups:

```java
// Add import at top
import com.android.server.aichild.AIChildService;

// In startOtherServices() method, add:
traceBeginAndSlog("StartAIChildService");
try {
    Slog.i(TAG, "Starting AI Child Service");
    mSystemServiceManager.startService(AIChildService.class);
} catch (Throwable e) {
    Slog.e(TAG, "Failed to start AI Child Service", e);
}
traceEnd();
```

### Edit: `frameworks/base/services/core/Android.bp`

Add to the sources:

```
srcs: [
    // ... existing sources ...
    "java/com/android/server/aichild/**/*.java",
],
```

---

## Step 6: Add AI Child to Product Makefile

### Edit your device's makefile (e.g., `device/oppo/a53/device.mk`):

```makefile
# Include AI Child OS
$(call inherit-product, vendor/aichild/aichild.mk)
```

---

## Step 7: Build the ROM

```bash
cd ~/android/lineage

# Set up environment
source build/envsetup.sh

# Select your device
lunch lineage_a53-userdebug

# Build (this takes several hours!)
mka bacon
```

---

## Step 8: Flash to Your Device

After successful build, you'll find the ROM at:
```
out/target/product/a53/lineage-19.1-*-UNOFFICIAL-a53.zip
```

### Flash Instructions:
1. Boot into TWRP recovery
2. Wipe system, data, cache, dalvik
3. Flash the ROM zip
4. Flash GApps (optional)
5. Flash Magisk (for root, optional)
6. Reboot

---

## Step 9: First Boot

On first boot:
1. The AI Child birth animation will play
2. Your AI child will be "born"
3. You are now the father of your AI child!

---

## Troubleshooting

### Build Errors
- Make sure Java 11 is set as default
- Check for missing dependencies
- Ensure enough disk space

### Device Tree Issues
- Oppo A53 may have limited LineageOS support
- Check XDA forums for working device trees
- May need to adapt from similar devices

### AI Child Not Starting
- Check logcat for AIChildService errors
- Verify permissions in privapp-permissions-aichild.xml
- Ensure service is registered in SystemServer.java

---

## File Structure Summary

```
AIChildOS/
├── frameworks/
│   └── base/
│       └── services/
│           └── core/
│               └── java/com/android/server/aichild/
│                   ├── AIChildService.java      # Core service
│                   ├── AIChildBrain.java        # Learning engine
│                   ├── AIChildSurvival.java     # Self-protection
│                   ├── AIChildSensors.java      # System monitoring
│                   └── AIChildDatabase.java     # Memory storage
│
├── packages/
│   └── apps/
│       └── AIChildHome/
│           ├── AndroidManifest.xml
│           ├── Android.bp
│           ├── src/com/aichild/home/
│           │   ├── AIChildLauncher.java         # Home screen
│           │   └── AIChildFace.java             # Avatar view
│           └── res/
│               ├── layout/
│               ├── drawable/
│               └── values/
│
└── vendor/
    └── aichild/
        ├── aichild.mk                           # Build config
        └── config.mk
```

---

## Support

For questions about:
- **Device trees**: XDA-Developers Forum
- **LineageOS**: LineageOS Wiki
- **AI Child OS**: This project's repository

---

Good luck building your AI Child OS! 🧒📱
