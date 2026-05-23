package ge.shadowcleaver;

import org.bukkit.plugin.java.JavaPlugin;

public class Doomfang extends JavaPlugin {

    private static Doomfang instance;

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(new DoomfangListener(this), this);
        getLogger().info("Doomfang plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Doomfang plugin disabled.");
    }

    public static Doomfang getInstance() {
        return instance;
    }
}
