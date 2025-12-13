package com.aetherguard.api;

import com.aetherguard.checks.Check;
import com.aetherguard.checks.CheckResult;
import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * AetherGuardAPI - Stable, documented public API
 * Version: v1.2.0
 *
 * <p>Provides safe, ergonomic and async-friendly access to AetherGuard
 * functionality for plugin authors and integrations.</p>
 */
public final class AetherGuardAPI {

    private final AetherGuard plugin;

    public AetherGuardAPI(AetherGuard plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public boolean isEnabled() { return plugin != null && plugin.isEnabled(); }

    public String getVersion() { return plugin.getDescription().getVersion(); }

    public List<Check> getAllChecks() { return plugin.getCheckManager().getAllChecks(); }

    public Optional<Check> getCheck(String name) { return Optional.ofNullable(plugin.getCheckManager().getCheck(name)); }

    public Optional<Check> getCheck(String category, String name, String type) { return Optional.ofNullable(plugin.getCheckManager().getCheck(category, name, type)); }

    public List<Check> getChecksByCategory(String category) { return plugin.getCheckManager().getCategoryChecks(category); }

    public boolean setCheckEnabled(String checkName, boolean enabled) { return plugin.getCheckManager().setCheckEnabled(checkName, enabled); }

    public boolean isCheckEnabled(String checkName) {
        Check c = plugin.getCheckManager().getCheck(checkName);
        return c != null && c.isEnabled();
    }

    public Map<String, Long> getViolations(Player player) {
        Map<String, Long> map = new HashMap<>();
        if (player == null) return map;
        for (Check check : plugin.getCheckManager().getEnabledChecks()) {
            long violations = check.getViolations();
            if (violations > 0) map.put(check.getFullName(), violations);
        }
        return map;
    }

    public long getViolationCount(Player player, String checkName) {
        if (player == null) return 0L;
        // Simplified - return total violations for player
        return plugin.getViolationManager().getTotalViolations(player);
    }

    public long getTotalViolations(Player player) { return player == null ? 0L : plugin.getViolationManager().getTotalViolations(player); }

    public void resetViolations(Player player) { if (player != null) plugin.getViolationManager().resetViolations(player); }

    public void resetViolations(Player player, String checkName) { if (player != null) plugin.getViolationManager().resetViolations(player, checkName); }

    public boolean isExempt(Player player) { return player != null && plugin.getPlayerManager().isExempt(player); }

    public boolean isExempt(Player player, String checkName) { return player != null && plugin.getPlayerManager().isExempt(player, checkName); }

    public void addExemption(Player player) { if (player != null) plugin.getPlayerManager().addExemption(player); }
    public void removeExemption(Player player) { if (player != null) plugin.getPlayerManager().removeExemption(player); }
    public void addExemption(Player player, String checkName) { if (player != null) plugin.getPlayerManager().addExemption(player, checkName); }
    public void removeExemption(Player player, String checkName) { if (player != null) plugin.getPlayerManager().removeExemption(player, checkName); }

    public CheckResult executeCheck(Player player, String checkName) {
        if (player == null) return CheckResult.pass();
        Check c = plugin.getCheckManager().getCheck(checkName);
        if (c == null) return CheckResult.pass();
        var data = new com.aetherguard.checks.CheckData(player);
        return c.executeCheck(player, data);
    }

    public CheckResult executeCheck(Player player, String checkName, com.aetherguard.checks.CheckData data) {
        if (player == null) return CheckResult.pass();
        Check c = plugin.getCheckManager().getCheck(checkName);
        if (c == null) return CheckResult.pass();
        return c.executeCheck(player, data);
    }

    public String getDetectionProfile() { return plugin.getConfigManager().getDetectionProfile(); }
    public void setDetectionProfile(String profile) { plugin.getConfigManager().setDetectionProfile(profile); }

    public boolean isDebugMode() { return plugin.isDebugMode(); }
    public void setDebugMode(boolean enabled) { plugin.setDebugMode(enabled); }

    public boolean isTestMode() { return plugin.isTestMode(); }
    public void setTestMode(boolean enabled) { plugin.setTestMode(enabled); }

    public APIStats getStats() {
        var vStats = plugin.getViolationManager().getStats();
        return new APIStats(plugin.getCheckManager().getTotalChecks(), plugin.getCheckManager().getEnabledChecksCount(),
                (int) vStats.getTotalPlayers(), (int) vStats.getTotalViolations(), getTPS(), plugin.getServer().getOnlinePlayers().size());
    }

    private double getTPS() {
        try {
            Object tpsObj = plugin.getServer().getClass().getMethod("getTPS").invoke(plugin.getServer());
            if (tpsObj instanceof double[]) { return ((double[]) tpsObj)[0]; }
        } catch (Exception ignored) {}
        return 20.0;
    }

    public void registerListener(org.bukkit.event.Listener listener) { if (listener != null) plugin.getServer().getPluginManager().registerEvents(listener, plugin); }
    public void unregisterListener(org.bukkit.event.Listener listener) { if (listener != null) HandlerList.unregisterAll(listener); }

    public com.aetherguard.managers.PlayerManager.PlayerData getPlayerData(Player player) { return player == null ? null : plugin.getPlayerManager().getPlayerData(player); }
    public Object getCustomData(Player player, String key) { var pd = getPlayerData(player); return pd == null ? null : pd.getCustomData().get(key); }
    public void setCustomData(Player player, String key, Object value) { var pd = getPlayerData(player); if (pd != null) pd.getCustomData().put(key, value); }

    // Async helper: execute check without blocking main thread
    public CompletableFuture<CheckResult> executeCheckAsync(Player player, String checkName, com.aetherguard.checks.CheckData data) {
        return CompletableFuture.supplyAsync(() -> executeCheck(player, checkName, data));
    }

    public static class APIStats {
        private final int totalChecks;
        private final int enabledChecks;
        private final int playersWithViolations;
        private final int totalViolations;
        private final double tps;
        private final int onlinePlayers;

        public APIStats(int totalChecks, int enabledChecks, int playersWithViolations, int totalViolations, double tps, int onlinePlayers) {
            this.totalChecks = totalChecks; this.enabledChecks = enabledChecks; this.playersWithViolations = playersWithViolations; this.totalViolations = totalViolations; this.tps = tps; this.onlinePlayers = onlinePlayers; }

        public int getTotalChecks() { return totalChecks; }
        public int getEnabledChecks() { return enabledChecks; }
        public int getPlayersWithViolations() { return playersWithViolations; }
        public int getTotalViolations() { return totalViolations; }
        public double getTPS() { return tps; }
        public int getOnlinePlayers() { return onlinePlayers; }
    }
}