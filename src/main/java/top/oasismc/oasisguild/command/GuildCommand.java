package top.oasismc.oasisguild.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static top.oasismc.oasisguild.OasisGuild.*;
import static top.oasismc.oasisguild.data.DataHandler.*;
import static top.oasismc.oasisguild.menu.impl.DefMenuDrawer.getDrawer;
import static top.oasismc.oasisguild.util.MsgTool.getMsgTool;
import static top.oasismc.oasisguild.util.MsgTool.sendMsg;

public class GuildCommand implements TabExecutor {

    private final GuildCommandManager guildCommandManager;
    private final List<String> subCommandList;
    private final List<String> subAdminCommandList;
    private final Map<String, BiConsumer<CommandSender, String[]>> subCommandMap;
    private final Map<String, Supplier<List<String>>> subCommandArgListMap;


    private static final GuildCommand guildCommand;

    static {
        guildCommand = new GuildCommand();
    }

    public GuildCommand() {
        guildCommandManager = new GuildCommandManager();
        subCommandMap = new ConcurrentHashMap<>();
        subCommandList = new ArrayList<>();
        subCommandArgListMap = new ConcurrentHashMap<>();
        subAdminCommandList = new ArrayList<>();
        regDefaultSubCommands();
    }

    public Map<String, BiConsumer<CommandSender, String[]>> getSubCommandMap() {
        return subCommandMap;
    }

    public void regSubCommand(String subCommand, BiConsumer<CommandSender, String[]> consumer) {
        regSubCommand(subCommand, consumer, false);
    }

    public void regSubCommand(String subCommand, BiConsumer<CommandSender, String[]> consumer, boolean admin) {
        regSubCommand(subCommand, consumer, () -> Collections.singletonList(""), admin);
    }

    public void regSubCommand(String subCommand, BiConsumer<CommandSender, String[]> consumer, Supplier<List<String>> args) {
        regSubCommand(subCommand, consumer, args, false);
    }

    public void regSubCommand(String subCommand, BiConsumer<CommandSender, String[]> consumer, Supplier<List<String>> args, boolean admin) {
        subCommandList.add(subCommand);
        subCommandMap.put(subCommand, consumer);
        subCommandArgListMap.put(subCommand, args);
        if (admin)
            subAdminCommandList.add(subCommand);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendMsg(sender, "command.noArgs");
            return true;
        }
        if (!(sender instanceof Player)) {
            if (!args[0].equals("reload") && !args[0].equals("version")) {
                sendMsg(sender, "command.notPlayer");
                return true;
            }
        }
        subCommandMap.getOrDefault(args[0], (a, b) -> sendMsg(sender, "command.noArgs")).accept(sender, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            if (!sender.hasPermission("oasis.guild.admin")) {
                ArrayList<String> subCommands = new ArrayList<>(subAdminCommandList);
                subCommands.removeAll(subAdminCommandList);
                return subCommands;
            } else {
                return subCommandList;
            }
        } else if (args.length == 2) {
            return subCommandArgListMap.getOrDefault(args[0], () -> Collections.singletonList("")).get();
        }
        return Collections.singletonList("");
    }

    private void regDefaultSubCommands() {
        regSubCommand("list", (sender, args) -> getCommandManager().openGuildListMenu(sender, 0));
        regSubCommand("apply", (sender, args) -> {
            if (args.length < 2) {
                sendMsg(sender, "command.missingValue.needGuildName");
                return;
            }
            guildCommandManager.applyGuild(sender, args[1]);
        }, () -> getDataHandler().getGuildNameList());
        regSubCommand("info", (sender, args) -> {
            String guildName = getDataHandler().getGuildNameByPlayer(sender.getName());
            if (guildName == null) {
                sendMsg(sender, "command.info.notJoinGuild");
            } else {
                guildCommandManager.openGuildInfoMenu(sender, guildName);
            }
        });
        regSubCommand("reload", (sender, args) -> {
            if (!sender.hasPermission("oasis.guild.admin")) {
                sendMsg(sender, "command.noPerm");
                return;
            }
            reloadPlugin();
            sendMsg(sender, "command.reload");
        });
        regSubCommand("version", (sender, args) -> {
            if (!sender.hasPermission("oasis.guild.admin")) {
                sendMsg(sender, "command.noPerm");
                return;
            }
            sendMsg(sender, "command.version");
        });
        regSubCommand("create", (sender, args) -> {
            if (args.length < 2) {
                sendMsg(sender, "command.missingValue.needGuildName");
                return;
            }
            if (args.length < 3) {
                sendMsg(sender, "command.create.needDesc");
                return;
            }
            guildCommandManager.createGuild(sender, args[1], args[2]);
        });
        regSubCommand("disband", (sender, strings) -> guildCommandManager.disbandGuild((Player) sender));
    }

    private void reloadPlugin() {
        getPlugin().reloadConfig();
        getMsgTool().getLangFile().reloadConfig();
        getDrawer().getMenuFile().reloadConfig();
    }

    public static GuildCommand getGuildCommand() {
        return guildCommand;
    }

    public GuildCommandManager getCommandManager() {
        return guildCommandManager;
    }
}
