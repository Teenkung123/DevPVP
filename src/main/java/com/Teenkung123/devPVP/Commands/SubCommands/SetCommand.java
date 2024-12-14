package com.Teenkung123.devPVP.Commands.SubCommands;

import com.Teenkung123.devPVP.DevPVP;
import com.Teenkung123.devPVP.Managers.Data.PlayerData;
import com.Teenkung123.devPVP.Managers.PlayerDataManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetCommand implements SubCommand {

        @Override
        public void execute(DevPVP plugin, CommandSender sender, String[] args) {
            //usage /devpvp set <player> <stat> <value>
            if (!sender.hasPermission("devpvp.set") && !sender.hasPermission("devpvp.*")) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getMessageUtils().getMessage("NoPermission")));
                return;
            }
            if (args.length != 4) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: /devpvp set <player> <stat> <value>"));
                return;
            }

            Player player = Bukkit.getOfflinePlayer(args[1]).getPlayer();
            if (player == null) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Player not found."));
                return;
            }

            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
            if (playerData == null) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Player not found."));
                return;
            }

            String status = args[2].toLowerCase();

            switch (status) {
                case "kills":
                    playerData.setKills(Integer.parseInt(args[3]));
                    break;
                case "deaths":
                    playerData.setDeaths(Integer.parseInt(args[3]));
                    break;
                case "killstreak":
                    playerData.setKillstreak(Integer.parseInt(args[3]));
                    break;
                case "score":
                    playerData.setScore(Integer.parseInt(args[3]));
                    break;
                case "max_score":
                    playerData.setMaxScore(Integer.parseInt(args[3]));
                    break;
                default:
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Invalid status. Available status: kills, deaths, killstreak, score"));
                    return;
            }

            playerData.saveData();

            sender.sendMessage(MiniMessage.miniMessage().deserialize(
                    plugin.getMessageUtils().getMessage("SetStatResponse"),
                    Placeholder.unparsed("player", player.getName()),
                    Placeholder.unparsed("stat", status),
                    Placeholder.unparsed("value", args[3])
            ));

        }
}
