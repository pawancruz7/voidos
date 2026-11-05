<div align="center">

# VoidOS
### *Privacy-First • De-Googled • High-Performance AOSP Custom Distribution*

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Target Architecture](https://img.shields.io/badge/Architecture-ARM64%20%7C%20Snapdragon-orange.svg)]()
[![Platform](https://img.shields.io/badge/AOSP-Android%2014-informational.svg)]()

</div>

---

## ⚡ Overview
**VoidOS** is a custom, zero-surveillance mobile operating system built from the ground up on top of the Android Open Source Project (AOSP). Engineered specifically for low-end to mid-range hardware (optimized for platforms like Snapdragon 450 / Realme RMX1805), VoidOS strips away all proprietary telemetry, cloud hooks, and intrusive tracking mechanisms while introducing a decentralized mesh networking core and fault-tolerant native architecture.

---

## 🛡️ Core Architectural Pillars

### 1. Hardened Native Engine (`void_sanitizer.cpp`)
* **Fault-Tolerant Regime:** Automatically traps unhandled exceptions and drops into a crash-proof safe-mode execution path.
* **Strict File Security:** Implements `O_NOFOLLOW` and `fstat` validation to reject group/world-writable configuration files, enforcing strict `0600` permission boundaries.
* **Direct System Logging:** Routes critical runtime states and failure metrics directly through Android's native logging subsystem (`__android_log_print`).

### 2. Decentralized Mesh Networking (`VoidNetworkManager.java`)
* **Android KeyStore Integration:** Cryptographic identities and asymmetric keys are secured directly inside hardware-backed secure enclaves.
* **High-Entropy Cryptographic IDs:** Generates 32-character SHA-256 base64-url-safe unique node identifiers (`void_...`).
* **Authenticated Routing Matrix:** Features bounded LRU caching (`LinkedHashMap`) to prevent memory exhaustion and processes signed, multi-hop mesh packets with strict TTL boundaries.
* **Zero-Surveillance File Sharing (`VoidDropEngine`):** Enforces strict maximum stream limits and filename sanitization to bypass third-party cloud relays.

### 3. Absolute Telemetry Elimination (`privacy_strip.py`)
* Automated AST and text-level scanner designed to isolate and sanitize hardcoded telemetry endpoints, captive-portal checks, and analytics hooks across the source tree without breaking compilation logic.
* Comprehensive system-level `hosts` blocklist neutralizing tracking domains at the network stack level.

---

## 📂 Repository Structure

```text
├── AndroidProducts.mk             # Product registry configuration
├── voidos_generic.mk              # GSI build target specifications
├── void_sanitizer.cpp             # Hardened C++ fault-tolerant daemon
├── org/voidos/network/            # Asymmetric identity & P2P mesh manager
├── launcher/                      # Minimalist grid UI & home adapter
├── privacy_strip.py               # Precise telemetry stripping utility
├── hosts                          # System-level telemetry blocklist
└── build_voidos.sh                # Automated AOSP compilation pipeline
