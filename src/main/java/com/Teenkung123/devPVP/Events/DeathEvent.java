package com.Teenkung123.devPVP.Events;

import com.Teenkung123.devPVP.DevPVP;
import com.Teenkung123.devPVP.Managers.Data.PlayerData;
import com.Teenkung123.devPVP.Record.KillRecord;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.title.Title;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Deque;
import java.util.UUID;

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

        // Check for kill farming
        boolean isKillFarming = checkKillFarming(victim, attacker);
        if (isKillFarming) {
            victim.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize(
                    plugin.getMessageUtils().getMessage("KillFarmingVictim"),
                    Placeholder.unparsed("player", event.getEntity().getKiller().getName()),
                    Placeholder.unparsed("score", String.valueOf(victim.getScore()))
            ));
            attacker.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize(
                    plugin.getMessageUtils().getMessage("KillFarmingAttacker"),
                    Placeholder.unparsed("player", event.getEntity().getName()),
                    Placeholder.unparsed("score", String.valueOf(attacker.getScore()))
            ));
            return;
        }

        int scoreDiff = victim.getScore() - attacker.getScore();
        int change = Double.valueOf(20 * (1 - (1 / (1 + Math.pow(10, (double) scoreDiff / 400))))).intValue();

        victim.setScore(Math.max(0, victim.getScore() - change));
        attacker.setScore(attacker.getScore() + change);

        victim.setKillstreak(0);
        attacker.setKillstreak(attacker.getKillstreak() + 1);

        victim.setDeaths(victim.getDeaths() + 1);
        attacker.setKills(attacker.getKills() + 1);

        // Record the kill
        KillRecord newKill = new KillRecord(victim.getPlayer().getUniqueId(), System.currentTimeMillis());
        attacker.addKillRecord(newKill);

        victim.saveData();
        attacker.saveData();

        attacker.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize(
                plugin.getMessageUtils().getMessage("KillMessage"),
                Placeholder.unparsed("player", event.getEntity().getName()),
                Placeholder.unparsed("score", String.valueOf(change)),
                Placeholder.unparsed("current", String.valueOf(attacker.getScore()))
        ));
        //split at "\n"
        String[] title = plugin.getMessageUtils().getMessage("KillTitle").split("\n");
        attacker.getPlayer().showTitle(Title.title(
                MiniMessage.miniMessage().deserialize(title[0]),
                MiniMessage.miniMessage().deserialize(title[1])
        ));

        // ---------------------------- Saperate between attacker and victim ----------------------------

        victim.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize(
                plugin.getMessageUtils().getMessage("DeathMessage"),
                Placeholder.unparsed("player", event.getEntity().getKiller().getName()),
                Placeholder.unparsed("score", String.valueOf(change)),
                Placeholder.unparsed("current", String.valueOf(victim.getScore()))
        ));
        //split at "\n"
        String[] title2 = plugin.getMessageUtils().getMessage("DeathTitle").split("\n");
        victim.getPlayer().showTitle(Title.title(
                MiniMessage.miniMessage().deserialize(title2[0]),
                MiniMessage.miniMessage().deserialize(title2[1])
        ));
    }

    /**
     * Checks if the attacker is killfarming the victim by killing them three times within the last 10 minutes.
     *
     * @param victim   The PlayerData of the victim.
     * @param attacker The PlayerData of the attacker.
     * @return True if kill farming is detected, else false.
     */
    private boolean checkKillFarming(PlayerData victim, PlayerData attacker) {
        Deque<KillRecord> recentKills = attacker.getRecentKills();
        UUID victimId = victim.getPlayer().getUniqueId();
        long currentTime = System.currentTimeMillis();
        long timeWindowMillis = 600_000; // 10 minutes

        int killCount = 0;

        for (KillRecord killRecord : recentKills) {
            if (killRecord.uuid().equals(victimId)) {
                killCount++;
                if (killCount == 3) {
                    long thirdKillTime = killRecord.timestamp();
                    return currentTime - thirdKillTime <= timeWindowMillis;
                }
            }

            // Stop checking if the kill record is older than the time window
            if (currentTime - killRecord.timestamp() > timeWindowMillis) {
                break;
            }
        }

        return false;
    }
}
