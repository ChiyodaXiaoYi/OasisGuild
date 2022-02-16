package top.oasismc.oasisguild.bukkit.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import top.oasismc.oasisguild.bukkit.api.objects.IGuildChunk;
import top.oasismc.oasisguild.bukkit.api.event.player.PlayerQuitGuildEvent;
import top.oasismc.oasisguild.bukkit.core.GuildManager;
import top.oasismc.oasisguild.bukkit.listener.GuildChunkListener;
import top.oasismc.oasisguild.bukkit.core.MsgSender;

import java.util.List;

import static top.oasismc.oasisguild.bukkit.api.job.Jobs.*;
import static top.oasismc.oasisguild.bukkit.data.DataManager.getDataManager;
import static top.oasismc.oasisguild.bukkit.core.GuildManager.playerQuitGuild;
import static top.oasismc.oasisguild.bukkit.menu.GuildMenuManager.getMenuManager;
import static top.oasismc.oasisguild.bukkit.core.MsgSender.sendMsg;

public final class GuildCommandManager {

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
        int code = GuildManager.disbandGuild(player);
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
            case "start":
                sendMsg(player, "command.chunk.start");
                GuildChunkListener.getListener().startChunkSelect(player);
                break;
            case "confirm":
                List<IGuildChunk> chunkList = GuildChunkListener.getListener().getSelChunkMap().get(gName);
                if (chunkList == null || chunkList.size() == 0) {
                    sendMsg(player, "command.chunk.notSelect");
                    return;
                }
                GuildChunkListener.getListener().endChunkSelect(player);
                GuildManager.addGuildChunks(gName, chunkList);
                sendMsg(player, "command.chunk.confirm");
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
