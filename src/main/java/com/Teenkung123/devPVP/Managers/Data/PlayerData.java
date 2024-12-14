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

/**
 * Manages individual player data within the DevPVP plugin.
 * <p>
 * This class handles loading, saving, and managing player-specific data such as scores,
 * kills, deaths, and kill streaks. It also manages recent kill records and handles
 * rank updates based on player performance.
 * </p>
 *
 * @author
 * @version 1.0
 * @since 2024-04-27
 */
public class PlayerData {

    /**
     * Reference to the main plugin instance.
     */
    private final DevPVP plugin;

    /**
     * The player instance associated with this data.
     */
    private final Player player;

    /**
     * The current score of the player.
     */
    private Integer score = 0;

    /**
     * The maximum score achieved by the player.
     */
    private Integer maxscore = 0;

    /**
     * The total number of kills by the player.
     */
    private Integer kill = 0;

    /**
     * The total number of deaths of the player.
     */
    private Integer death = 0;

    /**
     * The current kill streak of the player.
     */
    private Integer streak = 0;

    /**
     * A deque to store the recent kill records of the player.
     * <p>
     * Maintains a maximum of 16 recent kill records.
     * </p>
     */
    private final Deque<KillRecord> recentKills = new ArrayDeque<>();

    /**
     * Constructs a new {@code PlayerData} instance for a specific player.
     *
     * @param plugin The main plugin instance.
     * @param player The player for whom the data is being managed.
     */
    public PlayerData(DevPVP plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        loadData();
    }

    /**
     * Loads the player's data from the database asynchronously.
     * <p>
     * If the player's data does not exist in the database, a new entry is created with default values.
     * </p>
     */
    private void loadData() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Connection connection = plugin.getDatabaseUtils().getConnection();
            try {
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT score, maxscore, `kill`, `death`, `streak` FROM devpvp_data WHERE UUID = ?");
                statement.setString(1, player.getUniqueId().toString());
                var resultSet = statement.executeQuery();

                // Check if player data exists, if not insert new data
                if (!resultSet.next()) {
                    statement = connection.prepareStatement(
                            "INSERT INTO devpvp_data (UUID, score, maxscore, `kill`, `death`, `streak`) VALUES (?, ?, ?, ?, ?, ?)");
                    statement.setString(1, player.getUniqueId().toString());
                    statement.setInt(2, 0);
                    statement.setInt(3, 0);
                    statement.setInt(4, 0);
                    statement.setInt(5, 0);
                    statement.setInt(6, 0);
                    statement.executeUpdate();
                } else {
                    score = resultSet.getInt("score");
                    maxscore = resultSet.getInt("maxscore");
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

    /**
     * Saves the player's data to the database asynchronously.
     * <p>
     * This method schedules an asynchronous task to update the player's data in the database.
     * </p>
     */
    public void saveData() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::save);
    }

    /**
     * Saves the player's data to the database synchronously.
     * <p>
     * This method directly updates the player's data in the database without scheduling an asynchronous task.
     * It should be used cautiously to avoid blocking the main server thread.
     * </p>
     */
    public void saveDataNonAsync() {
        save();
    }

