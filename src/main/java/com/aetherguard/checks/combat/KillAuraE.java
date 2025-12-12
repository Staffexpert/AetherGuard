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
 * 🛡️ AetherGuard KillAura Type E Check
 *
 * Angle analysis
 * Detects impossible attack angles and perfect angle patterns
 *
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class KillAuraE extends CombatCheck {

    private final double perfectAngleThreshold;
    private final int angleHistorySize;
    private final double gimbalLockThreshold;

    public KillAuraE(AetherGuard plugin, String category, String name, String type) {
        super(plugin, category, name, type);

        this.perfectAngleThreshold = plugin.getConfigManager().getMainConfig().getDouble("checks.combat.killaura.E.perfect-angle-threshold", 0.1);
        this.angleHistorySize = plugin.getConfigManager().getMainConfig().getInt("checks.combat.killaura.E.angle-history-size", 15);
        this.gimbalLockThreshold = plugin.getConfigManager().getMainConfig().getDouble("checks.combat.killaura.E.gimbal-lock-threshold", 89.0);
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

        // Track attack angle
        trackAttackAngle(player, combat.angle);

        // Check for perfect angles
        if (isPerfectAngle(player)) {
            return CheckResult.violation("Perfect angle detected",
                String.format("Angle precision: %.3f°", getAnglePrecision(player)));
        }

        // Check for gimbal lock (impossible pitch angles)
        if (isGimbalLock(player)) {
            return CheckResult.violation("Gimbal lock detected",
                String.format("Pitch angle: %.2f°", player.getLocation().getPitch()));
        }

        return CheckResult.pass();
    }

    /**
     * Track attack angles for analysis
     */
    private void trackAttackAngle(Player player, double angle) {
        PlayerManager.PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        String key = "killaura_e_angles";

        @SuppressWarnings("unchecked")
        List<Double> angles = (List<Double>) playerData.getCustomData().computeIfAbsent(key, k -> new ArrayList<Double>());

        angles.add(angle);

        // Keep only recent angles
        while (angles.size() > angleHistorySize) {
            angles.remove(0);
        }
    }

    /**
     * Check if angles are suspiciously perfect
     */
    private boolean isPerfectAngle(Player player) {
        double precision = getAnglePrecision(player);
        return precision < perfectAngleThreshold;
    }

    /**
     * Check for gimbal lock (pitch near 90 degrees)
     */
    private boolean isGimbalLock(Player player) {
        float pitch = Math.abs(player.getLocation().getPitch());
        return pitch >= gimbalLockThreshold;
    }

    /**
     * Calculate angle precision (how close angles are to perfect values)
     */
    private double getAnglePrecision(Player player) {
        PlayerManager.PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        String key = "killaura_e_angles";

        @SuppressWarnings("unchecked")
        List<Double> angles = (List<Double>) playerData.getCustomData().get(key);

        if (angles == null || angles.size() < 2) {
            return Double.MAX_VALUE;
        }

        double totalPrecision = 0.0;
        int count = 0;

        for (double angle : angles) {
            // Check distance to common perfect angles (0°, 45°, 90°, etc.)
            double minDistance = Double.MAX_VALUE;
            for (int perfect = 0; perfect <= 360; perfect += 45) {
                double distance = Math.abs(angle - perfect);
                minDistance = Math.min(minDistance, distance);
                minDistance = Math.min(minDistance, Math.abs(angle - (perfect + 180))); // Account for 180° symmetry
            }

            totalPrecision += minDistance;
            count++;
        }

        return count > 0 ? totalPrecision / count : Double.MAX_VALUE;
    }
}
