package com.devilswrath.plugin;

import org.bukkit.plugin.java.JavaPlugin;

public class DevilsWrathPlugin extends JavaPlugin {

    private static DevilsWrathPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        // Register listeners
        getServer().getPluginManager().registerEvents(new DevilsWrathListener(this), this);

        // Start particle task for all sword holders
        new DevilsWrathParticleTask(this).runTaskTimer(this, 0L, 2L);

        getLogger().info("Devils Wrath plugin enabled! The demon awakens...");
    }

    @Override
    public void onDisable() {
        getLogger().info("Devils Wrath plugin disabled.");
    }

    public static DevilsWrathPlugin getInstance() {
        return instance;
    }
}
