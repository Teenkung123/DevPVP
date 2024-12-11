package com.Teenkung123.devPVP.Commands;

import com.Teenkung123.devPVP.Commands.SubCommands.GetCommand;
import com.Teenkung123.devPVP.DevPVP;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class MainCommand implements CommandExecutor {

    private final DevPVP plugin;
    private final GetCommand getCommand = new GetCommand();


    public MainCommand(DevPVP plugin) {
        this.plugin = plugin;
        //noinspection DataFlowIssue
        plugin.getCommand("devpvp").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                // Reload config
                break;
            case "get":
                getCommand.execute(plugin, sender, args);
                break;
            default:
                return false;
        }

        return false;
    }
}
