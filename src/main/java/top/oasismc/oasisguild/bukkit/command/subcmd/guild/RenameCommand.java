package top.oasismc.oasisguild.bukkit.command.subcmd.guild;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.oasismc.oasisguild.bukkit.api.command.ISubCommand;
import top.oasismc.oasisguild.bukkit.command.subcmd.AbstractSubCommand;
import top.oasismc.oasisguild.bukkit.core.GuildManager;

import java.util.List;

import static top.oasismc.oasisguild.bukkit.api.job.Jobs.VICE_LEADER;
import static top.oasismc.oasisguild.bukkit.core.MsgSender.sendMsg;
import static top.oasismc.oasisguild.bukkit.data.DataManager.getDataManager;

public final class RenameCommand extends AbstractSubCommand {

    public static final ISubCommand INSTANCE = new RenameCommand();

    private RenameCommand() {
        super("rename", null);
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        String gName = getDataManager().getGuildNameByPlayer(sender.getName());
        if (gName == null) {
            sendMsg(sender, "command.rename.notJoinGuild");
            return true;
        }
        int job = getDataManager().getPlayerJob(gName, sender.getName());
        if (job < VICE_LEADER) {
            sendMsg(sender, "command.chunk.notLeader");
            return true;
        }
        if (args.size() < 1) {
            sendMsg(sender, "command.rename.needNewName");
            return true;
        }

        switch (GuildManager.guildRename(gName, args.get(0), (Player) sender)) {
            case -1:
                sendMsg(sender, "command.rename.sameName");
                break;
            case 0:
                sendMsg(sender, "command.rename.success");
                break;
            case -2:
                sendMsg(sender, "command.rename.nameTooLong");
                break;
        }
        return true;
    }

}
