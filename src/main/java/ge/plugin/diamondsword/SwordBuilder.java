package ge.plugin.diamondsword;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class SwordBuilder {

    // NBT tag key used to identify this custom sword
    public static final String SWORD_TAG = "custom_diamond_sword";

    /**
     * Builds the custom diamond sword:
     *  - Looting IV  (passive enchant)
     *  - Unbreakable  (ანუ Unbreaking III-ის მაგივრად მთლიანად unbreakable)
     *  - +0.8 damage on every crit (handled by CritDamageListener)
     */
    public static ItemStack build() {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD, 1);
        ItemMeta meta = sword.getItemMeta();

        if (meta == null) return sword;

        // --- Display name ---
        // Void color = DARK_PURPLE (the darkest purple in Minecraft — "void"-ის ფერი)
        meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "⚔ Critical Fury");

        // --- Lore ---
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━",
                ChatColor.GOLD + "✦ " + ChatColor.YELLOW + "Looting IV",
                ChatColor.GOLD + "✦ " + ChatColor.GREEN + "Unbreakable",
                ChatColor.GOLD + "✦ " + ChatColor.RED + "Critical Hit: " + ChatColor.WHITE + "+0.8 Damage",
                ChatColor.GRAY + "━━━━━━━━━━━━━━━━━━━━",
                ChatColor.DARK_GRAY + "Crits stack with base damage.",
                ChatColor.DARK_GRAY + "Crit bonus: +0.8 (flat, no stack)"
        ));

        // --- Enchantments ---
        // Looting IV  (Looting max is III in vanilla; we use unsafe to force IV)
        meta.addEnchant(Enchantment.LOOT_BONUS_MOBS, 4, true); // Looting IV (unsafe level)

        // Unbreakable flag – this makes the item truly indestructible (not Unbreaking III)
        meta.setUnbreakable(true);

        // Hide the "Unbreakable" tooltip line if you want a clean look
        // (comment out if you want it visible)
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        sword.setItemMeta(meta);

        // Store a persistent tag so the listener can identify this sword
        sword = TagHelper.setTag(sword, SWORD_TAG, "true");

        return sword;
    }

    /**
     * Returns true if the given ItemStack is our custom sword.
     */
    public static boolean isCustomSword(ItemStack item) {
        if (item == null || item.getType() != Material.DIAMOND_SWORD) return false;
        return "true".equals(TagHelper.getTag(item, SWORD_TAG));
    }
}
