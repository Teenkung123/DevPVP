package com.Teenkung123.devPVP.Commands.SubCommands;

import com.Teenkung123.devPVP.DevPVP;
import org.bukkit.command.CommandSender;

public interface SubCommand {

    public void execute(DevPVP plugin, CommandSender sender, String[] args);

}
