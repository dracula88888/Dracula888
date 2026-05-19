package com.devilswrath.plugin;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DevilsWrathListener implements Listener {

    private final DevilsWrathPlugin plugin;

    // Tracks the task ID for the active ability expiry (8-second window)
    private final Map<UUID, Integer> activeAbilities = new HashMap<>();

    // Tracks players currently on cooldown (48 seconds after ability ends)
    private final Map<UUID, Integer> cooldownTasks = new HashMap<>();

    // Expose for particle task to check ability state
    public static final Map<UUID, Boolean> abilityActive = new HashMap<>();

    // How long the ability stays active: 8 seconds = 160 ticks
    private static final long ABILITY_DURATION_TICKS = 160L;

    // Cooldown after ability ends before it can be used again: 48 seconds = 960 ticks
    private static final long COOLDOWN_TICKS = 960L;

    public DevilsWrathListener(DevilsWrathPlugin plugin) {
        this.plugin = plugin;
    }

    // ──────────────────────────────────────────────
    // SNEAK → Activate ability (one-time use per cooldown)
    // ──────────────────────────────────────────────
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!event.isSneaking()) return;

        ItemStack held = player.getInventory().getItemInMainHand();
        if (!DevilsWrathSword.isDevilsWrath(held)) return;

        UUID uuid = player.getUniqueId();

        // If already active, ignore
        if (abilityActive.getOrDefault(uuid, false)) return;

        // If on cooldown, notify and block
        if (cooldownTasks.containsKey(uuid)) {
            player.sendMessage(ChatColor.DARK_GRAY + "Devils Wrath is recovering... wait for it to awaken again.");
            return;
        }

        activateAbility(player);
    }

    // ──────────────────────────────────────────────
    // HIT PLAYER → Apply blindness + slowness + sword particles
    // ──────────────────────────────────────────────
    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player attacker = (Player) event.getDamager();

        ItemStack held = attacker.getInventory().getItemInMainHand();
        if (!DevilsWrathSword.isDevilsWrath(held)) return;

        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity victim = (LivingEntity) event.getEntity();

        // Spawn black particles on the victim (sword hit effect)
        spawnHitParticles(victim.getLocation().add(0, 1, 0));

        // Only apply blindness/slowness when ability is active
        if (abilityActive.getOrDefault(attacker.getUniqueId(), false)) {
            // Blindness 4 seconds (duration in ticks = 4 * 20 = 80)
            victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0, false, true));
            // Slowness 1 for 4 seconds
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 0, false, true));
            // NOTE: timer is NOT reset on hit — ability is one-time use only
        }
    }

    // ──────────────────────────────────────────────
    // Internal helpers
    // ──────────────────────────────────────────────

    private void activateAbility(Player player) {
        UUID uuid = player.getUniqueId();
        abilityActive.put(uuid, true);

        // Visual/sound feedback for activation
        player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "⚔ Devils Wrath awakened! ⚔");
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.4f, 0.5f);
        spawnActivationBurst(player);

        // Schedule the 8-second ability window — no resets on hit
        int taskId = new BukkitRunnable() {
            @Override
            public void run() {
                deactivateAbility(player);
            }
        }.runTaskLater(plugin, ABILITY_DURATION_TICKS).getTaskId();

        activeAbilities.put(uuid, taskId);
    }

    private void deactivateAbility(Player player) {
        UUID uuid = player.getUniqueId();
        abilityActive.put(uuid, false);
        activeAbilities.remove(uuid);

        player.sendMessage(ChatColor.GRAY + "Devils Wrath fades... recovering for 48 seconds.");
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.3f, 1.5f);

        // Start 48-second cooldown
        int cooldownTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                cooldownTasks.remove(uuid);
                // Only notify if the player is still online
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && p.isOnline()) {
                    p.sendMessage(ChatColor.DARK_RED + "⚔ Devils Wrath is ready again. ⚔");
                    p.playSound(p.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.4f, 0.8f);
                }
            }
        }.runTaskLater(plugin, COOLDOWN_TICKS).getTaskId(); // 48 seconds = 960 ticks

        cooldownTasks.put(uuid, cooldownTaskId);
    }

    // ──────────────────────────────────────────────
    // Particle helpers
    // ──────────────────────────────────────────────

    /**
     * Burst of particles when ability is first activated (8-block radius scan)
     */
    private void spawnActivationBurst(Player player) {
        Location center = player.getLocation().add(0, 1, 0);
        World world = player.getWorld();

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 10) {
                    cancel();
                    return;
                }
                // Expand ring outward up to 8 blocks
                double radius = (ticks + 1) * 0.8;
                int points = 24;
                for (int i = 0; i < points; i++) {
                    double angle = (2 * Math.PI / points) * i;
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;
                    world.spawnParticle(Particle.SQUID_INK, center.clone().add(x, 0, z), 3, 0.1, 0.1, 0.1, 0.01);
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    /**
     * Black particles on hit
     */
    private void spawnHitParticles(Location loc) {
        loc.getWorld().spawnParticle(Particle.SQUID_INK, loc, 15, 0.3, 0.3, 0.3, 0.08);
        loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 8, 0.2, 0.2, 0.2, 0.05);
    }
}
