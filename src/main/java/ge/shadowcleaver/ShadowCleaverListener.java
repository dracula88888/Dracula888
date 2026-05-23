package ge.shadowcleaver;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ShadowCleaverListener implements Listener {

    private final ShadowCleaver plugin;

    private final Map<UUID, Integer> hitCountMap = new HashMap<>();
    private final Set<Location> processingBlocks = new HashSet<>();

    private static final Set<Material> LOGS = EnumSet.of(
            Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG,
            Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
            Material.MANGROVE_LOG, Material.CHERRY_LOG, Material.BAMBOO_BLOCK,
            Material.CRIMSON_STEM, Material.WARPED_STEM,
            Material.STRIPPED_OAK_LOG, Material.STRIPPED_SPRUCE_LOG,
            Material.STRIPPED_BIRCH_LOG, Material.STRIPPED_JUNGLE_LOG,
            Material.STRIPPED_ACACIA_LOG, Material.STRIPPED_DARK_OAK_LOG,
            Material.STRIPPED_MANGROVE_LOG, Material.STRIPPED_CHERRY_LOG,
            Material.STRIPPED_CRIMSON_STEM, Material.STRIPPED_WARPED_STEM
    );

    private static final int BLINDNESS_DURATION_TICKS = 16;

    private static final List<String> PASSIVE_LORE = Arrays.asList(
            "\u00A78\u00A7m--------------------",
            "\u00A75\u25C6 Passives:",
            "\u00A78\u25B8 \u00A75Every \u00A7d4th hit\u00A75: \u00A78Blindness \u00A7dI \u00A78(\u00A750.8s\u00A78)",
            "\u00A78\u25B8 \u00A75Vain Miner\u00A78: \u00A75Fells entire tree",
            "\u00A78\u00A7m--------------------"
    );

    public ShadowCleaverListener(ShadowCleaver plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int heldSlot = player.getInventory().getHeldItemSlot();

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (!isShadowCleaver(item)) continue;

            if (i == heldSlot) {
                removeLore(item);
            } else {
                addLore(item);
            }
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        ItemStack prev = player.getInventory().getItem(event.getPreviousSlot());
        if (isShadowCleaver(prev)) addLore(prev);

        ItemStack next = player.getInventory().getItem(event.getNewSlot());
        if (isShadowCleaver(next)) removeLore(next);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        ItemStack held = player.getInventory().getItemInMainHand();
        if (!isShadowCleaver(held)) return;

        if (!(event.getEntity() instanceof LivingEntity target)) return;

        UUID uuid = player.getUniqueId();
        int hits = hitCountMap.getOrDefault(uuid, 0) + 1;

        if (hits >= 4) {
            target.addPotionEffect(new PotionEffect(
                    PotionEffectType.BLINDNESS, BLINDNESS_DURATION_TICKS,
                    0, false, true, true
            ));
            hitCountMap.put(uuid, 0);
            player.playSound(player.getLocation(), Sound.ENTITY_WITCH_DRINK, 0.8f, 0.5f);
        } else {
            hitCountMap.put(uuid, hits);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        if (!isShadowCleaver(held)) return;

        Block broken = event.getBlock();
        Material brokenType = broken.getType();

        if (!LOGS.contains(brokenType)) return;
        if (processingBlocks.contains(broken.getLocation())) return;

        Set<Block> vein = findVein(broken, brokenType, 256);
        vein.remove(broken);

        for (Block block : vein) {
            processingBlocks.add(block.getLocation());
            block.breakNaturally(held);
            processingBlocks.remove(block.getLocation());

            ItemMeta meta = held.getItemMeta();
            if (meta instanceof org.bukkit.inventory.meta.Damageable damageable) {
                int newDmg = damageable.getDamage() + 1;
                if (newDmg >= held.getType().getMaxDurability()) {
                    player.getInventory().setItemInMainHand(null);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                    return;
                }
                damageable.setDamage(newDmg);
                held.setItemMeta(meta);
            }
        }
    }

    // ─────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────

    private void addLore(ItemStack item) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        List<String> existing = meta.getLore();
        if (existing != null && existing.equals(PASSIVE_LORE)) return;
        meta.setLore(PASSIVE_LORE);
        item.setItemMeta(meta);
    }

    private void removeLore(ItemStack item) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.setLore(Collections.emptyList());
        item.setItemMeta(meta);
    }

    private Set<Block> findVein(Block start, Material type, int maxSize) {
        Set<Block> visited = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);

        int[] offsets = {-1, 0, 1};
        while (!queue.isEmpty() && visited.size() < maxSize) {
            Block current = queue.poll();
            for (int dx : offsets)
                for (int dy : offsets)
                    for (int dz : offsets) {
                        if (Math.abs(dx) + Math.abs(dy) + Math.abs(dz) != 1) continue;
                        Block nb = current.getRelative(dx, dy, dz);
                        if (!visited.contains(nb) && nb.getType() == type) {
                            visited.add(nb);
                            queue.add(nb);
                        }
                    }
        }
        return visited;
    }

    /**
     * Shadow Cleaver-ის შემოწმება.
     * getDisplayName() კითხულობს legacy §-კოდებს,
     * stripColor()-ით ვშლით ფერებს და ვადარებთ წმინდა სახელს.
     */
    private boolean isShadowCleaver(ItemStack item) {
        if (item == null) return false;
        if (item.getType() != Material.DIAMOND_AXE) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        // hasDisplayName() + strip color codes → robust match
        if (meta.hasDisplayName()) {
            String plain = ChatColor.stripColor(meta.getDisplayName());
            if (plain != null && plain.contains("Shadow Cleaver")) return true;
        }

        // fallback: check display name raw (covers §-prefixed names)
        String raw = meta.getDisplayName();
        return raw != null && raw.contains("Shadow Cleaver");
    }
}
