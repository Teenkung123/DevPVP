package com.Teenkung123.devPVP.Utils;

import com.Teenkung123.devPVP.DevPVP;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;

public class MessageUtils {

    private final FileConfiguration messageConfig;

    public MessageUtils(DevPVP plugin) {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
            file = new File(plugin.getDataFolder(), "messages.yml");
        }
        messageConfig = YamlConfiguration.loadConfiguration(file);
    }

    public String getMessage(String key) {
        return messageConfig.getString("Messages."+key, "Message not found.");
    }
}
