package com.aetherguard.checks;

/**
 * 🛡️ AetherGuard Check Type
 * 
 * Enumeration of all check types supported by AetherGuard
 * Used for categorization and configuration
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public enum CheckType {
    
    // Movement checks
    MOVEMENT_FLY("Fly", "Detects illegal flying behavior", Category.MOVEMENT, Priority.HIGH),
    MOVEMENT_SPEED("Speed", "Detects excessive movement speed", Category.MOVEMENT, Priority.HIGH),
    MOVEMENT_NOSLOW("NoSlow", "Detects speed while using items/sneaking", Category.MOVEMENT, Priority.MEDIUM),
    MOVEMENT_STEP("Step", "Detects illegal step height", Category.MOVEMENT, Priority.MEDIUM),
    MOVEMENT_JESUS("Jesus", "Detects water/lava walking", Category.MOVEMENT, Priority.MEDIUM),
    MOVEMENT_PHASE("Phase", "Detects no-clip through blocks", Category.MOVEMENT, Priority.CRITICAL),
    MOVEMENT_TIMER("Timer", "Detects game speed manipulation", Category.MOVEMENT, Priority.HIGH),
    MOVEMENT_OMNISPRINT("OmniSprint", "Detects sprinting in all directions", Category.MOVEMENT, Priority.LOW),
    MOVEMENT_FASTLADDER("FastLadder", "Detects fast ladder climbing", Category.MOVEMENT, Priority.MEDIUM),
    MOVEMENT_AIRSTRAFE("AirStrafe", "Detects illegal air strafing", Category.MOVEMENT, Priority.MEDIUM),
    MOVEMENT_MOTIONDESYNC("MotionDesync", "Detects motion desynchronization", Category.MOVEMENT, Priority.LOW),
    MOVEMENT_STRAFELIMITS("StrafeLimits", "Detects strafe limit violations", Category.MOVEMENT, Priority.LOW),
    MOVEMENT_GROUNDSPOOF("GroundSpoof", "Detects ground spoofing", Category.MOVEMENT, Priority.MEDIUM),
    MOVEMENT_JUMPBOOSTSPOOF("JumpBoostSpoof", "Detects jump boost spoofing", Category.MOVEMENT, Priority.MEDIUM),
    MOVEMENT_VELOCITYSPOOF("VelocitySpoof", "Detects velocity spoofing", Category.MOVEMENT, Priority.HIGH),
    MOVEMENT_VERTICALBOOST("VerticalBoost", "Detects illegal vertical boost", Category.MOVEMENT, Priority.MEDIUM),
    MOVEMENT_SPIDER("Spider", "Detects wall climbing", Category.MOVEMENT, Priority.MEDIUM),
    MOVEMENT_ELYTRA("Elytra", "Detects illegal elytra movement", Category.MOVEMENT, Priority.MEDIUM),
    MOVEMENT_VEHICLESPEED("VehicleSpeed", "Detects vehicle speed hacks", Category.MOVEMENT, Priority.MEDIUM),
    MOVEMENT_VEHICLEPHASE("VehiclePhase", "Detects vehicle phasing", Category.MOVEMENT, Priority.MEDIUM),
    MOVEMENT_BOATFLY("BoatFly", "Detects boat flying", Category.MOVEMENT, Priority.MEDIUM),
    MOVEMENT_MINECARTGLIDE("MinecartGlide", "Detects minecart gliding", Category.MOVEMENT, Priority.LOW),
    
    // Combat checks
    COMBAT_KILLAURA("KillAura", "Detects automatic attack systems", Category.COMBAT, Priority.CRITICAL),
    COMBAT_CPS("CPS", "Detects abnormal click patterns", Category.COMBAT, Priority.HIGH),
    COMBAT_REACH("Reach", "Detects extended attack range", Category.COMBAT, Priority.HIGH),
    COMBAT_HITBOX("HitBox", "Detects hitbox manipulation", Category.COMBAT, Priority.MEDIUM),
    COMBAT_CRITICALS("Criticals", "Detects forced critical hits", Category.COMBAT, Priority.MEDIUM),
    COMBAT_AUTOCLICKER("AutoClicker", "Detects automatic clicking", Category.COMBAT, Priority.HIGH),
    COMBAT_PERFECTTIMINGS("PerfectTimings", "Detects perfect attack timing", Category.COMBAT, Priority.LOW),
    COMBAT_SMOOTHAIM("SmoothAim", "Detects aim smoothing", Category.COMBAT, Priority.MEDIUM),
    COMBAT_AIMASSIST("AimAssist", "Detects aim assistance", Category.COMBAT, Priority.MEDIUM),
    COMBAT_SNAPAIM("SnapAim", "Detects snap aim", Category.COMBAT, Priority.HIGH),
    COMBAT_TRIGGERBOT("TriggerBot", "Detects trigger bot", Category.COMBAT, Priority.HIGH),
    COMBAT_WTAP("WTap", "Detects illegal WTap", Category.COMBAT, Priority.LOW),
    COMBAT_STAP("STap", "Detects illegal STap", Category.COMBAT, Priority.LOW),
    COMBAT_MULTIAURA("MultiAura", "Detects multi-target attacks", Category.COMBAT, Priority.HIGH),
    COMBAT_BLOCKHIT("BlockHit", "Detects illegal block hitting", Category.COMBAT, Priority.LOW),
    COMBAT_SHIELDBYPASS("ShieldBypass", "Detects shield bypass attempts", Category.COMBAT, Priority.MEDIUM),
    
    // World checks
    WORLD_FASTBREAK("FastBreak", "Detects fast block breaking", Category.WORLD, Priority.HIGH),
    WORLD_FASTPLACE("FastPlace", "Detects fast block placing", Category.WORLD, Priority.HIGH),
    WORLD_NUKER("Nuker", "Detects area breaking hacks", Category.WORLD, Priority.CRITICAL),
    WORLD_XRAY("XRay", "Detects X-ray usage", Category.WORLD, Priority.MEDIUM),
    WORLD_SCAFFOLD("Scaffold", "Detects scaffold assistance", Category.WORLD, Priority.HIGH),
    WORLD_TOWER("Tower", "Detects tower building", Category.WORLD, Priority.MEDIUM),
    WORLD_AUTOBUILD("AutoBuild", "Detects automatic building", Category.WORLD, Priority.MEDIUM),
    WORLD_AUTOBRIDGE("AutoBridge", "Detects automatic bridging", Category.WORLD, Priority.MEDIUM),
    WORLD_ILLEGALBLOCKINTERACT("IllegalBlockInteract", "Detects illegal block interactions", Category.WORLD, Priority.MEDIUM),
    WORLD_GHOSTHAND("GhostHand", "Detects ghost hand", Category.WORLD, Priority.LOW),
    WORLD_NOSWING("NoSwing", "Detects missing swing animation", Category.WORLD, Priority.LOW),
    WORLD_FASTBOW("FastBow", "Detects fast bow shooting", Category.WORLD, Priority.MEDIUM),
    WORLD_BOWAIMBOT("BowAimbot", "Detects bow aim assistance", Category.WORLD, Priority.MEDIUM),
    WORLD_INSTANTEAT("InstantEat", "Detects instant eating", Category.WORLD, Priority.MEDIUM),
    WORLD_CONSUMESPEED("ConsumeSpeed", "Detects fast consumption", Category.WORLD, Priority.MEDIUM),
    WORLD_BUCKETFASTPLACE("BucketFastPlace", "Detects fast bucket placement", Category.WORLD, Priority.MEDIUM),
    
    // Packet checks
    PACKET_BADPACKETS("BadPackets", "Detects invalid packets", Category.PACKETS, Priority.CRITICAL),
    PACKET_PACKETORDER("PacketOrder", "Detects packet order issues", Category.PACKETS, Priority.MEDIUM),
    PACKET_PACKETFREQUENCY("PacketFrequency", "Detects packet flooding", Category.PACKETS, Priority.HIGH),
    PACKET_PACKETBURST("PacketBurst", "Detects packet bursts", Category.PACKETS, Priority.HIGH),
    PACKET_PAYLOADEXPLOIT("PayloadExploit", "Detects payload exploits", Category.PACKETS, Priority.CRITICAL),
    PACKET_VIAVERSIONBYPASS("ViaVersionBypass", "Detects ViaVersion bypass attempts", Category.PACKETS, Priority.MEDIUM),
    PACKET_KEEPALIVESPOOF("KeepAliveSpoof", "Detects KeepAlive spoofing", Category.PACKETS, Priority.MEDIUM),
    PACKET_PINGSPOOF("PingSpoof", "Detects ping spoofing", Category.PACKETS, Priority.MEDIUM),
    PACKET_BOOKSPAM("BookSpam", "Detects book spamming", Category.PACKETS, Priority.MEDIUM),
    PACKET_MAPSPAM("MapSpam", "Detects map spamming", Category.PACKETS, Priority.LOW),
    PACKET_SIGNEXPLOIT("SignExploit", "Detects sign exploits", Category.PACKETS, Priority.MEDIUM),
    PACKET_RESOURCEPACKEXPLOIT("ResourcePackExploit", "Detects resource pack exploits", Category.PACKETS, Priority.MEDIUM),
    
    // Exploit checks
    EXPLOIT_CRASHERS("Crashers", "Detects crash attempts", Category.EXPLOITS, Priority.CRITICAL),
    EXPLOIT_PLUGINMESSAGEFLOOD("PluginMessageFlood", "Detects plugin message flooding", Category.EXPLOITS, Priority.HIGH),
    EXPLOIT_TABCOMPLETEEXPLOIT("TabCompleteExploit", "Detects tab complete exploits", Category.EXPLOITS, Priority.MEDIUM),
    EXPLOIT_BOOKBAN("BookBan", "Detects book ban exploits", Category.EXPLOITS, Priority.HIGH),
    EXPLOIT_ILLEGALCHATPACKETS("IllegalChatPackets", "Detects illegal chat packets", Category.EXPLOITS, Priority.MEDIUM),
    EXPLOIT_ATTRIBUTEMODIFIEREXPLOIT("AttributeModifierExploit", "Detects attribute modifier exploits", Category.EXPLOITS, Priority.HIGH),
    EXPLOIT_ENCHANTMENTOVERFLOW("EnchantmentOverflow", "Detects enchantment overflow", Category.EXPLOITS, Priority.HIGH),
    EXPLOIT_ANVILRENAMEEXPLOIT("AnvilRenameExploit", "Detects anvil rename exploits", Category.EXPLOITS, Priority.MEDIUM),
    EXPLOIT_COMMANOVERFLOW("CommandOverflow", "Detects command overflow", Category.EXPLOITS, Priority.MEDIUM),
    EXPLOIT_VEHICLECRASH("VehicleCrash", "Detects vehicle crash attempts", Category.EXPLOITS, Priority.MEDIUM),
    
    // Automation checks
    AUTOMATION_MACRO("Macro", "Detects macro usage", Category.AUTOMATION, Priority.MEDIUM),
    AUTOMATION_AUTOFARM("AutoFarm", "Detects automatic farming", Category.AUTOMATION, Priority.MEDIUM),
    AUTOMATION_AUTOFISH("AutoFish", "Detects automatic fishing", Category.AUTOMATION, Priority.MEDIUM),
    AUTOMATION_AUTOMINE("AutoMine", "Detects automatic mining", Category.AUTOMATION, Priority.HIGH),
    AUTOMATION_AUTORECONNECT("AutoReconnect", "Detects auto-reconnect", Category.AUTOMATION, Priority.LOW),
    AUTOMATION_AUTOTEXT("AutoText", "Detects automatic text", Category.AUTOMATION, Priority.LOW),
    AUTOMATION_AUTOCLICKER("AutoClicker", "Detects automatic clicking", Category.AUTOMATION, Priority.HIGH),
    AUTOMATION_FASTBREAK("FastBreak", "Detects fast block breaking", Category.AUTOMATION, Priority.HIGH),
    AUTOMATION_FASTPLACE("FastPlace", "Detects fast block placing", Category.AUTOMATION, Priority.HIGH),
    
    // Heuristic checks
    HEURISTIC_BEHAVIOR("Behavior", "Analyzes player behavior patterns", Category.HEURISTICS, Priority.LOW),
    HEURISTIC_TIMING("Timing", "Analyzes action timing patterns", Category.HEURISTICS, Priority.LOW),
    HEURISTIC_CONSISTENCY("Consistency", "Analyzes consistency patterns", Category.HEURISTICS, Priority.LOW),
    HEURISTIC_PREDICTION("Prediction", "Predicts cheating behavior", Category.HEURISTICS, Priority.LOW),
    
    // Machine Learning checks
    ML_AUTOCLICKER_ML("AutoClickerML", "ML-based autoclicker detection", Category.ML, Priority.MEDIUM),
    ML_AIM_ML("AimML", "ML-based aim detection", Category.ML, Priority.MEDIUM),
    ML_MOVEMENT_ML("MovementML", "ML-based movement detection", Category.ML, Priority.MEDIUM),
    ML_BEHAVIOR_ML("BehaviorML", "ML-based behavior analysis", Category.ML, Priority.LOW);
    
    private final String displayName;
    private final String description;
    private final Category category;
    private final Priority priority;
    
    CheckType(String displayName, String description, Category category, Priority priority) {
        this.displayName = displayName;
        this.description = description;
        this.category = category;
        this.priority = priority;
    }
    
    // Getters
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public Priority getPriority() { return priority; }
    
    /**
     * Check category enumeration
     */
    public enum Category {
        MOVEMENT("Movement", "Movement-related checks"),
        COMBAT("Combat", "Combat-related checks"),
        WORLD("World", "World interaction checks"),
        PACKETS("Packets", "Packet-related checks"),
        EXPLOITS("Exploits", "Exploit-related checks"),
        AUTOMATION("Automation", "Automation-related checks"),
        HEURISTICS("Heuristics", "Heuristic analysis checks"),
        ML("Machine Learning", "Machine learning checks");
        
        private final String displayName;
        private final String description;
        
        Category(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Check priority enumeration
     */
    public enum Priority {
        LOW(1, "Low priority"),
        MEDIUM(2, "Medium priority"),
        HIGH(3, "High priority"),
        CRITICAL(4, "Critical priority");
        
        private final int level;
        private final String description;
        
        Priority(int level, String description) {
            this.level = level;
            this.description = description;
        }
        
        public int getLevel() { return level; }
        public String getDescription() { return description; }
        
        /**
         * Get color for priority level
         */
        public String getColor() {
            switch (this) {
                case LOW: return "§a";
                case MEDIUM: return "§e";
                case HIGH: return "§c";
                case CRITICAL: return "§4";
                default: return "§f";
            }
        }
    }
    
    /**
     * Find check type by name
     */
    public static CheckType fromName(String name) {
        for (CheckType type : values()) {
            if (type.name().equalsIgnoreCase(name) || type.displayName.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * Get all check types by category
     */
    public static CheckType[] getByCategory(Category category) {
        return java.util.Arrays.stream(values())
                .filter(type -> type.category == category)
                .toArray(CheckType[]::new);
    }
    
    /**
     * Get all check types by priority
     */
    public static CheckType[] getByPriority(Priority priority) {
        return java.util.Arrays.stream(values())
                .filter(type -> type.priority == priority)
                .toArray(CheckType[]::new);
    }
    
    @Override
    public String toString() {
        return String.format("%s%s§7 - %s", priority.getColor(), displayName, description);
    }
}