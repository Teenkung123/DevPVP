package com.Teenkung123.devPVP;

import com.Teenkung123.devPVP.Commands.MainCommand;
import com.Teenkung123.devPVP.Events.DeathEvent;
import com.Teenkung123.devPVP.Events.JoinEvent;
import com.Teenkung123.devPVP.Events.QuitEvent;
import com.Teenkung123.devPVP.Managers.PlayerDataManager;
import com.Teenkung123.devPVP.Utils.*;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class DevPVP extends JavaPlugin {

    private ConfigUtils configUtils;
    private MessageUtils messageUtils;
    private DatabaseUtils databaseUtils;
    private PlayerDataManager playerDataManager;
    private WorldguardUtils worldguardUtils;
    private BukkitTask cleanupTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configUtils = new ConfigUtils(this);
        messageUtils = new MessageUtils(this);
        databaseUtils = new DatabaseUtils(this);
        playerDataManager = new PlayerDataManager(this);
        worldguardUtils = new WorldguardUtils(this);

        new MainCommand(this);
        new DeathEvent(this);
        new JoinEvent(this);
        new QuitEvent(this);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            if (!PlaceholderAPI.isRegistered("devpvp")) new PlaceholderUtils(this).register();
        }

        configUtils.loadConfig();
        databaseUtils.connect();
        databaseUtils.createTableIfNotExists();

        for (Player player : Bukkit.getOnlinePlayers()) {
            playerDataManager.loadPlayerData(player);
        }

        startCleanupTask();
    }

    @Override
    public void onDisable() {
        playerDataManager.saveAllPlayerDataNonAsync();
        databaseUtils.stopKeepAliveTask();
        databaseUtils.disconnect();
        if (cleanupTask != null) cleanupTask.cancel();
    }

    public void startCleanupTask() {
        cleanupTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            for (OfflinePlayer player : playerDataManager.getLoadedPlayers()) {
                if (player.isOnline()) continue;
                playerDataManager.removePlayerData(player.getUniqueId());
            }
        }, 0, 20*60*5);
    }

    public ConfigUtils getConfigUtils() {
        return configUtils;
    }

    public DatabaseUtils getDatabaseUtils() {
        return databaseUtils;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public WorldguardUtils getWorldguardUtils() {
        return worldguardUtils;
    }

    public MessageUtils getMessageUtils() { return messageUtils; }

    public void reload() {
        playerDataManager.saveAllPlayerDataNonAsync();
        cleanupTask.cancel();

        configUtils = new ConfigUtils(this);
        messageUtils = new MessageUtils(this);

        configUtils.loadConfig();

        startCleanupTask();
    }
}
