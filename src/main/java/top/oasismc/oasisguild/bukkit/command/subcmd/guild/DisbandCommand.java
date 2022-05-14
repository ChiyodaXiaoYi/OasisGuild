package top.oasismc.oasisguild.bukkit.command.subcmd.guild;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.oasismc.oasisguild.bukkit.api.command.ISubCommand;
import top.oasismc.oasisguild.bukkit.command.subcmd.AbstractSubCommand;
import top.oasismc.oasisguild.bukkit.core.GuildManager;

import java.util.List;

import static top.oasismc.oasisguild.bukkit.core.MsgSender.sendMsg;
import static top.oasismc.oasisguild.bukkit.data.DataManager.getDataManager;

public final class DisbandCommand extends AbstractSubCommand {

    public static final ISubCommand INSTANCE = new DisbandCommand();

    private DisbandCommand() {
        super("disband", null);
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        String guildName = getDataManager().getGuildNameByPlayer(sender.getName());
        int code = GuildManager.disbandGuild((Player) sender, guildName);
        switch (code) {
            case -1:
                sendMsg(sender, "command.disband.notJoinGuild");
                break;
            case -2:
                sendMsg(sender, "command.disband.notLeader");
                break;
            case 0:
                sendMsg(sender, "command.disband.success");
                break;
            case 1:
                sendMsg(sender, "command.disband.confirm");
                break;
        }
        return true;
    }

}
