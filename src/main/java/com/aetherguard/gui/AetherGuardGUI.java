package com.aetherguard.gui;

import com.aetherguard.core.AetherGuard;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * 🛡️ AetherGuard Abstract GUI
 *
 * Base class for creating interactive GUIs within AetherGuard.
 * Provides helper methods for inventory creation and item management.
 *
 * @author AetherGuard Team
 * @version 1.0.0
 */
public abstract class AetherGuardGUI implements InventoryHolder {

    protected final AetherGuard plugin;
    protected final Player player;
    protected Inventory inventory;

    public AetherGuardGUI(AetherGuard plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    /**
     * Gets the inventory associated with this GUI.
     *
     * @return The GUI's inventory.
     */
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Opens the GUI for the player.
     */
    public void open() {
        inventory = Bukkit.createInventory(this, getInventorySize(), getTitle());
        initializeItems();
        player.openInventory(inventory);
    }

    /**
     * Handles clicks within the GUI.
     *
     * @param event The InventoryClickEvent.
     */
    public abstract void handleMenu(InventoryClickEvent event);

    /**
     * Defines the title of the GUI.
     *
     * @return The GUI title.
     */
    protected abstract String getTitle();

    /**
     * Defines the size of the inventory (must be a multiple of 9).
     *
     * @return The inventory size.
     */
    protected abstract int getInventorySize();

    /**
     * Initializes and places all items in the GUI.
     */
    protected abstract void initializeItems();

    /**
     * Helper method to create a GUI item.
     *
     * @param material The material of the item.
     * @param name The name of the item.
     * @param lore The lore (description) of the item.
     * @return The created ItemStack.
     */
    protected ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> loreList = Arrays.asList(lore);
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }
}