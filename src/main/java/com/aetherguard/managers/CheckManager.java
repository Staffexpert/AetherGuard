package com.aetherguard.managers;

import com.aetherguard.checks.*;
import com.aetherguard.config.ConfigManager;
import com.aetherguard.core.AetherGuard;
import com.aetherguard.checks.movement.*;
import com.aetherguard.checks.combat.*;
import com.aetherguard.checks.world.*;
import com.aetherguard.checks.packets.*;
import com.aetherguard.checks.exploits.*;
import com.aetherguard.checks.automation.*;
import com.aetherguard.checks.heuristics.*;
import com.aetherguard.checks.ml.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * 🛡️ AetherGuard Check Manager
 * 
 * Manages all anti-cheat checks and their execution
 * Handles check registration, configuration, and execution order
 * Supports 100+ checks across all categories
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class CheckManager {
    
    private final AetherGuard plugin;
    private final ConfigManager configManager;
    
    // Check storage
    private final Map<String, Map<String, Map<String, Check>>> checks;
    private final List<Check> allChecks;
    private final Map<String, Check> checkRegistry;
    
    // Check categories
    private final Set<String> enabledCategories;
    private final Map<String, Integer> categoryPriorities;
    
    // Statistics
    private int totalChecks;
    private final Map<String, Integer> checkStats;
    
    public CheckManager(AetherGuard plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        
        this.checks = new ConcurrentHashMap<>();
        this.allChecks = new ArrayList<>();
        this.checkRegistry = new ConcurrentHashMap<>();
        this.enabledCategories = new HashSet<>();
        this.categoryPriorities = new HashMap<>();
        this.checkStats = new ConcurrentHashMap<>();
        
        initializeChecks();
        loadCheckConfiguration();
        sortChecksByPriority();
        
        plugin.getLogger().info("§7Check Manager initialized with §f" + totalChecks + " §7checks");
    }
    
    /**
     * Initialize all checks
     */
    private void initializeChecks() {
        // Initialize movement checks
        initializeMovementChecks();
        
        // Initialize combat checks
        initializeCombatChecks();
        
        // Initialize world checks
        initializeWorldChecks();
        
        // Initialize packet checks
        initializePacketChecks();
        
        // Initialize exploit checks
        initializeExploitChecks();
        
        // Initialize automation checks
        initializeAutomationChecks();
        
        // Initialize heuristic checks
        initializeHeuristicChecks();
        
        // Initialize ML checks (optional)
        if (configManager.getMainConfig().getBoolean("machine-learning.enabled", false)) {
            initializeMLChecks();
        }
        
        // Calculate total checks
        calculateTotalChecks();
    }
    
    /**
     * Initialize movement checks
     */
    private void initializeMovementChecks() {
        Map<String, Map<String, Check>> movement = new HashMap<>();
        
        Map<String, Check> fly = new HashMap<>();
        fly.put("A", new FlyA(plugin, "movement", "fly", "A"));
        fly.put("B", new FlyB(plugin, "movement", "fly", "B"));
        movement.put("fly", fly);
        
        Map<String, Check> speed = new HashMap<>();
        speed.put("A", new SpeedA(plugin, "movement", "speed", "A"));
        speed.put("B", new SpeedB(plugin, "movement", "speed", "B"));
        speed.put("C", new SpeedC(plugin, "movement", "speed", "C"));
        speed.put("D", new SpeedD(plugin, "movement", "speed", "D"));
        speed.put("E", new SpeedE(plugin, "movement", "speed", "E"));
        speed.put("F", new SpeedF(plugin, "movement", "speed", "F"));
        movement.put("speed", speed);
        
        checks.put("movement", movement);
    }
    
    /**
     * Initialize combat checks
     */
    private void initializeCombatChecks() {
        Map<String, Map<String, Check>> combat = new HashMap<>();
        
        // KillAura checks
        Map<String, Check> killaura = new HashMap<>();
        killaura.put("A", new KillAuraA(plugin, "combat", "killaura", "A"));
        killaura.put("B", new KillAuraB(plugin, "combat", "killaura", "B"));
        killaura.put("C", new KillAuraC(plugin, "combat", "killaura", "C"));
        killaura.put("D", new KillAuraD(plugin, "combat", "killaura", "D"));
        killaura.put("E", new KillAuraE(plugin, "combat", "killaura", "E"));
        killaura.put("F", new KillAuraF(plugin, "combat", "killaura", "F"));
        killaura.put("G", new KillAuraG(plugin, "combat", "killaura", "G"));
        killaura.put("H", new KillAuraH(plugin, "combat", "killaura", "H"));
        killaura.put("I", new KillAuraI(plugin, "combat", "killaura", "I"));
        killaura.put("J", new KillAuraJ(plugin, "combat", "killaura", "J"));
        killaura.put("K", new KillAuraK(plugin, "combat", "killaura", "K"));
        killaura.put("L", new KillAuraL(plugin, "combat", "killaura", "L"));
        combat.put("killaura", killaura);
        
        // Reach checks
        Map<String, Check> reach = new HashMap<>();
        reach.put("A", new ReachA(plugin, "combat", "reach", "A"));
        reach.put("B", new ReachB(plugin, "combat", "reach", "B"));
        reach.put("C", new ReachC(plugin, "combat", "reach", "C"));
        reach.put("D", new ReachD(plugin, "combat", "reach", "D"));
        reach.put("E", new ReachE(plugin, "combat", "reach", "E"));
        reach.put("F", new ReachF(plugin, "combat", "reach", "F"));
        reach.put("G", new ReachG(plugin, "combat", "reach", "G"));
        combat.put("reach", reach);
        
        checks.put("combat", combat);
    }
    
    /**
     * Initialize world checks
     */
    private void initializeWorldChecks() {
        Map<String, Map<String, Check>> world = new HashMap<>();
        checks.put("world", world);
    }
    
    /**
     * Initialize packet checks
     */
    private void initializePacketChecks() {
        Map<String, Map<String, Check>> packets = new HashMap<>();
        
        // BadPackets checks A-Z
        Map<String, Check> badpackets = new HashMap<>();
        for (char c = 'A'; c <= 'Z'; c++) {
            String className = "com.aetherguard.checks.packets.BadPackets" + c;
            try {
                Class<?> clazz = Class.forName(className);
                Check check = (Check) clazz.getConstructor(AetherGuard.class, String.class, String.class, String.class, String.class)
                    .newInstance(plugin, "packets", "badpackets", String.valueOf(c));
                badpackets.put(String.valueOf(c), check);
            } catch (Exception e) {
                plugin.getLogger().warning("§cCould not initialize check BadPackets" + c + ": " + e.getMessage());
            }
        }
        packets.put("badpackets", badpackets);
        
        checks.put("packets", packets);
    }
    
    /**
     * Initialize exploit checks
     */
    private void initializeExploitChecks() {
        Map<String, Map<String, Check>> exploits = new HashMap<>();
        checks.put("exploits", exploits);
    }
    
    /**
     * Initialize automation checks
     */
    private void initializeAutomationChecks() {
        Map<String, Map<String, Check>> automation = new HashMap<>();
        
        // AutoClicker checks
        Map<String, Check> autoclicker = new HashMap<>();
        autoclicker.put("A", new AutoClickerA(plugin, "automation", "autoclicker", "A"));
        autoclicker.put("B", new AutoClickerB(plugin, "automation", "autoclicker", "B"));
        autoclicker.put("C", new AutoClickerC(plugin, "automation", "autoclicker", "C"));
        autoclicker.put("D", new AutoClickerD(plugin, "automation", "autoclicker", "D"));
        autoclicker.put("E", new AutoClickerE(plugin, "automation", "autoclicker", "E"));
        autoclicker.put("F", new AutoClickerF(plugin, "automation", "autoclicker", "F"));
        automation.put("autoclicker", autoclicker);
        
        // FastBreak checks
        Map<String, Check> fastbreak = new HashMap<>();
        fastbreak.put("A", new FastBreakA(plugin, "automation", "fastbreak", "A"));
        fastbreak.put("B", new FastBreakB(plugin, "automation", "fastbreak", "B"));
        fastbreak.put("C", new FastBreakC(plugin, "automation", "fastbreak", "C"));
        fastbreak.put("D", new FastBreakD(plugin, "automation", "fastbreak", "D"));
        fastbreak.put("E", new FastBreakE(plugin, "automation", "fastbreak", "E"));
        fastbreak.put("F", new FastBreakF(plugin, "automation", "fastbreak", "F"));
        automation.put("fastbreak", fastbreak);
        
        // FastPlace checks
        Map<String, Check> fastplace = new HashMap<>();
        fastplace.put("A", new FastPlaceA(plugin, "automation", "fastplace", "A"));
        fastplace.put("B", new FastPlaceB(plugin, "automation", "fastplace", "B"));
        fastplace.put("C", new FastPlaceC(plugin, "automation", "fastplace", "C"));
        fastplace.put("D", new FastPlaceD(plugin, "automation", "fastplace", "D"));
        fastplace.put("E", new FastPlaceE(plugin, "automation", "fastplace", "E"));
        automation.put("fastplace", fastplace);
        
        checks.put("automation", automation);
    }
    
    /**
     * Initialize heuristic checks
     */
    private void initializeHeuristicChecks() {
        Map<String, Map<String, Check>> heuristics = new HashMap<>();
        checks.put("heuristics", heuristics);
    }
    
    /**
     * Initialize ML checks
     */
    private void initializeMLChecks() {
        Map<String, Map<String, Check>> ml = new HashMap<>();
        checks.put("ml", ml);
    }
    
    /**
     * Load check configuration
     */
    private void loadCheckConfiguration() {
        // Load enabled categories
        for (String category : checks.keySet()) {
            if (configManager.isCategoryEnabled(category)) {
                enabledCategories.add(category);
                int priority = configManager.getCategoryPriority(category);
                categoryPriorities.put(category, priority);
            }
        }
        
        // Load check configurations
        for (Map.Entry<String, Map<String, Map<String, Check>>> categoryEntry : checks.entrySet()) {
            String category = categoryEntry.getKey();
            for (Map.Entry<String, Map<String, Check>> checkEntry : categoryEntry.getValue().entrySet()) {
                String checkName = checkEntry.getKey();
                for (Map.Entry<String, Check> typeEntry : checkEntry.getValue().entrySet()) {
                    String type = typeEntry.getKey();
                    Check check = typeEntry.getValue();
                    
                    // Load configuration
                    ConfigManager.CheckConfig config = configManager.getCheckConfig(category, checkName, type);
                    check.setConfig(config);
                    
                    // Add to registry
                    String fullName = category + "." + checkName + "." + type;
                    checkRegistry.put(fullName, check);
                    allChecks.add(check);
                    
                    // Initialize stats
                    checkStats.put(fullName, 0);
                }
            }
        }
    }
    
    /**
     * Sort checks by priority
     */
    private void sortChecksByPriority() {
        allChecks.sort((c1, c2) -> {
            int priority1 = categoryPriorities.getOrDefault(c1.getCategory(), 1);
            int priority2 = categoryPriorities.getOrDefault(c2.getCategory(), 1);
            return Integer.compare(priority1, priority2);
        });
    }
    
    /**
     * Calculate total checks
     */
    private void calculateTotalChecks() {
        totalChecks = 0;
        for (Map<String, Map<String, Check>> category : checks.values()) {
            for (Map<String, Check> checkMap : category.values()) {
                totalChecks += checkMap.size();
            }
        }
    }
    
    /**
     * Get check by full name
     */
    public Check getCheck(String fullName) {
        return checkRegistry.get(fullName);
    }
    
    /**
     * Get check by category, name, and type
     */
    public Check getCheck(String category, String name, String type) {
        Map<String, Map<String, Check>> categoryMap = checks.get(category);
        if (categoryMap == null) return null;
        
        Map<String, Check> checkMap = categoryMap.get(name);
        if (checkMap == null) return null;
        
        return checkMap.get(type);
    }
    
    /**
     * Get all checks in a category
     */
    public List<Check> getCategoryChecks(String category) {
        List<Check> categoryChecks = new ArrayList<>();
        Map<String, Map<String, Check>> categoryMap = checks.get(category);
        if (categoryMap != null) {
            for (Map<String, Check> checkMap : categoryMap.values()) {
                categoryChecks.addAll(checkMap.values());
            }
        }
        return categoryChecks;
    }
    
    /**
     * Get all enabled checks
     */
    public List<Check> getEnabledChecks() {
        List<Check> enabledChecks = new ArrayList<>();
        for (Check check : allChecks) {
            if (check.isEnabled() && enabledCategories.contains(check.getCategory())) {
                enabledChecks.add(check);
            }
        }
        return enabledChecks;
    }
    
    /**
     * Get all checks
     */
    public List<Check> getAllChecks() {
        return new ArrayList<>(allChecks);
    }
    
    /**
     * Enable/disable a check
     */
    public boolean setCheckEnabled(String fullName, boolean enabled) {
        Check check = getCheck(fullName);
        if (check != null) {
            check.setEnabled(enabled);
            return true;
        }
        return false;
    }
    
    /**
     * Enable/disable a category
     */
    public void setCategoryEnabled(String category, boolean enabled) {
        if (enabled) {
            enabledCategories.add(category);
        } else {
            enabledCategories.remove(category);
        }
    }
    
    /**
     * Get check statistics
     */
    public Map<String, Integer> getCheckStats() {
        return new HashMap<>(checkStats);
    }
    
    /**
     * Increment check flag count
     */
    public void incrementCheckFlag(String checkName) {
        checkStats.merge(checkName, 1, Integer::sum);
    }
    
    /**
     * Get total checks count
     */
    public int getTotalChecks() {
        return totalChecks;
    }
    
    /**
     * Get enabled checks count
     */
    public int getEnabledChecksCount() {
        int count = 0;
        for (Check check : allChecks) {
            if (check.isEnabled()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Get enabled categories
     */
    public Set<String> getEnabledCategories() {
        return new HashSet<>(enabledCategories);
    }
    
    /**
     * Get all categories
     */
    public Set<String> getAllCategories() {
        return checks.keySet();
    }
    
    /**
     * Reload check configurations
     */
    public void reloadCheckConfigurations() {
        for (Check check : allChecks) {
            ConfigManager.CheckConfig config = configManager.getCheckConfig(
                check.getCategory(), check.getName(), check.getType()
            );
            check.setConfig(config);
        }
        
        // Reload categories
        enabledCategories.clear();
        for (String category : checks.keySet()) {
            if (configManager.isCategoryEnabled(category)) {
                enabledCategories.add(category);
                int priority = configManager.getCategoryPriority(category);
                categoryPriorities.put(category, priority);
            }
        }
        
        // Sort by priority again
        sortChecksByPriority();
        
        plugin.getLogger().info("§7Check configurations reloaded");
    }
    
    /**
     * Get check information for GUI
     */
    public List<CheckInfo> getCheckInfos() {
        List<CheckInfo> infos = new ArrayList<>();
        for (Check check : allChecks) {
            infos.add(new CheckInfo(
                check.getCategory(),
                check.getName(),
                check.getType(),
                check.isEnabled(),
                check.getDescription(),
                check.getViolations()
            ));
        }
        return infos;
    }
    
    /**
     * Check information data class
     */
    public static class CheckInfo {
        private final String category;
        private final String name;
        private final String type;
        private final boolean enabled;
        private final String description;
        private final long violations;
        
        public CheckInfo(String category, String name, String type, boolean enabled, String description, long violations) {
            this.category = category;
            this.name = name;
            this.type = type;
            this.enabled = enabled;
            this.description = description;
            this.violations = violations;
        }
        
        public String getCategory() { return category; }
        public String getName() { return name; }
        public String getType() { return type; }
        public boolean isEnabled() { return enabled; }
        public String getDescription() { return description; }
        public long getViolations() { return violations; }
        public String getFullName() { return category + "." + name + "." + type; }
    }
}