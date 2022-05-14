package top.oasismc.oasisguild.bukkit.command.subcmd.guild;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.oasismc.oasisguild.bukkit.api.command.ISubCommand;
import top.oasismc.oasisguild.bukkit.command.subcmd.AbstractSubCommand;
import top.oasismc.oasisguild.bukkit.core.GuildManager;
import top.oasismc.oasisguild.bukkit.data.DataManager;

import java.util.Collections;
import java.util.List;

import static top.oasismc.oasisguild.bukkit.core.MsgSender.sendMsg;

public final class ApplyCommand extends AbstractSubCommand {

    public static final ISubCommand INSTANCE = new ApplyCommand();

    private ApplyCommand() {
        super("apply", null);
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (args.size() < 1) {
            sendMsg(sender, "command.missingValue.guildName");
            return true;
        }
        int success = GuildManager.playerApplyGuild(args.get(0), (Player) sender);
        if (success == 1) {
            sendMsg(sender, "command.apply.failed");
        } else if (success == 0) {
            sendMsg(sender, "command.apply.success");
        } else if (success == -1) {
            sendMsg(sender, "command.apply.hasGuild");
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
