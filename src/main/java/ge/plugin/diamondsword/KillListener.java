package ge.plugin.diamondsword;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class KillListener implements Listener {

    private static final Color BLOOD_1 = Color.fromRGB(200,  0,  0);
    private static final Color BLOOD_2 = Color.fromRGB(140,  0,  0);
    private static final Color BLOOD_3 = Color.fromRGB( 80,  0,  0);
    private static final Color BLOOD_4 = Color.fromRGB( 40,  0,  0);

    private final DiamondSwordPlugin plugin;

    public KillListener(DiamondSwordPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();
        Player killer = dead.getKiller();
        if (killer == null) return;
        ItemStack held = killer.getInventory().getItemInMainHand();
        if (!SwordBuilder.isCustomSword(held)) return;
        playBloodAnimation(dead.getLocation().add(0, 0.8, 0));
    }

    private void playBloodAnimation(Location loc) {

        // Phase 1 — instant blood burst
        blood(loc, BLOOD_1, 80, 1.4f, 0.55, 0.45, 0.55);
        blood(loc, BLOOD_2, 60, 1.1f, 0.45, 0.35, 0.45);
        blood(loc, BLOOD_3, 40, 0.9f, 0.35, 0.25, 0.35);
        blood(loc, BLOOD_4, 30, 0.7f, 0.25, 0.20, 0.25);
        loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 20, 0.3, 0.4, 0.3, 0.04);

        // Sounds
        loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_HURT,            1.0f, 0.35f);
        loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 0.30f);
        loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_DEATH,            0.6f, 0.50f);

        // Phase 2 — blood drips falling outward in a ring
        new BukkitRunnable() {
            int tick = 0;
            @Override
            public void run() {
                if (tick >= 14) { cancel(); return; }
                double r = 0.3 + tick * 0.12;
                for (int i = 0; i < 6; i++) {
                    double angle = tick * 0.4 + i * (Math.PI / 3);
                    Location splat = loc.clone().add(
                        Math.cos(angle) * r,
                        -tick * 0.04,
                        Math.sin(angle) * r
                    );
                    blood(splat, BLOOD_1, 3, 0.9f, 0.08, 0.08, 0.08);
                    blood(splat, BLOOD_3, 2, 0.6f, 0.06, 0.06, 0.06);
                }
                // rising dark smoke column
                Location up = loc.clone().add(0, tick * 0.1, 0);
                blood(up, BLOOD_4, 3, 0.7f, 0.15, 0.05, 0.15);
                up.getWorld().spawnParticle(Particle.SMOKE_NORMAL, up, 2, 0.1, 0.05, 0.1, 0.01);
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 2L);

        // Phase 3 — final blood explosion
        new BukkitRunnable() {
            @Override
            public void run() {
                blood(loc, BLOOD_1, 70, 1.5f, 0.65, 0.55, 0.65);
                blood(loc, BLOOD_2, 50, 1.2f, 0.50, 0.45, 0.50);
                blood(loc, BLOOD_3, 35, 1.0f, 0.40, 0.35, 0.40);
                blood(loc, BLOOD_4, 25, 0.8f, 0.30, 0.25, 0.30);
                loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 18, 0.4, 0.5, 0.4, 0.03);
                loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.3f, 1.7f);
            }
        }.runTaskLater(plugin, 16L);
    }

    private void blood(Location loc, Color color, int count, float size,
                       double sx, double sy, double sz) {
        Particle.DustOptions dust = new Particle.DustOptions(color, size);
        loc.getWorld().spawnParticle(Particle.REDSTONE, loc, count, sx, sy, sz, dust);
    }
}
