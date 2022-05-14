package top.oasismc.oasisguild.bukkit.command.subcmd.admin;

import org.bukkit.command.CommandSender;
import top.oasismc.oasisguild.bukkit.api.command.ISubCommand;
import top.oasismc.oasisguild.bukkit.api.objects.IGuild;
import top.oasismc.oasisguild.bukkit.command.subcmd.AbstractSubCommand;
import top.oasismc.oasisguild.bukkit.data.DataManager;

import java.util.Collections;
import java.util.List;

import static top.oasismc.oasisguild.bukkit.core.MsgSender.sendMsg;
import static top.oasismc.oasisguild.bukkit.data.DataManager.getDataManager;

public final class SetLevelCommand extends AbstractSubCommand {

    public static final ISubCommand INSTANCE = new SetLevelCommand();

    private SetLevelCommand() {
        super("setLevel", null);
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
        int level;
        try {
            level = Integer.parseInt(args.get(1));
        } catch (Exception e) {
            level = guild.getGuildLevel();
        }
        getDataManager().getGuildDao().setGuildLevel(guild.getGuildName(), level);
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
