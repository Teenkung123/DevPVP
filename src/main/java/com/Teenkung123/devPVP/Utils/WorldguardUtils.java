package com.Teenkung123.devPVP.Utils;

import com.Teenkung123.devPVP.DevPVP;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import java.util.List;

public class WorldguardUtils {

    private final DevPVP plugin;

    public WorldguardUtils(DevPVP plugin) {
        this.plugin = plugin;
    }

    /**
     * Check if a player is inside a specified region.
     *
     * @param player      The player to check.
     * @return            True if the player is inside the region, false otherwise.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean playerInRegion(Player player) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        List<String> regionName = plugin.getConfigUtils().getRegions();
        Location playerLocation = player.getLocation();

        RegionManager regionManager = container.get(BukkitAdapter.adapt(player.getWorld()));
        if (regionManager == null) {
            return false;
        }

        ApplicableRegionSet regions = regionManager.getApplicableRegions(BlockVector3.at(playerLocation.getX(), playerLocation.getY(), playerLocation.getZ()));

        for (ProtectedRegion region : regions) {
            if (regionName.contains(region.getId())) {
                return true;
            }
        }
        return false;
    }
}