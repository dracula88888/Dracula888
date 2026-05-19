package com.devilswrath.plugin;

import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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

    // Tracks players currently on cooldown (8 seconds after ability ends)
    private final Map<UUID, Integer> cooldownTasks = new HashMap<>();

    // Expose for particle task to check ability state
    public static final Map<UUID, Boolean> abilityActive = new HashMap<>();

    // Tracks remaining cooldown ticks per player (for action bar display)
    public static final Map<UUID, Integer> cooldownRemaining = new HashMap<>();

    // Tracks the action bar display task per player
    private final Map<UUID, Integer> actionBarTasks = new HashMap<>();

    // How long the ability stays active: 8 seconds = 160 ticks
    private static final long ABILITY_DURATION_TICKS = 160L;

    // Cooldown after ability ends: 8 seconds = 160 ticks
    private static final long COOLDOWN_TICKS = 160L;
    private static final int COOLDOWN_SECONDS = 8;

    // Radius for ability smoke cloud: 8 blocks
    private static final int ABILITY_RADIUS = 8;

    public DevilsWrathListener(DevilsWrathPlugin plugin) {
        this.plugin = plugin;
    }

    // ──────────────────────────────────────────────
    // SNEAK → Activate ability
    // ──────────────────────────────────────────────
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!event.isSneaking()) return;

        // Only trigger if holding DevilsWrath in MAIN HAND
        ItemStack held = player.getInventory().getItemInMainHand();
        if (!DevilsWrathSword.isDevilsWrath(held)) return;

        UUID uuid = player.getUniqueId();

        if (abilityActive.getOrDefault(uuid, false)) return;

        if (cooldownTasks.containsKey(uuid)) {
            int rem = cooldownRemaining.getOrDefault(uuid, 0);
            player.sendMessage(ChatColor.DARK_GRAY + "Devils Wrath is recovering... " + rem + "s remaining.");
            return;
        }

        activateAbility(player);
    }

    // ──────────────────────────────────────────────
    // HIT ENTITY → Apply blindness + slowness + hit particles
    // ──────────────────────────────────────────────
    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player attacker = (Player) event.getDamager();

        // Only main hand
        ItemStack held = attacker.getInventory().getItemInMainHand();
        if (!DevilsWrathSword.isDevilsWrath(held)) return;

        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity victim = (LivingEntity) event.getEntity();

        spawnHitParticles(victim.getLocation().add(0, 1, 0));

        if (abilityActive.getOrDefault(attacker.getUniqueId(), false)) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0, false, true));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 0, false, true));
        }
    }

    // ──────────────────────────────────────────────
    // CLEANUP on logout
    // ──────────────────────────────────────────────
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        Integer activeTaskId = activeAbilities.remove(uuid);
        if (activeTaskId != null) Bukkit.getScheduler().cancelTask(activeTaskId);

        Integer cooldownTaskId = cooldownTasks.remove(uuid);
        if (cooldownTaskId != null) Bukkit.getScheduler().cancelTask(cooldownTaskId);

        Integer actionBarTaskId = actionBarTasks.remove(uuid);
        if (actionBarTaskId != null) Bukkit.getScheduler().cancelTask(actionBarTaskId);

        abilityActive.remove(uuid);
        cooldownRemaining.remove(uuid);
    }

    // ──────────────────────────────────────────────
    // Internal helpers
    // ──────────────────────────────────────────────

    private void activateAbility(Player player) {
        UUID uuid = player.getUniqueId();
        abilityActive.put(uuid, true);

        player.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "⚔ Devils Wrath awakened! ⚔");
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.4f, 0.5f);
        spawnActivationBurst(player);

        // Spawn dark smoke cloud in 8x8 radius around player, once per second for 8 seconds
        spawnAbilityAreaSmoke(player);

        // Schedule ability end after 8 seconds
        int taskId = new BukkitRunnable() {
            @Override
            public void run() {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && p.isOnline()) {
                    deactivateAbility(p);
                } else {
                    abilityActive.remove(uuid);
                    activeAbilities.remove(uuid);
                }
            }
        }.runTaskLater(plugin, ABILITY_DURATION_TICKS).getTaskId();

        activeAbilities.put(uuid, taskId);
    }

    private void deactivateAbility(Player player) {
        UUID uuid = player.getUniqueId();
        abilityActive.put(uuid, false);
        activeAbilities.remove(uuid);

        player.sendMessage(ChatColor.GRAY + "Devils Wrath fades... recovering for " + COOLDOWN_SECONDS + " seconds.");
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.3f, 1.5f);

        // Initialize cooldown countdown
        cooldownRemaining.put(uuid, COOLDOWN_SECONDS);

        // Start action bar cooldown display (updates every second)
        int actionBarTaskId = new BukkitRunnable() {
            int secondsLeft = COOLDOWN_SECONDS;

            @Override
            public void run() {
                Player p = Bukkit.getPlayer(uuid);
                if (p == null || !p.isOnline()) {
                    cancel();
                    actionBarTasks.remove(uuid);
                    return;
                }

                // Check if still holding DevilsWrath in main hand
                ItemStack held = p.getInventory().getItemInMainHand();
                if (!DevilsWrathSword.isDevilsWrath(held)) {
                    // Don't show bar if not holding sword
                    secondsLeft--;
                    cooldownRemaining.put(uuid, secondsLeft);
                    if (secondsLeft <= 0) {
                        cancel();
                        actionBarTasks.remove(uuid);
                        cooldownRemaining.remove(uuid);
                    }
                    return;
                }

                if (secondsLeft <= 0) {
                    p.sendActionBar(ChatColor.GREEN + "⚔ Devils Wrath Ready! ⚔");
                    cancel();
                    actionBarTasks.remove(uuid);
                    cooldownRemaining.remove(uuid);
                    return;
                }

                // Build cooldown bar (10 segments)
                int totalSegments = 10;
                int filledSegments = (int) Math.ceil((double) secondsLeft / COOLDOWN_SECONDS * totalSegments);
                StringBuilder bar = new StringBuilder();
                bar.append(ChatColor.DARK_RED).append("⚔ ");
                for (int i = 0; i < totalSegments; i++) {
                    if (i < filledSegments) {
                        bar.append(ChatColor.DARK_RED).append("█");
                    } else {
                        bar.append(ChatColor.DARK_GRAY).append("░");
                    }
                }
                bar.append(ChatColor.DARK_RED).append(" ").append(secondsLeft).append("s");

                p.sendActionBar(bar.toString());

                cooldownRemaining.put(uuid, secondsLeft);
                secondsLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L).getTaskId();

        actionBarTasks.put(uuid, actionBarTaskId);

        // Start 8-second cooldown
        int cooldownTaskId = new BukkitRunnable() {
            @Override
            public void run() {
                cooldownTasks.remove(uuid);
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && p.isOnline()) {
                    p.sendMessage(ChatColor.DARK_RED + "⚔ Devils Wrath is ready again. ⚔");
                    p.playSound(p.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.4f, 0.8f);
                }
            }
        }.runTaskLater(plugin, COOLDOWN_TICKS).getTaskId();

        cooldownTasks.put(uuid, cooldownTaskId);
    }

    // ──────────────────────────────────────────────
    // Ability area: spawn dark smoke in 8x8 radius once per second for 8 sec
    // ──────────────────────────────────────────────
    private void spawnAbilityAreaSmoke(Player player) {
        UUID uuid = player.getUniqueId();

        new BukkitRunnable() {
            int elapsed = 0;

            @Override
            public void run() {
                // Stop if ability is no longer active or player offline
                if (elapsed >= COOLDOWN_SECONDS) {
                    cancel();
                    return;
                }
                Player p = Bukkit.getPlayer(uuid);
                if (p == null || !p.isOnline()) {
                    cancel();
                    return;
                }
                if (!abilityActive.getOrDefault(uuid, false)) {
                    cancel();
                    return;
                }

                Location center = p.getLocation();
                World world = p.getWorld();

                // Spawn SQUID_INK particles randomly in 8x8 block radius, at ground level ± 2
                for (int i = 0; i < 60; i++) {
                    double rx = (Math.random() * 2 - 1) * ABILITY_RADIUS;
                    double ry = Math.random() * 2.0;
                    double rz = (Math.random() * 2 - 1) * ABILITY_RADIUS;
                    Location spawnLoc = center.clone().add(rx, ry, rz);
                    // Spawn particle then schedule removal (particles auto-fade, just spawn briefly)
                    world.spawnParticle(Particle.SQUID_INK, spawnLoc, 1, 0.1, 0.3, 0.1, 0.0);
                    world.spawnParticle(Particle.LARGE_SMOKE, spawnLoc, 1, 0.15, 0.3, 0.15, 0.01);
                }

                elapsed++;
            }
        }.runTaskTimer(plugin, 0L, 20L); // every second
    }

    // ──────────────────────────────────────────────
    // Particle helpers
    // ──────────────────────────────────────────────

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

    private void spawnHitParticles(Location loc) {
        loc.getWorld().spawnParticle(Particle.SQUID_INK, loc, 15, 0.3, 0.3, 0.3, 0.08);
        loc.getWorld().spawnParticle(Particle.LARGE_SMOKE, loc, 8, 0.2, 0.2, 0.2, 0.05);
    }
}
