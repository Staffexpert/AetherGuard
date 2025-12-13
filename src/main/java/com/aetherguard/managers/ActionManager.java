package com.aetherguard.managers;

import com.aetherguard.checks.Check;
import com.aetherguard.checks.CheckResult;
import com.aetherguard.core.AetherGuard;
import com.aetherguard.managers.interfaces.IActionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * AetherGuard v1.2.0 - Action Manager
 *
 * Manages and executes punishment actions with async/sync scheduling,
 * cooldown tracking, and comprehensive error handling.
 * Supports multiple action types: FLAG, ALERT, KICK, BAN, TEMPBAN, FREEZE, COMMAND, LOG, WEBHOOK
 *
 * @author AetherGuard Team
 * @version 1.2.0
 */
public class ActionManager implements IActionManager {

    private final AetherGuard plugin;
    private final Map<String, ActionHandler> actionHandlers;
    private final Map<UUID, Map<String, Long>> lastActionTimesByType;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public ActionManager(AetherGuard plugin) {
        this.plugin = plugin;
        this.actionHandlers = new ConcurrentHashMap<>();
        this.lastActionTimesByType = new ConcurrentHashMap<>();
        initializeActionHandlers();
    }

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

    public void executeAction(Player player, String actionName, Check check, CheckResult result) {
        if (player == null || actionName == null) {
            return;
        }

        try {
            ActionInfo actionInfo = parseAction(actionName);
            String type = actionInfo.getType();
            long now = System.currentTimeMillis();

            Map<String, Long> perType = lastActionTimesByType.computeIfAbsent(
                player.getUniqueId(), k -> new ConcurrentHashMap<>()
            );
            Long lastExecution = perType.get(type);
            if (lastExecution != null && (now - lastExecution) < actionInfo.getCooldown()) {
                return;
            }

            ActionHandler handler = actionHandlers.get(type);
            if (handler == null) {
                plugin.getLogger().log(Level.WARNING, "Unknown action type: " + type);
                return;
            }

            Runnable task = () -> executeActionSafely(player, actionInfo, check, result, handler, perType, type);

            long delayMs = actionInfo.getDelay();
            if (delayMs > 0) {
                long ticks = Math.max(1L, (delayMs + 49) / 50);
                plugin.getServer().getScheduler().runTaskLater(plugin, task, ticks);
            } else {
                if (isAsyncAction(type)) {
                    plugin.getAsyncExecutor().submit(task);
                } else {
                    plugin.getServer().getScheduler().runTask(plugin, task);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error executing action", e);
        }
    }

    private void executeActionSafely(Player player, ActionInfo action, Check check, CheckResult result,
                                      ActionHandler handler, Map<String, Long> perType, String type) {
        try {
            handler.handle(player, action, check, result);
            perType.put(type, System.currentTimeMillis());
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Action handler failed for " + type, e);
        }
    }

    private boolean isAsyncAction(String type) {
        return type.equals("WEBHOOK") || type.equals("LOG") || type.equals("COMMAND");
    }

    private ActionInfo parseAction(String actionString) {
        String[] parts = actionString.split(":", 2);
        String type = parts[0].toUpperCase(Locale.ROOT);
        String parameter = parts.length > 1 ? parts[1] : "";

        long cooldown = 1000L;
        long delay = 0L;

        if (!parameter.isEmpty() && parameter.contains("|")) {
            String[] segs = parameter.split("\\|");
            StringBuilder leftover = new StringBuilder();
            for (String seg : segs) {
                if (seg.contains("=")) {
                    String[] kv = seg.split("=", 2);
                    String key = kv[0].toLowerCase(Locale.ROOT);
                    String value = kv[1];
                    try {
                        if ("cooldown".equals(key)) cooldown = Long.parseLong(value);
                        else if ("delay".equals(key)) delay = Long.parseLong(value);
                    } catch (NumberFormatException ignored) {}
                } else {
                    if (leftover.length() > 0) leftover.append("|");
                    leftover.append(seg);
                }
            }
            parameter = leftover.toString();
        }
        return new ActionInfo(type, parameter, cooldown, delay);
    }

    private void handleFlag(Player player, ActionInfo action, Check check, CheckResult result) {
        plugin.getViolationManager().addViolation(player.getUniqueId(), check.getFullName(), result.getConfidence(), result.getReason());
        plugin.getLogger().log(Level.INFO, "[FLAG] " + player.getName() + " flagged by " + check.getFullName());
    }

    private void handleAlert(Player player, ActionInfo action, Check check, CheckResult result) {
        String msg = plugin.getConfigManager().getMessage("alerts.punishment",
            "player", player.getName(), "reason", result.getReason());
        plugin.getServer().getOnlinePlayers().stream()
            .filter(p -> p.hasPermission("aetherguard.alerts"))
            .forEach(p -> p.sendMessage(msg));
    }

    private void handleKick(Player player, ActionInfo action, Check check, CheckResult result) {
        String reason = action.getParameter().isEmpty() ?
            plugin.getConfigManager().getMessage("general.default-kick-reason") :
            action.getParameter();
        
        plugin.getServer().getScheduler().runTask(plugin, () -> player.kickPlayer(reason));
        plugin.getLogger().log(Level.INFO, "Kicked " + player.getName() + " for: " + reason);
    }

    private void handleBan(Player player, ActionInfo action, Check check, CheckResult result) {
        String reason = action.getParameter().isEmpty() ?
            plugin.getConfigManager().getMessage("general.default-ban-reason") :
            action.getParameter();

        // Usar BAN WAVES para baneos inteligentes
        plugin.getBanWaveManager().queueBan(player, reason, check.getFullName());
    }

    private void handleTempBan(Player player, ActionInfo action, Check check, CheckResult result) {
        String param = action.getParameter();
        String[] parts = param.split(" ", 2);
        if (parts.length < 2) {
            plugin.getLogger().log(Level.WARNING, "Invalid tempban format: " + param);
            return;
        }

        long duration = parseDuration(parts[0]);
        if (duration <= 0) {
            plugin.getLogger().log(Level.WARNING, "Invalid duration: " + parts[0]);
            return;
        }

        String reason = parts[1];
        Date expiry = new Date(System.currentTimeMillis() + duration * 1000L);
        
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(player.getName(), reason, expiry, null);
            player.kickPlayer(reason + " (temp)");
        });
        plugin.getLogger().log(Level.INFO, "Temp-banned " + player.getName() + " for " + duration + "s");
    }

