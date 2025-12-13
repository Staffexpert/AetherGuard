package com.aetherguard.managers;

import com.aetherguard.core.AetherGuard;
import com.aetherguard.managers.analytics.BehavioralAnalysisManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
/**
 * 🛡️ AetherGuard BanWave Manager
 *
 * Gestiona la cola de baneos para agruparlos en olas, reduciendo el spam y
 * haciendo más difícil para los tramposos saber qué trampa fue detectada.
 *
 * Características:
 * - Cola de baneos priorizada.
 * - Olas de baneos automáticas y configurables.
 * - Ejecución asíncrona para no impactar el rendimiento del servidor.
 *
 * @author AetherGuard Team
 * @version 3.0.0-ULTRA
 */
public class BanWaveManager {

    private final AetherGuard plugin;
    private final PriorityQueue<BanRequest> banQueue;
    private final AtomicInteger waveCounter;
    private volatile boolean waveInProgress;
    private final Object waveLock = new Object();

    // Configuración ULTRA AVANZADA
    private final int maxBansPerWave;
    private final long waveIntervalMs;
    private final long banDelayMs;
    private final int maxQueueSize;
    private final int minQueueSizeForWave;

    // Métricas en tiempo real
    private final AtomicLong totalBansProcessed;

    public BanWaveManager(AetherGuard plugin) {
        this.plugin = plugin;
        this.banQueue = new PriorityQueue<>(Comparator.comparingLong(b -> b.priority));
        this.waveCounter = new AtomicInteger(0);
        this.waveInProgress = false;

        // Cargar configuración
        this.maxBansPerWave = plugin.getConfig().getInt("ban-wave.max-bans-per-wave", 10);
        this.waveIntervalMs = plugin.getConfig().getLong("ban-wave.interval-minutes", 5) * 60 * 1000;
        this.banDelayMs = plugin.getConfig().getLong("ban-wave.ban-delay-ms", 500);
        this.maxQueueSize = plugin.getConfig().getInt("ban-wave.max-queue-size", 1000);
        this.minQueueSizeForWave = plugin.getConfig().getInt("ban-wave.min-queue-for-wave", 5);

        this.totalBansProcessed = new AtomicLong(0);

        startWaveProcessor();
    }

    /**
     * Añade un jugador a la cola de baneo para la próxima ola.
     */
    public void queueBan(Player player, String reason, String checkName) {
        if (banQueue.size() >= maxQueueSize) {
            plugin.getLogger().log(Level.WARNING, "§c🚨 BAN WAVE ULTRA: Cola llena, baneo inmediato para " + player.getName());
            executeImmediateBan(player, reason);
            return;
        }

        // Calcular prioridad
        long priority = calculatePriority(player, checkName);

        BanRequest request = new BanRequest(player.getUniqueId(), player.getName(), reason, checkName, System.currentTimeMillis(), priority);
        banQueue.offer(request);

        plugin.getLogger().log(Level.INFO, "§b🌊 §fJugador " + player.getName() + " añadido a la cola de Ban Wave (prioridad: " + priority + ", cola: " + banQueue.size() + ")");

        // Iniciar una ola si se alcanza el umbral y no hay una en progreso
        if (banQueue.size() >= minQueueSizeForWave && !waveInProgress) {
            processWave();
        }
    }

    /**
     * Calcula la prioridad para el baneo. Mayor prioridad significa que se baneará antes.
     */
    private long calculatePriority(Player player, String checkName) {
        double threatMultiplier = getThreatMultiplier(checkName.toLowerCase());
        BehavioralAnalysisManager.BehavioralAnalysisResult behaviorResult = plugin.getServiceContainer().get(BehavioralAnalysisManager.class).analyzePlayer(player);

        // La prioridad se basa en el riesgo y la amenaza, no en el tiempo.
        return (long) (behaviorResult.riskScore * 1000 * threatMultiplier);
    }

    /**
     * Obtiene multiplicador de amenaza por tipo de check
     */
    private double getThreatMultiplier(String checkName) {
        switch (checkName.toLowerCase()) {
            case "combat": return 1.5;
            case "movement": return 1.2;
            case "automation": return 1.8;
            case "exploits": return 2.0;
            default: return 1.0;
        }
    }

