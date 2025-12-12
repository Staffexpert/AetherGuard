package com.aetherguard.commands;

import com.aetherguard.core.AetherGuard;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 🛡️ Security Management Command
 * 
 * Comprehensive security layer management and monitoring
 * Controls all anti-disable and security features
 * 
 * @author AetherGuard Team
 * @version 2.0.0
 */
public class SecurityCommand extends AetherGuardCommand {
    
    public SecurityCommand(AetherGuard plugin) {
        super(plugin, "security", "Manage security layers", "/ag security [layers|integrity|audit|report]", "aetherguard.admin");
    }
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            displayHelp(sender);
            return;
        }
        
        String action = args[0].toLowerCase();
        
        switch (action) {
            case "layers":
                displaySecurityLayers(sender);
                return;
                
            case "integrity":
                checkSystemIntegrity(sender);
                return;
                
            case "audit":
                performSecurityAudit(sender);
                return;
                
            case "report":
                generateSecurityReport(sender);
                return;
                
            case "disable-detection":
                checkDisablerDetection(sender);
                return;
                
            default:
                displayHelp(sender);
                return;
        }
    }
    
    private void displayHelp(CommandSender sender) {
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━ Security Manager ━━━━━━━━━━━━━━━━");
        sender.sendMessage("§a/ag security layers §7- View security layers");
        sender.sendMessage("§a/ag security integrity §7- Check system integrity");
        sender.sendMessage("§a/ag security audit §7- Perform security audit");
        sender.sendMessage("§a/ag security report §7- Generate security report");
        sender.sendMessage("§a/ag security disable-detection §7- Check disabler detection");
    }
    
    private void displaySecurityLayers(CommandSender sender) {
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━ Security Layers ━━━━━━━━━━━━━━━━");
        sender.sendMessage("§a1. §fClass Modification Detection §7- Detects modified player classes");
        sender.sendMessage("§a2. §fReflection Monitoring §7- Tracks reflection API abuse");
        sender.sendMessage("§a3. §fBytecode Manipulation Detection §7- Detects code injections");
        sender.sendMessage("§a4. §fClassLoader Hooking Detection §7- Monitors classloader replacement");
        sender.sendMessage("§a5. §fPacket Interception Detection §7- Tracks packet anomalies");
        sender.sendMessage("§a6. §fEvent Listener Monitoring §7- Detects listener removal");
        sender.sendMessage("§a7. §fPlugin Tampering Detection §7- Validates plugin integrity");
        sender.sendMessage("§a8. §fClient Brand Analysis §7- Identifies malicious clients");
        sender.sendMessage("§a9. §fNetwork Behavior Analysis §7- Analyzes packet patterns");
    }
    
    private void checkSystemIntegrity(CommandSender sender) {
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━ System Integrity ━━━━━━━━━━━━━━━━");
        
        boolean intact = plugin.getAntiDisablerSystem().isSystemIntact();
        sender.sendMessage("§6Status: " + (intact ? "§a✓ Intact" : "§c✗ Compromised"));
        
        if (!intact) {
            sender.sendMessage("§cWARNING: System integrity has been compromised!");
            sender.sendMessage("§cImmediate action recommended!");
        }
    }
    
    private void performSecurityAudit(CommandSender sender) {
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━ Security Audit ━━━━━━━━━━━━━━━━");
        sender.sendMessage("§eAuditing all security components...");
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Thread.sleep(1000);
                
                sender.sendMessage("§7■ Checking plugin components...");
                Thread.sleep(500);
                
                boolean checkOk = plugin.getCheckManager() != null;
                boolean violationOk = plugin.getViolationManager() != null;
                boolean playerOk = plugin.getPlayerManager() != null;
                
                sender.sendMessage((checkOk ? "§a✓" : "§c✗") + " CheckManager: " + (checkOk ? "OK" : "FAILED"));
                sender.sendMessage((violationOk ? "§a✓" : "§c✗") + " ViolationManager: " + (violationOk ? "OK" : "FAILED"));
                sender.sendMessage((playerOk ? "§a✓" : "§c✗") + " PlayerManager: " + (playerOk ? "OK" : "FAILED"));
                
                sender.sendMessage("§7■ Checking anti-disabler system...");
                Thread.sleep(500);
                
                boolean disablerOk = plugin.getAntiDisablerSystem() != null;
                sender.sendMessage((disablerOk ? "§a✓" : "§c✗") + " AntiDisablerSystem: " + (disablerOk ? "OK" : "FAILED"));
                
                sender.sendMessage("§a§lAudit complete");
            } catch (Exception e) {
                sender.sendMessage("§cAudit failed: " + e.getMessage());
            }
        });
    }
    
    private void generateSecurityReport(CommandSender sender) {
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━ Security Report ━━━━━━━━━━━━━━━━");
        sender.sendMessage("§aAetherGuard v" + plugin.getDescription().getVersion());
        sender.sendMessage("§aEnabled: " + plugin.isAntiCheatEnabled());
        sender.sendMessage("§aDebug Mode: " + plugin.isDebugMode());
        sender.sendMessage("§aTest Mode: " + plugin.isTestMode());
        
        int totalChecks = plugin.getCheckManager().getTotalChecks();
        int enabledChecks = plugin.getCheckManager().getEnabledChecksCount();
        
        sender.sendMessage("§aTotal Checks: " + totalChecks);
        sender.sendMessage("§aEnabled Checks: " + enabledChecks);
        
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        sender.sendMessage("§aOnline Players: " + onlinePlayers);
        
        boolean systemIntact = plugin.getAntiDisablerSystem().isSystemIntact();
        sender.sendMessage("§aSystem Integrity: " + (systemIntact ? "§a✓ Intact" : "§c✗ Compromised"));
    }
    
    private void checkDisablerDetection(CommandSender sender) {
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━ Disabler Detection ━━━━━━━━━━━━━━━━");
        sender.sendMessage("§eOnline players disabler analysis:");
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            double disablerScore = plugin.getAntiDisablerSystem().detectAntiDisabler(player);
            String status = "§a✓ Clean";
            if (disablerScore > 50) {
                status = "§e⚠ Suspicious";
            }
            if (disablerScore > 80) {
                status = "§c✗ Flagged";
            }
            
            sender.sendMessage(status + " §7" + player.getName() + " §8(§e" + String.format("%.1f", disablerScore) + "%§8)");
        }
    }
}
