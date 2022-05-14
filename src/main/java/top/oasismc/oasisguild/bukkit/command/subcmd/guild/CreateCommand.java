package top.oasismc.oasisguild.bukkit.command.subcmd.guild;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.oasismc.oasisguild.bukkit.api.command.ISubCommand;
import top.oasismc.oasisguild.bukkit.command.subcmd.AbstractSubCommand;
import top.oasismc.oasisguild.bukkit.core.GuildManager;

import java.util.List;

import static top.oasismc.oasisguild.bukkit.core.MsgSender.sendMsg;

public final class CreateCommand extends AbstractSubCommand {

    public final static ISubCommand INSTANCE = new CreateCommand();

    private CreateCommand() {
        super("create", null);
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (args.size() < 1) {
            sendMsg(sender, "command.missingValue.guildName");
            return true;
        }
        if (args.size() < 2) {
            sendMsg(sender, "command.create.needDesc");
            return true;
        }
        if (GuildManager.createGuild(args.get(0), (Player) sender, args.get(1))) {
            sendMsg(sender, "command.create.success");
        }
        return true;
    }

}
