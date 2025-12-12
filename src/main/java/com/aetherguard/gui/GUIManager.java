package com.aetherguard.gui;

import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class GUIManager {
    
    private final AetherGuard plugin;
    private final Map<Player, AetherGuardGUI> openGUIs = new HashMap<>();
    
    public GUIManager(AetherGuard plugin) {
        this.plugin = plugin;
    }
    
    public void openGUI(Player player, AetherGuardGUI gui) {
        gui.open();
        openGUIs.put(player, gui);
    }
    
    public void closeGUI(Player player) {
        openGUIs.remove(player);
    }
    
    public AetherGuardGUI getOpenGUI(Player player) {
        return openGUIs.get(player);
    }
}
