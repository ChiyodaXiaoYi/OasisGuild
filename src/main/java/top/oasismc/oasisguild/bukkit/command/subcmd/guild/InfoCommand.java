package top.oasismc.oasisguild.bukkit.command.subcmd.guild;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import top.oasismc.oasisguild.bukkit.api.command.ISubCommand;
import top.oasismc.oasisguild.bukkit.command.subcmd.AbstractSubCommand;

import java.util.List;

import static top.oasismc.oasisguild.bukkit.core.MsgSender.sendMsg;
import static top.oasismc.oasisguild.bukkit.data.DataManager.getDataManager;
import static top.oasismc.oasisguild.bukkit.menu.GuildMenuManager.getMenuManager;

public final class InfoCommand extends AbstractSubCommand {

    public static final ISubCommand INSTANCE = new InfoCommand();

    private InfoCommand() {
        super("info", null);
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        String guildName = getDataManager().getGuildNameByPlayer(sender.getName());
        if (guildName == null) {
            sendMsg(sender, "command.info.notJoinGuild");
        } else {
            Inventory inventory = getMenuManager().drawGuildInfoMenu(guildName, (Player) sender);
            ((Player) sender).openInventory(inventory);
        }
        return true;
    }

}
