package com.aetherguard.managers;

import com.aetherguard.gui.*;
import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * AetherGuard v1.2.0 - GUI Manager
 * 
 * Manages all GUI interfaces with efficient factory pattern,
 * event handling, and enterprise-grade inventory interactions.
 * 
 * @author AetherGuard Team
 * @version 1.2.0
 */
public class GUIManager implements Listener {

    private final AetherGuard plugin;
    private final Map<String, GuiFactory> guiFactories;

    public GUIManager(AetherGuard plugin) {
        this.plugin = plugin;
        this.guiFactories = new ConcurrentHashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        registerGUIs();
    }

    private void registerGUIs() {
        guiFactories.put("main", (p, data) -> new MainGUI(plugin, p));
        guiFactories.put("checks", (p, data) -> 
            (data instanceof String) ? new ChecksGUI(plugin, p, (String) data, 0) : null);
        guiFactories.put("config", (p, data) -> new ConfigGUI(plugin, p));
        guiFactories.put("detections", (p, data) -> new LiveDetectionsGUI(plugin, p));
        
        plugin.getLogger().log(Level.INFO, "Registered " + guiFactories.size() + " GUIs");
    }

    public void openGUI(Player player, String guiName) {
        openGUI(player, guiName, null);
    }

    public void openGUI(Player player, String guiName, Object data) {
        if (player == null || guiName == null) {
            return;
        }
        
        try {
            GuiFactory factory = guiFactories.get(guiName.toLowerCase());
            if (factory == null) {
                plugin.getLogger().log(Level.WARNING, "GUI not found: " + guiName);
                return;
            }
            
            AetherGuardGUI gui = factory.create(player, data);
            if (gui != null) {
                gui.open();
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error opening GUI: " + guiName, e);
        }
    }

    public void closeAllGUIs() {
        plugin.getServer().getOnlinePlayers().forEach(player -> {
            if (player.getOpenInventory() != null) {
                player.closeInventory();
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMenuClick(InventoryClickEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();
        if (holder instanceof AetherGuardGUI) {
            e.setCancelled(true);
            try {
                ((AetherGuardGUI) holder).handleMenu(e);
            } catch (Exception ex) {
                plugin.getLogger().log(Level.WARNING, "Error handling GUI click", ex);
            }
        }
    }

    @FunctionalInterface
    private interface GuiFactory {
        AetherGuardGUI create(Player player, Object data);
    }
}