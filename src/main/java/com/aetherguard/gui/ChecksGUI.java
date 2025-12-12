package com.aetherguard.gui;

import com.aetherguard.checks.Check;
import com.aetherguard.core.AetherGuard;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * 🛡️ AetherGuard Check Management GUI
 *
 * Allows staff to view, enable, and disable anti-cheat checks.
 *
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class ChecksGUI extends AetherGuardGUI {

    private final String category;
    private final int page;
    private static final int CHECKS_PER_PAGE = 45;

    public ChecksGUI(AetherGuard plugin, Player player, String category, int page) {
        super(plugin, player);
        this.category = category;
        this.page = page;
    }

    @Override
    protected String getTitle() {
        return ChatColor.DARK_AQUA + "Checks - " + category;
    }

    @Override
    protected int getInventorySize() {
        return 54; // 6 rows, allows for pagination later
    }

    @Override
    protected void initializeItems() {
        List<Check> checks = plugin.getCheckManager().getCategoryChecks(category);
        int startIndex = page * CHECKS_PER_PAGE;

        for (int i = 0; i < CHECKS_PER_PAGE; i++) {
            int checkIndex = startIndex + i;
            if (checkIndex >= checks.size()) break;

            Check check = checks.get(checkIndex);
            boolean isEnabled = check.isEnabled(); // Assuming isEnabled() exists on Check

            ItemStack checkItem = createGuiItem(
                isEnabled ? Material.LIME_DYE : Material.GRAY_DYE,
                (isEnabled ? ChatColor.GREEN : ChatColor.RED) + check.getName() + " " + check.getType(),
                ChatColor.GRAY + "Category: " + check.getCategory(),
                ChatColor.GRAY + "Status: " + (isEnabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"),
                "",
                ChatColor.YELLOW + "Click to " + (isEnabled ? "disable" : "enable") + "."
            );
            inventory.setItem(i, checkItem);
        }

        // Navigation
        if (page > 0) {
            inventory.setItem(45, createGuiItem(Material.ARROW, ChatColor.YELLOW + "Previous Page"));
        }

        inventory.setItem(49, createGuiItem(Material.BARRIER, ChatColor.GOLD + "Back to Categories"));

        int maxPages = (int) Math.ceil((double) checks.size() / CHECKS_PER_PAGE);
        if (page < maxPages - 1) {
            inventory.setItem(53, createGuiItem(Material.ARROW, ChatColor.YELLOW + "Next Page"));
        }
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(true);
        ItemStack clickedItem = e.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        Material clickedMaterial = clickedItem.getType();

        if (clickedMaterial == Material.ARROW) {
            if (ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()).contains("Next")) {
                new ChecksGUI(plugin, player, category, page + 1).open();
            } else {
                new ChecksGUI(plugin, player, category, page - 1).open();
            }
            return;
        }

        if (clickedMaterial == Material.BARRIER) {
            new CategoryGUI(plugin, player).open();
            return;
        }

        if (clickedItem.getType() == Material.LIME_DYE || clickedItem.getType() == Material.GRAY_DYE) {
            String checkName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()).split(" ")[0];
            String checkType = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()).split(" ")[1];
            String identifier = category + "." + checkName + "." + checkType;

            Check check = plugin.getCheckManager().getCheck(identifier);
            if (check != null) {
                // In a real scenario, you'd save this to a config file.
                // For now, we just toggle the state in memory.
                check.setEnabled(!check.isEnabled());
                player.sendMessage(ChatColor.GREEN + "Toggled " + check.getName() + " " + check.getType() + " to " + (check.isEnabled() ? "Enabled" : "Disabled") + ".");
                
                // Refresh the current page to show the change
                new ChecksGUI(plugin, player, category, page).open();
            }
        }
    }
}