package com.Teenkung123.devPVP.Managers.Data;

import com.Teenkung123.devPVP.DevPVP;
import com.Teenkung123.devPVP.Record.KillRecord;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class PlayerData {

    private final DevPVP plugin;
    private final Player player;
    private Integer score = 0;
    private Integer kill = 0;
    private Integer death = 0;
    private Integer streak = 0;
    private final Deque<KillRecord> recentKills = new ArrayDeque<>();

    public PlayerData(DevPVP plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        loadData();
    }

    private void loadData() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Connection connection = plugin.getDatabaseUtils().getConnection();
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT score, `kill`, `death`, `streak` FROM devpvp_data WHERE UUID = ?");
                statement.setString(1, player.getUniqueId().toString());
                var resultSet = statement.executeQuery();

                // Check if player data exists, if not insert new data
                if (!resultSet.next()) {
                    statement = connection.prepareStatement("INSERT INTO devpvp_data (UUID, score, `kill`, `death`, `streak`) VALUES (?, ?, ?, ?, ?)");
                    statement.setString(1, player.getUniqueId().toString());
                    statement.setInt(2, 0);
                    statement.setInt(3, 0);
                    statement.setInt(4, 0);
                    statement.setInt(5, 0);
                    statement.executeUpdate();
                } else {
                    score = resultSet.getInt("score");
                    kill = resultSet.getInt("kill");
                    death = resultSet.getInt("death");
                    streak = resultSet.getInt("streak");
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to load player data for " + player.getName());
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        });
    }

    public void saveData() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::save);
    }

    public void saveDataNonAsync() {
        save();
    }

    private void save() {
        Connection connection = plugin.getDatabaseUtils().getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE devpvp_data SET score = ?, `kill` = ?, `death` = ?, `streak` = ? WHERE UUID = ?");
            statement.setInt(1, score);
            statement.setInt(2, kill);
            statement.setInt(3, death);
            statement.setInt(4, streak);
            statement.setString(5, player.getUniqueId().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to save player data for " + player.getName());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(@NotNull Integer newScore) {

        int oldRankIndex = plugin.getConfigUtils().getRankIndexFromScore(this.score);
        int newRankIndex = plugin.getConfigUtils().getRankIndexFromScore(newScore);

        // Compare rank indices to determine if rank changed
        if (newRankIndex > oldRankIndex) {
            displayRankUp();
        } else if (newRankIndex < oldRankIndex) {
            displayRankDown();
        }

        this.score = newScore;
    }


    public Integer getKills() {
        return kill;
    }

    public void setKills(Integer kill) {
        this.kill = kill;
    }

    public Integer getDeaths() {
        return death;
    }

    public void setDeaths(Integer death) {
        this.death = death;
    }

    public Integer getKillstreak() {
        return streak;
    }

    public void setKillstreak(Integer streak) {
        this.streak = streak;
    }

    public Double getKDR() {
        return death == 0 ? (double) kill : (double) kill / death;
    }

    public Deque<KillRecord> getRecentKills() {
        return recentKills;
    }

    public void addKillRecord(KillRecord record) {
        recentKills.addFirst(record);
        if (recentKills.size() > 16) {
            recentKills.removeLast();
        }
    }

    public Player getPlayer() {
        return player;
    }

    private void displayRankUp() {
        String[] titles = plugin.getMessageUtils().getMessage("RankUpTitle").split("\n");
        player.showTitle(Title.title(
                MiniMessage.miniMessage().deserialize(titles[0]),
                MiniMessage.miniMessage().deserialize(titles[1])
        ));

        player.sendMessage(MiniMessage.miniMessage().deserialize(
                plugin.getMessageUtils().getMessage("RankUp"),
                Placeholder.unparsed("rank", plugin.getConfigUtils().getRankDisplayName(score)),
                Placeholder.unparsed("score", String.valueOf(score)),
                Placeholder.unparsed("next", String.valueOf(plugin.getConfigUtils().getNextRankThreshold(score)))
        ));
    }

    private void displayRankDown() {
        String[] titles = plugin.getMessageUtils().getMessage("RankDownTitle").split("\n");
        player.showTitle(Title.title(
                MiniMessage.miniMessage().deserialize(titles[0]),
                MiniMessage.miniMessage().deserialize(titles[1])
        ));

        player.sendMessage(MiniMessage.miniMessage().deserialize(
                plugin.getMessageUtils().getMessage("RankDown"),
                Placeholder.unparsed("rank", plugin.getConfigUtils().getRankDisplayName(score)),
                Placeholder.unparsed("score", String.valueOf(score)),
                Placeholder.unparsed("next", String.valueOf(plugin.getConfigUtils().getNextRankThreshold(score)))
        ));
    }
}