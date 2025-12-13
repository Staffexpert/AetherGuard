package com.aetherguard.managers;

import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🧠 AetherGuard Adaptive Learning Manager - PREMIUM Feature
 *
 * Sistema de aprendizaje automático que adapta los checks basándose en falsos positivos
 * y comportamientos legítimos observados, mejorando continuamente la precisión
 *
 * @author AetherGuard Team
 * @version 2.0.0
 */
public class AdaptiveLearningManager {

    private final AetherGuard plugin;
    private final Map<String, AdaptiveRule> adaptiveRules;
    private final Map<UUID, PlayerLearningProfile> playerProfiles;

    // Configuración de aprendizaje
    private static final int MIN_SAMPLES_FOR_LEARNING = 100;
    private static final double LEARNING_RATE = 0.01;
    private static final double FORGETTING_FACTOR = 0.95;

    public AdaptiveLearningManager(AetherGuard plugin) {
        this.plugin = plugin;
        this.adaptiveRules = new ConcurrentHashMap<>();
        this.playerProfiles = new ConcurrentHashMap<>();

        initializeAdaptiveRules();
        startLearningCycle();
    }

    /**
     * Registra un resultado de check para aprendizaje
     */
    public void learnFromCheckResult(String checkName, Player player, boolean wasFalsePositive, double confidence, Map<String, Object> context) {
        String ruleKey = checkName + ":" + player.getUniqueId();
        AdaptiveRule rule = adaptiveRules.computeIfAbsent(ruleKey, k -> new AdaptiveRule(checkName, player.getName()));

        rule.addSample(wasFalsePositive, confidence, context);

        // Actualizar perfil del jugador
        PlayerLearningProfile profile = playerProfiles.computeIfAbsent(player.getUniqueId(),
            k -> new PlayerLearningProfile(player.getName()));
        profile.updateBehavior(checkName, wasFalsePositive, confidence);
    }

    /**
     * Obtiene ajuste adaptativo para un check
     */
    public AdaptiveAdjustment getAdaptiveAdjustment(String checkName, Player player) {
        PlayerLearningProfile profile = playerProfiles.get(player.getUniqueId());
        if (profile == null) return new AdaptiveAdjustment(1.0, 0.0, "NO_DATA");

        double trustFactor = profile.getTrustFactor(checkName);
        double adjustment = calculateAdjustment(trustFactor, profile.getSampleCount(checkName));

        return new AdaptiveAdjustment(adjustment, trustFactor, profile.getBehaviorPattern(checkName));
    }

    /**
     * Predice si un resultado podría ser falso positivo
     */
    public double predictFalsePositiveProbability(String checkName, Player player, double currentConfidence) {
        PlayerLearningProfile profile = playerProfiles.get(player.getUniqueId());
        if (profile == null) return 0.0;

        double historicalFPRate = profile.getFalsePositiveRate(checkName);
        double trustFactor = profile.getTrustFactor(checkName);

        // Combinar factores históricos con confianza actual
        return (historicalFPRate * (1 - trustFactor)) + (currentConfidence * trustFactor * 0.1);
    }

    /**
     * Obtiene estadísticas de aprendizaje globales
     */
    public LearningStats getGlobalLearningStats() {
        int totalRules = adaptiveRules.size();
        int matureRules = (int) adaptiveRules.values().stream()
            .filter(rule -> rule.getSampleCount() >= MIN_SAMPLES_FOR_LEARNING)
            .count();

        double averageAdaptation = adaptiveRules.values().stream()
            .filter(rule -> rule.getSampleCount() >= MIN_SAMPLES_FOR_LEARNING)
            .mapToDouble(AdaptiveRule::getAdaptationFactor)
            .average()
            .orElse(1.0);

        return new LearningStats(totalRules, matureRules, averageAdaptation, playerProfiles.size());
    }

    private void initializeAdaptiveRules() {
        // Reglas base para diferentes tipos de checks
        adaptiveRules.put("MOVEMENT_SPEED", new AdaptiveRule("MOVEMENT_SPEED", "GLOBAL"));
        adaptiveRules.put("COMBAT_KILLAURA", new AdaptiveRule("COMBAT_KILLAURA", "GLOBAL"));
        adaptiveRules.put("WORLD_FASTBREAK", new AdaptiveRule("WORLD_FASTBREAK", "GLOBAL"));
    }

