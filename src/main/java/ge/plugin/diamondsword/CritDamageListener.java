package ge.plugin.diamondsword;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class CritDamageListener implements Listener {

    // Flat bonus added on every critical hit
    private static final double CRIT_BONUS = 0.8;

    private final DiamondSwordPlugin plugin;

    public CritDamageListener(DiamondSwordPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {

        // Damager must be a Player
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();

        // Player must be holding our custom sword
        ItemStack held = player.getInventory().getItemInMainHand();
        if (!SwordBuilder.isCustomSword(held)) return;

        // Check if this hit is a critical hit:
        //   - player is falling (velocity Y < 0)
        //   - player is NOT on the ground  (isFalling)
        //   - player is NOT sprinting      (sprinting crits are knock-back hits, not real crits)
        //   - player is NOT blind          (blind = no crit in some implementations)
        if (isCriticalHit(player)) {
            // Add flat +0.8 to final damage (does NOT multiply, just adds)
            double currentDamage = event.getDamage();
            event.setDamage(currentDamage + CRIT_BONUS);
        }
    }

    /**
     * Mirrors Minecraft's vanilla crit detection logic.
     * A crit happens when:
     *   1. Player is in the air (falling / jumping)
     *   2. Player is NOT sprinting
     *   3. Player is NOT blind
     *   4. Player is NOT on a ladder / in water / in lava
     */
    private boolean isCriticalHit(Player player) {
        if (player.isOnGround()) return false;
        if (player.isSprinting()) return false;
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS)) return false;
        if (player.isInsideVehicle()) return false;

        // Player's Y velocity must be negative (falling down)
        double velocityY = player.getVelocity().getY();
        if (velocityY >= 0) return false;

        return true;
    }
}
