package com.aetherguard.managers;

import com.aetherguard.gui.*;
import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * 🛡️ AetherGuard GUI Manager
 *
 * Manages all GUI interfaces for the anti-cheat
 * Handles GUI creation and interaction
 *
 * @author AetherGuard Team
 * @version 1.0.1
 */
public class GUIManager implements Listener {

    private final AetherGuard plugin;
    private final Map<String, GuiFactory> guiFactories = new HashMap<>();

    public GUIManager(AetherGuard plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        registerGUIs();
    }

    private void registerGUIs() {
        guiFactories.put("main", (p, data) -> new MainGUI(plugin, p));
        guiFactories.put("checks", (p, data) -> (data instanceof String) ? new ChecksGUI(plugin, p, (String) data, 0) : null);
        guiFactories.put("config", (p, data) -> new ConfigGUI(plugin, p));
        guiFactories.put("detections", (p, data) -> new LiveDetectionsGUI(plugin, p));
    }

    /**
     * Open GUI for player
     */
    public void openGUI(Player player, String guiName) {
        openGUI(player, guiName, null);
    }

    /**
     * Open GUI for player with data
     */
    public void openGUI(Player player, String guiName, Object data) {
        GuiFactory factory = guiFactories.get(guiName.toLowerCase());
        if (factory == null) {
            plugin.getLogger().warning("Tried to open a non-existent GUI: " + guiName);
            return;
        }
        AetherGuardGUI gui = factory.create(player, data);
        if (gui != null) {
            gui.open();
        }
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMenuClick(InventoryClickEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();

        // If the inventoryholder is one of our GUIs
        if (holder instanceof AetherGuardGUI) {
            e.setCancelled(true); // prevent players from taking items
            AetherGuardGUI gui = (AetherGuardGUI) holder;
            gui.handleMenu(e);
        }
    }

    @FunctionalInterface
    private interface GuiFactory {
        AetherGuardGUI create(Player player, Object data);
    }
}