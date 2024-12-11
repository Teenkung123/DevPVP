package com.Teenkung123.devPVP.Events;

import com.Teenkung123.devPVP.DevPVP;
import com.Teenkung123.devPVP.Managers.Data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathEvent implements Listener {

    private final DevPVP plugin;

    public DeathEvent(DevPVP plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() == null) {
            return;
        }

        if (!plugin.getWorldguardUtils().playerInRegion(event.getEntity())) {
            return;
        }

        if (!plugin.getWorldguardUtils().playerInRegion(event.getEntity().getKiller())) {
            return;
        }

        PlayerData victim = plugin.getPlayerDataManager().getPlayerData(event.getEntity());
        PlayerData attacker = plugin.getPlayerDataManager().getPlayerData(event.getEntity().getKiller());

        int scoreDiff = victim.getScore() - attacker.getScore();
        int change = 20 * (1 - (1 / (1 + 10^(scoreDiff / 400))));

        victim.setScore(Math.max(0, victim.getScore() - change));
        attacker.setScore(attacker.getScore() + change);

        victim.setKillstreak(0);
        attacker.setKillstreak(attacker.getKillstreak() + 1);

        victim.setDeaths(victim.getDeaths() + 1);
        attacker.setKills(attacker.getKills() + 1);

        victim.saveData();
        attacker.saveData();

        event.getEntity().sendMessage("§cYou were killed by " + event.getEntity().getKiller().getName());
        event.getEntity().getKiller().sendMessage("§aYou killed " + event.getEntity().getName());
    }

}
