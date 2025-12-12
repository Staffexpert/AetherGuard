package com.aetherguard.managers;

import com.aetherguard.gui.*;
import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;

/**
 * 🛡️ AetherGuard GUI Manager
 * 
 * Manages all GUI interfaces for the anti-cheat
 * Handles GUI creation and interaction
 * 
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class GUIManager {
    
    private final AetherGuard plugin;
    
    public GUIManager(AetherGuard plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Open GUI for player
     */
    public void openGUI(Player player, String guiName) {
        AetherGuardGUI gui = createGUI(player, guiName.toLowerCase());
        if (gui != null) {
            gui.open();
        } else {
            player.sendMessage(plugin.getConfigManager().getMessage("gui.not-found", 
                "name", guiName));
        }
    }
    
    /**
     * Open GUI for player with data
     */
    public void openGUI(Player player, String guiName, Object data) {
        AetherGuardGUI gui = createGUI(player, guiName.toLowerCase());
        if (gui != null) {
            gui.open();
        } else {
            player.sendMessage(plugin.getConfigManager().getMessage("gui.not-found", 
                "name", guiName));
        }
    }
    
    /**
     * Create GUI instance for player
     */
    private AetherGuardGUI createGUI(Player player, String guiName) {
        return null;
    }
    
    /**
     * Close all GUIs
     */
    public void closeAllGUIs() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getOpenInventory() != null) {
                player.closeInventory();
            }
        }
    }
}