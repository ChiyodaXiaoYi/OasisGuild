package top.oasismc.oasisguild.bukkit.command.subcmd.guild;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import top.oasismc.oasisguild.bukkit.api.command.ISubCommand;
import top.oasismc.oasisguild.bukkit.command.subcmd.AbstractSubCommand;
import top.oasismc.oasisguild.bukkit.core.MsgSender;

import java.util.List;

import static top.oasismc.oasisguild.bukkit.menu.GuildMenuManager.getMenuManager;

public final class ListCommand extends AbstractSubCommand {

    public static final ISubCommand INSTANCE = new ListCommand();

    private ListCommand() {
        super("list", null);
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player)) {
            MsgSender.sendMsg(sender, "only_player");
            return true;
        }
        Inventory inventory = getMenuManager().drawGuildListMenu((Player) sender, 0);
        ((Player) sender).openInventory(inventory);
        return true;
    }


}
