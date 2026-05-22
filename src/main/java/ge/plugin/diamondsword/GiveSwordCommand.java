package ge.plugin.diamondsword;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveSwordCommand implements CommandExecutor {

    private final DiamondSwordPlugin plugin;

    public GiveSwordCommand(DiamondSwordPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // /givesword [player]
        Player target;

        if (args.length == 0) {
            // Give to self
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Console-დან გამოყენებისას მიუთითე player სახელი: /givesword <player>");
                return true;
            }
            target = (Player) sender;
        } else {
            // Give to named player
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + args[0] + "' ვერ მოიძებნა!");
                return true;
            }
        }

        // Build and give the sword
        ItemStack sword = SwordBuilder.build();
        target.getInventory().addItem(sword);

        target.sendMessage(ChatColor.AQUA + "⚔ " + ChatColor.GREEN + "მიიღე უტეხი Diamond Sword!");
        if (!sender.equals(target)) {
            sender.sendMessage(ChatColor.GREEN + "მახვილი გადაეცა: " + target.getName());
        }

        return true;
    }
}
