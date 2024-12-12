package com.Teenkung123.devPVP.Managers;

import com.Teenkung123.devPVP.DevPVP;
import com.Teenkung123.devPVP.Managers.Data.PlayerData;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerDataManager {

    private final DevPVP plugin;
    private final HashMap<UUID, PlayerData> playerDataMap = new HashMap<>();

    public PlayerDataManager(DevPVP plugin) {
        this.plugin = plugin;
    }

    public void loadPlayerData(Player player) {
        if (playerDataMap.containsKey(player.getUniqueId())) {
            playerDataMap.get(player.getUniqueId());
            return;
        }

        PlayerData playerData = new PlayerData(plugin, player);
        playerDataMap.put(player.getUniqueId(), playerData);
    }

    public PlayerData getPlayerData(Player player) {
        return playerDataMap.get(player.getUniqueId());
    }

    public void removePlayerData(Player player) {
        playerDataMap.remove(player.getUniqueId());
    }

    public void removePlayerData(UUID uuid) {
        playerDataMap.remove(uuid);
    }

    @SuppressWarnings("unused")
    public void saveAllPlayerData() {
        playerDataMap.values().forEach(PlayerData::saveData);
    }

    public void saveAllPlayerDataNonAsync() {
        playerDataMap.values().forEach(PlayerData::saveDataNonAsync);
    }

    public void savePlayerData(Player player) {
        PlayerData playerData = playerDataMap.get(player.getUniqueId());
        if (playerData != null) {
            playerData.saveData();
        }
    }

    public List<OfflinePlayer> getLoadedPlayers() {
        return playerDataMap.keySet().stream().map(plugin.getServer()::getOfflinePlayer).toList();
    }

}
