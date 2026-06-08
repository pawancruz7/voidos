#!/usr/bin/env python3
"""
VoidOS Privacy Stripper
Scans AOSP source tree and removes/replaces Google telemetry URLs,
tracking endpoints, and analytics references.
"""

import os
import re
import sys
import argparse

# ── Telemetry patterns to strip ──────────────────────────────────────────────
TRACKING_PATTERNS = [
    # Google telemetry & analytics
    r"telemetry\.google\.com",
    r"analytics\.google\.com",
    r"play\.googleapis\.com",
    r"firebaselogging\.googleapis\.com",
    r"crashlytics\.com",
    r"firebase\.google\.com",
    r"safebrowsing\.googleapis\.com",
    r"connectivitycheck\.gstatic\.com",
    r"clients\d+\.google\.com",
    r"android\.clients\.google\.com",
    # Google account / GMS
    r"accounts\.google\.com",
    r"oauth2\.googleapis\.com",
    r"www\.googleapis\.com",
    # Android check-in / DRM
    r"android\.googleapis\.com",
    r"widevine\.l\.google\.com",
    # Ads
    r"googleadservices\.com",
    r"doubleclick\.net",
    r"googlesyndication\.com",
]

REPLACEMENT = "0.0.0.0"  # Null-route all tracking endpoints

# File extensions to scan
SCANNABLE_EXTENSIONS = {
    ".java", ".kt", ".xml", ".json", ".gradle",
    ".properties", ".smali", ".cpp", ".c", ".h",
    ".mk", ".bp", ".py", ".sh",
}

# ── Core functions ────────────────────────────────────────────────────────────

def should_scan(filepath: str) -> bool:
    _, ext = os.path.splitext(filepath)
    return ext.lower() in SCANNABLE_EXTENSIONS


def strip_file(filepath: str, dry_run: bool = False) -> int:
    """Strip tracking URLs from a single file. Returns count of replacements."""
    try:
        with open(filepath, "r", encoding="utf-8", errors="ignore") as f:
            original = f.read()
    except (IOError, PermissionError) as e:
        print(f"  [SKIP] Cannot read {filepath}: {e}")
        return 0

    modified = original
    total_hits = 0

    for pattern in TRACKING_PATTERNS:
        hits = len(re.findall(pattern, modified, flags=re.IGNORECASE))
        if hits:
            modified = re.sub(pattern, REPLACEMENT, modified, flags=re.IGNORECASE)
            total_hits += hits

    if total_hits > 0:
        print(f"  [STRIPPED] {filepath}  ({total_hits} reference(s) removed)")
        if not dry_run:
            with open(filepath, "w", encoding="utf-8") as f:
                f.write(modified)

    return total_hits


def scan_tree(root_dir: str, dry_run: bool = False) -> dict:
    """Recursively scan a directory tree."""
    stats = {"files_scanned": 0, "files_modified": 0, "total_hits": 0}

    for dirpath, dirnames, filenames in os.walk(root_dir):
        # Skip hidden dirs and build output dirs
        dirnames[:] = [
            d for d in dirnames
            if not d.startswith(".")
            and d not in {"out", ".git", "node_modules"}
        ]
        for filename in filenames:
            filepath = os.path.join(dirpath, filename)
            if not should_scan(filepath):
                continue
            stats["files_scanned"] += 1
            hits = strip_file(filepath, dry_run=dry_run)
            if hits:
                stats["files_modified"] += 1
                stats["total_hits"] += hits

    return stats


# ── CLI ───────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(
        description="VoidOS Privacy Stripper — removes Google telemetry from AOSP source"
    )
    parser.add_argument(
        "source_dir",
        nargs="?",
        default=".",
        help="Root directory of the AOSP source tree (default: current directory)",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Scan and report without modifying any files",
    )
    args = parser.parse_args()

    if not os.path.isdir(args.source_dir):
        print(f"[ERROR] Directory not found: {args.source_dir}")
        sys.exit(1)

    print("=" * 60)
    print("  VoidOS Privacy Stripper")
    print(f"  Target : {os.path.abspath(args.source_dir)}")
    print(f"  Mode   : {'DRY RUN (no changes)' if args.dry_run else 'LIVE (files will be modified)'}")
    print("=" * 60)

    stats = scan_tree(args.source_dir, dry_run=args.dry_run)

    print()
    print("=" * 60)
    print("  SUMMARY")
    print(f"  Files scanned  : {stats['files_scanned']}")
    print(f"  Files modified : {stats['files_modified']}")
    print(f"  Total hits     : {stats['total_hits']}")
    print("=" * 60)

    if stats["total_hits"] == 0:
        print("  No tracking references found. Source is clean.")
    elif args.dry_run:
        print("  Re-run without --dry-run to apply changes.")
    else:
        print("  De-Googling complete. VoidOS privacy enforced.")


if __name__ == "__main__":
    main()

