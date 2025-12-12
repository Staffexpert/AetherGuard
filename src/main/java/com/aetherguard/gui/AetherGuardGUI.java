package com.aetherguard.gui;

import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public abstract class AetherGuardGUI {
    
    protected final AetherGuard plugin;
    protected final Player player;
    
    public AetherGuardGUI(AetherGuard plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }
    
    public abstract Inventory getInventory();
    
    public void open() {
        player.openInventory(getInventory());
    }
}
