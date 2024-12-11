package com.Teenkung123.devPVP.Commands.SubCommands;

import com.Teenkung123.devPVP.DevPVP;
import com.Teenkung123.devPVP.Managers.Data.PlayerData;
import com.Teenkung123.devPVP.Managers.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GetCommand extends SubCommand {

    @Override
    public void execute(DevPVP plugin, CommandSender sender, String[] args) {
        if (args.length == 1) {
            sender.sendMessage("§cUsage: /devpvp get <player>");
            return;
        }

        Player player = Bukkit.getOfflinePlayer(args[1]).getPlayer();
        if (player == null) {
            sender.sendMessage("§cPlayer not found.");
            return;
        }

        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        PlayerData playerData = playerDataManager.getPlayerData(player);

        if (playerData == null) {
            sender.sendMessage("§cPlayer not found.");
            return;
        }

        sender.sendMessage("§aPlayer data:");
        sender.sendMessage("§7- Kills: " + playerData.getKills());
        sender.sendMessage("§7- Deaths: " + playerData.getDeaths());
        sender.sendMessage("§7- Killstreak: " + playerData.getKillstreak());
        sender.sendMessage("§7- KDR: " + playerData.getKDR());
        sender.sendMessage("§7- Score: " + playerData.getScore());
        sender.sendMessage("§7- Rank: " + plugin.getConfigUtils().getRankDisplayName(playerData.getScore()));
        sender.sendMessage("§7- Score Required: " + (plugin.getConfigUtils().getNextRankThreshold(playerData.getScore()) - playerData.getScore()));

    }

}
