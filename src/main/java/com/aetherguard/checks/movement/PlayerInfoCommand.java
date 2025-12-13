package com.aetherguard.commands.subcommands;

import com.aetherguard.core.AetherGuard;
import com.aetherguard.managers.analytics.BehavioralAnalysisManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerInfoCommand extends SubCommand {

    public PlayerInfoCommand(AetherGuard plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "player";
    }

    @Override
    public String getDescription() {
        return "Muestra información detallada de un jugador.";
    }

    @Override
    public String getSyntax() {
        return "/ag player <nombre>";
    }

    @Override
    public String getPermission() {
        return "aetherguard.admin.playerinfo";
    }

    @Override
    public boolean isConsoleAllowed() {
        return true;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Uso: " + getSyntax());
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Jugador no encontrado.");
            return;
        }

        BehavioralAnalysisManager.BehavioralAnalysisResult behavior = plugin.getServiceContainer().get(BehavioralAnalysisManager.class).analyzePlayer(target);

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&d&lPerfil de &f" + target.getName()));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&m----------------------------------------------------"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&bPing: &f%dms", target.getPing())));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&bAnálisis de Comportamiento: &f%s", behavior.assessment)));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&b  &7- Puntuación de Riesgo: &e%.2f%%", behavior.riskScore * 100)));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&b  &7- Entropía de Acciones: &e%.3f", behavior.actionEntropy)));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&b  &7- Consistencia de Tiempos: &e%.3f", behavior.timingConsistency)));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&m----------------------------------------------------"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return super.onTabComplete(sender, args);
    }
}