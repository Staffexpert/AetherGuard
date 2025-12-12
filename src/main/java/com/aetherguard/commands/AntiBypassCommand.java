package com.aetherguard.commands;

import com.aetherguard.core.AetherGuard;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 🛡️ Anti-Bypass Command
 * 
 * Detects and handles bypass attempts
 * Monitors suspicious patterns indicating bypass usage
 * 
 * @author AetherGuard Team
 * @version 2.0.0
 */
public class AntiBypassCommand extends AetherGuardCommand {
    
    public AntiBypassCommand(AetherGuard plugin) {
        super(plugin, "antibypass", "Manage anti-bypass settings", "/ag antibypass [enable|disable|check]", "aetherguard.admin");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            displayHelp(sender);
            return;
        }
        
        String action = args[0].toLowerCase();
        
        switch (action) {
            case "enable":
                plugin.getConfig().set("security.anti-bypass.enabled", true);
                plugin.saveConfig();
                sender.sendMessage("§a§lAetherGuard §7» §aAnti-bypass system enabled");
                return;
                
            case "disable":
                plugin.getConfig().set("security.anti-bypass.enabled", false);
                plugin.saveConfig();
                sender.sendMessage("§c§lAetherGuard §7» §cAnti-bypass system disabled");
                return;
                
            case "check":
                checkBypassAttempts(sender);
                return;
                
            case "scan":
                performSecurityScan(sender);
                return;
                
            default:
                displayHelp(sender);
                return;
        }
    }
    
    private void displayHelp(CommandSender sender) {
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━ Anti-Bypass ━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("§a/ag antibypass enable §7- Enable anti-bypass");
        sender.sendMessage("§a/ag antibypass disable §7- Disable anti-bypass");
        sender.sendMessage("§a/ag antibypass check §7- Check bypass attempts");
        sender.sendMessage("§a/ag antibypass scan §7- Perform security scan");
    }
    
    private void checkBypassAttempts(CommandSender sender) {
        boolean bypassEnabled = plugin.getConfig().getBoolean("security.anti-bypass.enabled", true);
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━ Bypass Status ━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("§a✓ Anti-Bypass: " + (bypassEnabled ? "§aEnabled" : "§cDisabled"));
        sender.sendMessage("§a✓ System Integrity: §a" + (plugin.getAntiDisablerSystem().isSystemIntact() ? "Intact" : "Compromised"));
    }
    
    private void performSecurityScan(CommandSender sender) {
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━ Security Scan ━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("§eScanning plugin integrity...");
        
        boolean checkManagerOk = plugin.getCheckManager() != null;
        boolean violationManagerOk = plugin.getViolationManager() != null;
        boolean playerManagerOk = plugin.getPlayerManager() != null;
        boolean actionManagerOk = plugin.getActionManager() != null;
        
        sender.sendMessage((checkManagerOk ? "§a✓" : "§c✗") + " CheckManager: " + (checkManagerOk ? "OK" : "FAILED"));
        sender.sendMessage((violationManagerOk ? "§a✓" : "§c✗") + " ViolationManager: " + (violationManagerOk ? "OK" : "FAILED"));
        sender.sendMessage((playerManagerOk ? "§a✓" : "§c✗") + " PlayerManager: " + (playerManagerOk ? "OK" : "FAILED"));
        sender.sendMessage((actionManagerOk ? "§a✓" : "§c✗") + " ActionManager: " + (actionManagerOk ? "OK" : "FAILED"));
        
        boolean allOk = checkManagerOk && violationManagerOk && playerManagerOk && actionManagerOk;
        sender.sendMessage("§6Result: " + (allOk ? "§aAll systems nominal" : "§cSome systems compromised"));
    }
}
