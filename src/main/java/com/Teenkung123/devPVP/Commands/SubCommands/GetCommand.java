package com.Teenkung123.devPVP.Commands.SubCommands;

import com.Teenkung123.devPVP.DevPVP;
import com.Teenkung123.devPVP.Managers.Data.PlayerData;
import com.Teenkung123.devPVP.Managers.PlayerDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GetCommand implements SubCommand {

    @Override
    public void execute(DevPVP plugin, CommandSender sender, String[] args) {
        if (!sender.hasPermission("devpvp.get") && !sender.hasPermission("devpvp.*")) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You do not have permission to use this command."));
            return;
        }
        if (args.length == 1) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: /devpvp get <player>"));
            return;
        }

        Player player = Bukkit.getOfflinePlayer(args[1]).getPlayer();
        if (player == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Player not found."));
            return;
        }

        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        PlayerData playerData = playerDataManager.getPlayerData(player);

        if (playerData == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Player not found."));
            return;
        }

        sender.sendMessage(colorize(
                "<yellow><player><gray>'s <red>PVP <gray>Status<br>" +
                        "<br>" +
                        "<gray>Kills: <yellow><kills><br>" +
                        "<gray>Deaths: <yellow><deaths><br>" +
                        "<gray>Killstreak: <yellow><killstreak><br>" +
                        "<gray>KDR: <yellow><kdr><br>" +
                        "<gray>Score: <yellow><score><br>" +
                        "<gray>Max Score: <yellow><max_score><br>" +
                        "<gray>Rank: <yellow><rank><br>" +
                        "<gray>Score to next rank: <yellow><score_req>",
                plugin,
                playerData
        ));

    }

    private Component colorize(String message, DevPVP plugin, PlayerData playerData) {
        return MiniMessage.miniMessage().deserialize(
                message,
                Placeholder.unparsed("player", playerData.getPlayer().getName()),
                Placeholder.unparsed("kills", String.valueOf(playerData.getKills())),
                Placeholder.unparsed("deaths", String.valueOf(playerData.getDeaths())),
                Placeholder.unparsed("killstreak", String.valueOf(playerData.getKillstreak())),
                Placeholder.unparsed("kdr", String.valueOf(playerData.getKDR())),
                Placeholder.unparsed("score", String.valueOf(playerData.getScore())),
                Placeholder.unparsed("max_score", String.valueOf(playerData.getMaxScore())),
                Placeholder.component("rank", plugin.getConfigUtils().getRankDisplayName(playerData.getScore())),
                Placeholder.unparsed("score_req", String.valueOf(plugin.getConfigUtils().getNextRankThreshold(playerData.getScore()) - playerData.getScore()))
        );

    }

}
