package com.aetherguard.prediction;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * 🛡️ AetherGuard Physics Predictor
 * 
 * Predicts expected player movement based on Minecraft physics
 * Similar to Grim's prediction engine
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class PhysicsPredictor {
    
    private static final double GRAVITY = 0.08;
    private static final double WATER_FRICTION = 0.8;
    private static final double LAVA_FRICTION = 0.5;
    private static final double AIR_FRICTION = 0.98;
    private static final double WALK_SPEED = 0.1;
    private static final double SPRINT_MULTIPLIER = 1.3;
    private static final double SNEAK_MULTIPLIER = 0.3;
    
    /**
     * Predict player position at next tick
     */
    public static Vector predictNextPosition(Player player, Location current, Vector velocity) {
        Vector predicted = velocity.clone();
        
        predicted = applyFriction(predicted, player);
        predicted = applyGravity(predicted, player);
        
        return predicted;
    }
    
    /**
     * Predict velocity with acceleration
     */
    public static Vector predictVelocity(Player player, Vector currentVelocity, Vector inputVector) {
        Vector velocity = currentVelocity.clone();
        
        if (player.isSprinting()) {
            velocity.multiply(SPRINT_MULTIPLIER);
        } else if (player.isSneaking()) {
            velocity.multiply(SNEAK_MULTIPLIER);
        }
        
        velocity.add(inputVector);
        
        return velocity;
    }
    
    /**
     * Apply friction based on environment
     */
    private static Vector applyFriction(Vector velocity, Player player) {
        double friction = AIR_FRICTION;
        
        if (isInWater(player)) {
            friction = WATER_FRICTION;
        } else if (isInLava(player)) {
            friction = LAVA_FRICTION;
        } else if (isOnGround(player)) {
            friction = 0.6;
        }
        
        return velocity.multiply(friction);
    }
    
    /**
     * Apply gravity
     */
    private static Vector applyGravity(Vector velocity, Player player) {
        if (!isOnGround(player) && !isInWater(player) && !isInLava(player)) {
            velocity.setY(velocity.getY() - GRAVITY);
        }
        return velocity;
    }
    
    /**
     * Calculate expected distance based on physics
     */
    public static double calculateExpectedDistance(Player player, Location from, Location to, double ticks) {
        double horizontalSpeed = WALK_SPEED;
        
        if (player.isSprinting()) {
            horizontalSpeed *= SPRINT_MULTIPLIER;
        } else if (player.isSneaking()) {
            horizontalSpeed *= SNEAK_MULTIPLIER;
        }
        
        return horizontalSpeed * ticks;
    }
    
    /**
     * Check if velocity is possible
     */
    public static boolean isPossibleVelocity(Player player, Vector velocity, double maxTicks) {
        double maxHorizontal = WALK_SPEED * maxTicks;
        
        if (player.isSprinting()) {
            maxHorizontal *= SPRINT_MULTIPLIER;
        }
        
        double horizontal = Math.sqrt(velocity.getX() * velocity.getX() + velocity.getZ() * velocity.getZ());
        
        return horizontal <= maxHorizontal * 1.5;
    }
    
    private static boolean isInWater(Player player) {
        return player.getLocation().getBlock().isLiquid() && 
               player.getLocation().getBlock().getType().toString().contains("WATER");
    }
    
    private static boolean isInLava(Player player) {
        return player.getLocation().getBlock().isLiquid() && 
               player.getLocation().getBlock().getType().toString().contains("LAVA");
    }
    
    private static boolean isOnGround(Player player) {
        return player.isOnGround();
    }
}
