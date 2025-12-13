package com.aetherguard.commands;

import com.aetherguard.core.AetherGuard;
import com.aetherguard.managers.BanWaveManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * 🛡️ AetherGuard BanWave Command - PREMIUM Feature
 *
 * Comando para gestionar el sistema de BAN WAVES
 * Permite ver estadísticas, forzar olas y gestionar la cola
 *
 * @author AetherGuard Team
 * @version 2.0.0
 */
public class BanWaveCommand extends BaseCommand {

    public BanWaveCommand(AetherGuard plugin) {
        super(plugin, "banwave", "aetherguard.admin", false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!checkPermission(sender)) return;

        if (args.length == 0) {
            displayHelp(sender);
            return;
        }

        BanWaveManager banWaveManager = plugin.getBanWaveManager();
        BanWaveManager.BanWaveStats stats = banWaveManager.getStats();

        switch (args[0].toLowerCase()) {
            case "stats":
            case "info":
                displayStats(sender, stats);
                break;

            case "force":
                forceWave(sender, banWaveManager);
                break;

            case "clear":
                clearQueue(sender, banWaveManager);
                break;

            case "help":
            default:
                displayHelp(sender);
                break;
        }
    }

    private void displayStats(CommandSender sender, BanWaveManager.BanWaveStats stats) {
        sender.sendMessage("§b🌊 §lBAN WAVE ESTADÍSTICAS §b🌊");
        sender.sendMessage("§b⚡ §fOlas totales: §e" + stats.totalWaves);
        sender.sendMessage("§b📋 §fJugadores en cola: §e" + stats.queueSize);
        sender.sendMessage("§b🔄 §fOla en progreso: §e" + (stats.waveInProgress ? "§aSí" : "§cNo"));
        sender.sendMessage("§b⏱️ §fTiempo promedio de espera: §e" + String.format("%.1f", stats.averageWaitTime / 1000.0) + "s");
        sender.sendMessage("");

        if (stats.queueSize > 0) {
            sender.sendMessage("§b📊 §fPróxima ola automática en: §e" + (stats.queueSize >= 5 ? "§aINMEDIATA" : "§e30 segundos"));
        } else {
            sender.sendMessage("§b✅ §fCola vacía - Todas las amenazas eliminadas");
        }
    }

    private void forceWave(CommandSender sender, BanWaveManager banWaveManager) {
        if (banWaveManager.getStats().queueSize == 0) {
            sender.sendMessage("§c❌ No hay jugadores en la cola de baneo");
            return;
        }

        banWaveManager.forceWave();
        sender.sendMessage("§b⚡ §fOla de baneo forzada ejecutándose...");
        plugin.getLogger().log(java.util.logging.Level.INFO, "§b⚡ BAN WAVE forzada por " + sender.getName());
    }

    private void clearQueue(CommandSender sender, BanWaveManager banWaveManager) {
        int cleared = banWaveManager.getStats().queueSize;
        if (cleared == 0) {
            sender.sendMessage("§c❌ La cola ya está vacía");
            return;
        }

        banWaveManager.clearQueue();
        sender.sendMessage("§b🧹 §fCola limpiada - §e" + cleared + " §fbaneos cancelados");
        plugin.getLogger().log(java.util.logging.Level.WARNING, "§b🧹 Cola de BAN WAVES limpiada por " + sender.getName() + " (" + cleared + " baneos cancelados)");
    }

    private void displayHelp(CommandSender sender) {
        sender.sendMessage("§b🌊 §lCOMANDO BAN WAVE §b🌊");
        sender.sendMessage("§b⚡ §f/banwave stats §7- Ver estadísticas del sistema");
        sender.sendMessage("§b⚡ §f/banwave force §7- Forzar ejecución de ola");
        sender.sendMessage("§b⚡ §f/banwave clear §7- Limpiar cola de baneos");
        sender.sendMessage("§b⚡ §f/banwave help §7- Mostrar esta ayuda");
        sender.sendMessage("");
        sender.sendMessage("§b🛡️ §fSistema inteligente que agrupa baneos para evitar sobrecarga");
        sender.sendMessage("§b📊 §fMáximo 5 baneos por ola, con delays controlados");
    }

    @Override
    public List<String> onTabCompleteList(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission(getPermission())) {
            return List.of("stats", "force", "clear", "help");
        }
        return List.of();
    }

    @Override
    public String getDescription() {
        return "Gestionar el sistema de BAN WAVES";
    }

    @Override
    public String getUsage() {
        return "/banwave <stats|force|clear|help>";
    }
}