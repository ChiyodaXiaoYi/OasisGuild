package top.oasismc.oasisguild.bukkit.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.oasismc.oasisguild.bukkit.api.event.player.PlayerQuitGuildEvent;
import top.oasismc.oasisguild.bukkit.api.objects.IGuildChunk;
import top.oasismc.oasisguild.bukkit.core.GuildManager;
import top.oasismc.oasisguild.bukkit.core.MsgSender;
import top.oasismc.oasisguild.bukkit.listener.GuildChunkListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static top.oasismc.oasisguild.bukkit.api.job.Jobs.ADVANCED;
import static top.oasismc.oasisguild.bukkit.api.job.Jobs.VICE_LEADER;
import static top.oasismc.oasisguild.bukkit.core.GuildManager.playerQuitGuild;
import static top.oasismc.oasisguild.bukkit.core.MsgSender.sendMsg;
import static top.oasismc.oasisguild.bukkit.data.DataManager.getDataManager;
import static top.oasismc.oasisguild.bukkit.menu.GuildMenuManager.getMenuManager;

public enum GuildCommandManager {

    INSTANCE;

    public void openGuildListMenu(CommandSender sender, int page) {
        Inventory inventory = getMenuManager().drawGuildListMenu((Player) sender, page);
        ((Player) sender).openInventory(inventory);
    }

    public void playerApplyGuildByCmd(CommandSender sender, String guildName) {
        int success = GuildManager.playerApplyGuild(guildName, (Player) sender);
        if (success == 1) {
            sendMsg(sender, "command.apply.failed");
        } else if (success == 0) {
            sendMsg(sender, "command.apply.success");
        } else if (success == -1) {
            sendMsg(sender, "command.apply.hasGuild");
        }
    }

    public void openGuildInfoMenu(CommandSender sender, String guildName) {
        Inventory inventory = getMenuManager().drawGuildInfoMenu(guildName, (Player) sender);
        ((Player) sender).openInventory(inventory);
    }

    public void createGuildByCmd(CommandSender sender, String gName, String desc) {
        if (GuildManager.createGuild(gName, (Player) sender, desc)) {
            sendMsg(sender, "command.create.success");
        }
    }

    public void guildRenameByCmd(Player player, String newName) {
        String gName = getDataManager().getGuildNameByPlayer(player.getName());
        if (gName == null) {
            sendMsg(player, "command.rename.notJoinGuild");
            return;
        }
        if (getDataManager().getPlayerJob(gName, player.getName()) < VICE_LEADER) {
            sendMsg(player, "command.rename.notLeader");
        }
        switch (GuildManager.guildRename(gName, newName, player)) {
            case -1:
                sendMsg(player, "command.rename.sameName");
                break;
            case 0:
                sendMsg(player, "command.rename.success");
                break;
            case -2:
                sendMsg(player, "command.rename.nameTooLong");
                break;
        }
    }

    public void disbandGuildByCmd(Player player) {
        String guildName = getDataManager().getGuildNameByPlayer(player.getName());
        int code = GuildManager.disbandGuild(player, guildName);
        switch (code) {
            case -1:
                sendMsg(player, "command.disband.notJoinGuild");
                break;
            case -2:
                sendMsg(player, "command.disband.notLeader");
                break;
            case 0:
                sendMsg(player, "command.disband.success");
                break;
            case 1:
                sendMsg(player, "command.disband.confirm");
                break;
        }
    }

    @Nullable
    static List<String> onTab(@NotNull CommandSender sender, @NotNull String[] args, List<String> subCommandList, List<String> subAdminCommandList, Map<String, Supplier<List<String>>> subCommandArgListMap) {
        if (args.length == 1) {
            if (!sender.hasPermission("oasis.guild.admin")) {
                ArrayList<String> subCommands = new ArrayList<>(subCommandList);
                subCommands.removeAll(subAdminCommandList);
                subCommands.removeIf(str -> !str.startsWith(args[0]));
                return subCommands;
            } else {
                return subCommandList;
            }
        } else if (args.length == 2) {
            List<String> returnList = subCommandArgListMap.getOrDefault(args[0], () -> Collections.singletonList("")).get();
            if (returnList instanceof ArrayList) {
                returnList.removeIf(str -> !str.startsWith(args[0]));
            }
            return returnList;
        }
        return Collections.singletonList("");
    }

    public void playerSelChunk(Player player, String subCmd) {
        String gName = getDataManager().getGuildNameByPlayer(player.getName());
        if (gName == null) {
            sendMsg(player, "command.chunk.notJoinGuild");
            return;
        }
        int job = getDataManager().getPlayerJob(gName, player.getName());
        if (job < ADVANCED) {
            sendMsg(player, "command.chunk.notLeader");
            return;
        }
        switch (subCmd) {
            case "add":
                sendMsg(player, "command.chunk.start");
                GuildChunkListener.getListener().startChunkAddSelect(player);
                break;
            case "confirm":
                List<IGuildChunk> chunkList = GuildChunkListener.getListener().getSelChunkMap().get(gName);
                if (chunkList == null || chunkList.size() == 0) {
                    sendMsg(player, "command.chunk.notSelect");
                    return;
                }
                switch (GuildChunkListener.getListener().getChunkSelSwitchMap().getOrDefault(player.getUniqueId(), 1)) {
                    case 1:
                        GuildManager.addGuildChunks(gName, chunkList);
                        sendMsg(player, "command.chunk.confirm");
                        break;
                    case -1:
                        GuildManager.removeGuildChunks(gName, chunkList);
                        sendMsg(player, "command.chunk.delete");
                        break;
                }
                GuildChunkListener.getListener().endChunkSelect(player);
                break;
            case "delete":
                sendMsg(player, "command.chunk.start");
                GuildChunkListener.getListener().startChunkRemoveSelect(player);
                break;
            case "cancel":
                sendMsg(player, "command.chunk.cancel");
                GuildChunkListener.getListener().endChunkSelect(player);
                break;
        }
    }

    public void playerQuitGuildByCmd(Player player) {
        String gName = getDataManager().getGuildNameByPlayer(player.getName());
        if (gName == null) {
            sendMsg(player, "command.quit.notJoinGuild");
            return;
        }
        if (getDataManager().getPlayerJob(gName, player.getName()) >= 250) {
            sendMsg(player, "command.quit.isLeader");
            return;
        }
        playerQuitGuild(gName, player.getName(), PlayerQuitGuildEvent.QuitReason.QUIT);
        MsgSender.sendMsg4replaceGuild(player, "command.quit.success", getDataManager().getGuildByName(gName));
    }

}
