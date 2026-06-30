$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64bit.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/aosp_base_telephony.mk)

PRODUCT_NAME := voidos_arm64
PRODUCT_DEVICE := voidos
PRODUCT_BRAND := voidOS
PRODUCT_MODEL := VoidOS Secure Device
PRODUCT_MANUFACTURER := voidOS_Labs

PRODUCT_PACKAGES += \
    VoidLauncher \
    void_sanitizer \
    libvoid_cryptoguard

TARGET_SYSTEM_PROP += system.prop

PRODUCT_MINIMIZE_JAVA_DEBUG_INFO := true
PRODUCT_ART_TARGET_BUILD := true
WITH_DEXPREOPT := true

PRODUCT_SYSTEM_DEFAULT_PROPERTIES += \
    ro.secure=1 \
    ro.adb.secure=1 \
    ro.debuggable=0
