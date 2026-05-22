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

        // ─── სახელი: შავი + წითელი გრადიენტი (DARK_RED bold) ───
        meta.setDisplayName(
            ChatColor.DARK_RED + "" + ChatColor.BOLD + "⚔ " +
            ChatColor.RED + "" + ChatColor.BOLD + "C" +
            ChatColor.DARK_RED + "" + ChatColor.BOLD + "r" +
            ChatColor.RED + "" + ChatColor.BOLD + "i" +
            ChatColor.DARK_RED + "" + ChatColor.BOLD + "t" +
            ChatColor.RED + "" + ChatColor.BOLD + "i" +
            ChatColor.DARK_RED + "" + ChatColor.BOLD + "c" +
            ChatColor.RED + "" + ChatColor.BOLD + "a" +
            ChatColor.DARK_RED + "" + ChatColor.BOLD + "l" +
            ChatColor.BLACK + "" + ChatColor.BOLD + " " +
            ChatColor.RED + "" + ChatColor.BOLD + "F" +
            ChatColor.DARK_RED + "" + ChatColor.BOLD + "u" +
            ChatColor.RED + "" + ChatColor.BOLD + "r" +
            ChatColor.DARK_RED + "" + ChatColor.BOLD + "y"
        );

        // ─── Lore ───
        meta.setLore(Arrays.asList(
            ChatColor.DARK_GRAY + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
            // მთავარი enchant-ები
            ChatColor.GRAY + "⚡ " + ChatColor.YELLOW + "Sharpness V",
            ChatColor.GRAY + "⚡ " + ChatColor.YELLOW + "Sweeping Edge III",
            ChatColor.GRAY + "⚡ " + ChatColor.GREEN + "Mending",
            ChatColor.DARK_GRAY + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
            // passive-ები
            ChatColor.GRAY + "✦ " + ChatColor.GOLD + "Looting IV",
            ChatColor.GRAY + "✦ " + ChatColor.AQUA + "Unbreakable",
            ChatColor.GRAY + "✦ " + ChatColor.RED + "Critical Hit" + ChatColor.WHITE + ": +0.8 Damage",
            ChatColor.DARK_GRAY + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬",
            ChatColor.DARK_RED + "  Kill-ზე სიკვდილის ანიმაცია"
        ));

        // ─── Enchantments ───
        meta.addEnchant(Enchantment.DAMAGE_ALL,        5, true);  // Sharpness V
        meta.addEnchant(Enchantment.SWEEPING_EDGE,     3, true);  // Sweeping Edge III
        meta.addEnchant(Enchantment.MENDING,           1, true);  // Mending
        meta.addEnchant(Enchantment.LOOT_BONUS_MOBS,  4, true);  // Looting IV (unsafe)

        // Unbreakable
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        // enchant list-ი დამალე (lore-ში ვაჩვენებთ)
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
