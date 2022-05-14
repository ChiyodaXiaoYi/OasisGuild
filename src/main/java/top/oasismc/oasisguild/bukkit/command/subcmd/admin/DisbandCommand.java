package top.oasismc.oasisguild.bukkit.command.subcmd.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.oasismc.oasisguild.bukkit.api.command.ISubCommand;
import top.oasismc.oasisguild.bukkit.command.subcmd.AbstractSubCommand;
import top.oasismc.oasisguild.bukkit.core.GuildManager;
import top.oasismc.oasisguild.bukkit.data.DataManager;

import java.util.Collections;
import java.util.List;

import static top.oasismc.oasisguild.bukkit.core.MsgSender.sendMsg;

public final class DisbandCommand extends AbstractSubCommand {

    public static final ISubCommand INSTANCE = new DisbandCommand();

    private DisbandCommand() {
        super("disband", null);
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (args.size() < 1) {
            sendMsg(sender, "adminCommand.notGuild");
            return true;
        }
        int code = GuildManager.disbandGuild((Player) sender, args.get(0));
        if (code == -1) {
            sendMsg(sender, "adminCommand.notGuild");
        } else if (code == 0){
            sendMsg(sender, "adminCommand.success");
        } else if (code == 1) {
            sendMsg(sender, "command.disband.confirm");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, List<String> args) {
        if (args.size() <= 1) {
            List<String> returnList = DataManager.getDataManager().getGuildNameList();
            returnList.removeIf(str -> !str.startsWith(args.get(0)));
            return returnList;
        } else {
            return Collections.singletonList("");
        }
    }

}
