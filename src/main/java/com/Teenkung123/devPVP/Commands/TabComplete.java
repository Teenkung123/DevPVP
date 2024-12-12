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
        if (args.length == 1) {
            return List.of("reload", "get");
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("get")) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            }
        }
        return null;
    }
}
