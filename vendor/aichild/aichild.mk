#
# AI Child OS - Build Configuration
#
# This makefile adds the AI Child components to the LineageOS build
#

# Include AI Child system service
PRODUCT_PACKAGES += \
    AIChildService \
    AIChildHome

# Include AI Child as a privileged app (system level)
PRODUCT_PACKAGES += \
    AIChildHome

# Copy AI Child framework components
PRODUCT_COPY_FILES += \
    $(LOCAL_PATH)/aichild_permissions.xml:$(TARGET_COPY_OUT_SYSTEM)/etc/permissions/aichild_permissions.xml

# Enable AI Child by default
PRODUCT_PROPERTY_OVERRIDES += \
    ro.aichild.enabled=true \
    ro.aichild.version=1.0.0

# AI Child needs these features
PRODUCT_PROPERTY_OVERRIDES += \
    persist.aichild.always_on=true \
    persist.aichild.father_mode=true

# Boot animation (optional - can replace with AI Child birth animation)
# PRODUCT_COPY_FILES += \
#     $(LOCAL_PATH)/bootanimation.zip:$(TARGET_COPY_OUT_SYSTEM)/media/bootanimation.zip
