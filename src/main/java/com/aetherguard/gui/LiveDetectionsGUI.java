package com.aetherguard.gui;

import com.aetherguard.core.AetherGuard;
import com.aetherguard.managers.ViolationManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Deque;

/**
 * 🛡️ AetherGuard Live Detections GUI
 *
 * Displays a real-time feed of the latest anti-cheat violations.
 *
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class LiveDetectionsGUI extends AetherGuardGUI {

    public LiveDetectionsGUI(AetherGuard plugin, Player player) {
        super(plugin, player);
    }

    @Override
    protected String getTitle() {
        return ChatColor.RED + "Live Detections";
    }

    @Override
    protected int getInventorySize() {
        return 54;
    }

    @Override
    protected void initializeItems() {
        // This assumes you have a ViolationManager instance in your main plugin class
        // ViolationManager violationManager = plugin.getViolationManager();
        // Deque<ViolationManager.Violation> violations = violationManager.getRecentViolations();

        // For now, we'll use a placeholder message.
        inventory.setItem(22, createGuiItem(Material.BARRIER,
                ChatColor.RED + "Feature Not Fully Implemented",
                ChatColor.GRAY + "ViolationManager needs to be integrated",
                ChatColor.GRAY + "to show live data."));

        // Navigation
        inventory.setItem(48, createGuiItem(Material.ARROW, ChatColor.GOLD + "Back to Main Menu"));
        inventory.setItem(50, createGuiItem(Material.LIME_DYE, ChatColor.GREEN + "Refresh"));
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        switch (clickedItem.getType()) {
            case ARROW:
                new MainGUI(plugin, player).open();
                break;
            case LIME_DYE:
                player.sendMessage(ChatColor.GREEN + "Refreshing detections...");
                open(); // Re-opens the GUI, effectively refreshing it
                break;
            default:
                break;
        }
    }
}