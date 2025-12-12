package com.aetherguard.checks.packets;

import com.aetherguard.checks.Check;
import com.aetherguard.checks.CheckData;
import com.aetherguard.checks.CheckResult;
import com.aetherguard.checks.CheckType;
import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;

/**
 * 🛡️ AetherGuard Packet Check Base Class
 * 
 * Base class for all packet-related checks
 * Provides common packet analysis utilities
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public abstract class PacketCheck extends Check {
    
    // Packet constants
    protected static final int MAX_PACKET_SIZE = 32767; // bytes
    protected static final int MAX_PACKETS_PER_SECOND = 100;
    protected static final int MAX_PACKET_BURST = 20;
    protected static final int MAX_CHAT_LENGTH = 256;
    protected static final int MAX_SIGN_LINES = 4;
    protected static final int MAX_SIGN_LINE_LENGTH = 15;
    protected static final int MAX_BOOK_PAGES = 100;
    protected static final int MAX_BOOK_PAGE_LENGTH = 256;
    
    public PacketCheck(AetherGuard plugin, String category, String name, String type) {
        super(plugin, category, name, type);
    }
    
    @Override
    public CheckType getCheckType() {
        // Default implementation - override in subclasses
        return CheckType.PACKET_BADPACKETS;
    }
    
    /**
     * Check if packet size is valid
     */
    protected boolean isValidPacketSize(Object packet) {
        try {
            // This would need packet serialization to get actual size
            // For now, return true as placeholder
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if packet frequency is within limits
     */
    protected boolean isPacketFrequencyValid(Player player, String packetType) {
        int packetCount = getPacketCount(player, packetType);
        return packetCount <= MAX_PACKETS_PER_SECOND;
    }
    
    /**
     * Check if packet burst is within limits
     */
    protected boolean isPacketBurstValid(Player player, String packetType) {
        int burstCount = getPacketBurst(player, packetType);
        return burstCount <= MAX_PACKET_BURST;
    }
    
    /**
     * Get packet count for player
     */
    protected int getPacketCount(Player player, String packetType) {
        // This would need to be tracked in player data
        // For now, return a default value
        return 1;
    }
    
    /**
     * Get packet burst count for player
     */
    protected int getPacketBurst(Player player, String packetType) {
        // This would need to be tracked in player data
        // For now, return a default value
        return 1;
    }
    
    /**
     * Check if chat message is valid
     */
    protected boolean isValidChatMessage(String message) {
        if (message == null) return false;
        if (message.length() > MAX_CHAT_LENGTH) return false;
        if (message.trim().isEmpty()) return false;
        
        // Check for suspicious patterns
        if (containsSuspiciousPatterns(message)) return false;
        
        return true;
    }
    
    /**
     * Check if sign text is valid
     */
    protected boolean isValidSignText(String[] lines) {
        if (lines == null || lines.length > MAX_SIGN_LINES) return false;
        
        for (String line : lines) {
            if (line != null && line.length() > MAX_SIGN_LINE_LENGTH) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Check if book content is valid
     */
    protected boolean isValidBookContent(String[] pages) {
        if (pages == null || pages.length > MAX_BOOK_PAGES) return false;
        
        for (String page : pages) {
            if (page != null && page.length() > MAX_BOOK_PAGE_LENGTH) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Check for suspicious patterns in text
     */
    protected boolean containsSuspiciousPatterns(String text) {
        // Check for command spam
        if (text.startsWith("/") && text.length() > 100) {
            return true;
        }
        
        // Check for excessive repeating characters
        if (hasExcessiveRepeats(text)) {
            return true;
        }
        
        // Check for potential crash patterns
        if (containsCrashPatterns(text)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check for excessive repeating characters
     */
    protected boolean hasExcessiveRepeats(String text) {
        if (text.length() < 10) return false;
        
        int maxRepeats = 0;
        int currentRepeats = 1;
        char lastChar = text.charAt(0);
        
        for (int i = 1; i < text.length(); i++) {
            char currentChar = text.charAt(i);
            if (currentChar == lastChar) {
                currentRepeats++;
                maxRepeats = Math.max(maxRepeats, currentRepeats);
            } else {
                currentRepeats = 1;
            }
            lastChar = currentChar;
        }
        
        return maxRepeats > 5;
    }
    
    /**
     * Check for crash patterns
     */
    protected boolean containsCrashPatterns(String text) {
        // Check for section sign spam
        long sectionCount = text.chars().filter(ch -> ch == '§').count();
        if (sectionCount > 50) return true;
        
        // Check for format code spam
        long formatCount = text.chars().filter(ch -> ch == '&' && text.indexOf(ch) + 1 < text.length()).count();
        if (formatCount > 50) return true;
        
        // Check for null characters
        if (text.contains("\0")) return true;
        
        // Check for control characters
        for (char c : text.toCharArray()) {
            if (Character.isISOControl(c) && c != '\n' && c != '\r' && c != '\t') {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if packet is valid for player state
     */
    protected boolean isValidForPlayerState(Player player, String packetType) {
        // Check if player is dead
        if (player.getHealth() <= 0) {
            // Some packets shouldn't be sent while dead
            if (packetType.equals("Flying") || packetType.equals("Position") || packetType.equals("Look")) {
                return false;
            }
        }
        
        // Check if player is in creative mode
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) {
            // Some restrictions may not apply in creative
        }
        
        return true;
    }
    
    /**
     * Check if coordinates are valid
     */
    protected boolean isValidCoordinates(double x, double y, double z) {
        // Check for NaN or infinite values
        if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)) {
            return false;
        }
        
        if (Double.isInfinite(x) || Double.isInfinite(y) || Double.isInfinite(z)) {
            return false;
        }
        
        // Check for extreme values
        if (Math.abs(x) > 30000000 || Math.abs(z) > 30000000) {
            return false;
        }
        
        if (y < -64 || y > 320) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if rotation is valid
     */
    protected boolean isValidRotation(float yaw, float pitch) {
        // Check for NaN values
        if (Float.isNaN(yaw) || Float.isNaN(pitch)) {
            return false;
        }
        
        // Check pitch limits (should be between -90 and 90)
        if (pitch < -90.0f || pitch > 90.0f) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if item stack is valid
     */
    protected boolean isValidItemStack(Object itemStack) {
        if (itemStack == null) return true; // Air is valid
        
        try {
            // This would need proper ItemStack checking
            // For now, return true as placeholder
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get packet data from check data
     */
    protected PacketData getPacketData(CheckData data) {
        return new PacketData(
            data.getPlayer(),
            data.getPacketType(),
            data.getPacket(),
            data.getPacketData()
        );
    }
    
    /**
     * Packet data container
     */
    protected static class PacketData {
        public final Player player;
        public final String packetType;
        public final Object packet;
        public final java.util.Map<String, Object> packetData;
        
        public PacketData(Player player, String packetType, Object packet, java.util.Map<String, Object> packetData) {
            this.player = player;
            this.packetType = packetType;
            this.packet = packet;
            this.packetData = packetData;
        }
        
        public boolean isValidPacket() {
            return packet != null && packetType != null;
        }
        
        public Object getPacketValue(String key) {
            return packetData != null ? packetData.get(key) : null;
        }
        
        @SuppressWarnings("unchecked")
        public <T> T getPacketValue(String key, Class<T> type) {
            Object value = getPacketValue(key);
            return value != null && type.isInstance(value) ? (T) value : null;
        }
        
        @SuppressWarnings("unchecked")
        public <T> T getPacketValue(String key, Class<T> type, T defaultValue) {
            Object value = getPacketValue(key);
            return value != null && type.isInstance(value) ? (T) value : defaultValue;
        }
    }
}