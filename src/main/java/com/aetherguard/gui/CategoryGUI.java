package com.aetherguard.gui;

import com.aetherguard.core.AetherGuard;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 🛡️ AetherGuard Check Category GUI
 *
 * Displays a selection of check categories (Movement, Combat, etc.).
 *
 * @author AetherGuard Team
 * @version 1.0.0
 */
public class CategoryGUI extends AetherGuardGUI {

    public CategoryGUI(AetherGuard plugin, Player player) {
        super(plugin, player);
    }

    @Override
    protected String getTitle() {
        return ChatColor.DARK_BLUE + "Select a Check Category";
    }

    @Override
    protected int getInventorySize() {
        return 27;
    }

    @Override
    protected void initializeItems() {
        Set<String> categories = plugin.getCheckManager().getAllChecks().stream()
                .map(check -> check.getCategory())
                .collect(Collectors.toSet());

        List<String> categoryList = new ArrayList<>(categories);
        
        // Assign materials to categories for visual representation
        for (int i = 0; i < categoryList.size(); i++) {
            if (i >= 27) break;
            String category = categoryList.get(i);
            Material icon = switch (category.toLowerCase()) {
                case "combat" -> Material.DIAMOND_SWORD;
                case "movement" -> Material.LEATHER_BOOTS;
                case "automation" -> Material.PISTON;
                default -> Material.PAPER;
            };
            
            inventory.setItem(10 + (i * 2), createGuiItem(icon, ChatColor.GOLD + category, ChatColor.GRAY + "Click to view " + category + " checks."));
        }

        inventory.setItem(26, createGuiItem(Material.ARROW, ChatColor.GOLD + "Back to Main Menu"));
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        if (clickedItem.getType() == Material.BARRIER || clickedItem.getType() == Material.ARROW) {
            new MainGUI(plugin, player).open();
            return;
        }

        String categoryName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        new ChecksGUI(plugin, player, categoryName, 0).open();
    }
}