package com.aetherguard.checks.movement;

import com.aetherguard.checks.Check;
import com.aetherguard.checks.CheckData;
import com.aetherguard.checks.CheckResult;
import com.aetherguard.checks.CheckType;
import com.aetherguard.core.AetherGuard;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * 🛡️ AetherGuard Movement Check Base Class
 * 
 * Base class for all movement-related checks
 * Provides common movement analysis utilities
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public abstract class MovementCheck extends Check {
    
    // Movement thresholds and constants
    protected static final double WALK_SPEED = 0.221;
    protected static final double SPRINT_SPEED = 0.288;
    protected static final double SNEAK_SPEED = 0.069;
    protected static final double SWIM_SPEED = 0.115;
    protected static final double FLY_SPEED = 0.1;
    protected static final double JUMP_MOTION = 0.42;
    protected static final double GRAVITY = 0.08;
    protected static final double WATER_FRICTION = 0.8;
    protected static final double LAVA_FRICTION = 0.5;
    protected static final double AIR_FRICTION = 0.98;
    protected static final double ICE_FRICTION = 0.98;
    protected static final double SLIME_BLOCK_BOUNCE = 0.5;
    protected static final double WEB_SPEED = 0.105;
    protected static final double SOUL_SAND_SPEED = 0.11;
    
    // Height limits
    protected static final double STEP_HEIGHT = 0.6;
    protected static final double JUMP_HEIGHT = 1.252;
    protected static final double WALL_JUMP_HEIGHT = 1.8;
    
    public MovementCheck(AetherGuard plugin, String category, String name, String type) {
        super(plugin, category, name, type);
    }
    
    @Override
    public CheckType getCheckType() {
        // Default implementation - override in subclasses
        return CheckType.MOVEMENT_FLY;
    }
    
    /**
     * Calculate expected horizontal movement speed
     */
    protected double calculateExpectedSpeed(Player player, Location from, Location to) {
        double baseSpeed = getBaseSpeed(player);
        double friction = getFriction(from);
        
        // Apply speed multipliers
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SPEED)) {
            int amplifier = player.getPotionEffect(org.bukkit.potion.PotionEffectType.SPEED).getAmplifier();
            baseSpeed *= (1.0 + 0.2 * (amplifier + 1));
        }
        
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SLOW)) {
            int amplifier = player.getPotionEffect(org.bukkit.potion.PotionEffectType.SLOW).getAmplifier();
            baseSpeed *= (1.0 - 0.15 * (amplifier + 1));
        }
        
        // Apply sprint modifier
        if (player.isSprinting() && player.isOnGround()) {
            baseSpeed *= 1.3;
        }
        
        // Apply sneak modifier
        if (player.isSneaking()) {
            baseSpeed *= 0.3;
        }
        
        // Apply flying modifier
        if (player.isFlying()) {
            baseSpeed = player.getFlySpeed() * 2.0;
        }
        
        // Apply swimming modifier
        if (isInWater(player) && !player.isOnGround()) {
            baseSpeed *= 0.8;
        }
        
        return baseSpeed * friction;
    }
    
    /**
     * Get base movement speed for player
     */
    protected double getBaseSpeed(Player player) {
        if (player.isFlying()) {
            return FLY_SPEED;
        } else if (player.isSprinting()) {
            return SPRINT_SPEED;
        } else if (player.isSneaking()) {
            return SNEAK_SPEED;
        } else {
            return WALK_SPEED;
        }
    }
    
    /**
     * Get friction at location
     */
    protected double getFriction(Location location) {
        Material block = location.getBlock().getType();
        Material blockBelow = location.clone().subtract(0, 1, 0).getBlock().getType();
        
        if (block == Material.WATER || blockBelow == Material.WATER) {
            return WATER_FRICTION;
        } else if (block == Material.LAVA || blockBelow == Material.LAVA) {
            return LAVA_FRICTION;
        } else if (block.name().contains("ICE")) {
            return ICE_FRICTION;
        } else if (block == Material.SOUL_SAND || blockBelow == Material.SOUL_SAND) {
            return SOUL_SAND_SPEED / WALK_SPEED;
        } else if (block == Material.COBWEB || blockBelow == Material.COBWEB) {
            return WEB_SPEED / WALK_SPEED;
        } else if (blockBelow.name().contains("SLIME")) {
            return SLIME_BLOCK_BOUNCE;
        } else {
            return AIR_FRICTION;
        }
    }
    
    /**
     * Check if player is in water
     */
    protected boolean isInWater(Player player) {
        Location loc = player.getLocation();
        return loc.getBlock().isLiquid() || 
               loc.clone().add(0, 1, 0).getBlock().isLiquid() ||
               loc.clone().add(0, -1, 0).getBlock().isLiquid();
    }
    
    /**
     * Check if player is in lava
     */
    protected boolean isInLava(Player player) {
        Location loc = player.getLocation();
        return loc.getBlock().getType().name().contains("LAVA") || 
               loc.clone().add(0, 1, 0).getBlock().getType().name().contains("LAVA") ||
               loc.clone().add(0, -1, 0).getBlock().getType().name().contains("LAVA");
    }
    
    /**
     * Check if player is near a wall
     */
    protected boolean isNearWall(Location location, double distance) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    
                    Location checkLoc = location.clone().add(x * distance, y * distance, z * distance);
                    if (!checkLoc.getBlock().getType().isAir()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Check if player is on a solid block
     */
    protected boolean isOnSolidGround(Player player) {
        Location loc = player.getLocation().clone().subtract(0, 0.1, 0);
        return loc.getBlock().getType().isSolid() && !loc.getBlock().getType().isAir();
    }
    
    /**
     * Check if player can stand at location
     */
    protected boolean canStand(Location location) {
        // Check if block below is solid
        if (!location.clone().subtract(0, 1, 0).getBlock().getType().isSolid()) {
            return false;
        }
        
        // Check if current and above location are not solid
        if (location.getBlock().getType().isSolid() || 
            location.clone().add(0, 1, 0).getBlock().getType().isSolid()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if player is in a valid position
     */
    protected boolean isValidPosition(Player player) {
        Location loc = player.getLocation();
        
        // Check if player is inside a solid block
        if (loc.getBlock().getType().isSolid() || 
            loc.clone().add(0, 1, 0).getBlock().getType().isSolid()) {
            return false;
        }
        
        // Check if player is too far below world
        if (loc.getY() < -64) {
            return false;
        }
        
        // Check if player is too high above world
        if (loc.getY() > 256) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Calculate expected vertical motion
     */
    protected double calculateExpectedYMotion(Player player, boolean onGround) {
        if (onGround) {
            return 0.0;
        }
        
        double motionY = player.getVelocity().getY();
        
        // Apply gravity
        motionY -= GRAVITY;
        
        // Apply air resistance
        motionY *= AIR_FRICTION;
        
        // Apply liquid friction
        if (isInWater(player)) {
            motionY *= WATER_FRICTION;
        } else if (isInLava(player)) {
            motionY *= LAVA_FRICTION;
        }
        
        return motionY;
    }
    
    /**
     * Check if movement is within acceptable range
     */
    protected boolean isAcceptableMovement(double expected, double actual, double tolerance) {
        return Math.abs(expected - actual) <= tolerance;
    }
    
    /**
     * Get movement tolerance based on ping
     */
    protected double getTolerance(Player player) {
        double ping = getPing(player);
        return 0.1 + (ping / 1000.0) * 0.05; // Base 0.1 + 0.05 per 100ms ping
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
     * Check if player is riding a vehicle
     */
    protected boolean isRiding(Player player) {
        return player.isInsideVehicle();
    }
    
    /**
     * Check if player has effects that affect movement
     */
    protected boolean hasMovementEffects(Player player) {
        return player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SPEED) ||
               player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SLOW) ||
               player.hasPotionEffect(org.bukkit.potion.PotionEffectType.JUMP) ||
               player.hasPotionEffect(org.bukkit.potion.PotionEffectType.LEVITATION) ||
               player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SLOW_FALLING);
    }
    
    /**
     * Get jump height with effects
     */
    protected double getJumpHeight(Player player) {
        double height = JUMP_HEIGHT;
        
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.JUMP)) {
            int amplifier = player.getPotionEffect(org.bukkit.potion.PotionEffectType.JUMP).getAmplifier();
            height += 0.1 * (amplifier + 1);
        }
        
        return height;
    }
    
    /**
     * Check if player is standing on a climbable block
     */
    protected boolean isOnClimbable(Player player) {
        Location loc = player.getLocation().clone().subtract(0, 0.1, 0);
        Material block = loc.getBlock().getType();
        
        return block == Material.LADDER ||
               block == Material.VINE ||
               block.name().contains("SCAFFOLD") ||
               block.name().contains("TRAPDOOR");
    }
    
    /**
     * Get move data from check data
     */
    protected MoveData getMoveData(CheckData data) {
        return new MoveData(
            data.getFrom(),
            data.getTo(),
            data.getOnGround(),
            data.getLastOnGround(),
            data.getDistance(),
            data.getHorizontalDistance(),
            data.getVerticalDistance(),
            data.getVelocity()
        );
    }
    
    /**
     * Movement data container
     */
    protected static class MoveData {
        public final Location from;
        public final Location to;
        public final boolean onGround;
        public final boolean lastOnGround;
        public final double distance;
        public final double horizontalDistance;
        public final double verticalDistance;
        public final Vector velocity;
        
        public MoveData(Location from, Location to, boolean onGround, boolean lastOnGround,
                       double distance, double horizontalDistance, double verticalDistance, Vector velocity) {
            this.from = from;
            this.to = to;
            this.onGround = onGround;
            this.lastOnGround = lastOnGround;
            this.distance = distance;
            this.horizontalDistance = horizontalDistance;
            this.verticalDistance = verticalDistance;
            this.velocity = velocity;
        }
        
        public boolean isMoving() {
            return distance > 0.001;
        }
        
        public boolean isMovingHorizontally() {
            return horizontalDistance > 0.001;
        }
        
        public boolean isMovingVertically() {
            return verticalDistance > 0.001;
        }
        
        public boolean isGroundStateChanged() {
            return onGround != lastOnGround;
        }
        
        public boolean isJumping() {
            return !lastOnGround && onGround && verticalDistance > 0;
        }
        
        public boolean isFalling() {
            return !onGround && !lastOnGround && verticalDistance < 0;
        }
        
        public boolean isLanding() {
            return lastOnGround && !onGround && verticalDistance < 0;
        }
    }
}