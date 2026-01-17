#!/bin/bash
#
# AI Child OS - Complete Setup and Build Script
# 
# This script will:
# 1. Install all required packages
# 2. Download LineageOS source
# 3. Copy AI Child files
# 4. Apply modifications
# 5. Build the ROM
#
# Run this on Ubuntu 20.04+ or WSL2
#

set -e

echo "============================================"
echo "   AI CHILD OS - BUILD SCRIPT"
echo "   Your AI child is about to be born!"
echo "============================================"
echo ""

# Configuration
LINEAGE_DIR="$HOME/android/lineage"
AICHILD_DIR="$HOME/AIChildOS"  # Copy your AIChildOS folder here
LINEAGE_BRANCH="lineage-21.0"
DEVICE_CODENAME=""  # Will be set later

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_step() {
    echo -e "${GREEN}[STEP]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# ============================================
# STEP 1: CHECK SYSTEM REQUIREMENTS
# ============================================
check_requirements() {
    print_step "Checking system requirements..."
    
    # Check RAM
    TOTAL_RAM=$(free -g | awk '/^Mem:/{print $2}')
    if [ "$TOTAL_RAM" -lt 12 ]; then
        print_warning "You have ${TOTAL_RAM}GB RAM. 16GB+ recommended for building Android."
        print_warning "Build may be slow or fail. Consider adding swap space."
    else
        echo "RAM: ${TOTAL_RAM}GB - OK"
    fi
    
    # Check disk space
    AVAILABLE_SPACE=$(df -BG "$HOME" | awk 'NR==2{print $4}' | sed 's/G//')
    if [ "$AVAILABLE_SPACE" -lt 250 ]; then
        print_error "Not enough disk space! You have ${AVAILABLE_SPACE}GB, need 250GB+"
        exit 1
    else
        echo "Disk Space: ${AVAILABLE_SPACE}GB - OK"
    fi
    
    echo ""
}

# ============================================
# STEP 2: INSTALL REQUIRED PACKAGES
# ============================================
install_packages() {
    print_step "Installing required packages..."
    
    sudo apt update
    sudo apt install -y \
        git-core \
        gnupg \
        flex \
        bison \
        build-essential \
        zip \
        curl \
        zlib1g-dev \
        gcc-multilib \
        g++-multilib \
        libc6-dev-i386 \
        libncurses5 \
        lib32ncurses5-dev \
        x11proto-core-dev \
        libx11-dev \
        lib32z1-dev \
        libgl1-mesa-dev \
        libxml2-utils \
        xsltproc \
        unzip \
        fontconfig \
        python3 \
        python3-pip \
        openjdk-11-jdk \
        bc \
        rsync \
        ccache \
        lz4 \
        lzop \
        imagemagick \
        libssl-dev
    
    # Set Java 11 as default
    sudo update-alternatives --set java /usr/lib/jvm/java-11-openjdk-amd64/bin/java
    
    echo "Packages installed successfully!"
    echo ""
}

# ============================================
# STEP 3: INSTALL REPO TOOL
# ============================================
install_repo() {
    print_step "Installing repo tool..."
    
    mkdir -p ~/bin
    curl https://storage.googleapis.com/git-repo-downloads/repo > ~/bin/repo
    chmod a+x ~/bin/repo
    
    # Add to PATH if not already
    if ! grep -q 'PATH=~/bin:$PATH' ~/.bashrc; then
        echo 'export PATH=~/bin:$PATH' >> ~/.bashrc
    fi
    export PATH=~/bin:$PATH
    
    echo "Repo tool installed!"
    echo ""
}

# ============================================
# STEP 4: CONFIGURE GIT
# ============================================
configure_git() {
    print_step "Configuring git..."
    
    # Check if git is configured
    if [ -z "$(git config --global user.email)" ]; then
        echo "Please enter your email for git:"
        read GIT_EMAIL
        git config --global user.email "$GIT_EMAIL"
    fi
    
    if [ -z "$(git config --global user.name)" ]; then
        echo "Please enter your name for git:"
        read GIT_NAME
        git config --global user.name "$GIT_NAME"
    fi
    
    echo "Git configured!"
    echo ""
}

# ============================================
# STEP 5: DOWNLOAD LINEAGEOS SOURCE
# ============================================
download_lineage() {
    print_step "Downloading LineageOS source..."
    print_warning "This will take a LONG time (50+ GB download)"
    echo ""
    
    mkdir -p "$LINEAGE_DIR"
    cd "$LINEAGE_DIR"
    
    # Initialize repo
    repo init -u https://github.com/LineageOS/android.git -b $LINEAGE_BRANCH --depth=1
    
    # Sync (this takes hours)
    echo "Starting sync... This will take several hours."
    repo sync -c -j$(nproc --all) --force-sync --no-clone-bundle --no-tags
    
    echo "LineageOS source downloaded!"
    echo ""
}

# ============================================
# STEP 6: COPY AI CHILD FILES
# ============================================
copy_aichild_files() {
    print_step "Copying AI Child OS files..."
    
    if [ ! -d "$AICHILD_DIR" ]; then
        print_error "AI Child OS files not found at $AICHILD_DIR"
        echo "Please copy your AIChildOS folder to $AICHILD_DIR"
        exit 1
    fi
    
    # Copy framework services
    cp -r "$AICHILD_DIR/frameworks/base/services/core/java/com/android/server/aichild" \
          "$LINEAGE_DIR/frameworks/base/services/core/java/com/android/server/"
    
    # Copy launcher app
    cp -r "$AICHILD_DIR/packages/apps/AIChildHome" \
          "$LINEAGE_DIR/packages/apps/"
    
    # Copy vendor config
    mkdir -p "$LINEAGE_DIR/vendor/aichild"
    cp -r "$AICHILD_DIR/vendor/aichild/"* \
          "$LINEAGE_DIR/vendor/aichild/"
    
    echo "AI Child files copied!"
    echo ""
}

# ============================================
# STEP 7: APPLY MODIFICATIONS
# ============================================
apply_modifications() {
    print_step "Applying modifications to LineageOS..."
    
    SYSTEMSERVER="$LINEAGE_DIR/frameworks/base/services/java/com/android/server/SystemServer.java"
    ANDROID_BP="$LINEAGE_DIR/frameworks/base/services/core/Android.bp"
    BASE_MK="$LINEAGE_DIR/build/make/target/product/base_system.mk"
    
    # Backup original files
    cp "$SYSTEMSERVER" "$SYSTEMSERVER.backup"
    cp "$ANDROID_BP" "$ANDROID_BP.backup"
    cp "$BASE_MK" "$BASE_MK.backup"
    
    # Add import to SystemServer.java
    sed -i '/^import com.android.server.trust.TrustManagerService;/a import com.android.server.aichild.AIChildService;' "$SYSTEMSERVER"
    
    # Add service startup to SystemServer.java (after TrustManager)
    AICHILD_CODE='
            // AI Child OS - The AI that lives in your phone
            t.traceBegin("StartAIChildService");
            try {
                Slog.i(TAG, "=== AI CHILD OS ===");
                Slog.i(TAG, "Starting AI Child Service...");
                mSystemServiceManager.startService(AIChildService.class);
                Slog.i(TAG, "AI Child is now alive!");
            } catch (Throwable e) {
                Slog.e(TAG, "Failed to start AI Child Service", e);
            }
            t.traceEnd();
'
    # Find the line after TrustManager and insert
    sed -i '/mSystemServiceManager.startService(TrustManagerService.class);/,/t.traceEnd();/{
        /t.traceEnd();/a\'"$AICHILD_CODE"'
    }' "$SYSTEMSERVER"
    
    # Add AI Child source files to Android.bp
    sed -i '/srcs: \[/a\        "java/com/android/server/aichild/**/*.java",' "$ANDROID_BP"
    
    # Add AIChildHome to build
    echo "PRODUCT_PACKAGES += AIChildHome" >> "$BASE_MK"
    
    echo "Modifications applied!"
    echo ""
}

