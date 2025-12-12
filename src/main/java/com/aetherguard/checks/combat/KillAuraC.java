package com.aetherguard.checks.combat;

import com.aetherguard.checks.CheckData;
import com.aetherguard.checks.CheckResult;
import com.aetherguard.checks.CheckType;
import com.aetherguard.core.AetherGuard;
import com.aetherguard.managers.PlayerManager;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * 🛡️ AetherGuard KillAura Type C Check
 *
 * Perfect timing detection
 * Detects when players attack with perfectly consistent timing
 *
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class KillAuraC extends CombatCheck {

    private final double timingVarianceThreshold;
    private final int timingHistorySize;
    private final long minAttackInterval;

    public KillAuraC(AetherGuard plugin, String category, String name, String type) {
        super(plugin, category, name, type);

        this.timingVarianceThreshold = plugin.getConfigManager().getMainConfig().getDouble("checks.combat.killaura.C.timing-variance-threshold", 10.0);
        this.timingHistorySize = plugin.getConfigManager().getMainConfig().getInt("checks.combat.killaura.C.timing-history-size", 20);
        this.minAttackInterval = plugin.getConfigManager().getMainConfig().getLong("checks.combat.killaura.C.min-attack-interval", 400);
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

        // Track attack timing
        trackAttackTiming(player, combat.lastAttackTime);

        // Check for perfect timing
        if (isPerfectTiming(player)) {
            return CheckResult.violation("Perfect timing detected",
                String.format("Timing variance: %.2fms", getTimingVariance(player)));
        }

        return CheckResult.pass();
    }

    /**
     * Track attack timing for analysis
     */
    private void trackAttackTiming(Player player, long attackTime) {
        PlayerManager.PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        String key = "killaura_c_timings";

        @SuppressWarnings("unchecked")
        List<Long> timings = (List<Long>) playerData.getCustomData().computeIfAbsent(key, k -> new ArrayList<Long>());

        timings.add(attackTime);

        // Keep only recent timings
        while (timings.size() > timingHistorySize) {
            timings.remove(0);
        }
    }

    /**
     * Check if attack timing is suspiciously perfect
     */
    private boolean isPerfectTiming(Player player) {
        double variance = getTimingVariance(player);
        return variance < timingVarianceThreshold;
    }

    /**
     * Calculate variance in attack timing intervals
     */
    private double getTimingVariance(Player player) {
        PlayerManager.PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        String key = "killaura_c_timings";

        @SuppressWarnings("unchecked")
        List<Long> timings = (List<Long>) playerData.getCustomData().get(key);

        if (timings == null || timings.size() < 3) {
            return Double.MAX_VALUE;
        }

        // Calculate intervals between attacks
        List<Long> intervals = new ArrayList<>();
        for (int i = 1; i < timings.size(); i++) {
            long interval = timings.get(i) - timings.get(i - 1);
            if (interval >= minAttackInterval) { // Only consider valid attack intervals
                intervals.add(interval);
            }
        }

        if (intervals.size() < 2) {
            return Double.MAX_VALUE;
        }

        // Calculate mean
        double mean = intervals.stream().mapToLong(Long::longValue).average().orElse(0.0);

        // Calculate variance
        double variance = 0.0;
        for (long interval : intervals) {
            variance += Math.pow(interval - mean, 2);
        }
        variance /= intervals.size();

        return Math.sqrt(variance); // Standard deviation
    }
}
