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
            return MiniMessageToLegacy.convert(plugin.getConfigUtils().getRankDisplayNameAsString(plugin.getPlayerDataManager().getPlayerData(player.getPlayer()).getScore()));
        }
        if (identifier.equalsIgnoreCase("kills")) {
            return String.valueOf(plugin.getPlayerDataManager().getPlayerData(player.getPlayer()).getKills());
        }
        if (identifier.equalsIgnoreCase("deaths")) {
            return String.valueOf(plugin.getPlayerDataManager().getPlayerData(player.getPlayer()).getDeaths());
        }
        if (identifier.equalsIgnoreCase("killstreak")) {
            return String.valueOf(plugin.getPlayerDataManager().getPlayerData(player.getPlayer()).getKillstreak());
        }
        if (identifier.equalsIgnoreCase("kdr")) {
            return String.valueOf(plugin.getPlayerDataManager().getPlayerData(player.getPlayer()).getKDR());
        }
        if (identifier.equalsIgnoreCase("max_score")) {
            return String.valueOf(plugin.getPlayerDataManager().getPlayerData(player.getPlayer()).getMaxScore());
        }
        if (identifier.equalsIgnoreCase("score_req")) {
            return String.valueOf(plugin.getConfigUtils().getNextRankThreshold(plugin.getPlayerDataManager().getPlayerData(player.getPlayer()).getScore()) - plugin.getPlayerDataManager().getPlayerData(player.getPlayer()).getScore());
        }
        return null;
    }
}
