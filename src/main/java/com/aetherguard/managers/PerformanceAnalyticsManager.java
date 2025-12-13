package com.aetherguard.managers;

import com.aetherguard.core.AetherGuard;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 📊 AetherGuard Performance Analytics Manager - PREMIUM Feature
 *
 * Sistema avanzado de monitoreo y analytics de rendimiento en tiempo real
 * Proporciona métricas detalladas, optimización automática y alertas de rendimiento
 *
 * @author AetherGuard Team
 * @version 2.0.0
 */
public class PerformanceAnalyticsManager {

    private final AetherGuard plugin;
    private final Map<String, PerformanceMetrics> metricsHistory;
    private final AtomicLong totalMeasurements;

    // Umbrales de rendimiento
    private static final long HIGH_CPU_THRESHOLD_MS = 50;
    private static final long CRITICAL_MEMORY_THRESHOLD_MB = 512;
    private static final double TPS_DEGRADATION_THRESHOLD = 17.0;

    // Sistema de métricas
    private final ThreadMXBean threadMXBean;
    private final MemoryMXBean memoryMXBean;

    public PerformanceAnalyticsManager(AetherGuard plugin) {
        this.plugin = plugin;
        this.metricsHistory = new ConcurrentHashMap<>();
        this.totalMeasurements = new AtomicLong(0);

        this.threadMXBean = ManagementFactory.getThreadMXBean();
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();

        startPerformanceMonitoring();
    }

    /**
     * Registra métricas de rendimiento para un componente
     */
    public void recordMetrics(String component, long executionTime, int operations, Map<String, Object> metadata) {
        PerformanceMetrics metrics = metricsHistory.computeIfAbsent(component, k -> new PerformanceMetrics(component));
        metrics.addMeasurement(executionTime, operations, metadata);
        totalMeasurements.incrementAndGet();
    }

    /**
     * Obtiene métricas de rendimiento consolidadas
     */
    public ConsolidatedMetrics getConsolidatedMetrics() {
        long totalExecutions = 0;
        long totalTime = 0;
        long maxExecutionTime = 0;
        long minExecutionTime = Long.MAX_VALUE;

        for (PerformanceMetrics metrics : metricsHistory.values()) {
            PerformanceSnapshot snapshot = metrics.getSnapshot();
            totalExecutions += snapshot.totalOperations;
            totalTime += (long) snapshot.averageExecutionTime * snapshot.totalOperations;
            maxExecutionTime = Math.max(maxExecutionTime, snapshot.maxExecutionTime);
            minExecutionTime = Math.min(minExecutionTime, snapshot.minExecutionTime);
        }

        double avgExecutionTime = totalExecutions > 0 ? (double) totalTime / totalExecutions : 0.0;

        return new ConsolidatedMetrics(
            totalExecutions,
            avgExecutionTime,
            maxExecutionTime,
            minExecutionTime,
            getSystemHealthScore()
        );
    }

    /**
     * Obtiene métricas detalladas de un componente
     */
    public PerformanceSnapshot getComponentMetrics(String component) {
        PerformanceMetrics metrics = metricsHistory.get(component);
        return metrics != null ? metrics.getSnapshot() : new PerformanceSnapshot(0, 0, 0, 0, 0, 0);
    }

    /**
     * Analiza cuellos de botella de rendimiento
     */
    public List<BottleneckAnalysis> analyzeBottlenecks() {
        List<BottleneckAnalysis> bottlenecks = new ArrayList<>();

        for (Map.Entry<String, PerformanceMetrics> entry : metricsHistory.entrySet()) {
            PerformanceSnapshot snapshot = entry.getValue().getSnapshot();

            if (snapshot.averageExecutionTime > HIGH_CPU_THRESHOLD_MS) {
                bottlenecks.add(new BottleneckAnalysis(
                    entry.getKey(),
                    "HIGH_EXECUTION_TIME",
                    snapshot.averageExecutionTime,
                    "Tiempo de ejecución promedio muy alto"
                ));
            }

            if (snapshot.totalOperations > 10000 && snapshot.averageExecutionTime > 10) {
                bottlenecks.add(new BottleneckAnalysis(
                    entry.getKey(),
                    "HIGH_OPERATION_LOAD",
                    snapshot.totalOperations,
                    "Alto volumen de operaciones con latencia significativa"
                ));
            }
        }

        // Análisis de memoria del sistema
        long usedMemoryMB = getUsedMemoryMB();
        if (usedMemoryMB > CRITICAL_MEMORY_THRESHOLD_MB) {
            bottlenecks.add(new BottleneckAnalysis(
                "SYSTEM_MEMORY",
                "CRITICAL_MEMORY_USAGE",
                usedMemoryMB,
                "Uso de memoria crítica detectado"
            ));
        }

        // Análisis de TPS
        long currentTPS = plugin.getLastTPS();
        if (currentTPS < TPS_DEGRADATION_THRESHOLD) {
            bottlenecks.add(new BottleneckAnalysis(
                "SERVER_TPS",
                "TPS_DEGRADATION",
                currentTPS,
                "Degradación significativa del rendimiento del servidor"
            ));
        }

        return bottlenecks;
    }

