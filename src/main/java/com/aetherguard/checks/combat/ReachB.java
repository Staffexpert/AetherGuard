package com.aetherguard.checks.combat;

import com.aetherguard.checks.CheckData;
import com.aetherguard.checks.CheckResult;
import com.aetherguard.checks.CheckType;
import com.aetherguard.core.AetherGuard;
import com.aetherguard.managers.PlayerManager;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * 🛡️ AetherGuard Reach Type B Check
 *
 * Reach pattern analysis
 * Detects suspicious reach patterns and consistent max reach usage
 *
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class ReachB extends CombatCheck {

    private final double patternThreshold;
    private final int reachHistorySize;
    private final double maxReachPercentage;

    public ReachB(AetherGuard plugin, String category, String name, String type) {
        super(plugin, category, name, type);

        this.patternThreshold = plugin.getConfigManager().getMainConfig().getDouble("checks.combat.reach.B.pattern-threshold", 0.8);
        this.reachHistorySize = plugin.getConfigManager().getMainConfig().getInt("checks.combat.reach.B.reach-history-size", 10);
        this.maxReachPercentage = plugin.getConfigManager().getMainConfig().getDouble("checks.combat.reach.B.max-reach-percentage", 0.95);
    }

    @Override
    public CheckType getCheckType() {
        return CheckType.COMBAT_REACH;
    }

    @Override
    public CheckResult check(Player player, CheckData data) {
        if (isExempt(player)) {
            return CheckResult.pass();
        }

        CombatData combat = getCombatData(data);

        // Check if target is valid
        if (!combat.isValidTarget()) {
            return CheckResult.pass();
        }

        // Track reach distance
        trackReachDistance(player, combat.reach);

        // Check for suspicious reach patterns
        if (isSuspiciousReachPattern(player)) {
            return CheckResult.violation("Suspicious reach pattern detected",
                String.format("Pattern confidence: %.2f", getPatternConfidence(player)));
        }

        return CheckResult.pass();
    }

    /**
     * Track reach distances for pattern analysis
     */
    private void trackReachDistance(Player player, double reach) {
        PlayerManager.PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        String key = "reach_b_distances";

        @SuppressWarnings("unchecked")
        List<Double> distances = (List<Double>) playerData.getCustomData().computeIfAbsent(key, k -> new ArrayList<Double>());

        distances.add(reach);

        // Keep only recent distances
        while (distances.size() > reachHistorySize) {
            distances.remove(0);
        }
    }

    /**
     * Check if reach pattern is suspicious
     */
    private boolean isSuspiciousReachPattern(Player player) {
        double confidence = getPatternConfidence(player);
        return confidence > patternThreshold;
    }

    /**
     * Calculate pattern confidence (how suspicious the reach pattern is)
     */
    private double getPatternConfidence(Player player) {
        PlayerManager.PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
        String key = "reach_b_distances";

        @SuppressWarnings("unchecked")
        List<Double> distances = (List<Double>) playerData.getCustomData().get(key);

        if (distances == null || distances.size() < 3) {
            return 0.0;
        }

        double maxReach = getMaxReachDistance(player);
        double threshold = maxReach * maxReachPercentage;

        // Count how many attacks were at or near max reach
        int nearMaxCount = 0;
        for (double distance : distances) {
            if (distance >= threshold) {
                nearMaxCount++;
            }
        }

        // Calculate confidence based on percentage of attacks at max reach
        double percentage = (double) nearMaxCount / distances.size();

        // Also check for low variance (consistent max reach)
        double variance = calculateVariance(distances);
        double normalizedVariance = variance / (maxReach * maxReach);

        // Higher confidence if both high percentage and low variance
        return (percentage * 0.7) + ((1.0 - Math.min(normalizedVariance, 1.0)) * 0.3);
    }

    /**
     * Calculate variance of reach distances
     */
    private double calculateVariance(List<Double> values) {
        if (values.isEmpty()) return 0.0;

        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = 0.0;

        for (double value : values) {
            variance += Math.pow(value - mean, 2);
        }

        return variance / values.size();
    }
}
