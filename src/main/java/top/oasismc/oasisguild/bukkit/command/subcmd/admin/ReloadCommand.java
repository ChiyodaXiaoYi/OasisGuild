package top.oasismc.oasisguild.bukkit.command.subcmd.admin;

import org.bukkit.command.CommandSender;
import top.oasismc.oasisguild.bukkit.api.command.ISubCommand;
import top.oasismc.oasisguild.bukkit.command.subcmd.AbstractSubCommand;

import java.util.List;

import static top.oasismc.oasisguild.bukkit.OasisGuild.getPlugin;
import static top.oasismc.oasisguild.bukkit.core.MsgSender.getMsgSender;
import static top.oasismc.oasisguild.bukkit.core.MsgSender.sendMsg;
import static top.oasismc.oasisguild.bukkit.data.DataManager.getDataManager;
import static top.oasismc.oasisguild.bukkit.data.loader.MysqlLoader.getMysqlTool;
import static top.oasismc.oasisguild.bukkit.menu.GuildMenuManager.getMenuManager;
import static top.oasismc.oasisguild.bukkit.util.MsgCatcher.getCatcher;

public final class ReloadCommand extends AbstractSubCommand {

    public static final ISubCommand INSTANCE = new ReloadCommand();

    private ReloadCommand() {
        super("reload", null);
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (!sender.hasPermission("oasis.guild.admin")) {
            sendMsg(sender, "noPerm");
            return true;
        }
        reloadPlugin();
        sendMsg(sender, "command.reload");
        return super.onCommand(sender, args);
    }

    private void reloadPlugin() {
        getPlugin().reloadConfig();
        getMsgSender().getLangFile().reloadConfig();
        getMenuManager().getMenuFile().reloadConfig();
        getMysqlTool().setConnectDBInfo();
        getCatcher().reloadCatcher();
        getDataManager().reloadData();
    }


}
