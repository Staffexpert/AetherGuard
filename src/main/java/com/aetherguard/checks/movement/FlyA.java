package com.aetherguard.checks.movement;

import com.aetherguard.checks.CheckData;
import com.aetherguard.checks.CheckResult;
import com.aetherguard.checks.CheckType;
import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;

/**
 * 🛡️ AetherGuard Fly Type A Check
 * 
 * Basic vertical flight detection
 * Detects when players maintain upward motion without valid reason
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class FlyA extends MovementCheck {
    
    private final double maxAirTime;
    private final double maxVerticalSpeed;
    
    public FlyA(AetherGuard plugin, String category, String name, String type) {
        super(plugin, category, name, type);
        
        this.maxAirTime = plugin.getConfigManager().getMainConfig().getDouble("checks.movement.fly.A.max-air-time", 3.0);
        this.maxVerticalSpeed = plugin.getConfigManager().getMainConfig().getDouble("checks.movement.fly.A.max-vertical-speed", 0.5);
    }
    
    @Override
    public CheckType getCheckType() {
        return CheckType.MOVEMENT_FLY;
    }
    
    @Override
    public CheckResult check(Player player, CheckData data) {
        if (isExempt(player)) {
            return CheckResult.pass();
        }
        
        MoveData move = getMoveData(data);
        
        // Skip if not moving
        if (!move.isMoving()) {
            return CheckResult.pass();
        }
        
        // Check for basic flight violations
        if (isBasicFlyViolation(player, move)) {
            return CheckResult.violation("Basic flight detected", 
                String.format("Speed: %.3f, Air: %s", move.verticalDistance, !move.onGround));
        }
        
        return CheckResult.pass();
    }
    
    /**
     * Check for basic flight violations
     */
    private boolean isBasicFlyViolation(Player player, MoveData move) {
        // Player is in air and moving up
        if (!move.onGround && move.verticalDistance > 0) {
            // Check if player has legitimate reasons to move up
            if (!hasLegitimateVerticalMovement(player, move)) {
                return true;
            }
        }
        
        // Check for sustained air time
        if (!move.onGround && !isInWater(player) && !isInLava(player)) {
            long airTime = getAirTime(player);
            if (airTime > maxAirTime * 1000) { // Convert to milliseconds
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if player has legitimate reasons for vertical movement
     */
    private boolean hasLegitimateVerticalMovement(Player player, MoveData move) {
        // Jump detection
        if (move.isJumping() && move.verticalDistance <= getJumpHeight(player)) {
            return true;
        }
        
        // Knockback
        if (player.getVelocity().getY() > 0.1) {
            return true;
        }
        
        // Levitation effect
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.LEVITATION)) {
            return true;
        }
        
        // Jump boost effect
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.JUMP)) {
            return true;
        }
        
        // Riding vehicle
        if (isRiding(player)) {
            return true;
        }
        
        // Swimming up
        if (isInWater(player) && move.verticalDistance <= SWIM_SPEED) {
            return true;
        }
        
        // Near wall (wall jumping)
        if (isNearWall(player.getLocation(), 0.5) && move.verticalDistance <= WALL_JUMP_HEIGHT) {
            return true;
        }
        
        // Slime block bounce
        if (isOnSlime(player)) {
            return true;
        }
        
        // Elytra (1.9+)
        if (player.isGliding()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Get air time for player
     */
    private long getAirTime(Player player) {
        return System.currentTimeMillis() - getLastGroundTime(player);
    }
    
    /**
     * Get last time player was on ground
     */
    private long getLastGroundTime(Player player) {
        // This would need to be tracked in player data
        // For now, return current time minus 1 second
        return System.currentTimeMillis() - 1000;
    }
    
    /**
     * Check if player is on slime block
     */
    private boolean isOnSlime(Player player) {
        return player.getLocation().clone().subtract(0, 1, 0).getBlock().getType().name().contains("SLIME");
    }
}