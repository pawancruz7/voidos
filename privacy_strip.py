#!/usr/bin/env python3

import os
import re
import sys
import argparse

TRACKING_PATTERNS = [
    r"\btelemetry\.google\.com\b",
    r"\banalytics\.google\.com\b",
    r"\bplay\.googleapis\.com\b",
    r"\bfirebaselogging\.googleapis\.com\b",
    r"\bcrashlytics\.com\b",
    r"\bfirebase\.google\.com\b",
    r"\bsafebrowsing\.googleapis\.com\b",
    r"\bconnectivitycheck\.gstatic\.com\b",
    r"\bclients\d+\.google\.com\b",
    r"\bandroid\.clients\.google\.com\b",
    r"\bandroid\.googleapis\.com\b",
    r"\bwidevine\.l\.google\.com\b",
    r"\bgoogleadservices\.com\b",
    r"\bdoubleclick\.net\b",
    r"\bgooglesyndication\.com\b",
]

REPLACEMENT = "0.0.0.0"

SCANNABLE_EXTENSIONS = {
    ".java", ".kt", ".xml", ".json", ".gradle",
    ".properties", ".cpp", ".c", ".h", ".mk", ".bp"
}

EXCLUDED_DIRS = {
    "out", ".git", "node_modules", "external", "prebuilts", "test", "tests"
}

def should_scan(filepath: str) -> bool:
    _, ext = os.path.splitext(filepath)
    return ext.lower() in SCANNABLE_EXTENSIONS

def strip_file(filepath: str, dry_run: bool = False) -> int:
    try:
        with open(filepath, "r", encoding="utf-8", errors="surrogateescape", newline="") as f:
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
            with open(filepath, "w", encoding="utf-8", errors="surrogateescape", newline="") as f:
                f.write(modified)

    return total_hits

def scan_tree(root_dir: str, dry_run: bool = False) -> dict:
    stats = {"files_scanned": 0, "files_modified": 0, "total_hits": 0}

    for dirpath, dirnames, filenames in os.walk(root_dir):
        dirnames[:] = [
            d for d in dirnames
            if not d.startswith(".") and d not in EXCLUDED_DIRS
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

def main():
    parser = argparse.ArgumentParser(
        description="VoidOS Safe Privacy Stripper — precise telemetry removal"
    )
    parser.add_argument(
        "source_dir",
        nargs="?",
        default=".",
        help="Root directory of the source tree",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Scan and report without modifying files",
    )
    args = parser.parse_args()

    if not os.path.isdir(args.source_dir):
        print(f"[ERROR] Directory not found: {args.source_dir}")
        sys.exit(1)

    print("=" * 60)
    print("  VoidOS Safe Privacy Stripper")
    print(f"  Target : {os.path.abspath(args.source_dir)}")
    print(f"  Mode   : {'DRY RUN' if args.dry_run else 'LIVE'}")
    print("=" * 60)

    stats = scan_tree(args.source_dir, dry_run=args.dry_run)

    print()
    print("=" * 60)
    print("  SUMMARY")
    print(f"  Files scanned  : {stats['files_scanned']}")
    print(f"  Files modified : {stats['files_modified']}")
    print(f"  Total hits     : {stats['total_hits']}")
    print("=" * 60)

if __name__ == "__main__":
    main()
