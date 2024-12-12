package com.Teenkung123.devPVP.Utils;

import com.Teenkung123.devPVP.DevPVP;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
public class ConfigUtils {

    private final DevPVP plugin;
    private final FileConfiguration config;
    private List<Integer> rankingIndex;
    private final HashMap<Integer, String> rankingMap = new HashMap<>();
    private List<String> regionList = new ArrayList<>();

    public ConfigUtils(DevPVP plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public void loadConfig() {
        loadRankings();
        regionList = config.getStringList("Options.WorldGuardRegions");
    }

    private void loadRankings() {
        ConfigurationSection section = config.getConfigurationSection("Ranking");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                int minimumScore = section.getInt(key + ".MinimumScore", 0);
                String displayName = section.getString(key + ".DisplayName", "");
                rankingMap.put(minimumScore, displayName);
            }
            rankingIndex = rankingMap.keySet().stream().sorted().toList();
        } else {
            plugin.getLogger().warning("Ranking section is missing in the config file.");
        }
    }

    public ConfigurationSection getDatabaseConfig() {
        return config.getConfigurationSection("Database");
    }

    public String getRankDisplayName(int score) {
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

    public int getNextRankThreshold(int score) {
        for (Integer threshold : rankingIndex) {
            if (score < threshold) {
                return threshold;
            }
        }
        return -1;
    }

    public List<Integer> getRankingIndex() {
        return Collections.unmodifiableList(rankingIndex);
    }

    public List<String> getRegions() {
        return Collections.unmodifiableList(regionList);
    }

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
