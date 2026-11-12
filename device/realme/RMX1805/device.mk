$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/full_base_telephony.mk)

PRODUCT_NAME := voidos_RMX1805
PRODUCT_DEVICE := RMX1805
PRODUCT_MANUFACTURER := Realme
PRODUCT_BRAND := realme
PRODUCT_MODEL := RMX1805

PRODUCT_PACKAGES += \
    VoidOSLauncher \
    VoidNetworkService \
    void_sanitizer

PRODUCT_SEPOLICY_DIRS += \
    device/realme/RMX1805/sepolicy/private
$(call inherit-product-if-exists, vendor/realme/RMX1805/RMX1805-vendor.mk)
