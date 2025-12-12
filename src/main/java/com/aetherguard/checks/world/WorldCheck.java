package com.aetherguard.checks.world;

import com.aetherguard.checks.Check;
import com.aetherguard.checks.CheckData;
import com.aetherguard.checks.CheckResult;
import com.aetherguard.checks.CheckType;
import com.aetherguard.core.AetherGuard;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * 🛡️ AetherGuard World Check Base Class
 * 
 * Base class for all world interaction checks
 * Provides common world analysis utilities
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public abstract class WorldCheck extends Check {
    
    // World constants
    protected static final long INSTANT_BREAK_TIME = 0; // ticks
    protected static final long FAST_BREAK_THRESHOLD = 100; // milliseconds
    protected static final long FAST_PLACE_THRESHOLD = 100; // milliseconds
    protected static final double MAX_BREAK_DISTANCE = 6.0;
    protected static final double MAX_PLACE_DISTANCE = 5.0;
    protected static final int MAX_BLOCKS_PER_SECOND = 5;
    protected static final int MAX_PLACEMENTS_PER_SECOND = 5;
    
    public WorldCheck(AetherGuard plugin, String category, String name, String type) {
        super(plugin, category, name, type);
    }
    
    @Override
    public CheckType getCheckType() {
        // Default implementation - override in subclasses
        return CheckType.WORLD_FASTBREAK;
    }
    
    /**
     * Calculate block break time
     */
    protected long calculateBreakTime(Player player, Block block) {
        Material tool = player.getInventory().getItemInMainHand().getType();
        Material blockType = block.getType();
        
        // Get base hardness
        float hardness = getBlockHardness(blockType);
        
        // Get tool efficiency
        float efficiency = getToolEfficiency(tool, blockType);
        
        // Calculate break time in ticks
        float breakTime = hardness / efficiency;
        
        // Apply enchantments
        breakTime = applyEnchantmentModifiers(player, breakTime);
        
        // Apply effects
        breakTime = applyEffectModifiers(player, breakTime);
        
        return (long) (breakTime * 20); // Convert to ticks
    }
    
    /**
     * Get block hardness
     */
    protected float getBlockHardness(Material material) {
        switch (material) {
            case STONE:
            case COBBLESTONE:
            case IRON_ORE:
            case GOLD_ORE:
                return 3.0f;
            case DIAMOND_ORE:
            case EMERALD_ORE:
                return 5.0f;
            case OBSIDIAN:
                return 50.0f;
            case BEDROCK:
                return -1.0f; // Unbreakable
            case DIRT:
            case SAND:
            case GRAVEL:
                return 0.5f;
            case GLASS:
                return 0.3f;
            default:
                return 1.0f;
        }
    }
    
    /**
     * Get tool efficiency against block type
     */
    protected float getToolEfficiency(Material tool, Material block) {
        if (tool == null) return 1.0f;
        
        String toolName = tool.name();
        String blockName = block.name();
        
        // Check for correct tool type
        if (isCorrectTool(tool, block)) {
            return getToolSpeed(tool);
        }
        
        return 1.0f;
    }
    
    /**
     * Check if tool is correct for block
     */
    protected boolean isCorrectTool(Material tool, Material block) {
        String toolName = tool.name();
        String blockName = block.name();
        
        // Pickaxe for ores and stone
        if ((toolName.contains("PICKAXE") && 
            (blockName.contains("ORE") || blockName.contains("STONE") || blockName.contains("COBBLE") || 
             blockName.equals("OBSIDIAN") || blockName.contains("COAL") || blockName.contains("IRON") ||
             blockName.contains("GOLD") || blockName.contains("DIAMOND") || blockName.contains("EMERALD")))) {
            return true;
        }
        
        // Axe for wood
        if ((toolName.contains("AXE") && 
            (blockName.contains("WOOD") || blockName.contains("LOG") || blockName.contains("PLANKS")))) {
            return true;
        }
        
        // Shovel for dirt, sand, gravel
        if ((toolName.contains("SHOVEL") && 
            (blockName.contains("DIRT") || blockName.contains("SAND") || blockName.contains("GRAVEL")))) {
            return true;
        }
        
        // Hoe for farming blocks
        if ((toolName.contains("HOE") && 
            (blockName.contains("GRASS") || blockName.contains("FARMLAND")))) {
            return true;
        }
        
        // Shears for wool, leaves
        if (tool.name().equals("SHEARS") && 
            (blockName.contains("WOOL") || blockName.contains("LEAVES"))) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Get tool speed multiplier
     */
    protected float getToolSpeed(Material tool) {
        switch (tool) {
            case WOODEN_PICKAXE:
            case WOODEN_AXE:
            case WOODEN_SHOVEL:
            case WOODEN_HOE:
                return 2.0f;
            case STONE_PICKAXE:
            case STONE_AXE:
            case STONE_SHOVEL:
            case STONE_HOE:
                return 4.0f;
            case IRON_PICKAXE:
            case IRON_AXE:
            case IRON_SHOVEL:
            case IRON_HOE:
                return 6.0f;
            case GOLDEN_PICKAXE:
            case GOLDEN_AXE:
            case GOLDEN_SHOVEL:
            case GOLDEN_HOE:
                return 12.0f;
            case DIAMOND_PICKAXE:
            case DIAMOND_AXE:
            case DIAMOND_SHOVEL:
            case DIAMOND_HOE:
                return 8.0f;
            case NETHERITE_PICKAXE:
            case NETHERITE_AXE:
            case NETHERITE_SHOVEL:
            case NETHERITE_HOE:
                return 9.0f;
            case SHEARS:
                return 15.0f;
            default:
                return 1.0f;
        }
    }
    
    /**
     * Apply enchantment modifiers to break time
     */
    protected float applyEnchantmentModifiers(Player player, float breakTime) {
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null) return breakTime;
        
        // Efficiency enchantment
        if (tool.containsEnchantment(org.bukkit.enchantments.Enchantment.DIG_SPEED)) {
            int level = tool.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.DIG_SPEED);
            breakTime /= (1.0f + 0.3f * level);
        }
        
        return breakTime;
    }
    
    /**
     * Apply effect modifiers to break time
     */
    protected float applyEffectModifiers(Player player, float breakTime) {
        // Haste effect
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.FAST_DIGGING)) {
            int amplifier = player.getPotionEffect(org.bukkit.potion.PotionEffectType.FAST_DIGGING).getAmplifier();
            breakTime *= (1.0f - 0.2f * (amplifier + 1));
        }
        
        // Mining fatigue
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SLOW_DIGGING)) {
            int amplifier = player.getPotionEffect(org.bukkit.potion.PotionEffectType.SLOW_DIGGING).getAmplifier();
            breakTime *= (1.0f + 0.3f * (amplifier + 1));
        }
        
        // Conduit power (underwater mining)
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.CONDUIT_POWER)) {
            breakTime *= 0.9f;
        }
        
        return breakTime;
    }
    
    /**
     * Check if player can reach block
     */
    protected boolean canReachBlock(Player player, Location blockLocation) {
        double distance = player.getLocation().distance(blockLocation);
        return distance <= MAX_BREAK_DISTANCE;
    }
    
    /**
     * Check if player is looking at block
     */
    protected boolean isLookingAtBlock(Player player, Location blockLocation, double maxAngle) {
        Vector playerDirection = player.getLocation().getDirection();
        Vector toBlock = blockLocation.toVector().subtract(player.getLocation().toVector()).normalize();
        
        double angle = Math.toDegrees(Math.acos(playerDirection.dot(toBlock)));
        return angle <= maxAngle;
    }
    
    /**
     * Check if block is in line of sight
     */
    protected boolean hasLineOfSightToBlock(Player player, Location blockLocation) {
        // Simple ray trace
        Vector direction = blockLocation.toVector().subtract(player.getLocation().toVector()).normalize();
        double distance = player.getLocation().distance(blockLocation);
        
        for (double i = 0.5; i < distance; i += 0.5) {
            Vector checkPos = player.getLocation().toVector().add(direction.clone().multiply(i));
            if (player.getWorld().getBlockAt(checkPos.toLocation(player.getWorld())).getType().isSolid() &&
                !checkPos.toLocation(player.getWorld()).getBlock().equals(blockLocation.getBlock())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Check if player has correct tool for block
     */
    protected boolean hasCorrectTool(Player player, Block block) {
        Material tool = player.getInventory().getItemInMainHand().getType();
        return isCorrectTool(tool, block.getType());
    }
    
    /**
     * Calculate block placement time
     */
    protected long calculatePlacementTime(Player player, Material blockType) {
        // Base placement time
        long baseTime = 100; // milliseconds
        
        // Apply effects
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.FAST_DIGGING)) {
            int amplifier = player.getPotionEffect(org.bukkit.potion.PotionEffectType.FAST_DIGGING).getAmplifier();
            baseTime -= 20 * (amplifier + 1);
        }
        
        return Math.max(50, baseTime); // Minimum 50ms
    }
    
    /**
     * Check if block placement is valid
     */
    protected boolean isValidPlacement(Location location, Material blockType) {
        // Check if location is valid
        if (location == null || location.getBlock() == null) {
            return false;
        }
        
        // Check if block can be placed at location
        if (location.getBlock().getType().isSolid() && !blockType.isBlock()) {
            return false;
        }
        
        // Check for spawn protection
        if (location.getWorld().getSpawnLocation().distance(location) < 16) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if block break is valid
     */
    protected boolean isValidBreak(Player player, Block block) {
        // Check if block can be broken
        if (block.getType() == Material.BEDROCK || block.getType() == Material.BARRIER) {
            return false;
        }
        
        // Check spawn protection
        if (block.getLocation().getWorld().getSpawnLocation().distance(block.getLocation()) < 16) {
            return false;
        }
        
        // Check if player has permission to break in area
        // This would integrate with WorldGuard or other protection plugins
        
        return true;
    }
    
    /**
     * Get world data from check data
     */
    protected WorldData getWorldData(CheckData data) {
        return new WorldData(
            data.getPlayer(),
            data.getBlockType(),
            data.getBlockLocation(),
            data.isBlockInteract()
        );
    }
    
    /**
     * World data container
     */
    protected static class WorldData {
        public final Player player;
        public final String blockType;
        public final Location blockLocation;
        public final boolean interacted;
        
        public WorldData(Player player, String blockType, Location blockLocation, boolean interacted) {
            this.player = player;
            this.blockType = blockType;
            this.blockLocation = blockLocation;
            this.interacted = interacted;
        }
        
        public boolean isValidBlock() {
            return blockType != null && !blockType.isEmpty();
        }
        
        public boolean isValidLocation() {
            return blockLocation != null;
        }
        
        public Material getBlockMaterial() {
            try {
                return Material.valueOf(blockType.toUpperCase());
            } catch (IllegalArgumentException e) {
                return Material.AIR;
            }
        }
    }
}