    /**
     * Genera recomendaciones de optimización
     */
    public List<OptimizationRecommendation> generateOptimizationRecommendations() {
        List<OptimizationRecommendation> recommendations = new ArrayList<>();
        List<BottleneckAnalysis> bottlenecks = analyzeBottlenecks();

        for (BottleneckAnalysis bottleneck : bottlenecks) {
            switch (bottleneck.type) {
                case "HIGH_EXECUTION_TIME":
                    recommendations.add(new OptimizationRecommendation(
                        bottleneck.component,
                        "OPTIMIZE_ALGORITHM",
                        "Considera optimizar el algoritmo o usar procesamiento asíncrono",
                        OptimizationPriority.HIGH
                    ));
                    break;

                case "HIGH_OPERATION_LOAD":
                    recommendations.add(new OptimizationRecommendation(
                        bottleneck.component,
                        "IMPLEMENT_CACHING",
                        "Implementa caché para reducir operaciones repetitivas",
                        OptimizationPriority.MEDIUM
                    ));
                    break;

                case "CRITICAL_MEMORY_USAGE":
                    recommendations.add(new OptimizationRecommendation(
                        bottleneck.component,
                        "MEMORY_OPTIMIZATION",
                        "Revisa gestión de memoria y posibles leaks",
                        OptimizationPriority.CRITICAL
                    ));
                    break;

                case "TPS_DEGRADATION":
                    recommendations.add(new OptimizationRecommendation(
                        bottleneck.component,
                        "REDUCE_LOAD",
                        "Reduce la carga del servidor o mejora la infraestructura",
                        OptimizationPriority.CRITICAL
                    ));
                    break;
            }
        }

        return recommendations;
    }

    /**
     * Obtiene score de salud del sistema (0-100)
     */
    private double getSystemHealthScore() {
        double memoryHealth = Math.max(0, 100 - (getUsedMemoryMB() / 10.0)); // Cada 100MB resta 10 puntos
        double tpsHealth = Math.min(100, plugin.getLastTPS() * 5.83); // TPS * 5.83 para escalar a 100
        double cpuHealth = 100 - (getAverageExecutionTime() / 10.0); // Cada 10ms promedio resta 10 puntos

        return Math.max(0, Math.min(100, (memoryHealth + tpsHealth + cpuHealth) / 3.0));
    }

    private long getUsedMemoryMB() {
        return (memoryMXBean.getHeapMemoryUsage().getUsed() +
                memoryMXBean.getNonHeapMemoryUsage().getUsed()) / (1024 * 1024);
    }

    private double getAverageExecutionTime() {
        return metricsHistory.values().stream()
            .mapToDouble(metrics -> metrics.getSnapshot().averageExecutionTime)
            .average()
            .orElse(0.0);
    }

