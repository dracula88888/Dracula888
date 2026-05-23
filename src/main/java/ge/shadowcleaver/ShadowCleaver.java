package ge.shadowcleaver;

import org.bukkit.plugin.java.JavaPlugin;

public class ShadowCleaver extends JavaPlugin {

    private static ShadowCleaver instance;

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(new ShadowCleaverListener(this), this);
        getLogger().info("ShadowCleaver plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ShadowCleaver plugin disabled.");
    }

    public static ShadowCleaver getInstance() {
        return instance;
    }
}
