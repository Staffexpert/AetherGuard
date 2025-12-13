package com.aetherguard.managers.analytics;

import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🧠 AetherGuard Behavioral Analysis Manager
 *
 * Analiza patrones de comportamiento del jugador a lo largo del tiempo para detectar
 * anomalías que puedan indicar el uso de trampas, bots o scripts.
 * Se enfoca en métricas observables en lugar de simulaciones.
 *
 * @author AetherGuard Team
 * @version 3.0.0
 */
public class BehavioralAnalysisManager {

    private final AetherGuard plugin;
    private final Map<UUID, PlayerBehaviorProfile> behaviorProfiles;

    // Constantes de análisis
    private static final int HISTORY_SIZE = 200; // Últimas 10 segundos de acciones (a 20 TPS)
    private static final double HIGH_RISK_THRESHOLD = 0.8;
    private static final double MEDIUM_RISK_THRESHOLD = 0.6;

    public BehavioralAnalysisManager(AetherGuard plugin) {
        this.plugin = plugin;
        this.behaviorProfiles = new ConcurrentHashMap<>();

        startAnalysisScheduler();
    }

    /**
     * Registra una acción del jugador para su posterior análisis.
     * @param player El jugador que realizó la acción.
     * @param actionType El tipo de acción (e.g., "MOVE", "ATTACK", "INTERACT").
     * @param metadata Datos adicionales sobre la acción.
     */
    public void recordPlayerAction(Player player, String actionType, Map<String, Double> metadata) {
        UUID uuid = player.getUniqueId();
        PlayerBehaviorProfile profile = behaviorProfiles.computeIfAbsent(uuid, k -> new PlayerBehaviorProfile(player.getName()));
        profile.addAction(new PlayerAction(actionType, metadata));
    }

    /**
     * Analiza el perfil de comportamiento de un jugador y devuelve un resultado.
     * @param player El jugador a analizar.
     * @return Un objeto `BehavioralAnalysisResult` con la puntuación de riesgo y otras métricas.
     */
    public BehavioralAnalysisResult analyzePlayer(Player player) {
        PlayerBehaviorProfile profile = behaviorProfiles.get(player.getUniqueId());
        if (profile == null || profile.getActionHistory().size() < 50) {
            return new BehavioralAnalysisResult(0.0, "INSUFFICIENT_DATA", 0.0, 0.0);
        }

        // Calcular métricas de comportamiento
        double actionEntropy = calculateActionEntropy(profile);
        double timingConsistency = calculateTimingConsistency(profile);

        // La entropía alta puede ser humana, la entropía muy baja es robótica.
        // La consistencia de tiempo muy alta es robótica.
        double entropyRisk = 1.0 - Math.abs(actionEntropy - 0.7); // El pico de "humanidad" está en 0.7
        double timingRisk = timingConsistency;

        // Combinar riesgos (ponderado)
        double totalRisk = (entropyRisk * 0.4) + (timingRisk * 0.6);
        totalRisk = Math.min(1.0, totalRisk); // Normalizar

        String assessment = assessRisk(totalRisk);

        return new BehavioralAnalysisResult(totalRisk, assessment, actionEntropy, timingConsistency);
    }

    private String assessRisk(double riskScore) {
        if (riskScore >= HIGH_RISK_THRESHOLD) return "HIGH_RISK_BEHAVIOR";
        if (riskScore >= MEDIUM_RISK_THRESHOLD) return "SUSPICIOUS_BEHAVIOR";
        return "NORMAL_BEHAVIOR";
    }

    /**
     * Calcula la entropía de las acciones del jugador.
     * Una entropía muy baja puede indicar un bot (acciones repetitivas).
     * Una entropía muy alta puede indicar un humano o un cheat caótico.
     */
    private double calculateActionEntropy(PlayerBehaviorProfile profile) {
        Map<String, Long> actionCounts = new HashMap<>();
        profile.getActionHistory().forEach(action ->
            actionCounts.merge(action.actionType, 1L, Long::sum)
        );

        double entropy = 0.0;
        int totalActions = profile.getActionHistory().size();
        for (long count : actionCounts.values()) {
            double probability = (double) count / totalActions;
            entropy -= probability * (Math.log(probability) / Math.log(2));
        }

        // Normalizar por el número de tipos de acción
        if (actionCounts.size() > 1) {
            entropy /= (Math.log(actionCounts.size()) / Math.log(2));
        }

        return entropy;
    }

    /**
     * Mide la consistencia en los intervalos de tiempo entre acciones similares.
     * Una consistencia muy alta (jitter bajo) es un fuerte indicador de automatización.
     */
    private double calculateTimingConsistency(PlayerBehaviorProfile profile) {
        List<Long> intervals = new ArrayList<>();
        long lastTimestamp = -1;

        for (PlayerAction action : profile.getActionHistory()) {
            if (lastTimestamp != -1) {
                intervals.add(action.timestamp - lastTimestamp);
            }
            lastTimestamp = action.timestamp;
        }

        if (intervals.size() < 10) return 0.0;

        double averageInterval = intervals.stream().mapToLong(l -> l).average().orElse(0.0);
        double variance = intervals.stream()
            .mapToDouble(i -> Math.pow(i - averageInterval, 2))
            .average().orElse(0.0);

        double stdDev = Math.sqrt(variance);
        double jitter = stdDev / averageInterval; // Coeficiente de variación (jitter normalizado)

        // Un jitter bajo significa alta consistencia (malo). Invertimos el valor.
        return Math.max(0.0, 1.0 - jitter);
    }

    private void startAnalysisScheduler() {
        plugin.getAsyncExecutor().scheduleAtFixedRate(() -> {
            try {
                // Limpiar perfiles de jugadores desconectados
                behaviorProfiles.keySet().removeIf(uuid -> plugin.getServer().getPlayer(uuid) == null);
            } catch (Exception e) {
                plugin.getAetherGuardLogger().error("Error en el scheduler de análisis de comportamiento", e);
            }
        }, 5, 5, java.util.concurrent.TimeUnit.MINUTES);
    }

    /**
     * Perfil de comportamiento de un jugador.
     */
    private static class PlayerBehaviorProfile {
        private final String playerName;
        private final Deque<PlayerAction> actionHistory;

        public PlayerBehaviorProfile(String playerName) {
            this.playerName = playerName;
            this.actionHistory = new ArrayDeque<>(HISTORY_SIZE + 1);
        }

        public void addAction(PlayerAction action) {
            if (actionHistory.size() >= HISTORY_SIZE) {
                actionHistory.poll(); // Remove oldest action
            }
            actionHistory.offer(action);
        }

        public Deque<PlayerAction> getActionHistory() {
            return actionHistory;
        }
    }

    /**
     * Representa una única acción realizada por un jugador.
     */
    private static class PlayerAction {
        final long timestamp;
        final String actionType;
        final Map<String, Double> metadata;

        PlayerAction(String actionType, Map<String, Double> metadata) {
            this.timestamp = System.currentTimeMillis();
            this.actionType = actionType;
            this.metadata = metadata;
        }
    }

    /**
     * Resultado del análisis de comportamiento.
     */
    public static class BehavioralAnalysisResult {
        public final double riskScore;
        public final String assessment;
        public final double actionEntropy;
        public final double timingConsistency;

        public BehavioralAnalysisResult(double riskScore, String assessment, double actionEntropy, double timingConsistency) {
            this.riskScore = riskScore;
            this.assessment = assessment;
            this.actionEntropy = actionEntropy;
            this.timingConsistency = timingConsistency;
        }
    }
}