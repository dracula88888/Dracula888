package ge.plugin.diamondsword;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class TagHelper {

    /**
     * Stores a String tag on an ItemStack using PersistentDataContainer.
     * Returns the modified ItemStack.
     */
    public static ItemStack setTag(ItemStack item, String key, String value) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        NamespacedKey nsKey = new NamespacedKey(DiamondSwordPlugin.getInstance(), key);
        meta.getPersistentDataContainer().set(nsKey, PersistentDataType.STRING, value);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Reads a String tag from an ItemStack. Returns null if absent.
     */
    public static String getTag(ItemStack item, String key) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        NamespacedKey nsKey = new NamespacedKey(DiamondSwordPlugin.getInstance(), key);
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(nsKey, PersistentDataType.STRING)) {
            return pdc.get(nsKey, PersistentDataType.STRING);
        }
        return null;
    }
}
