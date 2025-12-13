package com.aetherguard.managers;

import com.aetherguard.core.AetherGuard;
import com.aetherguard.managers.interfaces.IPlayerManager;
import com.google.gson.Gson;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * AetherGuard v1.2.0 - Player Manager
 *
 * High-performance concurrent player data store with scheduled persistence,
 * exemption tracking, reputation system, and efficient dirty-flag optimization.
 *
 * @author AetherGuard Team
 * @version 1.2.0
 */
public class PlayerManager implements IPlayerManager {

    private static final long SAVE_INTERVAL_TICKS = 20L * 60L * 5L;
    private static final long CLEANUP_INTERVAL_TICKS = 20L * 60L * 30L;

    private final AetherGuard plugin;
    private final Map<UUID, PlayerData> playerData;
    private final Set<UUID> globallyExempt;
    private final Map<UUID, Set<String>> checkExemptions;
    private final Map<UUID, Set<String>> categoryExemptions;
    private final Gson gson = new Gson();

    public PlayerManager(AetherGuard plugin) {
        this.plugin = plugin;
        this.playerData = new ConcurrentHashMap<>();
        this.globallyExempt = ConcurrentHashMap.newKeySet();
        this.checkExemptions = new ConcurrentHashMap<>();
        this.categoryExemptions = new ConcurrentHashMap<>();
        schedulePersistence();
    }

