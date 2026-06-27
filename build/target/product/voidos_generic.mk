# VoidOS — Universal Target Product Configuration Matrix
# Subsystem: Build System / Device Hardware Architecture Router
# Author: @pawancruz7 | Year: 2026
# Description: Defines global compilation targets enabling VoidOS Core 
# to build as a Generic System Image (GSI) supporting Project Treble devices.

PRODUCT_NAME := voidos_generic
PRODUCT_DEVICE := generic_arm64
PRODUCT_BRAND := VoidOS
PRODUCT_MODEL := VoidOS Universal Developer Core

# ------------------------------------------------------------------
# CORE FRAMEWORK INJECTION
# ------------------------------------------------------------------
# Inherit from standard AOSP generic arm64 configuration baseline
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64bit.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/gsi_common.mk)

# ------------------------------------------------------------------
# VOIDOS NATIVE CORE DAEMONS INTEGRATION
# ------------------------------------------------------------------
# Product packages to be compiled into the system partition (/system/bin)
PRODUCT_PACKAGES += \
    void_engine_core \
    void_crypto_core \
    void_storage_core \
    VoidStealthService \
    VoidExtensionEngine \
    void_security_hook

# ------------------------------------------------------------------
# HARDWARE ISOLATION & TREBLE COMPLIANCE
# ------------------------------------------------------------------
# Enforce systemless decoupling from vendor partition drivers
PRODUCT_COMPATIBILITY_MATRIX_LEVEL_OVERRIDE := 8
PRODUCT_SHIPPING_API_LEVEL := 34

# System property flags to boot the customized VoidOS kernel hooks
PRODUCT_SYSTEM_PROPERTIES += \
    ro.voidos.version=2026.1.prod \
    ro.voidos.freedom_mode=1 \
    ro.voidos.hardened_lsm=active \
    persist.sys.voidos.sandbox=strict

PRODUCT_MANUFACTURER := VoidOS-Community
