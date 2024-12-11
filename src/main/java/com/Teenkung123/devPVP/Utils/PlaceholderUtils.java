package com.Teenkung123.devPVP.Utils;

import com.Teenkung123.devPVP.DevPVP;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderUtils extends PlaceholderExpansion {

    private final DevPVP plugin;

    public PlaceholderUtils(DevPVP plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "devpvp";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Teenkung123";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier) {
        Player plr = player.getPlayer();
        if (plr == null) return null;
        if (identifier.equalsIgnoreCase("score")) {
            return String.valueOf(plugin.getPlayerDataManager().getPlayerData(player.getPlayer()).getScore());
        }
        if (identifier.equalsIgnoreCase("rank")) {
            return plugin.getConfigUtils().getRankDisplayName(plugin.getPlayerDataManager().getPlayerData(player.getPlayer()).getScore());
        }
        return null;
    }
}