    private void schedulePersistence() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(
            plugin, this::saveDirtyPlayers, SAVE_INTERVAL_TICKS, SAVE_INTERVAL_TICKS
        );
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(
            plugin, this::cleanup, CLEANUP_INTERVAL_TICKS, CLEANUP_INTERVAL_TICKS
        );
    }

    public void registerPlayer(Player player) {
        loadPlayerData(player.getUniqueId());
    }

    public void unregisterPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData pd = playerData.remove(uuid);
        if (pd != null) savePlayerData(pd);
        globallyExempt.remove(uuid);
        checkExemptions.remove(uuid);
        categoryExemptions.remove(uuid);
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId(), player.getName());
    }

    public PlayerData getPlayerData(UUID uuid, String fallbackName) {
        return playerData.computeIfAbsent(uuid, id -> loadOrCreate(id, fallbackName));
    }

    private PlayerData loadOrCreate(UUID uuid, String fallbackName) {
        PlayerData data = loadPlayerData(uuid);
        return data != null ? data : new PlayerData(uuid, fallbackName != null ? fallbackName : "unknown");
    }

    public boolean isExempt(Player player) {
        return player != null && (globallyExempt.contains(player.getUniqueId()) || 
               player.hasPermission("aetherguard.bypass"));
    }

    public boolean isExempt(Player player, String checkName) {
        if (player == null) return false;
        Set<String> exemptions = checkExemptions.get(player.getUniqueId());
        return exemptions != null && exemptions.contains(checkName);
    }

    public boolean isExemptFromCategory(Player player, String category) {
        if (player == null) return false;
        Set<String> exemptions = categoryExemptions.get(player.getUniqueId());
        return exemptions != null && exemptions.contains(category);
    }

    public void addExemption(Player player) {
        if (player != null) globallyExempt.add(player.getUniqueId());
    }

    public void removeExemption(Player player) {
        if (player != null) globallyExempt.remove(player.getUniqueId());
    }

    public void addExemption(Player player, String checkName) {
        if (player != null) {
            checkExemptions.computeIfAbsent(player.getUniqueId(), k -> ConcurrentHashMap.newKeySet())
                .add(checkName);
        }
    }

    public void removeExemption(Player player, String checkName) {
        if (player == null) return;
        Set<String> set = checkExemptions.get(player.getUniqueId());
        if (set != null) {
            set.remove(checkName);
            if (set.isEmpty()) checkExemptions.remove(player.getUniqueId());
        }
    }

    public void addCategoryExemption(Player player, String category) {
        if (player != null) {
            categoryExemptions.computeIfAbsent(player.getUniqueId(), k -> ConcurrentHashMap.newKeySet())
                .add(category);
        }
    }

    public void removeCategoryExemption(Player player, String category) {
        if (player == null) return;
        Set<String> set = categoryExemptions.get(player.getUniqueId());
        if (set != null) {
            set.remove(category);
            if (set.isEmpty()) categoryExemptions.remove(player.getUniqueId());
        }
    }

    private void saveDirtyPlayers() {
        try {
            File dataDir = new File(plugin.getDataFolder(), "player-data");
            if (!dataDir.exists()) dataDir.mkdirs();

            playerData.values().stream()
                .filter(PlayerData::markAndCheckDirty)
                .forEach(this::savePlayerData);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save dirty players", e);
        }
    }

    private void savePlayerData(PlayerData data) {
        try {
            File dataDir = new File(plugin.getDataFolder(), "player-data");
            if (!dataDir.exists()) dataDir.mkdirs();

            File file = new File(dataDir, data.uuid.toString() + ".json");
            String json = gson.toJson(data.toMemento());
            Files.writeString(file.toPath(), json, StandardCharsets.UTF_8);
        } catch (Exception e) {
            plugin.getLogger().log(Level.FINE, "Failed to save data for " + data.uuid, e);
        }
    }

    private PlayerData loadPlayerData(UUID uuid) {
        try {
            File dataDir = new File(plugin.getDataFolder(), "player-data");
            File file = new File(dataDir, uuid.toString() + ".json");
            if (!file.exists()) return null;

            String json = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            PlayerData.Memento memento = gson.fromJson(json, PlayerData.Memento.class);
            PlayerData playerData = new PlayerData(memento);
            this.playerData.put(uuid, playerData);
            return playerData;
        } catch (Exception e) {
            plugin.getLogger().log(Level.FINE, "Failed to load data for " + uuid, e);
            return null;
        }
    }

    public void cleanup() {
        playerData.keySet().removeIf(uuid -> {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p == null || !p.isOnline()) {
                PlayerData pd = playerData.remove(uuid);
                if (pd != null) savePlayerData(pd);
                return true;
            }
            return false;
        });
    }

    public int getPlayerCount() {
        return playerData.size();
    }

    public long getTotalViolations() {
        return playerData.values().stream()
            .mapToLong(PlayerData::getTotalViolations)
            .sum();
    }

    public void saveAllData() {
        try {
            File dataDir = new File(plugin.getDataFolder(), "player-data");
            if (!dataDir.exists()) dataDir.mkdirs();
            playerData.values().forEach(this::savePlayerData);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save all player data", e);
        }
    }

    @Override
    public void savePlayerData(Player player) {
        PlayerData pd = playerData.get(player.getUniqueId());
        if (pd != null) savePlayerData(pd);
    }

    @Override
    public boolean isExemptFromCheck(Player player, String checkName) {
        return isExempt(player, checkName);
    }

    @Override
    public void setExempt(Player player, boolean exempt) {
        if (exempt) {
            addExemption(player);
        } else {
            removeExemption(player);
        }
    }

    @Override
    public void setCheckExemption(Player player, String checkName, boolean exempt) {
        if (exempt) {
            addExemption(player, checkName);
        } else {
            removeExemption(player, checkName);
        }
    }

    /**
     * Lightweight player data record with atomic dirty flag
     */
    public static class PlayerData {
        private final UUID uuid;
        private final String name;
        private final long joinTime;
        private volatile long lastViolationTime;
        private volatile long totalViolations;
        private final Map<String, Long> checkViolations;
        private final Map<String, Object> customData;
        private volatile double reputation;
        private final java.util.concurrent.atomic.AtomicBoolean dirty = new java.util.concurrent.atomic.AtomicBoolean(false);

        public PlayerData(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
            this.joinTime = System.currentTimeMillis();
            this.lastViolationTime = 0;
            this.totalViolations = 0;
            this.checkViolations = new ConcurrentHashMap<>();
            this.customData = new ConcurrentHashMap<>();
            this.reputation = 0.0;
        }

        public PlayerData(Memento m) {
            this.uuid = UUID.fromString(m.uuid);
            this.name = m.name;
            this.joinTime = m.joinTime;
            this.lastViolationTime = m.lastViolationTime;
            this.totalViolations = m.totalViolations;
            this.checkViolations = new ConcurrentHashMap<>(m.checkViolations != null ? m.checkViolations : Map.of());
            this.customData = new ConcurrentHashMap<>(m.customData != null ? m.customData : Map.of());
            this.reputation = m.reputation;
        }

        public boolean markAndCheckDirty() { return dirty.getAndSet(false); }
        public void markDirty() { dirty.set(true); }

        public UUID getUuid() { return uuid; }
        public String getName() { return name; }
        public long getJoinTime() { return joinTime; }
        public long getLastViolationTime() { return lastViolationTime; }
        public void setLastViolationTime(long t) { this.lastViolationTime = t; markDirty(); }
        public long getTotalViolations() { return totalViolations; }
        public void addViolation(long severity) { this.totalViolations += severity; this.lastViolationTime = System.currentTimeMillis(); markDirty(); }
        public Map<String, Long> getCheckViolations() { return checkViolations; }
        public Map<String, Object> getCustomData() { return customData; }
        public double getReputation() { return reputation; }
        public void adjustReputation(double delta) { this.reputation = Math.max(-100.0, Math.min(100.0, this.reputation + delta)); markDirty(); }

        public Memento toMemento() {
            Memento m = new Memento();
            m.uuid = uuid.toString();
            m.name = name;
            m.joinTime = joinTime;
            m.lastViolationTime = lastViolationTime;
            m.totalViolations = totalViolations;
            m.checkViolations = new HashMap<>(checkViolations);
            m.customData = new HashMap<>(customData);
            m.reputation = reputation;
            return m;
        }

        public static final class Memento {
            public String uuid;
            public String name;
            public long joinTime;
            public long lastViolationTime;
            public long totalViolations;
            public Map<String, Long> checkViolations;
            public Map<String, Object> customData;
            public double reputation;
        }
    }
}