    /**
     * Procesa una ola de baneos
     */
    private void processWave() {
        synchronized (waveLock) {
            if (waveInProgress || banQueue.isEmpty()) {
                return;
            }

            waveInProgress = true;
            int waveNumber = waveCounter.incrementAndGet();

            plugin.getLogger().log(Level.INFO, "");
            plugin.getLogger().log(Level.INFO, "§b🌊 §lBAN WAVE #" + waveNumber + " INICIANDO §b🌊");
            plugin.getLogger().log(Level.INFO, "§b⚡ §fProcesando hasta " + maxBansPerWave + " baneos...");
            plugin.getLogger().log(Level.INFO, "");

            List<BanRequest> currentWave = new ArrayList<>();
            for (int i = 0; i < maxBansPerWave && !banQueue.isEmpty(); i++) {
                BanRequest request = banQueue.poll();
                if (request != null) {
                    currentWave.add(request);
                }
            }

            // Ejecutar baneos con delay controlado
            plugin.getAsyncExecutor().submit(() -> {
                try {
                    for (int i = 0; i < currentWave.size(); i++) {
                        BanRequest request = currentWave.get(i);

                        // Ejecutar el ban en el hilo principal de Bukkit
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            Player player = Bukkit.getPlayer(request.uuid);
                            if (player != null && player.isOnline()) {
                                executeBan(player, request.reason);
                                plugin.getLogger().log(Level.INFO, "§b🚫 §fBaneado: §c" + request.playerName + " §f- §7" + request.reason);
                            } else {
                                executeOfflineBan(request.playerName, request.reason);
                                plugin.getLogger().log(Level.INFO, "§b🚫 §fBan offline: §c" + request.playerName + " §f- §7" + request.reason);
                            }
                        });

                        // Delay entre baneos para evitar sobrecarga
                        if (i < currentWave.size() - 1) {
                            try {
                                Thread.sleep(banDelayMs);
                            } catch (InterruptedException e) {
                                plugin.getLogger().warning("Ban Wave delay interrumpido.");
                                Thread.currentThread().interrupt();
                                break;
                            }
                        }
                    }

                    plugin.getLogger().log(Level.INFO, "");
                    plugin.getLogger().log(Level.INFO, "§b✅ §lBAN WAVE #" + waveNumber + " COMPLETADA §b✅");
                    plugin.getLogger().log(Level.INFO, "§b📊 §fBaneos procesados: " + currentWave.size() + " | Cola restante: " + banQueue.size());
                    plugin.getLogger().log(Level.INFO, "");

                } finally {
                    synchronized (waveLock) {
                        waveInProgress = false;
                    }
                }
            });
        }
    }

    /**
     * Ejecuta un baneo inmediato (para casos críticos)
     */
    private void executeImmediateBan(Player player, String reason) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(player.getName(), reason, null, null);
            player.kickPlayer(reason);
        });
        plugin.getLogger().log(Level.SEVERE, "§c🚫 BAN INMEDIATO: " + player.getName() + " - " + reason);
    }

    /**
     * Ejecuta baneo normal
     */
    private void executeBan(Player player, String reason) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(player.getName(), reason, null, null);
            player.kickPlayer(reason);
        });
    }

    /**
     * Ejecuta ban offline
     */
    private void executeOfflineBan(String playerName, String reason) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(playerName, reason, null, null);
        });
    }

    /**
     * Inicia el procesador automático de olas
     */
    private void startWaveProcessor() {
        plugin.getAsyncExecutor().scheduleAtFixedRate(() -> {
            try {
                if (banQueue.size() >= minQueueSizeForWave) {
                    processWave();
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error en el procesador de Ban Waves", e);
            }
        }, waveIntervalMs, waveIntervalMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * Obtiene estadísticas de BAN WAVES
     */
    public BanWaveStats getStats() {
        return new BanWaveStats(
            waveCounter.get(),
            banQueue.size(),
            waveInProgress,
            banQueue.stream().mapToLong(r -> System.currentTimeMillis() - r.timestamp).average().orElse(0)
        );
    }

    /**
     * Fuerza el procesamiento de una ola
     */
    public void forceWave() {
        if (!waveInProgress) {
            processWave();
        }
    }

    /**
     * Limpia la cola de baneos
     */
    public void clearQueue() {
        int cleared = banQueue.size();
        banQueue.clear();
        plugin.getLogger().log(Level.INFO, "§b🧹 §fCola de BAN WAVES limpiada (" + cleared + " baneos cancelados)");
    }

    /**
     * Clase interna para solicitudes de baneo
     */
    private static class BanRequest {
        final UUID uuid;
        final String playerName;
        final String reason;
        final String checkName;
        final long timestamp;
        final long priority;

        BanRequest(UUID uuid, String playerName, String reason, String checkName, long timestamp, long priority) {
            this.uuid = uuid;
            this.playerName = playerName;
            this.reason = reason;
            this.checkName = checkName;
            this.timestamp = timestamp;
            this.priority = priority;
        }
    }

    /**
     * Clase para estadísticas
     */
    public static class BanWaveStats {
        public final int totalWaves;
        public final int queueSize;
        public final boolean waveInProgress;
        public final double averageWaitTime;

        public BanWaveStats(int totalWaves, int queueSize, boolean waveInProgress, double averageWaitTime) {
            this.totalWaves = totalWaves;
            this.queueSize = queueSize;
            this.waveInProgress = waveInProgress;
            this.averageWaitTime = averageWaitTime;
        }
    }
}