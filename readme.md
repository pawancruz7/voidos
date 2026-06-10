<div align="center">

<img src="https://img.shields.io/badge/VoidOS-Android%2014-black?style=for-the-badge&logo=android&logoColor=white" />
<img src="https://img.shields.io/badge/Status-Pre--Alpha-red?style=for-the-badge" />
<img src="https://img.shields.io/badge/License-AGPL%20v3-blue?style=for-the-badge" />
<img src="https://img.shields.io/badge/Base-AOSP%2014-green?style=for-the-badge" />

# VoidOS

### *The Android That Belongs to You — Not to Google.*

**VoidOS** is a fully de-Googled, privacy-first Android OS built on AOSP 14.  
No telemetry. No tracking. No compromises.  
And for the first time — an OS that lets **you modify itself.**

[**📥 Get Builds**](#build-status) · [**🤝 Contribute**](CONTRIBUTING.md) · [**📱 Supported Devices**](DEVICES.md) · [**💬 Community**](#community)

</div>

---

## ✨ Why VoidOS?

Every Android OS tracks you — even the "private" ones stop at the surface.  
VoidOS goes deeper.

| Feature | VoidOS | Stock Android | GrapheneOS | CalyxOS |
|---|---|---|---|---|
| Google completely removed | ✅ | ❌ | Partial | Partial |
| System-level ad/tracker blocking | ✅ | ❌ | ❌ | ❌ |
| Hardware entropy hardening | ✅ | ❌ | ✅ | ❌ |
| **Self-modifiable by developer** | ✅ | ❌ | ❌ | ❌ |
| Custom boot architecture | ✅ | ❌ | ❌ | ❌ |

---

## 🔥 What Makes VoidOS Different

### 🛡️ Total De-Googling
Every Google service, telemetry endpoint, Firebase call, and Play Services hook has been removed at the system level — not just disabled. Blocked via a hardened hosts blocklist covering 50+ tracking domains including Google Analytics, Crashlytics, Firebase, and Safe Browsing.

### 🔐 Cryptographic Hardening
VoidOS ships a custom **Crypto-Guard subsystem** (`void_crypto_guard.c`) — a native C module that injects hardware entropy directly into the system keystore on every boot. Your encryption keys are never predictable.

### ⚡ VoidLauncher
A clean, minimal launcher built from scratch. No bloat. No suggestions. No "recommended" apps. Just your phone — yours.

### 🧬 Developer Mode — Reimagined *(Coming Soon)*
The most unique feature in any Android OS:

> **Enable Developer Mode → Get full access to VoidOS source code, on your device, in real time.**

Modify the OS. Rebuild it. Flash your changes — without a PC.  
VoidOS is the first Android OS designed to be modified by the person holding it.

---

## 📱 Supported Devices

VoidOS is currently in **Pre-Alpha**. Builds are being tested.

### Redmi Note Series
| Device | Codename | Status |
|---|---|---|
| Redmi Note 10 | sunny | 📋 Planned |
| Redmi Note 10 Pro | sweet | 📋 Planned |
| Redmi Note 11 | spes | 📋 Planned |
| Redmi Note 12 | sunstone | 📋 Planned |

### Poco Series
| Device | Codename | Status |
|---|---|---|
| Poco X3 Pro | vayu | 📋 Planned |
| Poco X4 Pro | veux | 📋 Planned |
| Poco M4 Pro | fleur | 📋 Planned |

### Future Targets
| Device | Codename | Status |
|---|---|---|
| OnePlus Nord CE 2 | lemonades | 🔬 Research |
| Realme 9 Pro+ | marshmallow | 🔬 Research |

> **Want your device supported?** [Open an issue](https://github.com/pawancruz7/voidos/issues/new) with your device name, codename, and a link to an existing AOSP/LineageOS device tree.

---

## 🏗️ Build VoidOS

### Requirements
- Linux machine (Ubuntu 20.04+ recommended)
- 16GB RAM minimum (32GB recommended)
- 300GB free disk space
- Python 3, Git, Repo tool

### Quick Start

```bash
# Clone VoidOS
git clone https://github.com/pawancruz7/voidos

# Run the build script
cd voidos
chmod +x buildvoidos.sh

# Build for emulator (generic arm64)
./buildvoidos.sh

# Build for specific device
./buildvoidos.sh miatoll   # Redmi Note 9 Pro
./buildvoidos.sh sunny     # Redmi Note 10
```

> ⚠️ First build syncs ~100GB of AOSP source. This will take several hours depending on your connection.

---

## 🤝 Contributing

VoidOS is built by the community, for the community.

**Ways to contribute:**
- 🐛 **Report bugs** — test builds on your device
- 💡 **Suggest features** — open a discussion
- 💻 **Submit code** — fork, branch, pull request
- 📖 **Improve docs** — every word matters
- 📱 **Port to new devices** — bring VoidOS to more hardware

Read [CONTRIBUTING.md](CONTRIBUTING.md) to get started.

---

## 💬 Community

| Platform | Link |
|---|---|
| Telegram | *Coming Soon* |
| Discord | *Coming Soon* |
| XDA Thread | *Coming Soon* |

---

## 📖 About the Creator

VoidOS was started by an 18-year-old developer from India — built initially on a phone, with no laptop, no funding, and no team.

Just a vision: *an Android OS that truly belongs to its user.*

If you believe privacy is a right, not a feature — **you belong here.**

---

## 📄 License

VoidOS is licensed under the **GNU Affero General Public License v3.0**.  
See [LICENSE](LICENSE) for full terms.

Any use, modification, or distribution must remain open source and credit the original project.

---

<div align="center">

**VoidOS — Built in the void. For everyone.**

⭐ Star this repo if you believe in privacy-first Android.

</div>
