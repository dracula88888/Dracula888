package ge.plugin.diamondsword;

import org.bukkit.plugin.java.JavaPlugin;

public class DiamondSwordPlugin extends JavaPlugin {

    private static DiamondSwordPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        // Register listeners
        getServer().getPluginManager().registerEvents(new CritDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new KillListener(this), this);

        // Register command
        getCommand("givesword").setExecutor(new GiveSwordCommand(this));

        getLogger().info("DiamondSwordPlugin ჩაირთო! ⚔");
    }

    @Override
    public void onDisable() {
        getLogger().info("DiamondSwordPlugin გაითიშა!");
    }

    public static DiamondSwordPlugin getInstance() {
        return instance;
    }
}
