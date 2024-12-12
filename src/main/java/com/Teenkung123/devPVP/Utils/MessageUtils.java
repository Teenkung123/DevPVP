package com.Teenkung123.devPVP.Utils;

import com.Teenkung123.devPVP.DevPVP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MessageUtils {

    private final FileConfiguration messageConfig;

    public MessageUtils(DevPVP plugin) {
        plugin.saveResource("messages.yml", false);
        File file = new File(plugin.getDataFolder(), "messages.yml");
        messageConfig = YamlConfiguration.loadConfiguration(file);
    }

    public String getMessage(String key) {
        return messageConfig.getString("Messages."+key, "Message not found.");
    }
}
