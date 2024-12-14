package com.Teenkung123.devPVP.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TabComplete implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("devpvp.set") && !sender.hasPermission("devpvp.get") && !sender.hasPermission("devpvp.reload") && !sender.hasPermission("devpvp.*")) {
            return null;
        }
        if (args.length == 1) {
            return List.of("reload", "get", "set");
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("get")) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            }
            if (args[0].equalsIgnoreCase("set")) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            }
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set")) {
                return List.of("kills", "deaths", "killstreak", "score", "max_score");
            }
        }
        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("set")) {
                return List.of("value");
            }
        }
        return null;
    }
}
