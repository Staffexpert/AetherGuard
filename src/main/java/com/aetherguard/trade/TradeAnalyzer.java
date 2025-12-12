package com.aetherguard.trade;

import org.bukkit.entity.Player;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ Trade Analyzer
 * Exclusive Feature - Detects suspicious trading patterns
 */
public class TradeAnalyzer {
    
    private final Map<String, TradeProfile> trades;
    
    public TradeAnalyzer() {
        this.trades = new ConcurrentHashMap<>();
    }
    
    public void recordTrade(Player player1, Player player2) {
        String key = player1.getUniqueId() + "_" + player2.getUniqueId();
        trades.computeIfAbsent(key, k -> new TradeProfile()).tradeCount++;
    }
    
    public double analyzeTradeSuspicion(Player player1, Player player2) {
        String key = player1.getUniqueId() + "_" + player2.getUniqueId();
        TradeProfile profile = trades.get(key);
        
        if (profile != null && profile.tradeCount > 100) return 60.0;
        
        return 0.0;
    }
    
    static class TradeProfile {
        int tradeCount = 0;
    }
}
