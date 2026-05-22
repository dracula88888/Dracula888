package ge.plugin.diamondsword;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class KillListener implements Listener {

    private final DiamondSwordPlugin plugin;

    public KillListener(DiamondSwordPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();
        Player killer = dead.getKiller();

        // killer უნდა იყოს Player და უნდა ეჭიროს ჩვენი sword
        if (killer == null) return;
        ItemStack held = killer.getInventory().getItemInMainHand();
        if (!SwordBuilder.isCustomSword(held)) return;

        Location loc = dead.getLocation().add(0, 1, 0);
        playKillAnimation(loc, killer);
    }

    /**
     * Kill animation:
     *  - შავ-წითელი particle burst (REDSTONE dust)
     *  - შავი smoke სვეტი
     *  - lightning-ის ეფექტი (without actual damage)
     *  - dramatic sound combo
     */
    private void playKillAnimation(Location loc, Player killer) {

        // ─── Phase 1: instant burst ───
        // წითელი particle-ები
        spawnDust(loc, Color.RED,       80, 1.2f);
        // მუქი წითელი particle-ები
        spawnDust(loc, Color.fromRGB(120, 0, 0), 60, 0.8f);
        // შავი particle-ები (Color.BLACK)
        spawnDust(loc, Color.BLACK,     100, 1.5f);

        // შავი smoke
        loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 30, 0.4, 0.6, 0.4, 0.05);

        // ─── Sound: kill sound ───
        loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.6f, 0.5f);
        loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_DEATH,            0.8f, 0.6f);
        loc.getWorld().playSound(loc, Sound.ENTITY_WITHER_DEATH,           0.4f, 1.5f);

        // ─── Phase 2: rising smoke column (delayed) ───
        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                if (tick >= 10) { cancel(); return; }

                Location rise = loc.clone().add(0, tick * 0.15, 0);
                spawnDust(rise, Color.BLACK, 8, 1.0f);
                spawnDust(rise, Color.fromRGB(80, 0, 0), 5, 0.6f);
                rise.getWorld().spawnParticle(Particle.SMOKE_LARGE, rise, 4, 0.2, 0.1, 0.2, 0.02);

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 2L);

        // ─── Phase 3: final flash ───
        new BukkitRunnable() {
            @Override
            public void run() {
                spawnDust(loc, Color.RED,   40, 1.5f);
                spawnDust(loc, Color.BLACK, 40, 1.5f);
                loc.getWorld().spawnParticle(Particle.FLAME, loc, 20, 0.3, 0.5, 0.3, 0.05);
                loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.3f, 1.8f);
            }
        }.runTaskLater(plugin, 10L);
    }

    /** Helper: spawns colored REDSTONE dust particles */
    private void spawnDust(Location loc, Color color, int count, float size) {
        Particle.DustOptions dust = new Particle.DustOptions(color, size);
        loc.getWorld().spawnParticle(Particle.REDSTONE, loc, count, 0.5, 0.5, 0.5, dust);
    }
}
