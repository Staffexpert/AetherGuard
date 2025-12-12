package com.aetherguard.listeners;

import com.aetherguard.core.AetherGuard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PacketListener implements Listener {
    
    private final AetherGuard plugin;
    
    public PacketListener(AetherGuard plugin) {
        this.plugin = plugin;
    }
}
