package com.aetherguard.checks.combat;

import com.aetherguard.checks.Check;
import com.aetherguard.checks.CheckData;
import com.aetherguard.checks.CheckResult;
import com.aetherguard.checks.CheckType;
import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * 🛡️ AetherGuard Combat Check Base Class
 * 
 * Base class for all combat-related checks
 * Provides common combat analysis utilities
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public abstract class CombatCheck extends Check {
    
    // Combat constants
    protected static final double MAX_ATTACK_DISTANCE = 6.0;
    protected static final double MAX_REACH_DISTANCE = 3.5;
    protected static final double MAX_REACH_DISTANCE_SPRINT = 4.0;
    protected static final double MAX_BLOCK_REACH = 5.0;
    protected static final double MAX_ENTITY_REACH = 3.0;
    
    // Timing constants
    protected static final long ATTACK_COOLDOWN = 500; // milliseconds
    protected static final long ATTACK_COOLDOWN_1_9 = 600; // 1.9+ cooldown
    protected static final double MAX_CPS = 20.0;
    protected static final double MAX_CPS_BURST = 25.0;
    
    // Rotation constants
    protected static final double MAX_ROTATION_SPEED = 180.0; // degrees per tick
    protected static final double MAX_AIM_DIFF = 30.0; // degrees
    protected static final double PERFECT_ANGLE_THRESHOLD = 1.0; // degrees
    
    public CombatCheck(AetherGuard plugin, String category, String name, String type) {
        super(plugin, category, name, type);
    }
    
    @Override
    public CheckType getCheckType() {
        // Default implementation - override in subclasses
        return CheckType.COMBAT_KILLAURA;
    }
    
    /**
     * Calculate distance between two entities
     */
    protected double calculateDistance(Entity attacker, Entity target) {
        return attacker.getLocation().distance(target.getLocation());
    }
    
    /**
     * Calculate 3D distance between two entities
     */
    protected double calculate3DDistance(Entity attacker, Entity target) {
        return attacker.getLocation().toVector().distance(target.getLocation().toVector());
    }
    
    /**
     * Calculate maximum reach distance for attacker
     */
    protected double getMaxReachDistance(Player attacker) {
        double baseReach = attacker.isSprinting() ? MAX_REACH_DISTANCE_SPRINT : MAX_REACH_DISTANCE;
        
        // Apply effects
        try {
            java.util.Collection<org.bukkit.potion.PotionEffect> effects = attacker.getActivePotionEffects();
            for (org.bukkit.potion.PotionEffect effect : effects) {
                if (effect.getType().equals(org.bukkit.potion.PotionEffectType.SPEED)) {
                    int amplifier = effect.getAmplifier();
                    baseReach += 0.05 * (amplifier + 1);
                    break;
                }
            }
        } catch (Exception e) {
            // Fallback if potion effects unavailable
        }
        
        // Apply latency compensation
        double ping = getPing(attacker);
        baseReach += (ping / 1000.0) * 0.1; // 0.1 block per 100ms ping
        
        return baseReach;
    }
    
    /**
     * Check if entity can be attacked
     */
    protected boolean canAttack(Entity attacker, Entity target) {
        // Check if attacker is a player
        if (!(attacker instanceof Player)) {
            return false;
        }
        
        // Check if target is valid
        if (!(target instanceof LivingEntity)) {
            return false;
        }
        
        // Check if target is dead
        if (((LivingEntity) target).getHealth() <= 0) {
            return false;
        }
        
        // Check if entities are in same world
        if (!attacker.getWorld().equals(target.getWorld())) {
            return false;
        }
        
        // Check distance
        double distance = calculateDistance(attacker, target);
        double maxDistance = getMaxReachDistance((Player) attacker);
        
        return distance <= maxDistance;
    }
    
    /**
     * Calculate rotation difference between two locations
     */
    protected double calculateRotationDifference(Vector from, Vector to) {
        Vector direction = to.clone().subtract(from).normalize();
        double yaw = Math.toDegrees(Math.atan2(direction.getZ(), direction.getX())) - 90;
        double pitch = Math.toDegrees(-Math.asin(direction.getY()));
        
        // Normalize angles
        yaw = (yaw + 360) % 360;
        
        return Math.sqrt(yaw * yaw + pitch * pitch);
    }
    
    /**
     * Check if rotation is within acceptable range
     */
    protected boolean isAcceptableRotation(Vector current, Vector target, double maxDifference) {
        double difference = calculateRotationDifference(current, target);
        return difference <= maxDifference;
    }
    
    /**
     * Check if player is looking at target
     */
    protected boolean isLookingAt(Player player, Entity target, double maxAngle) {
        Vector playerDirection = player.getLocation().getDirection();
        Vector toTarget = target.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
        
        double angle = Math.toDegrees(Math.acos(playerDirection.dot(toTarget)));
        return angle <= maxAngle;
    }
    
    /**
     * Check if attack has valid cooldown
     */
    protected boolean hasValidAttackCooldown(Player player, long lastAttackTime) {
        long currentTime = System.currentTimeMillis();
        long timeSinceAttack = currentTime - lastAttackTime;
        
        // Check version-specific cooldown
        boolean is1_9Plus = isVersion1_9Plus();
        long cooldown = is1_9Plus ? ATTACK_COOLDOWN_1_9 : ATTACK_COOLDOWN;
        
        return timeSinceAttack >= cooldown;
    }
    
    /**
     * Calculate clicks per second
     */
    protected double calculateCPS(long[] clickTimes) {
        if (clickTimes == null || clickTimes.length == 0) {
            return 0.0;
        }
        
        long currentTime = System.currentTimeMillis();
        int clicksInLastSecond = 0;
        
        for (long clickTime : clickTimes) {
            if (currentTime - clickTime <= 1000) {
                clicksInLastSecond++;
            }
        }
        
        return clicksInLastSecond;
    }
    
    /**
     * Check if CPS is within acceptable range
     */
    protected boolean isAcceptableCPS(double cps, double maxCPS) {
        return cps <= maxCPS;
    }
    
    /**
     * Check if attack pattern is consistent
     */
    protected boolean isConsistentPattern(long[] clickTimes) {
        if (clickTimes == null || clickTimes.length < 3) {
            return true; // Not enough data
        }
        
        // Calculate intervals between clicks
        long[] intervals = new long[clickTimes.length - 1];
        for (int i = 1; i < clickTimes.length; i++) {
            intervals[i - 1] = clickTimes[i] - clickTimes[i - 1];
        }
        
        // Check if intervals are too consistent (bot-like)
        double variance = calculateVariance(intervals);
        return variance > 50.0; // Allow some variance
    }
    
    /**
     * Calculate variance of click intervals
     */
    private double calculateVariance(long[] values) {
        if (values.length == 0) return 0.0;
        
        double mean = 0.0;
        for (long value : values) {
            mean += value;
        }
        mean /= values.length;
        
        double variance = 0.0;
        for (long value : values) {
            variance += Math.pow(value - mean, 2);
        }
        variance /= values.length;
        
        return Math.sqrt(variance);
    }
    
    /**
     * Check if wall is between attacker and target
     */
    protected boolean hasWallBetween(Entity attacker, Entity target) {
        // Simple ray trace check
        Vector direction = target.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize();
        double distance = calculateDistance(attacker, target);
        
        for (double i = 0.5; i < distance; i += 0.5) {
            Vector checkPos = attacker.getLocation().toVector().add(direction.clone().multiply(i));
            if (attacker.getWorld().getBlockAt(checkPos.toLocation(attacker.getWorld())).getType().isSolid()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if attacker has line of sight to target
     */
    protected boolean hasLineOfSight(Entity attacker, Entity target) {
        if (attacker instanceof Player) {
            return ((Player) attacker).hasLineOfSight(target);
        }
        
        // Fallback for non-players
        return !hasWallBetween(attacker, target);
    }
    
    /**
     * Get player ping
     */
    protected double getPing(Player player) {
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object ping = entityPlayer.getClass().getField("ping").get(entityPlayer);
            return ((Number) ping).doubleValue();
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    /**
     * Check if server version is 1.9 or higher
     */
    protected boolean isVersion1_9Plus() {
        String version = plugin.getServerVersion();
        return version.contains("1.9") || version.contains("1.10") || 
               version.contains("1.11") || version.contains("1.12") ||
               version.contains("1.13") || version.contains("1.14") ||
               version.contains("1.15") || version.contains("1.16") ||
               version.contains("1.17") || version.contains("1.18") ||
               version.contains("1.19") || version.contains("1.20") ||
               version.contains("1.21");
    }
    
    /**
     * Get combat data from check data
     */
    protected CombatData getCombatData(CheckData data) {
        return new CombatData(
            data.getPlayer(),
            data.getTarget(),
            data.getReach(),
            data.isAttacked(),
            data.getLastAttackTime(),
            data.getCombo(),
            data.getAngle()
        );
    }
    
    /**
     * Combat data container
     */
    protected static class CombatData {
        public final Player attacker;
        public final Entity target;
        public final double reach;
        public final boolean attacked;
        public final long lastAttackTime;
        public final int combo;
        public final double angle;
        
        public CombatData(Player attacker, Entity target, double reach, boolean attacked,
                         long lastAttackTime, int combo, double angle) {
            this.attacker = attacker;
            this.target = target;
            this.reach = reach;
            this.attacked = attacked;
            this.lastAttackTime = lastAttackTime;
            this.combo = combo;
            this.angle = angle;
        }
        
        public boolean isValidTarget() {
            return target instanceof LivingEntity && ((LivingEntity) target).getHealth() > 0;
        }
        
        public boolean hasValidReach() {
            return reach <= MAX_REACH_DISTANCE_SPRINT;
        }
        
        public boolean hasCooldownPassed() {
            return System.currentTimeMillis() - lastAttackTime >= ATTACK_COOLDOWN;
        }
    }
}