# VoidOS Target Product Configuration
# Subsystem: Build System / Device Target Router
# Generic System Image (GSI) target configuration for ARM64 platforms.

PRODUCT_NAME := voidos_generic
PRODUCT_DEVICE := generic_arm64
PRODUCT_BRAND := VoidOS
PRODUCT_MODEL := VoidOS Universal Developer Core
PRODUCT_MANUFACTURER := VoidOS-Community

# Inherit standard AOSP arm64 architecture base
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64bit.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/gsi_common.mk)

# VoidOS System Core Packages
PRODUCT_PACKAGES += \
    void_engine_core \
    void_crypto_core \
    void_storage_core \
    VoidStealthService \
    VoidExtensionEngine \
    void_security_hook

# Hardware Isolation & Treble Compatibility
PRODUCT_COMPATIBILITY_MATRIX_LEVEL_OVERRIDE := 8
PRODUCT_SHIPPING_API_LEVEL := 34

# System Properties
PRODUCT_SYSTEM_PROPERTIES += \
    ro.voidos.version=2026.1.prod \
    ro.voidos.freedom_mode=1 \
    ro.voidos.hardened_lsm=active \
    persist.sys.voidos.sandbox=strict
