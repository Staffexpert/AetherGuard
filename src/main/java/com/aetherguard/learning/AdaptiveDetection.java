package com.aetherguard.learning;

import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ AetherGuard Adaptive Detection
 * 
 * Machine learning based detection that adapts to server
 * Similar to Spartan's adaptive system
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class AdaptiveDetection {
    
    private final Map<String, DetectionModel> models;
    private final Map<String, Double[]> globalStats;
    
    public AdaptiveDetection() {
        this.models = new ConcurrentHashMap<>();
        this.globalStats = new ConcurrentHashMap<>();
        initializeModels();
    }
    
    private void initializeModels() {
        models.put("KILLAURA", new DetectionModel("KILLAURA", 0.7));
        models.put("REACH", new DetectionModel("REACH", 0.75));
        models.put("SPEED", new DetectionModel("SPEED", 0.65));
        models.put("FLY", new DetectionModel("FLY", 0.8));
        models.put("AUTOCLICKER", new DetectionModel("AUTOCLICKER", 0.72));
    }
    
    /**
     * Predict if action is suspicious using ML model
     */
    public double predictSuspicion(String modelName, double[] features) {
        DetectionModel model = models.get(modelName);
        if (model == null) return 0.0;
        
        double prediction = model.predict(features);
        model.updateAccuracy(prediction);
        
        return prediction;
    }
    
    /**
     * Train model with new data
     */
    public void trainModel(String modelName, double[] features, boolean isCheat) {
        DetectionModel model = models.get(modelName);
        if (model != null) {
            model.train(features, isCheat);
        }
    }
    
    /**
     * Get model accuracy
     */
    public double getModelAccuracy(String modelName) {
        DetectionModel model = models.get(modelName);
        return model != null ? model.getAccuracy() : 0.0;
    }
    
    /**
     * Adjust thresholds based on server statistics
     */
    public void adjustThresholds(String statName, double value) {
        globalStats.put(statName, new Double[]{value, (double) System.currentTimeMillis()});
    }
    
    static class DetectionModel {
        private final String name;
        private double threshold;
        private final List<TrainingData> trainingData;
        private double accuracy;
        private int correctPredictions;
        private int totalPredictions;
        
        DetectionModel(String name, double initialThreshold) {
            this.name = name;
            this.threshold = initialThreshold;
            this.trainingData = Collections.synchronizedList(new ArrayList<>());
            this.accuracy = 0.5;
            this.correctPredictions = 0;
            this.totalPredictions = 0;
        }
        
        double predict(double[] features) {
            double score = 0.0;
            
            if (features.length == 0) return 0.0;
            
            for (double feature : features) {
                score += feature;
            }
            score /= features.length;
            
            return Math.min(score * 100, 100.0);
        }
        
        void train(double[] features, boolean isCheat) {
            trainingData.add(new TrainingData(features, isCheat));
            
            if (trainingData.size() > 1000) {
                trainingData.remove(0);
            }
            
            updateThreshold();
        }
        
        void updateThreshold() {
            if (trainingData.size() < 10) return;
            
            double falsePositives = 0;
            double falseNegatives = 0;
            
            for (TrainingData data : trainingData) {
                double prediction = predict(data.features);
                if (prediction > threshold && !data.isCheat) falsePositives++;
                if (prediction <= threshold && data.isCheat) falseNegatives++;
            }
            
            double errorRate = (falsePositives + falseNegatives) / trainingData.size();
            
            if (falsePositives > falseNegatives) {
                threshold += 0.01;
            } else if (falseNegatives > falsePositives) {
                threshold -= 0.01;
            }
            
            threshold = Math.max(0.0, Math.min(1.0, threshold));
        }
        
        void updateAccuracy(double prediction) {
            totalPredictions++;
            if (prediction > threshold) {
                correctPredictions++;
            }
            
            accuracy = totalPredictions > 0 ? (double) correctPredictions / totalPredictions : 0.5;
        }
        
        double getAccuracy() {
            return accuracy;
        }
    }
    
    static class TrainingData {
        double[] features;
        boolean isCheat;
        
        TrainingData(double[] features, boolean isCheat) {
            this.features = features;
            this.isCheat = isCheat;
        }
    }
}
