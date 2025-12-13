package com.aetherguard.gui;

import com.aetherguard.core.AetherGuard;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GUIManager - Gestiona la creación de interfaces gráficas.
 */
public class GUIManager {

    private final AetherGuard plugin;

    public GUIManager(AetherGuard plugin) {
        this.plugin = plugin;
    }

    public ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> coloredLore = Arrays.stream(lore).map(line -> "§7" + line).collect(Collectors.toList());
        meta.setLore(coloredLore);
        item.setItemMeta(meta);
        return item;
    }
}