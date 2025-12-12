package com.aetherguard.versioning;

import com.aetherguard.core.AetherGuard;
import org.bukkit.Bukkit;

/**
 * 🛡️ AetherGuard Version Compatibility
 * 
 * Handles version-specific detections and checks
 * Support for 1.8.x - 1.21.x
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class VersionCompatibility {
    
    private final AetherGuard plugin;
    private final MinecraftVersion version;
    
    public VersionCompatibility(AetherGuard plugin) {
        this.plugin = plugin;
        this.version = detectVersion();
    }
    
    private MinecraftVersion detectVersion() {
        String version = Bukkit.getVersion();
        
        if (version.contains("1.8")) return MinecraftVersion.V1_8;
        if (version.contains("1.9")) return MinecraftVersion.V1_9;
        if (version.contains("1.10")) return MinecraftVersion.V1_10;
        if (version.contains("1.11")) return MinecraftVersion.V1_11;
        if (version.contains("1.12")) return MinecraftVersion.V1_12;
        if (version.contains("1.13")) return MinecraftVersion.V1_13;
        if (version.contains("1.14")) return MinecraftVersion.V1_14;
        if (version.contains("1.15")) return MinecraftVersion.V1_15;
        if (version.contains("1.16")) return MinecraftVersion.V1_16;
        if (version.contains("1.17")) return MinecraftVersion.V1_17;
        if (version.contains("1.18")) return MinecraftVersion.V1_18;
        if (version.contains("1.19")) return MinecraftVersion.V1_19;
        if (version.contains("1.20")) return MinecraftVersion.V1_20;
        if (version.contains("1.21")) return MinecraftVersion.V1_21;
        
        return MinecraftVersion.LATEST;
    }
    
    public MinecraftVersion getVersion() {
        return version;
    }
    
    /**
     * Get attack cooldown for current version
     */
    public long getAttackCooldown() {
        if (version.isOlderThan(MinecraftVersion.V1_9)) {
            return 500;
        }
        return 600;
    }
    
    /**
     * Get max reach distance
     */
    public double getMaxReach() {
        return 3.5;
    }
    
    /**
     * Check if version supports dual wielding
     */
    public boolean supportsDualWielding() {
        return version.isNewerOrEqual(MinecraftVersion.V1_9);
    }
    
    /**
     * Check if version supports shields
     */
    public boolean supportsShields() {
        return version.isNewerOrEqual(MinecraftVersion.V1_9);
    }
    
    /**
     * Check if version supports elytra
     */
    public boolean supportsElytra() {
        return version.isNewerOrEqual(MinecraftVersion.V1_9);
    }
    
    /**
     * Check if version supports off-hand
     */
    public boolean supportsOffHand() {
        return version.isNewerOrEqual(MinecraftVersion.V1_9);
    }
    
    /**
     * Get sprint speed for version
     */
    public double getSprintSpeed() {
        return 0.288;
    }
    
    /**
     * Get walk speed for version
     */
    public double getWalkSpeed() {
        return 0.221;
    }
    
    public enum MinecraftVersion {
        V1_8(8, "1.8"),
        V1_9(9, "1.9"),
        V1_10(10, "1.10"),
        V1_11(11, "1.11"),
        V1_12(12, "1.12"),
        V1_13(13, "1.13"),
        V1_14(14, "1.14"),
        V1_15(15, "1.15"),
        V1_16(16, "1.16"),
        V1_17(17, "1.17"),
        V1_18(18, "1.18"),
        V1_19(19, "1.19"),
        V1_20(20, "1.20"),
        V1_21(21, "1.21"),
        LATEST(99, "latest");
        
        private final int versionNumber;
        private final String versionName;
        
        MinecraftVersion(int versionNumber, String versionName) {
            this.versionNumber = versionNumber;
            this.versionName = versionName;
        }
        
        public int getVersionNumber() {
            return versionNumber;
        }
        
        public String getVersionName() {
            return versionName;
        }
        
        public boolean isOlderThan(MinecraftVersion other) {
            return this.versionNumber < other.versionNumber;
        }
        
        public boolean isNewerThan(MinecraftVersion other) {
            return this.versionNumber > other.versionNumber;
        }
        
        public boolean isNewerOrEqual(MinecraftVersion other) {
            return this.versionNumber >= other.versionNumber;
        }
        
        public boolean isOlderOrEqual(MinecraftVersion other) {
            return this.versionNumber <= other.versionNumber;
        }
    }
}
