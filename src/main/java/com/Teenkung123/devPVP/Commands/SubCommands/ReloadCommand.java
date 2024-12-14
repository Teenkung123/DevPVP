package com.Teenkung123.devPVP.Commands.SubCommands;

import com.Teenkung123.devPVP.DevPVP;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements SubCommand{

    @Override
    public void execute(DevPVP plugin, CommandSender sender, String[] args) {
        if (!sender.hasPermission("devpvp.reload") && !sender.hasPermission("devpvp.*")) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getMessageUtils().getMessage("NoPermission")));
            return;
        }

        long ms = System.currentTimeMillis();
        plugin.reload();
        sender.sendMessage(MiniMessage.miniMessage().deserialize(
                plugin.getMessageUtils().getMessage("ReloadResponse"),
                Placeholder.unparsed("time", String.valueOf(System.currentTimeMillis() - ms))
        ));


    }
}
