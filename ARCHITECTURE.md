# VoidOS — Architecture Document
> Living document. Update after every major design decision.
> Last updated: June 2026

---

## 1. Project Identity

VoidOS is a de-Googled, privacy-first Android OS built on AOSP 14.
It rejects centralized corporate identities, DNS protocols, and cloud dependencies.
The system daemon runs autonomously at OS level, transforming physical hardware into sovereign local infrastructure.

---

## 2. Core Engines

### 2.1 Cryptographic Identity Engine (`IdentityEngine`)

**Status:** Architecture designed. Compilation pending (August 2026).

**Design decisions:**
- ~~RSA-2048~~ **Deprecated** — replaced with **Android Keystore-backed Ed25519 (Curve25519)**
- Reason: smaller packet size, lower compute overhead, better fit for mesh networking
- First-boot local key generation — no server, no cloud
- SHA-256 hashing with URL-safe Base64 encoding for public keys
- Masked user identifier prefixed with `void_`
- Closed internal storage secure stream bindings (no external I/O)

**Why Ed25519 over RSA-2048:**
- RSA-2048 signature: ~256 bytes. Ed25519 signature: 64 bytes — critical in mesh where every byte counts
- Android Keystore natively supports Ed25519 from API 31+
- Faster sign/verify on low-power hardware

---

### 2.2 Multi-Hop Mesh Routing Matrix (`MeshRoutingEngine`)

**Status:** Architecture designed. Hardware binding pending.

**Design decisions:**
- **Primary link-layer:** Wi-Fi Aware (NAN) — high bandwidth pipe
- **Fallback link-layer:** BLE beacons — dynamic fallback for non-NAN devices
- Adaptive routing at framework level — automatically switches based on hardware capability
- Stateful dynamic ad-hoc routing tables
- Standalone `MeshPacket` structures with cryptographic signatures (Ed25519)
- TTL ceiling: **64 hops** — prevents network loops
- Autonomous background packet forwarding for remote peers

**Hardware fragmentation note:**
Wi-Fi Aware support is fragmented across Android devices. BLE fallback is **mandatory** — without it, mesh only works on a subset of devices.

---

### 2.3 VoidDrop Air-Gapped Share Pipeline (`VoidDropEngine`)

**Status:** Architecture designed. SELinux policy binding pending.

**Design decisions:**
- Direct raw Layer-4 transport (byte-streams into raw TCP/UDP interfaces)
- Cuts out Android's high-level media frameworks entirely
- Zero OS-level logging — no data footprint
- Zero-compression for maximum local bandwidth
- Automated incoming/outgoing payload queue handlers

**Clarification on "TCP bypass":**
Not a protocol bypass — routes direct byte-streams into raw layer-4 transport interfaces,
bypassing Android's media framework and system log stack to eliminate data footprinting.

---

## 3. SystemUI Bindings

**Framework:** AOSP default base hooks (inherited, pre-configured)

**Components ready:**
- Back button global lifecycle callbacks
- Home button navigation handlers
- Notification bar pull-down window manager hooks
- Volume slider / hardware key broadcast receivers

**Next phase:** Inject custom dark cyberpunk UX overlays via `/patches` and `/launcher`

---

## 4. SELinux Security Policy

### 4.1 VoidOS Daemon Domain — `sepolicy/voidos_daemon.te`

```
# VoidOS daemon — custom SELinux domain
# Do NOT inherit from a restricted parent domain (neverallow risk)

type voidos_daemon, domain;
type voidos_daemon_exec, exec_type, file_type;

# Allow daemon to transition into its domain on exec
init_daemon_domain(voidos_daemon)

# Network raw socket permissions for VoidDrop layer-4 transport
allow voidos_daemon self:packet_socket create_socket_perms;
allow voidos_daemon self:capability { net_raw net_admin };

# Allow read/write to VoidOS internal storage only
allow voidos_daemon voidos_data_file:dir { read write search };
allow voidos_daemon voidos_data_file:file { read write create unlink };
```

### 4.2 File Contexts — `sepolicy/file_contexts`

```
# VoidOS daemon binary path — update this with actual install path at build time
/system/bin/voidos_daemon     u:object_r:voidos_daemon_exec:s0
/data/voidos(/.*)?            u:object_r:voidos_data_file:s0
```

### 4.3 Critical Build Warning — `neverallow`

AOSP has hardcoded `neverallow` rules for `net_raw` on certain domains.
If `voidos_daemon` inherits from a restricted parent, **sepolicy will fail at compile time**, not runtime.

**Checklist before building:**
- [ ] Confirm `voidos_daemon` is a fresh domain, not inheriting from `untrusted_app` or `platform_app`
- [ ] Run `audit2allow` on emulator first to catch denials before flashing
- [ ] Check `neverallow` conflicts: `grep -r "neverallow.*net_raw" external/sepolicy/`

---

## 5. Build Roadmap

| Phase | Task | Target |
|-------|------|--------|
| 0 | Architecture design | ✅ Done |
| 1 | Ubuntu 22.04 setup + AOSP 14 sync (~200GB) | August 2026 |
| 2 | First emulator build — baseline AOSP | August 2026 |
| 3 | Splice `VoidNetworkManager` into `frameworks/base/services` | August 2026 |
| 4 | Bind `MeshRoutingEngine` to Wi-Fi Aware (NAN) + BLE link-layers | Post August |
| 5 | `IdentityEngine` Ed25519 keystore integration | Post August |
| 6 | `VoidDrop` SELinux policy + layer-4 binding | Post August |
| 7 | System compilation + dual test node flashing | TBD |

---

## 6. Key Technical Decisions Log

| Decision | Chosen | Rejected | Reason |
|----------|--------|----------|--------|
| Crypto primitive | Ed25519 (Curve25519) | RSA-2048 | Smaller signatures, faster, better for mesh |
| Mesh primary link | Wi-Fi Aware (NAN) | Wi-Fi Direct | Lower latency, better multi-peer support |
| Mesh fallback link | BLE beacons | None | Hardware fragmentation — NAN not universal |
| Daemon isolation | Custom SELinux domain | Shared domain | Minimize blast radius if daemon is compromised |
| DNS | Quad9 (9.9.9.9) + Cloudflare (1.1.1.1) | Google (8.8.8.8) | Privacy |
| Telemetry | Blocked at hosts + source level | Opt-out | Zero tolerance policy |

---

*VoidOS — Built sovereign. No clouds. No tracking. No compromise.*