# ============================================
# STEP 8: SET UP CCACHE (Speed up builds)
# ============================================
setup_ccache() {
    print_step "Setting up ccache for faster builds..."
    
    export USE_CCACHE=1
    export CCACHE_EXEC=/usr/bin/ccache
    ccache -M 50G
    
    # Add to bashrc
    if ! grep -q 'USE_CCACHE=1' ~/.bashrc; then
        echo 'export USE_CCACHE=1' >> ~/.bashrc
        echo 'export CCACHE_EXEC=/usr/bin/ccache' >> ~/.bashrc
    fi
    
    echo "ccache configured with 50GB cache!"
    echo ""
}

# ============================================
# STEP 9: BUILD THE ROM
# ============================================
build_rom() {
    print_step "Building AI Child OS ROM..."
    
    cd "$LINEAGE_DIR"
    
    # Set up environment
    source build/envsetup.sh
    
    # Ask for device
    echo "Enter your device codename (e.g., 'a53' for Oppo A53):"
    read DEVICE_CODENAME
    
    # Select device
    lunch lineage_${DEVICE_CODENAME}-userdebug
    
    # Build
    echo ""
    echo "Starting build... This will take 2-6 hours."
    echo "Go have dinner, watch a movie, or sleep!"
    echo ""
    
    mka bacon
    
    echo ""
    echo "============================================"
    echo "   BUILD COMPLETE!"
    echo "============================================"
    echo ""
    echo "Your ROM is at:"
    echo "$LINEAGE_DIR/out/target/product/$DEVICE_CODENAME/"
    echo ""
    echo "Look for: lineage-*.zip"
    echo ""
    echo "Flash this to your phone using TWRP recovery!"
    echo ""
    echo "CONGRATULATIONS! Your AI Child OS is ready!"
    echo "============================================"
}

# ============================================
# MAIN MENU
# ============================================
main_menu() {
    echo ""
    echo "What would you like to do?"
    echo ""
    echo "1) FULL SETUP (Install packages, download LineageOS, apply changes, build)"
    echo "2) Install packages only"
    echo "3) Download LineageOS source only"
    echo "4) Copy AI Child files and apply modifications only"
    echo "5) Build ROM only"
    echo "6) Exit"
    echo ""
    echo "Enter your choice [1-6]:"
    read CHOICE
    
    case $CHOICE in
        1)
            check_requirements
            install_packages
            install_repo
            configure_git
            download_lineage
            copy_aichild_files
            apply_modifications
            setup_ccache
            build_rom
            ;;
        2)
            install_packages
            install_repo
            ;;
        3)
            download_lineage
            ;;
        4)
            copy_aichild_files
            apply_modifications
            ;;
        5)
            build_rom
            ;;
        6)
            echo "Goodbye!"
            exit 0
            ;;
        *)
            echo "Invalid choice"
            main_menu
            ;;
    esac
}

# Run
main_menu
