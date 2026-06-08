#!/bin/bash

echo "=== Starting VoidOS Mobile Build System ==="
echo "Initializing repository..."

# 1. Initialize AOSP Repo with VoidOS Manifest
repo init -u https://android.googlesource.com/platform/manifest -b android-14.0.0_r50

# 2. Sync the source code (Downloading core android architecture)
echo "Downloading core source code..."
repo sync -c -j$(nproc --all) --force-sync --no-clone-bundle --no-tags

# 3. Setup Environment
echo "Setting up build environment..."
source build/envsetup.sh

# 4. Lunch configuration (Targeting generic ARM64 devices)
echo "Configuring for generic smartphone (ARM64)..."
lunch aosp_arm64-userdebug

# 5. Build VoidOS System Images
echo "Compiling VoidOS..."
make systemimage -j$(nproc --all)

echo "=== VoidOS Build Completed Successfully! ==="
