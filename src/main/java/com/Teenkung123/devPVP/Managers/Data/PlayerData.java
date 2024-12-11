package com.Teenkung123.devPVP.Managers.Data;

import com.Teenkung123.devPVP.DevPVP;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PlayerData {

    private final DevPVP plugin;
    private final Player player;
    private Integer score = 0;
    private Integer kill = 0;
    private Integer death = 0;
    private Integer streak = 0;

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
                e.printStackTrace();
            }
        });
    }

    public void saveData() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
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
                e.printStackTrace();
            }
        });
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
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

    public Player getPlayer() {
        return player;
    }
}