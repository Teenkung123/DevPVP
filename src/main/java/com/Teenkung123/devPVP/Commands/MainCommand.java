package com.Teenkung123.devPVP.Commands;

import com.Teenkung123.devPVP.Commands.SubCommands.GetCommand;
import com.Teenkung123.devPVP.Commands.SubCommands.ReloadCommand;
import com.Teenkung123.devPVP.Commands.SubCommands.SetCommand;
import com.Teenkung123.devPVP.DevPVP;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class MainCommand implements CommandExecutor {

    private final DevPVP plugin;
    private final GetCommand getCommand = new GetCommand();
    private final SetCommand setCommand = new SetCommand();
    private final ReloadCommand reloadCommand = new ReloadCommand();


    public MainCommand(DevPVP plugin) {
        this.plugin = plugin;
        //noinspection DataFlowIssue
        plugin.getCommand("devpvp").setExecutor(this);
        //noinspection DataFlowIssue
        plugin.getCommand("devpvp").setTabCompleter(new TabComplete());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                reloadCommand.execute(plugin, sender, args);
                break;
            case "get":
                getCommand.execute(plugin, sender, args);
                break;
            case "set":
                setCommand.execute(plugin, sender, args);
                break;
            default:
                sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getMessageUtils().getMessage("UnknownCommand")));
                return false;
        }

        return true;
    }
}
