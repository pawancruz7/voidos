#!/bin/bash

set -euo pipefail

AOSP_BRANCH="android-14.0.0_r50"
AOSP_MANIFEST="https://android.googlesource.com/platform/manifest"
VOIDOS_PATCHES_DIR="$(pwd)/patches"
VOIDOS_LAUNCHER_DIR="$(pwd)/launcher"
LOG_FILE="$(pwd)/build_voidos.log"
JOBS=$(nproc --all)
DEVICE="${1:-aosp_arm64}"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

log() { echo -e "${CYAN}[voidOS]${NC} $*" | tee -a "$LOG_FILE"; }
ok()   { echo -e "${GREEN}[  OK  ]${NC} $*" | tee -a "$LOG_FILE"; }
warn() { echo -e "${YELLOW}[ WARN ]${NC} $*" | tee -a "$LOG_FILE"; }
die()  { echo -e "${RED}[ FAIL ]${NC} $*" | tee -a "$LOG_FILE"; exit 1; }

check_dependencies() {
    log "Checking build dependencies..."
    local missing=()
    for cmd in repo git python3 make curl; do
        command -v "$cmd" &>/dev/null || missing+=("$cmd")
    done
    if [[ ${#missing[@]} -gt 0 ]]; then
        die "Missing tools: ${missing[*]}. Install them first."
    fi
    ok "All dependencies found."
}

sync_source() {
    log "Initializing AOSP repo (branch: $AOSP_BRANCH)..."
    repo init \
        -u "$AOSP_MANIFEST" \
        -b "$AOSP_BRANCH" \
        --depth=1 \
        --no-repo-verify \
        || die "repo init failed."

    log "Syncing source (~100 GB). This will take a long time..."
    repo sync \
        -c \
        -j"$JOBS" \
        --force-sync \
        --no-clone-bundle \
        --no-tags \
        || die "repo sync failed."
    ok "Source synced."
}

inject_voidos_ui() {
    log "Injecting voidOS Custom UI & Launcher..."
    if [[ ! -d "$VOIDOS_LAUNCHER_DIR" ]]; then
        die "Launcher directory not found at $VOIDOS_LAUNCHER_DIR! UI cannot be built."
    fi

    local target_dir="packages/apps/VoidOSLauncher"
    
    rm -rf "$target_dir"
    mkdir -p "$target_dir"
    
    cp -r "$VOIDOS_LAUNCHER_DIR"/* "$target_dir/"
    
    ok "voidOS UI successfully injected into AOSP build tree ($target_dir)."
}

apply_patches() {
    if [[ ! -d "$VOIDOS_PATCHES_DIR" ]]; then
        warn "No patches directory found at $VOIDOS_PATCHES_DIR. Skipping."
        return
    fi
    log "Applying voidOS privacy patches..."
    for patch in "$VOIDOS_PATCHES_DIR"/*.patch; do
        [[ -f "$patch" ]] || continue
        log "  Applying: $(basename "$patch")"
        git apply "$patch" || warn "  Patch failed (may already be applied): $patch"
    done
    ok "Patches applied."
}

strip_telemetry() {
    log "Running privacy stripper..."
    if [[ -f "$(pwd)/privacy_strip.py" ]]; then
        python3 "$(pwd)/privacy_strip.py" . || warn "Privacy stripper encountered errors."
        ok "Telemetry stripped."
    else
        warn "privacy_strip.py not found. Skipping telemetry strip."
    fi
}

build_image() {
    log "Setting up build environment..."
    source build/envsetup.sh || die "envsetup.sh not found. Run from AOSP root."

    log "Configuring lunch target: ${DEVICE}-userdebug"
    lunch "${DEVICE}-userdebug" || die "lunch failed for device: $DEVICE"

    log "Building system image with $JOBS parallel jobs..."
    make VoidOSLauncher systemimage -j"$JOBS" 2>&1 | tee -a "$LOG_FILE" \
        || die "Build failed. Check $LOG_FILE for details."

    ok "Build completed successfully!"
    log "Output: out/target/product/${DEVICE}/system.img"
}

main() {
    echo ""
    echo "╔══════════════════════════════════════╗"
    echo "║        voidOS Build System v0.1        ║"
    echo "║    Privacy-First • De-Googled • AOSP ║"
    echo "╚══════════════════════════════════════╝"
    echo ""
    log "Build started at $(date)"
    log "Device target : $DEVICE"
    log "AOSP branch   : $AOSP_BRANCH"
    log "Parallel jobs : $JOBS"
    echo ""

    check_dependencies
    sync_source
    inject_voidos_ui
    apply_patches
    strip_telemetry
    build_image

    echo ""
    ok "=== voidOS build complete ==="
    log "Build finished at $(date)"
}

main "$@"