    private void startPerformanceMonitoring() {
        plugin.getAsyncExecutor().scheduleAtFixedRate(() -> {
            try {
                // Monitoreo continuo de métricas del sistema
                recordSystemMetrics();
            } catch (Exception e) {
                plugin.getLogger().severe("Error en monitoreo de rendimiento: " + e.getMessage());
            }
        }, 30 * 1000L, 30 * 1000L, java.util.concurrent.TimeUnit.MILLISECONDS); // Cada 30 segundos

        // Análisis de cuellos de botella cada 5 minutos
        plugin.getAsyncExecutor().scheduleAtFixedRate(() -> {
            try {
                List<BottleneckAnalysis> bottlenecks = analyzeBottlenecks();
                if (!bottlenecks.isEmpty()) {
                    plugin.getLogger().info("§b📊 §fAnálisis de rendimiento: " + bottlenecks.size() + " cuellos de botella detectados");
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error en análisis de cuellos de botella: " + e.getMessage());
            }
        }, 5 * 60 * 1000L, 5 * 60 * 1000L, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    private void recordSystemMetrics() {
        long usedMemory = getUsedMemoryMB();
        long tps = plugin.getLastTPS();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("memory_mb", usedMemory);
        metadata.put("tps", tps);
        metadata.put("active_threads", Thread.activeCount());

        recordMetrics("SYSTEM", 0, 1, metadata);
    }

    /**
     * Métricas de rendimiento para un componente
     */
    private static class PerformanceMetrics {
        private final String componentName;
        private final List<Measurement> measurements;
        private long totalExecutionTime;
        private long totalOperations;
        private long maxExecutionTime;
        private long minExecutionTime;

        public PerformanceMetrics(String componentName) {
            this.componentName = componentName;
            this.measurements = Collections.synchronizedList(new ArrayList<>());
            this.totalExecutionTime = 0;
            this.totalOperations = 0;
            this.maxExecutionTime = 0;
            this.minExecutionTime = Long.MAX_VALUE;
        }

        public void addMeasurement(long executionTime, int operations, Map<String, Object> metadata) {
            Measurement measurement = new Measurement(executionTime, operations, metadata, System.currentTimeMillis());
            measurements.add(measurement);

            totalExecutionTime += executionTime;
            totalOperations += operations;
            maxExecutionTime = Math.max(maxExecutionTime, executionTime);
            minExecutionTime = Math.min(minExecutionTime, executionTime);

            // Mantener solo las últimas 1000 mediciones
            if (measurements.size() > 1000) {
                Measurement oldest = measurements.remove(0);
                totalExecutionTime -= oldest.executionTime;
                totalOperations -= oldest.operations;
            }
        }

        public PerformanceSnapshot getSnapshot() {
            long count = measurements.size();
            double avgExecutionTime = count > 0 ? (double) totalExecutionTime / count : 0.0;
            double operationsPerSecond = count > 0 ? (double) totalOperations / (measurements.get((int)count - 1).timestamp - measurements.get(0).timestamp) * 1000 : 0.0;

            return new PerformanceSnapshot(
                totalOperations,
                avgExecutionTime,
                maxExecutionTime,
                minExecutionTime == Long.MAX_VALUE ? 0 : minExecutionTime,
                operationsPerSecond,
                count
            );
        }
    }

    /**
     * Medición individual
     */
    private static class Measurement {
        final long executionTime;
        final int operations;
        final Map<String, Object> metadata;
        final long timestamp;

        Measurement(long executionTime, int operations, Map<String, Object> metadata, long timestamp) {
            this.executionTime = executionTime;
            this.operations = operations;
            this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
            this.timestamp = timestamp;
        }
    }

    /**
     * Snapshot de rendimiento
     */
    public static class PerformanceSnapshot {
        public final long totalOperations;
        public final double averageExecutionTime;
        public final long maxExecutionTime;
        public final long minExecutionTime;
        public final double operationsPerSecond;
        public final long measurementCount;

        public PerformanceSnapshot(long totalOperations, double averageExecutionTime, long maxExecutionTime,
                                 long minExecutionTime, double operationsPerSecond, long measurementCount) {
            this.totalOperations = totalOperations;
            this.averageExecutionTime = averageExecutionTime;
            this.maxExecutionTime = maxExecutionTime;
            this.minExecutionTime = minExecutionTime;
            this.operationsPerSecond = operationsPerSecond;
            this.measurementCount = measurementCount;
        }
    }

    /**
     * Métricas consolidadas
     */
    public static class ConsolidatedMetrics {
        public final long totalOperations;
        public final double averageExecutionTime;
        public final long maxExecutionTime;
        public final long minExecutionTime;
        public final double systemHealthScore;

        public ConsolidatedMetrics(long totalOperations, double averageExecutionTime, long maxExecutionTime,
                                 long minExecutionTime, double systemHealthScore) {
            this.totalOperations = totalOperations;
            this.averageExecutionTime = averageExecutionTime;
            this.maxExecutionTime = maxExecutionTime;
            this.minExecutionTime = minExecutionTime;
            this.systemHealthScore = systemHealthScore;
        }
    }

    /**
     * Análisis de cuello de botella
     */
    public static class BottleneckAnalysis {
        public final String component;
        public final String type;
        public final double severity;
        public final String description;

        public BottleneckAnalysis(String component, String type, double severity, String description) {
            this.component = component;
            this.type = type;
            this.severity = severity;
            this.description = description;
        }
    }

    /**
     * Recomendación de optimización
     */
    public static class OptimizationRecommendation {
        public final String component;
        public final String type;
        public final String description;
        public final OptimizationPriority priority;

        public OptimizationRecommendation(String component, String type, String description, OptimizationPriority priority) {
            this.component = component;
            this.type = type;
            this.description = description;
            this.priority = priority;
        }
    }

    /**
     * Prioridad de optimización
     */
    public enum OptimizationPriority {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}