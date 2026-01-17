# How to Add AI Child to LineageOS

This guide shows you **exactly** which files to modify in LineageOS source.

---

## Quick Overview

You only need to modify **3 files** in LineageOS:

1. `frameworks/base/services/java/com/android/server/SystemServer.java` - Add AI Child startup
2. `frameworks/base/services/core/Android.bp` - Include AI Child source files
3. `build/make/target/product/base_system.mk` - Add AI Child to build

Then **copy** all our AI Child files into the right folders.

---

## Step-by-Step Instructions

### Step 1: Download LineageOS Source

```bash
# Create directory
mkdir -p ~/android/lineage && cd ~/android/lineage

# Initialize
repo init -u https://github.com/LineageOS/android.git -b lineage-21.0

# Download (this takes hours)
repo sync -c -j8
```

---

### Step 2: Copy AI Child Files

```bash
# Copy framework services
cp -r AIChildOS/frameworks/base/services/core/java/com/android/server/aichild \
      ~/android/lineage/frameworks/base/services/core/java/com/android/server/

# Copy launcher app
cp -r AIChildOS/packages/apps/AIChildHome \
      ~/android/lineage/packages/apps/

# Copy vendor config
cp -r AIChildOS/vendor/aichild \
      ~/android/lineage/vendor/
```

---

### Step 3: Modify SystemServer.java

**File:** `frameworks/base/services/java/com/android/server/SystemServer.java`

**Add import (around line 100):**
```java
import com.android.server.aichild.AIChildService;
```

**Add service startup (in startOtherServices method, around line 1200):**
```java
// AI Child OS
t.traceBegin("StartAIChildService");
try {
    Slog.i(TAG, "Starting AI Child Service...");
    mSystemServiceManager.startService(AIChildService.class);
} catch (Throwable e) {
    Slog.e(TAG, "Failed to start AI Child Service", e);
}
t.traceEnd();
```

---

### Step 4: Modify Android.bp

**File:** `frameworks/base/services/core/Android.bp`

**Find the `srcs` section and add:**
```
"java/com/android/server/aichild/**/*.java",
```

---

### Step 5: Add to Build

**File:** `build/make/target/product/base_system.mk`

**Add this line:**
```makefile
PRODUCT_PACKAGES += AIChildHome
```

---

### Step 6: Build the ROM

```bash
cd ~/android/lineage
source build/envsetup.sh
lunch lineage_<your_device>-userdebug
mka bacon
```

---

### Step 7: Flash

Flash the resulting ZIP file to your phone using TWRP recovery.

---

## Files We Created

| Our File | Goes To (in LineageOS) |
|----------|------------------------|
| `AIChildOS/frameworks/base/services/core/java/com/android/server/aichild/*` | `frameworks/base/services/core/java/com/android/server/aichild/` |
| `AIChildOS/packages/apps/AIChildHome/*` | `packages/apps/AIChildHome/` |
| `AIChildOS/vendor/aichild/*` | `vendor/aichild/` |

---

## That's It!

Once you follow these steps, your custom ROM will have the AI Child built right into it!
