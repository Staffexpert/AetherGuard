package com.aetherguard.managers;

import com.aetherguard.checks.Check;
import com.aetherguard.checks.CheckResult;
import com.aetherguard.config.ConfigManager;
import com.aetherguard.core.AetherGuard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ AetherGuard Action Manager
 * 
 * Manages punishment actions and their execution
 * Handles action chains and delays
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class ActionManager {
    
    private final AetherGuard plugin;
    private final Map<String, ActionHandler> actionHandlers;
    private final Map<UUID, Long> lastActionTimes;
    
    public ActionManager(AetherGuard plugin) {
        this.plugin = plugin;
        this.actionHandlers = new HashMap<>();
        this.lastActionTimes = new ConcurrentHashMap<>();
        
        initializeActionHandlers();
    }
    
    /**
     * Initialize action handlers
     */
    private void initializeActionHandlers() {
        actionHandlers.put("FLAG", this::handleFlag);
        actionHandlers.put("ALERT", this::handleAlert);
        actionHandlers.put("KICK", this::handleKick);
        actionHandlers.put("BAN", this::handleBan);
        actionHandlers.put("TEMPBAN", this::handleTempBan);
        actionHandlers.put("FREEZE", this::handleFreeze);
        actionHandlers.put("COMMAND", this::handleCommand);
        actionHandlers.put("LOG", this::handleLog);
        actionHandlers.put("WEBHOOK", this::handleWebhook);
    }
    
    /**
     * Execute action for player
     */
    public void executeAction(Player player, String actionName, Check check, CheckResult result) {
        if (plugin.isTestMode()) {
            plugin.getLogger().info("§7[TEST] Would execute action: " + actionName + " on " + player.getName());
            return;
        }
        
        // Parse action with parameters
        ActionInfo actionInfo = parseAction(actionName);
        
        // Check cooldown
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        Long lastTime = lastActionTimes.get(uuid);
        
        if (lastTime != null && (currentTime - lastTime) < actionInfo.getCooldown()) {
            return; // Cooldown not passed
        }
        
        // Get action handler
        ActionHandler handler = actionHandlers.get(actionInfo.getType());
        if (handler == null) {
            plugin.getLogger().warning("§cUnknown action type: " + actionInfo.getType());
            return;
        }
        
        // Execute action with delay
        long delay = actionInfo.getDelay();
        if (delay > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    handler.handle(player, actionInfo, check, result);
                    lastActionTimes.put(uuid, System.currentTimeMillis());
                }
            }, delay * 20 / 1000); // Convert milliseconds to ticks
        } else {
            handler.handle(player, actionInfo, check, result);
            lastActionTimes.put(uuid, currentTime);
        }
    }
    
    /**
     * Parse action string
     */
    private ActionInfo parseAction(String actionString) {
        String[] parts = actionString.split(":", 2);
        String type = parts[0].toUpperCase();
        String parameter = parts.length > 1 ? parts[1] : "";
        
        return new ActionInfo(type, parameter);
    }
    
    /**
     * Handle FLAG action
     */
    private void handleFlag(Player player, ActionInfo action, Check check, CheckResult result) {
        // Flag is handled by violation system
        // This is just a placeholder
    }
    
    /**
     * Handle ALERT action
     */
    private void handleAlert(Player player, ActionInfo action, Check check, CheckResult result) {
        String message = plugin.getConfigManager().getMessage(
            "alerts.punishment",
            "player", player.getName(),
            "reason", result.getReason()
        );
        
        // Send to all staff
        Bukkit.getOnlinePlayers().forEach(staff -> {
            if (staff.hasPermission("aetherguard.alerts")) {
                staff.sendMessage(message);
            }
        });
    }
    
    /**
     * Handle KICK action
     */
    private void handleKick(Player player, ActionInfo action, Check check, CheckResult result) {
        String reason = action.getParameter().isEmpty() ? 
            plugin.getConfigManager().getMessage("general.default-kick-reason") : 
            action.getParameter();
        
        player.kickPlayer(reason);
        
        plugin.getLogger().info("§cKicked player " + player.getName() + " for: " + reason);
    }
    
    /**
     * Handle BAN action
     */
    private void handleBan(Player player, ActionInfo action, Check check, CheckResult result) {
        String reason = action.getParameter().isEmpty() ? 
            plugin.getConfigManager().getMessage("general.default-ban-reason") : 
            action.getParameter();
        
        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(player.getName(), reason, null, null);
        player.kickPlayer(reason);
        
        plugin.getLogger().info("§cBanned player " + player.getName() + " for: " + reason);
    }
    
    /**
     * Handle TEMPBAN action
     */
    private void handleTempBan(Player player, ActionInfo action, Check check, CheckResult result) {
        String parameter = action.getParameter();
        String[] parts = parameter.split(" ", 2);
        
        if (parts.length < 2) {
            plugin.getLogger().warning("§cInvalid tempban format: " + parameter);
            return;
        }
        
        String durationStr = parts[0];
        String reason = parts[1];
        
        long duration = parseDuration(durationStr);
        if (duration <= 0) {
            plugin.getLogger().warning("§cInvalid duration format: " + durationStr);
            return;
        }
        
        java.util.Date banExpiry = new java.util.Date(System.currentTimeMillis() + (duration * 60 * 1000));
        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(player.getName(), reason + " (Expires: " + durationStr + ")", banExpiry, null);
        player.kickPlayer(reason + " (Duration: " + durationStr + ")");
        
        // Schedule unban
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).pardon(player.getName());
        }, duration * 20 * 60); // Convert minutes to ticks
        
        plugin.getLogger().info("§cTemporarily banned player " + player.getName() + " for " + durationStr + ": " + reason);
    }
    
    /**
     * Handle FREEZE action - Prevents player movement for specified duration
     */
    private void handleFreeze(Player player, ActionInfo action, Check check, CheckResult result) {
        long duration = parseDuration(action.getParameter());
        if (duration <= 0) {
            duration = 30;
        }
        
        plugin.getPlayerManager().getPlayerData(player).setFrozen(true);
        
        org.bukkit.Location frozenLoc = player.getLocation();
        player.teleport(frozenLoc);
        player.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
        
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (player.isOnline()) {
                plugin.getPlayerManager().getPlayerData(player).setFrozen(false);
                player.sendMessage("§a§lAetherGuard §7» §aYou have been unfrozen");
            }
        }, duration * 20);
        
        player.sendMessage("§c§lAetherGuard §7» §cYou have been frozen for " + duration + " seconds");
        plugin.getLogger().warning("§cFroze player " + player.getName() + " for " + duration + " seconds");
    }
    
    /**
     * Handle COMMAND action
     */
    private void handleCommand(Player player, ActionInfo action, Check check, CheckResult result) {
        String command = action.getParameter()
                .replace("%player%", player.getName())
                .replace("%uuid%", player.getUniqueId().toString())
                .replace("%reason%", result.getReason())
                .replace("%check%", check.getFullName());
        
        if (!command.startsWith("/")) {
            command = "/" + command;
        }
        
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        plugin.getLogger().info("§7Executed command: " + command);
    }
    
    /**
     * Handle LOG action - Write violation to log file
     */
    private void handleLog(Player player, ActionInfo action, Check check, CheckResult result) {
        String logMessage = String.format(
            "[%s] %s (%s) - Check: %s - Reason: %s - Confidence: %.2f",
            new java.util.Date().toString(),
            player.getName(),
            player.getUniqueId().toString(),
            check.getFullName(),
            result.getReason(),
            result.getConfidence()
        );
        
        plugin.getLogger().info("§7[LOG] " + logMessage);
        writeToLogFile(logMessage, player, check);
    }
    
    /**
     * Write violation to persistent log file
     */
    private void writeToLogFile(String logMessage, Player player, Check check) {
        try {
            java.io.File logsDir = new java.io.File(plugin.getDataFolder(), "logs");
            if (!logsDir.exists()) {
                logsDir.mkdirs();
            }
            
            String date = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
            java.io.File logFile = new java.io.File(logsDir, "violations-" + date + ".log");
            
            java.io.FileWriter writer = new java.io.FileWriter(logFile, true);
            writer.write(logMessage + "\n");
            writer.close();
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to write to log file: " + e.getMessage());
        }
    }
    
    /**
     * Handle WEBHOOK action - Send violation to Discord/webhook
     */
    private void handleWebhook(Player player, ActionInfo action, Check check, CheckResult result) {
        String webhookUrl = action.getParameter();
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            return;
        }
        
        plugin.getExecutorService().submit(() -> {
            try {
                String payload = buildWebhookPayload(player, check, result);
                sendWebhook(webhookUrl, payload);
                plugin.getLogger().info("§7[WEBHOOK] Sent violation report for " + player.getName());
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send webhook: " + e.getMessage());
            }
        });
    }
    
    /**
     * Build Discord webhook JSON payload
     */
    private String buildWebhookPayload(Player player, Check check, CheckResult result) {
        StringBuilder json = new StringBuilder("{");
        json.append("\"embeds\":[{");
        json.append("\"title\":\"Violation Alert\",");
        json.append("\"description\":\"Player flagged by anti-cheat\",");
        json.append("\"color\":16711680,");
        json.append("\"fields\":[");
        json.append("{\"name\":\"Player\",\"value\":\"").append(player.getName()).append("\",\"inline\":true},");
        json.append("{\"name\":\"UUID\",\"value\":\"").append(player.getUniqueId()).append("\",\"inline\":true},");
        json.append("{\"name\":\"Check\",\"value\":\"").append(check.getFullName()).append("\",\"inline\":false},");
        json.append("{\"name\":\"Reason\",\"value\":\"").append(result.getReason()).append("\",\"inline\":false},");
        json.append("{\"name\":\"Confidence\",\"value\":\"").append(String.format("%.2f", result.getConfidence())).append("%\",\"inline\":true}");
        json.append("]}]}");
        return json.toString();
    }
    
    /**
     * Send webhook to URL
     */
    private void sendWebhook(String webhookUrl, String payload) throws Exception {
        java.net.URL url = new java.net.URL(webhookUrl);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        
        try (java.io.OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        
        conn.getResponseCode();
        conn.disconnect();
    }
    
    /**
     * Parse duration string
     */
    private long parseDuration(String durationStr) {
        try {
            if (durationStr.endsWith("s")) {
                return Long.parseLong(durationStr.substring(0, durationStr.length() - 1));
            } else if (durationStr.endsWith("m")) {
                return Long.parseLong(durationStr.substring(0, durationStr.length() - 1)) * 60;
            } else if (durationStr.endsWith("h")) {
                return Long.parseLong(durationStr.substring(0, durationStr.length() - 1)) * 3600;
            } else if (durationStr.endsWith("d")) {
                return Long.parseLong(durationStr.substring(0, durationStr.length() - 1)) * 86400;
            } else {
                return Long.parseLong(durationStr) * 60; // Default to minutes
            }
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    /**
     * Action information container
     */
    private static class ActionInfo {
        private final String type;
        private final String parameter;
        private final long cooldown;
        private final long delay;
        
        public ActionInfo(String type, String parameter) {
            this.type = type;
            this.parameter = parameter;
            this.cooldown = 1000; // 1 second default cooldown
            this.delay = 0; // No delay by default
        }
        
        public String getType() { return type; }
        public String getParameter() { return parameter; }
        public long getCooldown() { return cooldown; }
        public long getDelay() { return delay; }
    }
    
    /**
     * Action handler interface
     */
    @FunctionalInterface
    private interface ActionHandler {
        void handle(Player player, ActionInfo action, Check check, CheckResult result);
    }
}