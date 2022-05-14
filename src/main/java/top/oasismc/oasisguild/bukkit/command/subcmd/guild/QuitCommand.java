package top.oasismc.oasisguild.bukkit.command.subcmd.guild;

import org.bukkit.command.CommandSender;
import top.oasismc.oasisguild.bukkit.api.command.ISubCommand;
import top.oasismc.oasisguild.bukkit.api.event.player.PlayerQuitGuildEvent;
import top.oasismc.oasisguild.bukkit.command.subcmd.AbstractSubCommand;
import top.oasismc.oasisguild.bukkit.core.MsgSender;

import java.util.List;

import static top.oasismc.oasisguild.bukkit.core.GuildManager.playerQuitGuild;
import static top.oasismc.oasisguild.bukkit.core.MsgSender.sendMsg;
import static top.oasismc.oasisguild.bukkit.data.DataManager.getDataManager;

public final class QuitCommand extends AbstractSubCommand {

    public static final ISubCommand INSTANCE = new QuitCommand();

    private QuitCommand() {
        super("quit", null);
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        String gName = getDataManager().getGuildNameByPlayer(sender.getName());
        if (gName == null) {
            sendMsg(sender, "command.quit.notJoinGuild");
            return true;
        }
        if (getDataManager().getPlayerJob(gName, sender.getName()) >= 250) {
            sendMsg(sender, "command.quit.isLeader");
            return true;
        }
        playerQuitGuild(gName, sender.getName(), PlayerQuitGuildEvent.QuitReason.QUIT);
        MsgSender.sendMsg4replaceGuild(sender, "command.quit.success", getDataManager().getGuildByName(gName));
        return true;
    }

}