    private void startLearningCycle() {
        plugin.getAsyncExecutor().scheduleAtFixedRate(() -> {
            try {
                performLearningCycle();
            } catch (Exception e) {
                plugin.getLogger().severe("Error en ciclo de aprendizaje adaptativo: " + e.getMessage());
            }
        }, 10 * 60 * 1000L, 10 * 60 * 1000L, java.util.concurrent.TimeUnit.MILLISECONDS); // Cada 10 minutos
    }

    private void performLearningCycle() {
        // Aplicar olvido a reglas antiguas
        adaptiveRules.values().forEach(AdaptiveRule::applyForgetting);

        // Limpiar perfiles inactivos
        long cutoffTime = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L); // 30 días
        playerProfiles.entrySet().removeIf(entry -> entry.getValue().getLastActivity() < cutoffTime);

        plugin.getLogger().info("§b🧠 §fCiclo de aprendizaje completado - Reglas activas: " + adaptiveRules.size());
    }

    private double calculateAdjustment(double trustFactor, int sampleCount) {
        if (sampleCount < MIN_SAMPLES_FOR_LEARNING) return 1.0;

        // Ajuste basado en confianza: más confianza = más estricto
        double baseAdjustment = 1.0 + (trustFactor - 0.5) * 0.2;

        // Factor de madurez: más muestras = ajuste más significativo
        double maturityFactor = Math.min(1.0, sampleCount / 1000.0);

        return baseAdjustment * maturityFactor + 1.0 * (1.0 - maturityFactor);
    }

    /**
     * Regla adaptativa para un check específico
     */
    private static class AdaptiveRule {
        private final String checkName;
        private final String playerName;
        private final List<LearningSample> samples;
        private double adaptationFactor;

        public AdaptiveRule(String checkName, String playerName) {
            this.checkName = checkName;
            this.playerName = playerName;
            this.samples = Collections.synchronizedList(new ArrayList<>());
            this.adaptationFactor = 1.0;
        }

        public void addSample(boolean wasFalsePositive, double confidence, Map<String, Object> context) {
            samples.add(new LearningSample(wasFalsePositive, confidence, context, System.currentTimeMillis()));

            // Mantener solo las últimas 1000 muestras
            if (samples.size() > 1000) {
                samples.remove(0);
            }

            // Recalcular factor de adaptación
            recalculateAdaptationFactor();
        }

        private void recalculateAdaptationFactor() {
            if (samples.size() < MIN_SAMPLES_FOR_LEARNING) {
                adaptationFactor = 1.0;
                return;
            }

            double falsePositiveRate = samples.stream()
                .mapToInt(sample -> sample.wasFalsePositive ? 1 : 0)
                .average()
                .orElse(0.0);

            // Ajuste basado en tasa de falsos positivos
            adaptationFactor = 1.0 + (falsePositiveRate - 0.1) * 0.5; // Centrado en 10% FPR
            adaptationFactor = Math.max(0.5, Math.min(1.5, adaptationFactor));
        }

        public void applyForgetting() {
            // Aplicar factor de olvido a muestras antiguas
            samples.forEach(sample -> sample.confidence *= FORGETTING_FACTOR);

            // Remover muestras muy antiguas
            long cutoffTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L); // 7 días
            samples.removeIf(sample -> sample.timestamp < cutoffTime);

            recalculateAdaptationFactor();
        }

        public int getSampleCount() {
            return samples.size();
        }

        public double getAdaptationFactor() {
            return adaptationFactor;
        }
    }

    /**
     * Perfil de aprendizaje de un jugador
     */
    private static class PlayerLearningProfile {
        private final String playerName;
        private final Map<String, BehaviorData> behaviorData;
        private long lastActivity;

        public PlayerLearningProfile(String playerName) {
            this.playerName = playerName;
            this.behaviorData = new ConcurrentHashMap<>();
            this.lastActivity = System.currentTimeMillis();
        }

        public void updateBehavior(String checkName, boolean wasFalsePositive, double confidence) {
            BehaviorData data = behaviorData.computeIfAbsent(checkName, k -> new BehaviorData());
            data.addSample(wasFalsePositive, confidence);
            lastActivity = System.currentTimeMillis();
        }

        public double getTrustFactor(String checkName) {
            BehaviorData data = behaviorData.get(checkName);
            return data != null ? data.getTrustFactor() : 0.5;
        }

        public double getFalsePositiveRate(String checkName) {
            BehaviorData data = behaviorData.get(checkName);
            return data != null ? data.getFalsePositiveRate() : 0.0;
        }

        public int getSampleCount(String checkName) {
            BehaviorData data = behaviorData.get(checkName);
            return data != null ? data.getSampleCount() : 0;
        }

        public String getBehaviorPattern(String checkName) {
            BehaviorData data = behaviorData.get(checkName);
            if (data == null) return "UNKNOWN";

            double fpRate = data.getFalsePositiveRate();
            if (fpRate > 0.3) return "FREQUENT_FP";
            if (fpRate > 0.1) return "OCCASIONAL_FP";
            if (fpRate < 0.05) return "VERY_TRUSTWORTHY";
            return "NORMAL";
        }

        public long getLastActivity() {
            return lastActivity;
        }
    }

    /**
     * Datos de comportamiento para un check específico
     */
    private static class BehaviorData {
        private final List<BehaviorSample> samples;

        public BehaviorData() {
            this.samples = Collections.synchronizedList(new ArrayList<>());
        }

        public void addSample(boolean wasFalsePositive, double confidence) {
            samples.add(new BehaviorSample(wasFalsePositive, confidence, System.currentTimeMillis()));

            // Mantener solo últimas 500 muestras por check
            if (samples.size() > 500) {
                samples.remove(0);
            }
        }

        public double getTrustFactor() {
            if (samples.size() < MIN_SAMPLES_FOR_LEARNING) return 0.5;

            // Factor de confianza basado en consistencia y tiempo
            double consistency = calculateConsistency();
            double timeFactor = Math.min(1.0, samples.size() / 200.0);

            return (consistency + timeFactor) / 2.0;
        }

        public double getFalsePositiveRate() {
            if (samples.isEmpty()) return 0.0;

            long fpCount = samples.stream().mapToLong(s -> s.wasFalsePositive ? 1 : 0).sum();
            return (double) fpCount / samples.size();
        }

        public int getSampleCount() {
            return samples.size();
        }

        private double calculateConsistency() {
            if (samples.size() < 10) return 0.5;

            // Medir consistencia en los resultados
            double meanFP = getFalsePositiveRate();
            double variance = samples.stream()
                .mapToDouble(s -> Math.pow((s.wasFalsePositive ? 1.0 : 0.0) - meanFP, 2))
                .average()
                .orElse(0.0);

            return Math.max(0.0, 1.0 - variance);
        }
    }

    /**
     * Muestra de aprendizaje
     */
    private static class LearningSample {
        final boolean wasFalsePositive;
        double confidence;
        final Map<String, Object> context;
        final long timestamp;

        LearningSample(boolean wasFalsePositive, double confidence, Map<String, Object> context, long timestamp) {
            this.wasFalsePositive = wasFalsePositive;
            this.confidence = confidence;
            this.context = context != null ? new HashMap<>(context) : new HashMap<>();
            this.timestamp = timestamp;
        }
    }

    /**
     * Muestra de comportamiento
     */
    private static class BehaviorSample {
        final boolean wasFalsePositive;
        final double confidence;
        final long timestamp;

        BehaviorSample(boolean wasFalsePositive, double confidence, long timestamp) {
            this.wasFalsePositive = wasFalsePositive;
            this.confidence = confidence;
            this.timestamp = timestamp;
        }
    }

    /**
     * Ajuste adaptativo
     */
    public static class AdaptiveAdjustment {
        public final double multiplier;
        public final double trustFactor;
        public final String behaviorPattern;

        public AdaptiveAdjustment(double multiplier, double trustFactor, String behaviorPattern) {
            this.multiplier = multiplier;
            this.trustFactor = trustFactor;
            this.behaviorPattern = behaviorPattern;
        }
    }

    /**
     * Estadísticas de aprendizaje
     */
    public static class LearningStats {
        public final int totalRules;
        public final int matureRules;
        public final double averageAdaptation;
        public final int activeProfiles;

        public LearningStats(int totalRules, int matureRules, double averageAdaptation, int activeProfiles) {
            this.totalRules = totalRules;
            this.matureRules = matureRules;
            this.averageAdaptation = averageAdaptation;
            this.activeProfiles = activeProfiles;
        }
    }
}