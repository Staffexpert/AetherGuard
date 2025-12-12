package com.aetherguard.gui;

import com.aetherguard.core.AetherGuard;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * 🛡️ AetherGuard Main Control Panel GUI
 *
 * The central hub for managing the anti-cheat via an in-game interface.
 *
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class MainGUI extends AetherGuardGUI {

    public MainGUI(AetherGuard plugin, Player player) {
        super(plugin, player);
    }

    @Override
    protected String getTitle() {
        return ChatColor.DARK_RED + "" + ChatColor.BOLD + "AetherGuard Control Panel";
    }

    @Override
    protected int getInventorySize() {
        return 27; // 3 rows
    }

    @Override
    protected void initializeItems() {
        // Functional Items
        inventory.setItem(10, createGuiItem(Material.COMPASS,
                ChatColor.AQUA + "Global Status",
                ChatColor.GRAY + "View server performance and",
                ChatColor.GRAY + "AetherGuard's operational status."));

        inventory.setItem(12, createGuiItem(Material.COMMAND_BLOCK,
                ChatColor.GOLD + "Check Management",
                ChatColor.GRAY + "Enable, disable, and configure",
                ChatColor.GRAY + "all anti-cheat checks."));

        inventory.setItem(14, createGuiItem(Material.ANVIL,
                ChatColor.YELLOW + "Configuration",
                ChatColor.GRAY + "Edit plugin settings directly",
                ChatColor.GRAY + "from the game."));

        inventory.setItem(16, createGuiItem(Material.BOOK,
                ChatColor.GREEN + "Live Detections",
                ChatColor.GRAY + "View a real-time log of",
                ChatColor.GRAY + "all violations."));
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) return;

        Material clickedMaterial = e.getCurrentItem().getType();

        switch (clickedMaterial) {
            case COMMAND_BLOCK:
                // Open the category selection GUI
                new CategoryGUI(plugin, player).open();
                break;
            case ANVIL:
                new ConfigGUI(plugin, player).open();
                break;
            case BOOK:
                 new LiveDetectionsGUI(plugin, player).open();
                 break;
            default:
                break;
        }
    }
}