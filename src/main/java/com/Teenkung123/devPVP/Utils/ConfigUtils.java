package com.Teenkung123.devPVP.Utils;

import com.Teenkung123.devPVP.DevPVP;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Utility class for managing plugin configuration related to rankings, rewards, and regions.
 * <p>
 * This class handles loading and accessing configuration settings from the plugin's config file.
 * It manages rankings based on player scores, associated rewards, and WorldGuard regions.
 * </p>
 *
 * @author
 * @version 1.0
 * @since 2024-04-27
 */
@SuppressWarnings("unused")
public class ConfigUtils {

    /**
     * Reference to the main plugin instance.
     */
    private final DevPVP plugin;

    /**
     * The plugin's configuration file.
     */
    private final FileConfiguration config;

    /**
     * List of ranking score thresholds in ascending order.
     */
    private List<Integer> rankingIndex;

    /**
     * Mapping from score thresholds to rank display names.
     * <p>
     * Key: Minimum score required for the rank.
     * Value: Display name of the rank.
     * </p>
     */
    private final HashMap<Integer, String> rankingMap = new HashMap<>();

    /**
     * Mapping from rank display names to their associated rewards.
     * <p>
     * Key: Display name of the rank.
     * Value: List of rewards for the rank.
     * </p>
     */
    private HashMap<String, List<String>> rewardMap = new HashMap<>();

    /**
     * List of WorldGuard regions from the configuration.
     */
    private List<String> regionList = new ArrayList<>();

    /**
     * Constructs a new {@code ConfigUtils} instance.
     *
     * @param plugin The main plugin instance.
     */
    public ConfigUtils(DevPVP plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    /**
     * Loads the configuration settings, including rankings and WorldGuard regions.
     * <p>
     * This method should be called during the plugin's initialization to set up necessary
     * configuration data.
     * </p>
     */
    public void loadConfig() {
        loadRankings();
        regionList = config.getStringList("Options.WorldGuardRegions");
    }

    /**
     * Loads the ranking information from the configuration.
     * <p>
     * It reads each rank's minimum score, display name, and associated rewards, then populates
     * the ranking and reward mappings accordingly.
     * </p>
     */
    private void loadRankings() {
        ConfigurationSection section = config.getConfigurationSection("Ranking");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                int minimumScore = section.getInt(key + ".MinimumScore", 0);
                String displayName = section.getString(key + ".DisplayName", "");
                rewardMap.put(displayName, section.getStringList(key + ".Rewards"));
                rankingMap.put(minimumScore, displayName);
            }
            rankingIndex = rankingMap.keySet().stream().sorted().toList();
        } else {
            plugin.getLogger().warning("Ranking section is missing in the config file.");
        }
    }

    /**
     * Retrieves the database configuration section from the main config.
     *
     * @return The {@link ConfigurationSection} for the database settings, or {@code null} if not found.
     */
    public ConfigurationSection getDatabaseConfig() {
        return config.getConfigurationSection("Database");
    }

    /**
     * Gets the display name of the rank corresponding to the given score, formatted as a {@link Component}.
     *
     * @param score The player's score.
     * @return A {@link Component} representing the rank's display name.
     */
    public Component getRankDisplayName(int score) {
        String rank = "";
        for (Integer threshold : rankingIndex) {
            if (score >= threshold) {
                rank = rankingMap.get(threshold);
            } else {
                break;
            }
        }
        return MiniMessage.miniMessage().deserialize(rank);
    }

    /**
     * Gets the display name of the rank corresponding to the given score as a {@code String}.
     *
     * @param score The player's score.
     * @return The rank's display name.
     */
    public String getRankDisplayNameAsString(int score) {
        String rank = "";
        for (Integer threshold : rankingIndex) {
            if (score >= threshold) {
                rank = rankingMap.get(threshold);
            } else {
                break;
            }
        }
        return rank;
    }

    /**
     * Determines the score threshold required for the next rank based on the current score.
     *
     * @param score The player's current score.
     * @return The next rank's minimum score threshold, or {@code -1} if the player has reached the highest rank.
     */
    public int getNextRankThreshold(int score) {
        for (Integer threshold : rankingIndex) {
            if (score < threshold) {
                return threshold;
            }
        }
        return -1;
    }

    /**
     * Retrieves the list of rewards associated with a specific rank.
     *
     * @param rank The display name of the rank.
     * @return A {@code List<String>} of rewards for the specified rank, or {@code null} if the rank does not exist.
     */
    public List<String> getRewards(String rank) {
        return rewardMap.get(rank);
    }

    /**
     * Retrieves the list of rewards based on the player's current score.
     *
     * @param score The player's score.
     * @return A {@code List<String>} of rewards for the player's current rank.
     *         Returns an empty list if no rewards are found or the rank is invalid.
     */
    public List<String> getRewardsFromScore(int score) {
        int rankIndex = getRankIndexFromScore(score);
        if (rankIndex >= 0 && rankIndex < rankingIndex.size()) {
            int rankThreshold = rankingIndex.get(rankIndex);
            String rank = rankingMap.get(rankThreshold);
            List<String> rewards = rewardMap.get(rank);

            if (rewards != null) {
                return rewards;
            } else {
                plugin.getLogger().warning("No rewards found for rank: " + rank);
                return Collections.emptyList();
            }
        } else {
            plugin.getLogger().warning("Invalid rank index: " + rankIndex + " for score: " + score);
            return Collections.emptyList();
        }
    }

    /**
     * Retrieves an unmodifiable list of ranking score thresholds.
     *
     * @return An unmodifiable {@code List<Integer>} of ranking thresholds.
     */
    public List<Integer> getRankingIndex() {
        return Collections.unmodifiableList(rankingIndex);
    }

    /**
     * Retrieves an unmodifiable list of WorldGuard regions.
     *
     * @return An unmodifiable {@code List<String>} of region names.
     */
    public List<String> getRegions() {
        return Collections.unmodifiableList(regionList);
    }

    /**
     * Determines the index of the current rank based on the player's score.
     *
     * @param score The player's current score.
     * @return The index of the rank in the ranking list, or {@code 0} if no rank is found.
     */
    public int getRankIndexFromScore(int score) {
        int rankIndex = 0;
        for (int i = 0; i < rankingIndex.size(); i++) {
            if (score >= rankingIndex.get(i)) {
                rankIndex = i;
            } else {
                break;
            }
        }
        return rankIndex;
    }

}
