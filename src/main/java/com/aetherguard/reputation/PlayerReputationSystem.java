package com.aetherguard.reputation;

import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 🛡️ Player Reputation System
 * Exclusive Feature - Tracks player trustworthiness over time
 *
 * This system maintains player reputation scores based on violations and clean behavior.
 * Reputation affects trust levels and can influence anti-cheat decisions.
 */
public class PlayerReputationSystem {
    
    // Configurable constants
    private static final double VIOLATION_PENALTY = 5.0;
    private static final double CLEAN_HOUR_REWARD = 1.0;
    private static final double MAX_REPUTATION = 100.0;
    private static final double DEFAULT_REPUTATION = 50.0;
    private static final double TRUSTED_THRESHOLD = 75.0;
    
    private final Map<UUID, ReputationProfile> profiles;
    private final File dataFile;
    
    public PlayerReputationSystem() {
        this.profiles = new ConcurrentHashMap<>();
        this.dataFile = new File("plugins/AetherGuard/reputation.dat");
        loadData();
    }
    
    /**
     * Records a violation for the player, decreasing their reputation.
     * @param player The player who committed the violation
     */
    public void recordViolation(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        try {
            ReputationProfile profile = profiles.computeIfAbsent(player.getUniqueId(), 
                k -> new ReputationProfile());
            profile.violations.incrementAndGet();
            profile.reputation.updateAndGet(d -> Math.max(0, d - VIOLATION_PENALTY));
            saveData();
        } catch (Exception e) {
            System.err.println("Error recording clean hours: " + e.getMessage());
        }
    }
    
    /**
     * Records clean hours for the player, increasing their reputation.
     * @param player The player with clean behavior
     */
    public void recordCleanHours(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        try {
            ReputationProfile profile = profiles.computeIfAbsent(player.getUniqueId(), 
                k -> new ReputationProfile());
            profile.reputation.updateAndGet(d -> Math.min(MAX_REPUTATION, d + CLEAN_HOUR_REWARD));
            saveData();
        } catch (Exception e) {
            System.err.println("Error updating player reputation: " + e.getMessage());
        }
    }
    
    /**
     * Gets the current reputation score of the player.
     * @param player The player
     * @return The reputation score
     */
    public double getReputation(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        ReputationProfile profile = profiles.get(player.getUniqueId());
        return profile != null ? profile.reputation.get() : DEFAULT_REPUTATION;
    }
    
    /**
     * Checks if the player is trusted based on reputation.
     * @param player The player
     * @return True if trusted
     */
    public boolean isTrusted(Player player) {
        return getReputation(player) > TRUSTED_THRESHOLD;
    }
    
    /**
     * Saves reputation data to file with integrity check.
     */
    private void saveData() {
        try {
            dataFile.getParentFile().mkdirs();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile))) {
                oos.writeObject(profiles);
                // Add checksum
                String checksum = generateChecksum(profiles);
                oos.writeObject(checksum);
            }
        } catch (IOException e) {
            System.err.println("Error saving reputation data: " + e.getMessage());
        }
    }
    
    /**
     * Loads reputation data from file with integrity verification.
     */
    @SuppressWarnings("unchecked")
    private void loadData() {
        if (!dataFile.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
            Map<UUID, ReputationProfile> loaded = (Map<UUID, ReputationProfile>) ois.readObject();
            String checksum = (String) ois.readObject();
            if (generateChecksum(loaded).equals(checksum)) {
                profiles.putAll(loaded);
            } else {
                // Data tampered, reset or log
                System.err.println("Reputation data integrity check failed. Data may be tampered.");
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading reputation data: " + e.getMessage());
        }
    }
    
    /**
     * Generates a checksum for data integrity.
     */
    private String generateChecksum(Object data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data.toString().getBytes());
            byte[] hash = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Inner class representing a player's reputation profile.
     */
    static class ReputationProfile implements Serializable {
        private static final long serialVersionUID = 1L;
        AtomicReference<Double> reputation = new AtomicReference<>(DEFAULT_REPUTATION);
        AtomicInteger violations = new AtomicInteger(0);
    }
}