    private void handleFreeze(Player player, ActionInfo action, Check check, CheckResult result) {
        long duration = parseDuration(action.getParameter());
        if (duration <= 0) duration = 30;

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            player.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
            player.sendMessage("§c§lAetherGuard §7» §cYou have been frozen");
        });

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.sendMessage("§a§lAetherGuard §7» §aYou have been unfrozen");
            }
        }, duration * 20);
    }

    private void handleCommand(Player player, ActionInfo action, Check check, CheckResult result) {
        String command = action.getParameter()
            .replace("%player%", player.getName())
            .replace("%uuid%", player.getUniqueId().toString())
            .replace("%reason%", result.getReason())
            .replace("%check%", check.getFullName());

        String finalCommand = command.startsWith("/") ? command : "/" + command;
        
        plugin.getAsyncExecutor().submit(() -> {
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Command execution failed", e);
            }
        });
    }

    private void handleLog(Player player, ActionInfo action, Check check, CheckResult result) {
        String msg = String.format("[%s] %s (%s) - Check: %s - Reason: %s - Confidence: %.2f",
            dateFormat.format(new Date()), player.getName(), player.getUniqueId(),
            check.getFullName(), result.getReason(), result.getConfidence());
        
        plugin.getAsyncExecutor().submit(() -> {
            try {
                writeToLogFile(msg);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Log write failed", e);
            }
        });
    }

    private void writeToLogFile(String message) throws Exception {
        File logsDir = new File(plugin.getDataFolder(), "logs");
        if (!logsDir.exists()) logsDir.mkdirs();

        String date = logDateFormat.format(new Date());
        File logFile = new File(logsDir, "violations-" + date + ".log");

        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(message + "\n");
            writer.flush();
        }
    }

    private void handleWebhook(Player player, ActionInfo action, Check check, CheckResult result) {
        String webhookUrl = action.getParameter();
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            return;
        }

        plugin.getAsyncExecutor().submit(() -> {
            try {
                String payload = buildWebhookPayload(player, check, result);
                sendWebhook(webhookUrl, payload);
                plugin.getLogger().log(Level.FINE, "Webhook sent for " + player.getName());
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Webhook failed", e);
            }
        });
    }

    private String buildWebhookPayload(Player player, Check check, CheckResult result) {
        return "{"
            + "\"embeds\":[{"
            + "\"title\":\"Violation Alert\","
            + "\"description\":\"Player flagged by AetherGuard\","
            + "\"color\":16711680,"
            + "\"fields\":["
            + "{\"name\":\"Player\",\"value\":\"" + escapeJson(player.getName()) + "\",\"inline\":true},"
            + "{\"name\":\"UUID\",\"value\":\"" + player.getUniqueId() + "\",\"inline\":true},"
            + "{\"name\":\"Check\",\"value\":\"" + escapeJson(check.getFullName()) + "\",\"inline\":false},"
            + "{\"name\":\"Reason\",\"value\":\"" + escapeJson(result.getReason()) + "\",\"inline\":false},"
            + "{\"name\":\"Confidence\",\"value\":\"" + String.format("%.2f%%", result.getConfidence()) + "\",\"inline\":true}"
            + "]}]}";
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private void sendWebhook(String webhookUrl, String payload) throws Exception {
        URL url = new URL(webhookUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode >= 400) {
            throw new RuntimeException("Webhook responded with " + responseCode);
        }
        conn.disconnect();
    }

    private long parseDuration(String durationStr) {
        if (durationStr == null || durationStr.isEmpty()) return -1;
        try {
            if (durationStr.endsWith("s")) return Long.parseLong(durationStr.substring(0, durationStr.length() - 1));
            if (durationStr.endsWith("m")) return Long.parseLong(durationStr.substring(0, durationStr.length() - 1)) * 60;
            if (durationStr.endsWith("h")) return Long.parseLong(durationStr.substring(0, durationStr.length() - 1)) * 3600;
            if (durationStr.endsWith("d")) return Long.parseLong(durationStr.substring(0, durationStr.length() - 1)) * 86400;
            return Long.parseLong(durationStr) * 60;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static class ActionInfo {
        private final String type;
        private final String parameter;
        private final long cooldown;
        private final long delay;

        public ActionInfo(String type, String parameter, long cooldown, long delay) {
            this.type = type;
            this.parameter = parameter;
            this.cooldown = cooldown;
            this.delay = delay;
        }

        public String getType() { return type; }
        public String getParameter() { return parameter; }
        public long getCooldown() { return cooldown; }
        public long getDelay() { return delay; }
    }

    @FunctionalInterface
    private interface ActionHandler {
        void handle(Player player, ActionInfo action, Check check, CheckResult result);
    }

    @Override
    public void executeAction(Player player, String action, Object source, Object result) {
        // Simplified implementation
        if (source instanceof Check && result instanceof CheckResult) {
            executeAction(player, action, (Check) source, (CheckResult) result);
        }
    }

    @Override
    public void executeActionAsync(Player player, String action, Object source, Object result) {
        plugin.getAsyncExecutor().submit(() -> executeAction(player, action, source, result));
    }

    @Override
    public boolean isActionCooldownActive(Player player, String action) {
        if (player == null) return false;
        Map<String, Long> perType = lastActionTimesByType.get(player.getUniqueId());
        if (perType == null) return false;
        Long last = perType.get(action);
        return last != null && (System.currentTimeMillis() - last) < 1000; // 1 second default
    }

    @Override
    public void setActionCooldown(Player player, String action, long durationMs) {
        if (player != null) {
            lastActionTimesByType.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .put(action, System.currentTimeMillis() + durationMs);
        }
    }

    @Override
    public int getExecutedActionsCount() {
        return lastActionTimesByType.values().stream()
            .mapToInt(Map::size)
            .sum();
    }

    @Override
    public void resetActionCooldowns(Player player) {
        if (player != null) {
            lastActionTimesByType.remove(player.getUniqueId());
        }
    }
}