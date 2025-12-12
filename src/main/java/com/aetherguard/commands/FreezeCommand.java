package com.aetherguard.commands;

import com.aetherguard.core.AetherGuard;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 🛡️ Advanced Freeze Command
 * 
 * Prevents player movement, breaking, placing, and interactions
 * Multi-layer freeze system preventing all forms of gameplay
 * 
 * @author AetherGuard Team
 * @version 2.0.0
 */
public class FreezeCommand extends AetherGuardCommand implements Listener {
    
    private final Map<UUID, FrozenPlayer> frozenPlayers;
    
    public FreezeCommand(AetherGuard plugin) {
        super(plugin, "freeze", "Freeze/Unfreeze a player", "/ag freeze <player> [duration]", "aetherguard.freeze");
        this.frozenPlayers = new ConcurrentHashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command must be run by a player");
            return;
        }
        
        Player player = (Player) sender;
        if (args.length < 1) {
            sender.sendMessage("§c/ag freeze <player> [duration]");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found: " + args[0]);
            return;
        }
        
        long duration = 0;
        if (args.length >= 2) {
            try {
                String durationStr = args[1];
                if (durationStr.endsWith("s")) {
                    duration = Long.parseLong(durationStr.substring(0, durationStr.length() - 1)) * 20;
                } else if (durationStr.endsWith("m")) {
                    duration = Long.parseLong(durationStr.substring(0, durationStr.length() - 1)) * 1200;
                } else {
                    duration = Long.parseLong(durationStr) * 20;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid duration: " + args[1]);
                return;
            }
        }
        
        if (isFrozen(target)) {
            unfreeze(target);
            sender.sendMessage("§a§lAetherGuard §7» §a" + target.getName() + " has been unfrozen");
            target.sendMessage("§a§lAetherGuard §7» §aYou have been unfrozen");
            return;
        }
        
        freeze(target, duration);
        sender.sendMessage("§a§lAetherGuard §7» §c" + target.getName() + " has been frozen" + (duration > 0 ? " for " + (duration / 20) + " seconds" : " indefinitely"));
        target.sendMessage("§c§lAetherGuard §7» §cYou have been frozen" + (duration > 0 ? " for " + (duration / 20) + " seconds" : " indefinitely"));
    }
    
    /**
     * Freeze a player completely
     */
    public void freeze(Player player, long durationTicks) {
        UUID uuid = player.getUniqueId();
        FrozenPlayer frozen = new FrozenPlayer(player, durationTicks);
        frozenPlayers.put(uuid, frozen);
        
        player.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
        player.teleport(player.getLocation());
        
        if (durationTicks > 0) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                if (frozenPlayers.containsKey(uuid)) {
                    unfreeze(player);
                    if (player.isOnline()) {
                        player.sendMessage("§a§lAetherGuard §7» §aYour freeze duration has expired");
                    }
                }
            }, durationTicks);
        }
    }
    
    /**
     * Unfreeze a player
     */
    public void unfreeze(Player player) {
        frozenPlayers.remove(player.getUniqueId());
    }
    
    /**
     * Check if player is frozen
     */
    public boolean isFrozen(Player player) {
        return frozenPlayers.containsKey(player.getUniqueId());
    }
    
    /**
     * Prevent frozen players from moving
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (isFrozen(player)) {
            event.setCancelled(true);
            player.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
        }
    }
    
    /**
     * Prevent frozen players from breaking blocks
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isFrozen(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou cannot break blocks while frozen");
        }
    }
    
    /**
     * Prevent frozen players from placing blocks
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isFrozen(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou cannot place blocks while frozen");
        }
    }
    
    /**
     * Prevent frozen players from interacting
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (isFrozen(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
    
    /**
     * Frozen player tracking
     */
    private static class FrozenPlayer {
        final Player player;
        final long frozenTime;
        final long durationTicks;
        
        FrozenPlayer(Player player, long durationTicks) {
            this.player = player;
            this.frozenTime = System.currentTimeMillis();
            this.durationTicks = durationTicks;
        }
        
        boolean isExpired() {
            if (durationTicks <= 0) return false;
            long elapsed = System.currentTimeMillis() - frozenTime;
            return elapsed >= (durationTicks * 50);
        }
    }
}
