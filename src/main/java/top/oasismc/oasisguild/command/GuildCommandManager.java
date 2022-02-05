package top.oasismc.oasisguild.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import top.oasismc.oasisguild.data.objects.GuildChunk;
import top.oasismc.oasisguild.data.objects.GuildMember;
import top.oasismc.oasisguild.event.player.PlayerQuitGuildEvent;
import top.oasismc.oasisguild.factory.GuildFactory;
import top.oasismc.oasisguild.listener.GuildChunkListener;
import top.oasismc.oasisguild.util.MsgSender;

import java.util.ArrayList;
import java.util.List;

import static top.oasismc.oasisguild.data.DataHandler.getDataHandler;
import static top.oasismc.oasisguild.factory.GuildFactory.playerQuitGuild;
import static top.oasismc.oasisguild.menu.impl.DefMenuDrawer.getDrawer;
import static top.oasismc.oasisguild.util.MsgSender.sendMsg;
import static top.oasismc.oasisguild.job.Jobs.*;

public final class GuildCommandManager {

    public void openGuildListMenu(CommandSender sender, int page) {
        Inventory inventory = getDrawer().drawGuildListMenu(page);
        ((Player) sender).openInventory(inventory);
    }

    public void playerApplyGuildByCmd(CommandSender sender, String guildName) {
        int success = GuildFactory.playerApplyGuild(guildName, (Player) sender);
        if (success == 1) {
            sendMsg(sender, "command.apply.failed");
        } else if (success == 0) {
            sendMsg(sender, "command.apply.success");
        } else if (success == -1) {
            sendMsg(sender, "command.apply.hasGuild");
        }
    }

    public void openGuildInfoMenu(CommandSender sender, String guildName) {
        List<GuildMember> players = getDataHandler().getGuildMembers().getOrDefault(guildName, new ArrayList<>());
        Inventory inventory = getDrawer().drawGuildInfoMenu(players, guildName, (Player) sender);
        ((Player) sender).openInventory(inventory);
    }

    public void createGuildByCmd(CommandSender sender, String gName, String desc) {
        if (GuildFactory.createGuild(gName, (Player) sender, desc)) {
            sendMsg(sender, "command.create.success");
        }
    }

    public void guildRenameByCmd(Player player, String newName) {
        String gName = getDataHandler().getGuildNameByPlayer(player.getName());
        if (gName == null) {
            sendMsg(player, "command.rename.notJoinGuild");
            return;
        }
        if (getDataHandler().getPlayerJob(gName, player.getName()) < VICE_LEADER) {
            sendMsg(player, "command.rename.notLeader");
        }
        switch (GuildFactory.guildRename(gName, newName, player)) {
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
        int code = GuildFactory.disbandGuild(player);
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
        String gName = getDataHandler().getGuildNameByPlayer(player.getName());
        if (gName == null) {
            sendMsg(player, "command.chunk.notJoinGuild");
            return;
        }
        int job = getDataHandler().getPlayerJob(gName, player.getName());
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
                List<GuildChunk> chunkList = GuildChunkListener.getListener().getSelChunkMap().get(gName);
                if (chunkList == null || chunkList.size() == 0) {
                    sendMsg(player, "command.chunk.notSelect");
                    return;
                }
                GuildChunkListener.getListener().endChunkSelect(player);
                GuildFactory.addGuildChunks(gName, chunkList);
                sendMsg(player, "command.chunk.confirm");
                break;
            case "cancel":
                sendMsg(player, "command.chunk.cancel");
                GuildChunkListener.getListener().endChunkSelect(player);
                break;
        }
    }

    public void playerQuitGuildByCmd(Player player) {
        String gName = getDataHandler().getGuildNameByPlayer(player.getName());
        if (gName == null) {
            sendMsg(player, "command.quit.notJoinGuild");
            return;
        }
        if (getDataHandler().getPlayerJob(gName, player.getName()) >= 250) {
            sendMsg(player, "command.quit.isLeader");
            return;
        }
        playerQuitGuild(gName, player.getName(), PlayerQuitGuildEvent.QuitReason.QUIT);
        MsgSender.sendMsg4replaceGuild(player, "command.quit.success", getDataHandler().getGuildByName(gName));
    }

}
