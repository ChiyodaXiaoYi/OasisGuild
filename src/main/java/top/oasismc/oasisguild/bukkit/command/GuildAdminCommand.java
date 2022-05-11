package top.oasismc.oasisguild.bukkit.command;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.oasismc.oasisguild.bukkit.api.objects.IGuild;
import top.oasismc.oasisguild.bukkit.core.GuildManager;
import top.oasismc.oasisguild.bukkit.data.DataManager;
import top.oasismc.oasisguild.bukkit.data.MysqlTool;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static top.oasismc.oasisguild.bukkit.command.GuildCommandManager.onTab;
import static top.oasismc.oasisguild.bukkit.core.MsgSender.sendMsg;
import static top.oasismc.oasisguild.bukkit.data.DataManager.getDataManager;

public enum GuildAdminCommand implements TabExecutor {

    INSTANCE;

    private final List<String> subCommandList;
    private final List<String> subAdminCommandList;
    private final Map<String, BiConsumer<CommandSender, String[]>> subCommandMap;
    private final Map<String, Supplier<List<String>>> subCommandArgListMap;
    private final List<String> items;

    GuildAdminCommand() {
        subCommandMap = new ConcurrentHashMap<>();
        subCommandList = new ArrayList<>();
        subCommandArgListMap = new ConcurrentHashMap<>();
        subAdminCommandList = new ArrayList<>();
        items = new ArrayList<>();
        loadMaterials();
        regDefaultSubCommands();
    }

    private void loadMaterials() {
        for (Material value : Material.values()) {
            items.add(value.name());
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("oasis.guild.admin")) {
            sendMsg(sender, "noPerm");
            return true;
        }
        if (args.length == 0) {
            sendMsg(sender, "command.missingSubCmd");
            return true;
        }
        subCommandMap.getOrDefault(args[0], (a, b) -> sendMsg(sender, "command.noArgs")).accept(sender, args);
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 3) {
            return onTab(sender, args, subCommandList, subAdminCommandList, subCommandArgListMap);
        } else {
            switch (args[0]) {
                case "setIcon":
                case "seticon":
                case "SETICON":
                    return items;
                default:
                    return Collections.singletonList("");
            }
        }
    }

    private void regDefaultSubCommands() {
        regSubCommand("disband", (sender, args) -> {
            if (args.length < 2) {
                sendMsg(sender, "adminCommand.notGuild");
                return;
            }
            int code = GuildManager.disbandGuild((Player) sender, args[1]);
            if (code == -1) {
                sendMsg(sender, "adminCommand.notGuild");
            } else if (code == 0){
                sendMsg(sender, "adminCommand.success");
            } else if (code == 1) {
                sendMsg(sender, "command.disband.confirm");
            }
        }, () -> getDataManager().getGuildNameList());
        regSubCommand("setLevel", ((sender, args) -> {
            if (args.length < 2) {
                sendMsg(sender, "adminCommand.notGuild");
                return;
            }
            if (args.length < 3) {
                sendMsg(sender, "adminCommand.missingParam");
                return;
            }
            IGuild guild = getDataManager().getGuildByName(args[1]);
            if (guild == null) {
                sendMsg(sender, "adminCommand.notGuild");
                return;
            }
            int level;
            try {
                level = Integer.parseInt(args[2]);
            } catch (Exception e) {
                level = guild.getGuildLevel();
            }
            getDataManager().getGuildDao().setGuildLevel(guild.getGuildName(), level);
            sendMsg(sender, "adminCommand.success");
        }), () -> getDataManager().getGuildNameList());
        regSubCommand("setMaxMember", ((sender, args) -> {
            if (args.length < 2) {
                sendMsg(sender, "adminCommand.notGuild");
                return;
            }
            if (args.length < 3) {
                sendMsg(sender, "adminCommand.missingParam");
                return;
            }
            IGuild guild = getDataManager().getGuildByName(args[1]);
            if (guild == null) {
                sendMsg(sender, "adminCommand.notGuild");
                return;
            }
            int maxMember;
            try {
                maxMember = Integer.parseInt(args[2]);
            } catch (Exception e) {
                maxMember = guild.getMaxMember();
            }
            getDataManager().getGuildDao().setGuildMaxMember(guild.getGuildName(), maxMember);
            sendMsg(sender, "adminCommand.success");
        }), () -> getDataManager().getGuildNameList());
        regSubCommand("setIcon", ((sender, args) -> {
            if (args.length < 2) {
                sendMsg(sender, "adminCommand.notGuild");
                return;
            }
            if (args.length < 3) {
                sendMsg(sender, "adminCommand.missingParam");
                return;
            }
            IGuild guild = getDataManager().getGuildByName(args[1]);
            if (guild == null) {
                sendMsg(sender, "adminCommand.notGuild");
                return;
            }
            Material material = Material.matchMaterial(args[2]);
            if (material == null) {
                sendMsg(sender, "adminCommand.illegalItems");
                return;
            }
            DataManager.getDataManager().getGuildDao().setGuildIcon(args[1], args[2]);
            sendMsg(sender, "adminCommand.success");
        }), () -> getDataManager().getGuildNameList());
    }

    public List<String> getSubCommandList() {
        return subCommandList;
    }

    public List<String> getSubAdminCommandList() {
        return subAdminCommandList;
    }

    public Map<String, BiConsumer<CommandSender, String[]>> getSubCommandMap() {
        return subCommandMap;
    }

    public Map<String, Supplier<List<String>>> getSubCommandArgListMap() {
        return subCommandArgListMap;
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

}
