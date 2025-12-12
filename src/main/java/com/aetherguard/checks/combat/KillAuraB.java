package com.aetherguard.checks.combat;

import com.aetherguard.checks.CheckData;
import com.aetherguard.checks.CheckResult;
import com.aetherguard.checks.CheckType;
import com.aetherguard.core.AetherGuard;
import com.aetherguard.managers.PlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * 🛡️ AetherGuard KillAura Type B Check
 *
 * Abnormal rotation detection
 * Detects when players rotate unnaturally fast or with perfect angles
 *
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class KillAuraB extends CombatCheck {

    private final double maxRotationSpeed;
    private final double perfectAngleThreshold;
    private final int rotationHistorySize;

    public KillAuraB(AetherGuard plugin, String category, String name, String type) {
        super(plugin, category, name, type);

        this.maxRotationSpeed = plugin.getConfigManager().getMainConfig().getDouble("checks.combat.killaura.B.max-rotation-speed", 180.0);
        this.perfectAngleThreshold = plugin.getConfigManager().getMainConfig().getDouble("checks.combat.killaura.B.perfect-angle-threshold", 1.0);
        this.rotationHistorySize = plugin.getConfigManager().getMainConfig().getInt("checks.combat.killaura.B.rotation-history-size", 10);
    }

    @Override
    public CheckType getCheckType() {
        return CheckType.COMBAT_KILLAURA;
    }

    @Override
    public CheckResult check(Player player, CheckData data) {
        if (isExempt(player)) {
            return CheckResult.pass();
        }

        CombatData combat = getCombatData(data);

        // Track current rotation
        trackRotation(player, player.getLocation().getYaw(), player.getLocation().getPitch());

        // Check for abnormal rotation
        if (isAbnormalRotation(player, combat)) {
            return CheckResult.violation("Abnormal rotation detected",
                String.format("Rotation speed: %.2f°/tick", getRotationSpeed(player)));
        }

        // Check for perfect angles
        if (isPerfectAngle(player, combat)) {
            return CheckResult.violation("Perfect angle detected",
                String.format("Angle precision: %.2f°", getAnglePrecision(player)));
        }

        return CheckResult.pass();
    }

    /**
     * Track player rotation for analysis
     */
    private void trackRotation(Player player, float yaw, float pitch) {
        PlayerManager.PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        String key = "killaura_b_rotations";

        @SuppressWarnings("unchecked") // Using a Deque (like ArrayDeque) is more efficient for this pattern
        Deque<RotationData> rotations = (Deque<RotationData>) playerData.getCustomData().computeIfAbsent(key, k -> new ArrayDeque<RotationData>());

        rotations.add(new RotationData(yaw, pitch, System.currentTimeMillis()));

        // Keep only recent rotations
        if (rotations.size() > rotationHistorySize) {
            rotations.poll(); // More efficient than list.remove(0)
        }
    }

    /**
     * Check for abnormal rotation speed
     */
    private boolean isAbnormalRotation(Player player, CombatData combat) {
        double rotationSpeed = getRotationSpeed(player);
        return rotationSpeed > maxRotationSpeed;
    }

    /**
     * Check for perfect angles (suspiciously precise rotations)
     */
    private boolean isPerfectAngle(Player player, CombatData combat) {
        double precision = getAnglePrecision(player);
        return precision < perfectAngleThreshold;
    }

    /**
     * Calculate average rotation speed in degrees per tick
     */
    private double getRotationSpeed(Player player) {
        PlayerManager.PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        String key = "killaura_b_rotations";

        @SuppressWarnings("unchecked")
        Deque<RotationData> rotations = (Deque<RotationData>) playerData.getCustomData().get(key);

        if (rotations == null || rotations.size() < 2) {
            return 0.0;
        }

        Iterator<RotationData> iterator = rotations.iterator();
        RotationData prev = iterator.next();
        double totalSpeed = 0;

        while(iterator.hasNext()) {
            RotationData curr = iterator.next();
            double yawDiff = Math.abs(curr.yaw - prev.yaw);
            double pitchDiff = Math.abs(curr.pitch - prev.pitch);
            double angleDiff = Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);

            long timeDiff = curr.timestamp - prev.timestamp;
            if (timeDiff > 0) {
                totalSpeed += angleDiff / (timeDiff / 50.0); // Convert to degrees per tick (50ms = 1 tick)
            }
            prev = curr;
        }

        return totalSpeed / (rotations.size() - 1);
    }

    /**
     * Calculate angle precision (how close rotations are to perfect angles)
     */
    private double getAnglePrecision(Player player) {
        PlayerManager.PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        String key = "killaura_b_rotations";

        @SuppressWarnings("unchecked")
        Deque<RotationData> rotations = (Deque<RotationData>) playerData.getCustomData().get(key);

        if (rotations == null || rotations.size() < 2) {
            return Double.MAX_VALUE;
        }

        Iterator<RotationData> iterator = rotations.iterator();
        RotationData prev = iterator.next();
        double totalPrecision = 0;

        while(iterator.hasNext()) {
            RotationData curr = iterator.next();
            double yawDiff = Math.abs(curr.yaw - prev.yaw);
            double pitchDiff = Math.abs(curr.pitch - prev.pitch);

            // Check if angles are suspiciously round numbers
            double yawPrecision = Math.abs(yawDiff - Math.round(yawDiff));
            double pitchPrecision = Math.abs(pitchDiff - Math.round(pitchDiff));
            totalPrecision += Math.min(yawPrecision, pitchPrecision);
            prev = curr;
        }

        return totalPrecision / (rotations.size() - 1);
    }

    /**
     * Rotation data container
     */
    private static class RotationData {
        public final float yaw;
        public final float pitch;
        public final long timestamp;

        public RotationData(float yaw, float pitch, long timestamp) {
            this.yaw = yaw;
            this.pitch = pitch;
            this.timestamp = timestamp;
        }
    }
}
