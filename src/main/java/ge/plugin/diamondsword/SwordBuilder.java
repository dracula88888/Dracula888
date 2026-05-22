package ge.plugin.diamondsword;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class SwordBuilder {

    public static final String SWORD_TAG = "custom_diamond_sword";

    public static ItemStack build() {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD, 1);
        ItemMeta meta = sword.getItemMeta();
        if (meta == null) return sword;

        // სახელი: სრულად DARK_RED (blood)
        meta.setDisplayName(
            ChatColor.DARK_RED + "" + ChatColor.BOLD + "⚔ Blood Fury"
        );

        meta.setLore(Arrays.asList(
            ChatColor.DARK_RED + "━━━━━━━━━━━━━━━━━━━━",
            ChatColor.DARK_RED + "⚔ Enchantments",
            ChatColor.DARK_RED + "━━━━━━━━━━━━━━━━━━━━",
            // enchants — WHITE
            ChatColor.WHITE + " ⚡ Sharpness V",
            ChatColor.WHITE + " ⚡ Sweeping Edge III",
            ChatColor.WHITE + " ⚡ Mending",
            ChatColor.DARK_RED + "━━━━━━━━━━━━━━━━━━━━",
            ChatColor.DARK_RED + "☠ Passives",
            ChatColor.DARK_RED + "━━━━━━━━━━━━━━━━━━━━",
            // passives — DARK_RED (blood)
            ChatColor.DARK_RED + " ✦ Looting IV",
            ChatColor.DARK_RED + " ✦ Unbreakable",
            ChatColor.DARK_RED + " ✦ Critical Hit: +0.8 Damage",
            ChatColor.DARK_RED + " ✦ Kill: Blood Animation",
            ChatColor.DARK_RED + "━━━━━━━━━━━━━━━━━━━━"
        ));

        meta.addEnchant(Enchantment.DAMAGE_ALL,      5, true);
        meta.addEnchant(Enchantment.SWEEPING_EDGE,   3, true);
        meta.addEnchant(Enchantment.MENDING,         1, true);
        meta.addEnchant(Enchantment.LOOT_BONUS_MOBS, 4, true);

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        sword.setItemMeta(meta);
        sword = TagHelper.setTag(sword, SWORD_TAG, "true");
        return sword;
    }

    public static boolean isCustomSword(ItemStack item) {
        if (item == null || item.getType() != Material.DIAMOND_SWORD) return false;
        return "true".equals(TagHelper.getTag(item, SWORD_TAG));
    }
}
