LOCAL_PATH := $(call my-dir)

BOARD_SEPOLICY_DIRS += \
    $(LOCAL_PATH)/sepolicy/private

TARGET_ARCH := arm64
TARGET_ARCH_VARIANT := armv8-a
TARGET_CPU_ABI := arm64-v8a
TARGET_CPU_ABI2 :=
TARGET_CPU_VARIANT := generic
