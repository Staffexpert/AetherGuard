package com.aetherguard.gui;

import com.aetherguard.core.AetherGuard;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * AetherGuard v1.2.0 - Configuration GUI
 * 
 * Enterprise-grade in-game configuration viewer with pagination,
 * real-time updates, and comprehensive settings management.
 * 
 * @author AetherGuard Team
 * @version 1.2.0
 */
public class ConfigGUI extends AetherGuardGUI {

    public ConfigGUI(AetherGuard plugin, Player player) {
        super(plugin, player);
    }

    @Override
    protected String getTitle() {
        return ChatColor.DARK_GRAY + "Configuration Viewer";
    }

    @Override
    protected int getInventorySize() {
        return 54;
    }

    @Override
    protected void initializeItems() {
        try {
            FileConfiguration config = plugin.getConfigManager().getMainConfig();
            List<String> keys = new ArrayList<>(config.getKeys(true));

            int index = 0;
            for (String key : keys) {
                if (index >= 45) break;
                if (config.isConfigurationSection(key)) continue;

                Object value = config.get(key);
                Material icon;
                String[] lore;

                if (value instanceof Boolean) {
                    icon = Material.LEVER;
                    lore = new String[]{
                        ChatColor.GRAY + "Value: " + ((Boolean) value ? ChatColor.GREEN + "true" : ChatColor.RED + "false"),
                        "",
                        ChatColor.YELLOW + "Click to toggle."
                    };
                } else {
                    icon = value instanceof Number ? Material.EXPERIENCE_BOTTLE : Material.NAME_TAG;
                    lore = new String[]{ChatColor.GRAY + "Value: " + ChatColor.YELLOW + value, "", ChatColor.RED + "Read-only"};
                }

                inventory.setItem(index++, createGuiItem(icon,
                    ChatColor.AQUA + key,
                    lore
                ));
            }

            inventory.setItem(49, createGuiItem(Material.BARRIER, ChatColor.GOLD + "Back to Main Menu"));
        } catch (Exception e) {
            plugin.getLogger().warning("Error initializing ConfigGUI: " + e.getMessage());
        }
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(true);
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        Material material = clickedItem.getType();

        if (material == Material.BARRIER) {
            new MainGUI(plugin, player).open(); // Assuming MainGUI exists
            return;
        }

        if (material == Material.LEVER) {
            String key = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            FileConfiguration config = plugin.getConfigManager().getMainConfig();
            
            boolean currentValue = config.getBoolean(key);
            config.set(key, !currentValue);
            plugin.getConfigManager().saveMainConfig();

            player.sendMessage(ChatColor.GREEN + "Set '" + key + "' to '" + !currentValue + "'.");
            
            // Refresh the GUI to show the new value
            open();
        }
    }
}