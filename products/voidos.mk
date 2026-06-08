# ============================================================================
#  VoidOS Core Product Definition Matrix
#  Target: AOSP Core Compilation & Subsystem Injection Linker
#  Architect: voidOS Core Platform Architecture
# ============================================================================

# Inherit from standard generic ARM64 smartphone core configuration
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64bit.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/aosp_base_telephony.mk)

# Operating System Identification Tags
PRODUCT_NAME := voidos_arm64
PRODUCT_DEVICE := voidos
PRODUCT_BRAND := voidOS
PRODUCT_MODEL := VoidOS Secure Device
PRODUCT_MANUFACTURER := voidOS_Labs

# 1. VoidOS UI Component Injections
# Overrides stock launchers and enforces our custom dynamic interface
PRODUCT_PACKAGES += \
    VoidLauncher

# 2. Hardened Native Security Daemons Insertion
# Binds our memory purifier and hardware firewall registers at target flash
PRODUCT_PACKAGES += \
    void_sanitizer \
    libvoid_cryptoguard

# 3. Inherit Custom Security Properties Matrix
# Injects telemetry blockers and privacy fuzzer settings directly into system/build.prop
TARGET_SYSTEM_PROP += system.prop

# 4. Global Build Optimization Overrides
# Forces high-efficiency compiler compilation flags across the entire ROM
PRODUCT_MINIMIZE_JAVA_DEBUG_INFO := true
PRODUCT_ART_TARGET_BUILD := true
WITH_DEXPREOPT := true

# Security Build Variant Tags Configuration
PRODUCT_SYSTEM_DEFAULT_PROPERTIES += \
    ro.secure=1 \
    ro.adb.secure=1 \
    ro.debuggable=0
