package top.oasismc.oasisguild.bukkit.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import top.oasismc.oasisguild.bukkit.api.command.ISubCommand;
import top.oasismc.oasisguild.bukkit.command.subcmd.admin.*;
import top.oasismc.oasisguild.bukkit.core.MsgSender;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public enum GuildAdminCommand implements TabExecutor {

    INSTANCE;

    private final Map<String, ISubCommand> subCommandMap;

    GuildAdminCommand() {
        subCommandMap = new ConcurrentHashMap<>();
        regDefSubCommands();
    }

    private void regDefSubCommands() {
        regSubCommand(ReloadCommand.INSTANCE);
        regSubCommand(VersionCommand.INSTANCE);
        regSubCommand(DisbandCommand.INSTANCE);
        regSubCommand(SetIconCommand.INSTANCE);
        regSubCommand(SetNameCommand.INSTANCE);
        regSubCommand(SetLevelCommand.INSTANCE);
        regSubCommand(SetMaxMemberCommand.INSTANCE);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<String> argList = Arrays.asList(args);
        if (argList.size() < 1) {
            MsgSender.sendMsg(sender, "command.missingSubCmd");
            return true;
        }
        ISubCommand subCommand = subCommandMap.get(argList.get(0));
        if (subCommand != null)
            return subCommand.onCommand(sender, argList.subList(1, argList.size()));
        else {
            MsgSender.sendMsg(sender, "command.missingSubCmd");
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> argList = Arrays.asList(args);
        if (argList.size() <= 1) {
            List<String> returnList = new ArrayList<>(subCommandMap.keySet());
            returnList.removeIf(str -> !str.startsWith(args[0]));
            return returnList;
        }
        ISubCommand subCommand = subCommandMap.get(argList.get(0));
        if (subCommand != null)
            return subCommand.onTabComplete(sender, argList.subList(1, argList.size()));
        else
            return Collections.singletonList("");
    }

    public Map<String, ISubCommand> getSubCommandMap() {
        return subCommandMap;
    }

    public void regSubCommand(ISubCommand subCommand) {
        subCommandMap.put(subCommand.getSubCommand(), subCommand);
    }

}
