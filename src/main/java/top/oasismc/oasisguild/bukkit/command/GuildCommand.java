package top.oasismc.oasisguild.bukkit.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import top.oasismc.oasisguild.bukkit.api.command.ISubCommand;
import top.oasismc.oasisguild.bukkit.command.subcmd.guild.*;
import top.oasismc.oasisguild.bukkit.core.MsgSender;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public enum GuildCommand implements TabExecutor {

    INSTANCE;

    private final Map<String, ISubCommand> subCommandMap;

    GuildCommand() {
        subCommandMap = new ConcurrentHashMap<>();
        regDefaultSubCommands();
    }

    public Map<String, ISubCommand> getSubCommandMap() {
        return subCommandMap;
    }

    public void regSubCommand(ISubCommand subCommand) {
        subCommandMap.put(subCommand.getSubCommand(), subCommand);
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

    private void regDefaultSubCommands() {
        regSubCommand(ListCommand.INSTANCE);
        regSubCommand(ApplyCommand.INSTANCE);
        regSubCommand(ChunkCommand.INSTANCE);
        regSubCommand(CreateCommand.INSTANCE);
        regSubCommand(DisbandCommand.INSTANCE);
        regSubCommand(InfoCommand.INSTANCE);
        regSubCommand(QuitCommand.INSTANCE);
        regSubCommand(RenameCommand.INSTANCE);
    }

    public static GuildCommand getGuildCommand() {
        return INSTANCE;
    }

}
