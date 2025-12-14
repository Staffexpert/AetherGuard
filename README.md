# 🛡️ AetherGuard AntiCheat 

**The Most Complete and Advanced Open-Source Anti-Cheat for Minecraft!**

[![GitHub license](https://img.shields.io/github/license/Staffexpert/AetherGuard-AntiCheat?style=for-the-badge&color=blue)](LICENSE)
[![Discord](https://img.shields.io/discord/1077702874130090098?label=Discord&logo=discord&style=for-the-badge&color=7289DA)](https://discord.gg/z5qBaSxTZV)
[![Java Version](https://img.shields.io/badge/Java-17+-orange?style=for-the-badge&logo=openjdk)](https://www.java.com/)

AetherGuard is a **professional, modular, and ultra-complete** solution, offering **predictive detection** and **minimal overhead**.

---

## ✨ Core Features & **v1.1 Highlights**

| Feature Category | Description | AetherGuard Focus |
| :--- | :--- | :--- |
| **🎯 Security** | **100+ Advanced Checks** across 8 core categories (Movement, Combat, World, Packets, Exploits, etc.). | Enhanced detection logic and exploit patches. |
| **🧠 Intelligence** | Predictive detection with **Pattern Analysis** and **Machine Learning** (Optional). | **Advanced ML Models** for improved accuracy. |
| **⚡ Performance** | Adaptive optimization, Latency Compensation, and **Asynchronous Processing**. | Refined thread pool management. |
| **🛠️ Management** | Complete Interactive GUIs, Extensive Command Suite, and JSON/YAML Logging. | **Mobile App Monitoring** integration (Planned for release). |
| **🔌 Extensibility** | Open API, Multi-version support (1.8-1.21+), and integration with PlaceholderAPI/LuckPerms. | **Cloud-based Analytics API** expansion. |

---

## 🚀 Installation & Setup

### ⚙️ Requirements

* **Java 17+**
* **Spigot/Paper 1.8-1.21+**
* **4GB+ RAM** Recommended.

### 📥 Basic Installation

1.  Download the latest version from [**Releases (v1.1)**](https://github.com/Staffexpert/AetherGuard/releases).
2.  Place `AetherGuardAntiCheat.jar` in your `plugins/` folder.
3.  **Restart** the server. (The configuration will be generated automatically).

### ⚙️ Initial Configuration (`config.yml`)

Adjust the core detection profile:

```yaml
core: 
  # Profiles: LOW, MEDIUM, STRICT, ULTRA
  detection-profile: "MEDIUM" 
  enabled: true

action-manager:
  # Default actions on 'flag'
  default-actions: 
    - "FLAG" 
    - "ALERT"
    - "KICK:Cheating is not allowed"
