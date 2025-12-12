package com.aetherguard.commands;

import com.aetherguard.core.AetherGuard;
import org.bukkit.command.CommandSender;

public abstract class AetherGuardCommand {
    
    protected final AetherGuard plugin;
    protected final String name;
    protected final String description;
    protected final String usage;
    protected final String permission;
    
    public AetherGuardCommand(AetherGuard plugin, String name, String description, String usage, String permission) {
        this.plugin = plugin;
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.permission = permission;
    }
    
    public abstract void execute(CommandSender sender, String[] args);
    
    public String[] getAliases() { return new String[]{}; }
    
    public boolean isPlayerOnly() { return false; }
    
    public String[] onTabComplete(CommandSender sender, String[] args) { return new String[]{}; }
    
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getUsage() { return usage; }
    public String getPermission() { return permission; }
}
