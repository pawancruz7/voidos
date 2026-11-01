PRODUCT_NAME := voidos_generic
PRODUCT_DEVICE := generic_arm64
PRODUCT_BRAND := VoidOS
PRODUCT_MODEL := VoidOS Universal Developer Core
PRODUCT_MANUFACTURER := VoidOS-Community

$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64bit.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/gsi_common.mk)

PRODUCT_PACKAGES += \
    void_engine_core \
    void_crypto_core \
    void_storage_core \
    VoidStealthService \
    VoidExtensionEngine \
    void_security_hook

PRODUCT_COMPATIBILITY_MATRIX_LEVEL_OVERRIDE := 8
PRODUCT_SHIPPING_API_LEVEL := 34

PRODUCT_SYSTEM_PROPERTIES += \
    ro.voidos.version=2026.1.prod \
    ro.voidos.freedom_mode=1 \
    ro.voidos.hardened_lsm=active \
    persist.sys.voidos.sandbox=strict
