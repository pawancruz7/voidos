# VoidOS 🛡️

> **A Privacy-Focused, Hardened Mobile Operating System Built Directly on AOSP.**

---

## 📌 Overview

**VoidOS** is an experimental, privacy-hardened mobile operating system built from the ground up on top of the vanilla **Android Open Source Project (AOSP)** source tree. Unlike standard custom ROMs that rely solely on stripping Google Apps (GApps) or running background VPN applications, VoidOS implements **framework-level isolation** and **subsystem-level firewalling** directly inside the Android runtime and system daemons.

---

## ✨ Key Architectural Highlights

### 🛡️ 1. Core Security Master Controller
* **Thread-Safe Boot Sequence:** Managed by `VoidSecurityMasterController`, ensuring thread-safe subsystem initialization with zero lock contention on the Main Looper.
* **Resilient Lifecycle Management:** Guarantees safe fallback mechanisms and UI thread callbacks during startup failures.

### 🔐 2. Hardware Keystore Isolation
* **Hardware-Backed AES-256 Key Generation:** Integrated with `AndroidKeyStore` via `VoidCryptoEngine`.
* **Zero Software Key Exposure:** Encryption keys are anchored strictly inside the hardware-backed Security Element / TEE.

### 🌐 3. Dynamic Framework Firewall
* **Subsystem-Level Filtering:** Enforces domain blocking (`NetworkFirewall`) using structured `iptables` execution at the OS level—no third-party VPN apps needed.
* **Telemetry & Analytics Interception:** Intercepts and drops tracker requests (e.g., Google telemetry, Apple analytics) system-wide.
* **Injection-Proof Execution:** Validates domain structures via strict regular expressions to prevent command injection vectors.

### ⚡ 4. De-Googled Core Framework
* **Direct AOSP Base:** Not a LineageOS fork, providing fine-grained control over SELinux policies and init scripts.
* **Stripped Framework Telemetry:** Redirects captive portal checks, location services, and system pinging away from default Google endpoints.
* **Zero GMS Dependency:** Runs entirely without Google Play Services by default (optional, sandboxed MicroG support planned).

---

## 🏗️ Repository Architecture

