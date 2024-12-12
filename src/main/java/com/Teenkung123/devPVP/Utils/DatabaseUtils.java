package com.Teenkung123.devPVP.Utils;

import com.Teenkung123.devPVP.DevPVP;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class DatabaseUtils {

    private final DevPVP plugin;
    private Connection connection;

    private String host;
    private int port;
    private String database;
    private String username;
    private String password;

    private BukkitTask keepAliveTask;

    public DatabaseUtils(DevPVP plugin) {
        this.plugin = plugin;
        loadDatabaseConfig();
    }

    private void loadDatabaseConfig() {
        ConfigurationSection config = plugin.getConfigUtils().getDatabaseConfig();
        this.host = config.getString("Host", "localhost");
        this.port = config.getInt("Port", 3306);
        this.database = config.getString("Database", "minecraft");
        this.username = config.getString("User", "root");
        this.password = config.getString("Password", "root");
    }

    public void connect() {
        if (!isConnected()) {
            try {
                connection = DriverManager.getConnection(
                        "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false",
                        username,
                        password
                );
                plugin.getLogger().log(Level.INFO, "Database connected successfully!");
                startKeepAliveTask();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not connect to the database!", e);
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
        }
    }

    public void disconnect() {
        if (isConnected()) {
            try {
                if (keepAliveTask != null && !keepAliveTask.isCancelled()) {
                    keepAliveTask.cancel();
                }
                connection.close();
                plugin.getLogger().log(Level.INFO, "Database disconnected successfully!");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not disconnect from the database!", e);
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isConnected() {
        return connection != null;
    }

    private void startKeepAliveTask() {
        keepAliveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (isConnected()) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT 1")) {
                    statement.executeQuery();
                } catch (SQLException e) {
                    plugin.getLogger().log(Level.WARNING, "Error while keeping the database connection alive.", e);
                }
            }
        }, 0L, 1200L); // Run every 60 seconds (1200 ticks)
    }

    public void stopKeepAliveTask() {
        if (keepAliveTask != null && !keepAliveTask.isCancelled()) {
            keepAliveTask.cancel();
        }
    }

    public void createTableIfNotExists() {
        if (isConnected()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS devpvp_data (" +
                    "ID INT AUTO_INCREMENT PRIMARY KEY, " +
                    "UUID VARCHAR(36) NOT NULL, " +
                    "Score INT NOT NULL, " +
                    "`Kill` INT NOT NULL, " +
                    "`Death` INT NOT NULL, " +
                    "`Streak` INT NOT NULL" +
                    ");";
            try (PreparedStatement statement = connection.prepareStatement(createTableSQL)) {
                statement.executeUpdate();
                plugin.getLogger().log(Level.INFO, "PlayerData table ensured to exist.");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create PlayerData table!", e);
            }
        } else {
            plugin.getLogger().log(Level.WARNING, "Cannot create table, no database connection.");
        }
    }

}