    /**
     * Performs the actual saving of the player's data to the database.
     * <p>
     * Updates the score, max score, kills, deaths, and streak in the database for the associated player.
     * </p>
     */
    private void save() {
        Connection connection = plugin.getDatabaseUtils().getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE devpvp_data SET score = ?, maxscore = ?, `kill` = ?, `death` = ?, `streak` = ? WHERE UUID = ?");
            statement.setInt(1, score);
            statement.setInt(2, maxscore);
            statement.setInt(3, kill);
            statement.setInt(4, death);
            statement.setInt(5, streak);
            statement.setString(6, player.getUniqueId().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to save player data for " + player.getName());
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the current score of the player.
     *
     * @return The player's current score.
     */
    public Integer getScore() {
        return score;
    }

    /**
     * Sets a new score for the player and handles rank updates if necessary.
     *
     * @param newScore The new score to be set.
     */
    public void setScore(@NotNull Integer newScore) {
        int oldRankIndex = plugin.getConfigUtils().getRankIndexFromScore(this.score);
        int newRankIndex = plugin.getConfigUtils().getRankIndexFromScore(newScore);

        if (newScore > maxscore) {
            maxscore = newScore;
        }

        this.score = newScore;

        // Compare rank indices to determine if rank changed
        if (newRankIndex > oldRankIndex) {
            displayRankUp();
        } else if (newRankIndex < oldRankIndex) {
            displayRankDown();
        }
    }

    /**
     * Retrieves the maximum score achieved by the player.
     *
     * @return The player's maximum score.
     */
    public Integer getMaxScore() {
        return maxscore;
    }

    /**
     * Sets a new maximum score for the player.
     *
     * @param newMaxScore The new maximum score to be set.
     */
    public void setMaxScore(@NotNull Integer newMaxScore) {
        this.maxscore = newMaxScore;
    }

    /**
     * Retrieves the total number of kills by the player.
     *
     * @return The player's kill count.
     */
    public Integer getKills() {
        return kill;
    }

    /**
     * Sets the total number of kills for the player.
     *
     * @param kill The new kill count to be set.
     */
    public void setKills(Integer kill) {
        this.kill = kill;
    }

    /**
     * Retrieves the total number of deaths of the player.
     *
     * @return The player's death count.
     */
    public Integer getDeaths() {
        return death;
    }

    /**
     * Sets the total number of deaths for the player.
     *
     * @param death The new death count to be set.
     */
    public void setDeaths(Integer death) {
        this.death = death;
    }

    /**
     * Retrieves the current kill streak of the player.
     *
     * @return The player's current kill streak.
     */
    public Integer getKillstreak() {
        return streak;
    }

    /**
     * Sets a new kill streak value for the player.
     *
     * @param streak The new kill streak to be set.
     */
    public void setKillstreak(Integer streak) {
        this.streak = streak;
    }

    /**
     * Calculates the Kill-Death Ratio (KDR) of the player.
     *
     * @return The player's KDR. Returns the number of kills if deaths are zero to avoid division by zero.
     */
    public Double getKDR() {
        return death == 0 ? (double) kill : (double) kill / death;
    }

    /**
     * Retrieves the deque of recent kill records for the player.
     *
     * @return A {@code Deque<KillRecord>} containing the player's recent kill records.
     */
    public Deque<KillRecord> getRecentKills() {
        return recentKills;
    }

    /**
     * Adds a new kill record to the player's recent kills deque.
     * <p>
     * Maintains a maximum of 16 recent kill records by removing the oldest record if necessary.
     * </p>
     *
     * @param record The {@code KillRecord} to be added.
     */
    public void addKillRecord(KillRecord record) {
        recentKills.addFirst(record);
        if (recentKills.size() > 16) {
            recentKills.removeLast();
        }
    }

    /**
     * Retrieves the player associated with this data.
     *
     * @return The {@code Player} instance.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Displays a rank-up notification to the player.
     * <p>
     * This method constructs and displays a title and message indicating the player's rank has increased.
     * It also executes any reward commands associated with the new rank.
     * </p>
     */
    private void displayRankUp() {
        String[] titles = plugin.getMessageUtils().getMessage("RankUpTitle").split("<br>");
        if (titles.length == 0) {
            titles = new String[]{"", ""};
        } else if (titles.length == 1) {
            titles = new String[]{titles[0], ""};
        }
        player.showTitle(Title.title(
                MiniMessage.miniMessage().deserialize(titles[0],
                        Placeholder.component("rank", plugin.getConfigUtils().getRankDisplayName(score)),
                        Placeholder.unparsed("score", String.valueOf(score)),
                        Placeholder.unparsed("next", String.valueOf(plugin.getConfigUtils().getNextRankThreshold(score)))),
                MiniMessage.miniMessage().deserialize(titles[1],
                        Placeholder.component("rank", plugin.getConfigUtils().getRankDisplayName(score)),
                        Placeholder.unparsed("score", String.valueOf(score)),
                        Placeholder.unparsed("next", String.valueOf(plugin.getConfigUtils().getNextRankThreshold(score))))
        ));

        player.sendMessage(MiniMessage.miniMessage().deserialize(
                plugin.getMessageUtils().getMessage("RankUp"),
                Placeholder.component("rank", plugin.getConfigUtils().getRankDisplayName(score)),
                Placeholder.unparsed("score", String.valueOf(score)),
                Placeholder.unparsed("next", String.valueOf(plugin.getConfigUtils().getNextRankThreshold(score)))
        ));

        for (String command : plugin.getConfigUtils().getRewardsFromScore(score)) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("<player>", player.getName()));
        }
    }

    /**
     * Displays a rank-down notification to the player.
     * <p>
     * This method constructs and displays a title and message indicating the player's rank has decreased.
     * </p>
     */
    private void displayRankDown() {
        String[] titles = plugin.getMessageUtils().getMessage("RankDownTitle").split("<br>");
        if (titles.length == 0) {
            titles = new String[]{"", ""};
        } else if (titles.length == 1) {
            titles = new String[]{titles[0], ""};
        }
        player.showTitle(Title.title(
                MiniMessage.miniMessage().deserialize(titles[0],
                        Placeholder.component("rank", plugin.getConfigUtils().getRankDisplayName(score)),
                        Placeholder.unparsed("score", String.valueOf(score)),
                        Placeholder.unparsed("next", String.valueOf(plugin.getConfigUtils().getNextRankThreshold(score)))),
                MiniMessage.miniMessage().deserialize(titles[1],
                        Placeholder.component("rank", plugin.getConfigUtils().getRankDisplayName(score)),
                        Placeholder.unparsed("score", String.valueOf(score)),
                        Placeholder.unparsed("next", String.valueOf(plugin.getConfigUtils().getNextRankThreshold(score))))
        ));

        player.sendMessage(MiniMessage.miniMessage().deserialize(
                plugin.getMessageUtils().getMessage("RankDown"),
                Placeholder.component("rank", plugin.getConfigUtils().getRankDisplayName(score)),
                Placeholder.unparsed("score", String.valueOf(score)),
                Placeholder.unparsed("next", String.valueOf(plugin.getConfigUtils().getNextRankThreshold(score)))
        ));
    }
}
