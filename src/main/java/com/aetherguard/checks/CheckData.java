package com.aetherguard.checks;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

/**
 * 🛡️ AetherGuard Check Data
 * 
 * Container for all data needed by checks during execution
 * Provides a unified interface for different check types
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class CheckData {
    
    // Basic player information
    private final Player player;
    private final long timestamp;
    private final int protocolVersion;
    
    // Movement data
    private Location from;
    private Location to;
    private Vector velocity;
    private boolean onGround;
    private boolean lastOnGround;
    private double distance;
    private double horizontalDistance;
    private double verticalDistance;
    
    // Combat data
    private Player target;
    private double reach;
    private boolean attacked;
    private long lastAttackTime;
    private int combo;
    private double angle;
    
    // Packet data
    private String packetType;
    private Object packet;
    private Map<String, Object> packetData;
    
    // World data
    private String blockType;
    private Location blockLocation;
    private boolean blockInteract;
    
    // Generic data map for custom checks
    private final Map<String, Object> customData;
    
    // Check-specific data
    private final Map<String, Object> checkData;
    
    /**
     * Create basic check data
     */
    public CheckData(Player player) {
        this.player = player;
        this.timestamp = System.currentTimeMillis();
        this.protocolVersion = getProtocolVersion(player);
        this.customData = new HashMap<>();
        this.checkData = new HashMap<>();
    }
    
    /**
     * Create movement check data
     */
    public static CheckData movement(Player player, Location from, Location to, boolean onGround, boolean lastOnGround) {
        CheckData data = new CheckData(player);
        data.from = from;
        data.to = to;
        data.onGround = onGround;
        data.lastOnGround = lastOnGround;
        data.velocity = player.getVelocity();
        data.distance = from.distance(to);
        data.horizontalDistance = Math.sqrt(
            Math.pow(to.getX() - from.getX(), 2) + 
            Math.pow(to.getZ() - from.getZ(), 2)
        );
        data.verticalDistance = Math.abs(to.getY() - from.getY());
        return data;
    }
    
    /**
     * Create combat check data
     */
    public static CheckData combat(Player player, Player target, double reach, boolean attacked) {
        CheckData data = new CheckData(player);
        data.target = target;
        data.reach = reach;
        data.attacked = attacked;
        data.lastAttackTime = System.currentTimeMillis();
        data.angle = calculateAngle(player.getLocation(), target.getLocation());
        return data;
    }
    
    /**
     * Create packet check data
     */
    public static CheckData packet(Player player, String packetType, Object packet) {
        CheckData data = new CheckData(player);
        data.packetType = packetType;
        data.packet = packet;
        data.packetData = new HashMap<>();
        return data;
    }
    
    /**
     * Create world check data
     */
    public static CheckData world(Player player, String blockType, Location blockLocation, boolean interact) {
        CheckData data = new CheckData(player);
        data.blockType = blockType;
        data.blockLocation = blockLocation;
        data.blockInteract = interact;
        return data;
    }
    
    /**
     * Calculate angle between two locations
     */
    private static double calculateAngle(Location from, Location to) {
        Vector direction = to.toVector().subtract(from.toVector()).normalize();
        Vector playerDirection = from.getDirection().normalize();
        return Math.toDegrees(Math.acos(direction.dot(playerDirection)));
    }
    
    /**
     * Get protocol version for player
     */
    private static int getProtocolVersion(Player player) {
        // This would need protocol lib or similar to get actual protocol version
        // For now, return a default based on server version
        try {
            // Try to get from reflection if available
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
            Object networkManager = playerConnection.getClass().getField("networkManager").get(playerConnection);
            return (int) networkManager.getClass().getMethod("getVersion").invoke(networkManager);
        } catch (Exception e) {
            // Fallback to server version detection
            String version = player.getServer().getBukkitVersion();
            if (version.contains("1.8")) return 47;
            if (version.contains("1.9")) return 107;
            if (version.contains("1.10")) return 210;
            if (version.contains("1.11")) return 315;
            if (version.contains("1.12")) return 338;
            if (version.contains("1.13")) return 393;
            if (version.contains("1.14")) return 452;
            if (version.contains("1.15")) return 550;
            if (version.contains("1.16")) return 735;
            if (version.contains("1.17")) return 755;
            if (version.contains("1.18")) return 757;
            if (version.contains("1.19")) return 759;
            if (version.contains("1.20")) return 763;
            if (version.contains("1.21")) return 767;
            return 47; // Default to 1.8
        }
    }
    
    // Setters for chain calls
    public CheckData setVelocity(Vector velocity) {
        this.velocity = velocity;
        return this;
    }
    
    public CheckData setCombo(int combo) {
        this.combo = combo;
        return this;
    }
    
    public CheckData setPacketData(String key, Object value) {
        if (packetData == null) {
            packetData = new HashMap<>();
        }
        packetData.put(key, value);
        return this;
    }
    
    public CheckData setCustomData(String key, Object value) {
        customData.put(key, value);
        return this;
    }
    
    public CheckData setCheckData(String key, Object value) {
        checkData.put(key, value);
        return this;
    }
    
    // Utility methods
    public boolean isMoving() {
        return distance > 0.001;
    }
    
    public boolean isMovingHorizontally() {
        return horizontalDistance > 0.001;
    }
    
    public boolean isMovingVertically() {
        return verticalDistance > 0.001;
    }
    
    public boolean isSprinting() {
        return player.isSprinting();
    }
    
    public boolean isSneaking() {
        return player.isSneaking();
    }
    
    public boolean isFlying() {
        return player.isFlying();
    }
    
    public boolean isInWater() {
        return player.getLocation().getBlock().isLiquid();
    }
    
    public boolean isInLava() {
        return player.getLocation().getBlock().getType().name().contains("LAVA");
    }
    
    public boolean isOnGround() {
        return onGround;
    }
    
    public boolean wasOnGround() {
        return lastOnGround;
    }
    
    public boolean isGroundStateChanged() {
        return onGround != lastOnGround;
    }
    
    public double getSpeed() {
        return horizontalDistance;
    }
    
    public double getVerticalSpeed() {
        return verticalDistance;
    }
    
    public double getPing() {
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object ping = entityPlayer.getClass().getField("ping").get(entityPlayer);
            return ((Number) ping).doubleValue();
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    // Getters
    public Player getPlayer() { return player; }
    public long getTimestamp() { return timestamp; }
    public int getProtocolVersion() { return protocolVersion; }
    
    public Location getFrom() { return from; }
    public Location getTo() { return to; }
    public Vector getVelocity() { return velocity; }
    public boolean getOnGround() { return onGround; }
    public boolean getLastOnGround() { return lastOnGround; }
    public double getDistance() { return distance; }
    public double getHorizontalDistance() { return horizontalDistance; }
    public double getVerticalDistance() { return verticalDistance; }
    
    public Player getTarget() { return target; }
    public double getReach() { return reach; }
    public boolean isAttacked() { return attacked; }
    public long getLastAttackTime() { return lastAttackTime; }
    public int getCombo() { return combo; }
    public double getAngle() { return angle; }
    
    public String getPacketType() { return packetType; }
    public Object getPacket() { return packet; }
    public Map<String, Object> getPacketData() { return packetData; }
    
    public String getBlockType() { return blockType; }
    public Location getBlockLocation() { return blockLocation; }
    public boolean isBlockInteract() { return blockInteract; }
    
    public Map<String, Object> getCustomData() { return customData; }
    public Map<String, Object> getCheckData() { return checkData; }
    
    // Generic data getters
    @SuppressWarnings("unchecked")
    public <T> T getCustomData(String key, Class<T> type) {
        Object value = customData.get(key);
        return value != null && type.isInstance(value) ? (T) value : null;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getCustomData(String key, Class<T> type, T defaultValue) {
        Object value = customData.get(key);
        return value != null && type.isInstance(value) ? (T) value : defaultValue;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getCheckData(String key, Class<T> type) {
        Object value = checkData.get(key);
        return value != null && type.isInstance(value) ? (T) value : null;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getCheckData(String key, Class<T> type, T defaultValue) {
        Object value = checkData.get(key);
        return value != null && type.isInstance(value) ? (T) value : defaultValue;
    }
    
    @Override
    public String toString() {
        return String.format("CheckData{player=%s, type=%s, distance=%.3f, timestamp=%d}",
                player.getName(), getDataType(), distance, timestamp);
    }
    
    /**
     * Determine the type of check data
     */
    public String getDataType() {
        if (from != null && to != null) return "MOVEMENT";
        if (target != null) return "COMBAT";
        if (packet != null) return "PACKET";
        if (blockType != null) return "WORLD";
        return "GENERIC";
    }
    
    /**
     * Create a copy of this check data
     */
    public CheckData copy() {
        CheckData copy = new CheckData(player);
        copy.from = from;
        copy.to = to;
        copy.velocity = velocity;
        copy.onGround = onGround;
        copy.lastOnGround = lastOnGround;
        copy.distance = distance;
        copy.horizontalDistance = horizontalDistance;
        copy.verticalDistance = verticalDistance;
        copy.target = target;
        copy.reach = reach;
        copy.attacked = attacked;
        copy.lastAttackTime = lastAttackTime;
        copy.combo = combo;
        copy.angle = angle;
        copy.packetType = packetType;
        copy.packet = packet;
        copy.packetData = packetData != null ? new HashMap<>(packetData) : new HashMap<>();
        copy.blockType = blockType;
        copy.blockLocation = blockLocation;
        copy.blockInteract = blockInteract;
        copy.customData.putAll(customData);
        copy.checkData.putAll(checkData);
        return copy;
    }
}