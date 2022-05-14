package top.oasismc.oasisguild.bukkit.command.subcmd.admin;

import org.bukkit.command.CommandSender;
import top.oasismc.oasisguild.bukkit.api.command.ISubCommand;
import top.oasismc.oasisguild.bukkit.api.objects.IGuild;
import top.oasismc.oasisguild.bukkit.command.subcmd.AbstractSubCommand;
import top.oasismc.oasisguild.bukkit.data.DataManager;

import java.util.Collections;
import java.util.List;

import static top.oasismc.oasisguild.bukkit.OasisGuild.getPlugin;
import static top.oasismc.oasisguild.bukkit.core.GuildManager.checkGuildName;
import static top.oasismc.oasisguild.bukkit.core.MsgSender.sendMsg;
import static top.oasismc.oasisguild.bukkit.data.DataManager.getDataManager;

public final class SetNameCommand extends AbstractSubCommand {

    public static final ISubCommand INSTANCE = new SetNameCommand();

    private SetNameCommand() {
        super("setName", null);
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (args.size() < 1) {
            sendMsg(sender, "adminCommand.notGuild");
            return true;
        }
        if (args.size() < 2) {
            sendMsg(sender, "adminCommand.missingParam");
            return true;
        }
        IGuild guild = getDataManager().getGuildByName(args.get(0));
        if (guild == null) {
            sendMsg(sender, "adminCommand.notGuild");
            return true;
        }
        String newName = args.get(1);
        switch (checkGuildName(newName)) {
            case -1:
                sendMsg(sender, "command.rename.sameName");
                return true;
            case -2:
                sendMsg(sender, "command.rename.nameTooLong");
                return true;
            case 1:
                String defaultColor = getPlugin().getConfig().getString("guildSettings.name.defaultColor", "&f");
                newName = defaultColor + newName;
                break;
        }
        getDataManager().getGuildDao().guildRename(args.get(0), newName);
        sendMsg(sender, "adminCommand.success");
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
