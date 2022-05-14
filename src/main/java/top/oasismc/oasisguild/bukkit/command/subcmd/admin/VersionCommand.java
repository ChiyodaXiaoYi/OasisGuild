package top.oasismc.oasisguild.bukkit.command.subcmd.admin;

import org.bukkit.command.CommandSender;
import top.oasismc.oasisguild.bukkit.api.command.ISubCommand;
import top.oasismc.oasisguild.bukkit.command.subcmd.AbstractSubCommand;

import java.util.List;

import static top.oasismc.oasisguild.bukkit.core.MsgSender.sendMsg;

public final class VersionCommand extends AbstractSubCommand {

    public static final ISubCommand INSTANCE = new VersionCommand();

    public VersionCommand() {
        super("version", null);
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (!sender.hasPermission("oasis.guild.admin")) {
            sendMsg(sender, "noPerm");
            return true;
        }
        sendMsg(sender, "command.version");
        return true;
    }

}
