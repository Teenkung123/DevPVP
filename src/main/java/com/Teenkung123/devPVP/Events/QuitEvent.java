package com.Teenkung123.devPVP.Events;

import com.Teenkung123.devPVP.DevPVP;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitEvent implements Listener {

    private final DevPVP plugin;

    public QuitEvent(DevPVP plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getPlayerDataManager().savePlayerData(event.getPlayer());
        plugin.getPlayerDataManager().removePlayerData(event.getPlayer());
    }

}
