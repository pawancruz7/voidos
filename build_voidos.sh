#!/bin/bash
# ============================================================
#  VoidOS Build Script
#  Builds a de-Googled Android (AOSP) system image for voidOS
#  Usage: ./build_voidos.sh [device_codename]
#         ./build_voidos.sh           → generic arm64 (emulator)
#         ./build_voidos.sh miatoll   → Redmi Note 9 Pro
# ============================================================

set -euo pipefail  # Exit on error, undefined vars, pipe failures

# ── Config ───────────────────────────────────────────────────
AOSP_BRANCH="android-14.0.0_r50"
AOSP_MANIFEST="https://android.googlesource.com/platform/manifest"
VOIDOS_PATCHES_DIR="$(pwd)/patches"
LOG_FILE="$(pwd)/build_voidos.log"
JOBS=$(nproc --all)
DEVICE="${1:-aosp_arm64}"  # Default: generic arm64

# ── Colors ───────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# ── Logging ──────────────────────────────────────────────────
log()  { echo -e "${CYAN}[voidOS]${NC} $*" | tee -a "$LOG_FILE"; }
ok()   { echo -e "${GREEN}[  OK  ]${NC} $*" | tee -a "$LOG_FILE"; }
warn() { echo -e "${YELLOW}[ WARN ]${NC} $*" | tee -a "$LOG_FILE"; }
die()  { echo -e "${RED}[ FAIL ]${NC} $*" | tee -a "$LOG_FILE"; exit 1; }

# ── Dependency check ─────────────────────────────────────────
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

# ── Repo init & sync ─────────────────────────────────────────
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

# ── Apply voidOS patches ──────────────────────────────────────
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

# ── Strip Google telemetry ────────────────────────────────────
strip_telemetry() {
    log "Running privacy stripper..."
    if [[ -f "$(pwd)/privacy_strip.py" ]]; then
        python3 "$(pwd)/privacy_strip.py" . || warn "Privacy stripper encountered errors."
        ok "Telemetry stripped."
    else
        warn "privacy_strip.py not found. Skipping telemetry strip."
    fi
}

# ── Build ────────────────────────────────────────────────────
build_image() {
    log "Setting up build environment..."
    # shellcheck source=/dev/null
    source build/envsetup.sh || die "envsetup.sh not found. Run from AOSP root."

    log "Configuring lunch target: ${DEVICE}-userdebug"
    lunch "${DEVICE}-userdebug" || die "lunch failed for device: $DEVICE"

    log "Building system image with $JOBS parallel jobs..."
    make systemimage -j"$JOBS" 2>&1 | tee -a "$LOG_FILE" \
        || die "Build failed. Check $LOG_FILE for details."

    ok "Build completed successfully!"
    log "Output: out/target/product/${DEVICE}/system.img"
}

# ── Main ─────────────────────────────────────────────────────
main() {
    echo ""
    echo "╔══════════════════════════════════════╗"
    echo "║       voidOS Build System v0.1       ║"
    echo "║   Privacy-First • De-Googled • AOSP  ║"
    echo "╚══════════════════════════════════════╝"
    echo ""
    log "Build started at $(date)"
    log "Device target : $DEVICE"
    log "AOSP branch   : $AOSP_BRANCH"
    log "Parallel jobs : $JOBS"
    echo ""

    check_dependencies
    sync_source
    apply_patches
    strip_telemetry
    build_image

    echo ""
    ok "=== voidOS build complete ==="
    log "Build finished at $(date)"
}

main "$@"
