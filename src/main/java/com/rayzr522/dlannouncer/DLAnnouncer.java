package com.rayzr522.dlannouncer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Rayzr
 */
public class DLAnnouncer extends JavaPlugin {
    private static DLAnnouncer instance;

    private DLAnnouncerTask task;

    @Override
    public void onEnable() {
        instance = this;

        getCommand("announcer").setExecutor(new CommandAnnouncer(this));

        reload();
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public void broadcast(String message) {
        StringBuilder newMessage = new StringBuilder();

        int wrap = getConfig().getInt("line-wrap");

        while (message.length() >= wrap) {
            newMessage.append(message.substring(0, wrap).trim()).append("\n");
            message = message.substring(wrap);
        }

        newMessage.append(message);

        String formatted = String.format("%s\n&r%s\n%s", getConfig().getString("prefix"), newMessage.toString(), getConfig().getString("suffix"));
        getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', formatted));
    }

    /**
     * (Re)loads all configs from the disk
     */
    public void reload() {
        saveDefaultConfig();
        reloadConfig();

        if (task != null) {
            task.cancel();
        }

        task = new DLAnnouncerTask(this);

        long time = getConfig().getLong("delay");
        task.runTaskTimer(this, time, time);
    }

    public void save() {
        saveConfig();
    }

    public List<String> getBroadcasts() {
        return getConfig().isList("broadcasts") ? getConfig().getStringList("broadcasts") : new ArrayList<>();
    }

    /**
     * If the file is not found and there is a default file in the JAR, it saves the default file to the plugin data folder first
     * 
     * @param path The path to the config file (relative to the plugin data folder)
     * @return The {@link YamlConfiguration}
     */
    public YamlConfiguration getConfig(String path) {
        if (!getFile(path).exists() && getResource(path) != null) {
            saveResource(path, true);
        }
        return YamlConfiguration.loadConfiguration(getFile(path));
    }

    /**
     * Attempts to save a {@link YamlConfiguration} to the disk, and any {@link IOException}s are printed to the console
     * 
     * @param config The config to save
     * @param path The path to save the config file to (relative to the plugin data folder)
     */
    public void saveConfig(YamlConfiguration config, String path) {
        try {
            config.save(getFile(path));
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to save config", e);
        }
    }

    public void set(String path, Object value) {
        getConfig().set(path, value);
        save();
    }

    /**
     * @param path The path of the file (relative to the plugin data folder)
     * @return The {@link File}
     */
    public File getFile(String path) {
        return new File(getDataFolder(), path.replace('/', File.pathSeparatorChar));
    }

    public static DLAnnouncer getInstance() {
        return instance;
    }

    private class DLAnnouncerTask extends BukkitRunnable {

        private DLAnnouncer plugin;

        public DLAnnouncerTask(DLAnnouncer plugin) {
            this.plugin = plugin;
        }

        private String randomFrom(List<String> options) {
            return options.get(ThreadLocalRandom.current().nextInt(options.size()));
        }

        public void run() {
            List<String> broadcasts = plugin.getBroadcasts();
            if (broadcasts.size() < 1) {
                plugin.getLogger().severe("No broadcasts were found in the config file, so there was nothing to broadcast!");
                return;
            }

            plugin.broadcast(randomFrom(broadcasts));
        }

    }

}
