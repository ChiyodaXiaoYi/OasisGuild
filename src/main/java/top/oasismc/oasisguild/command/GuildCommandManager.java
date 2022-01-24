package top.oasismc.oasisguild.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import top.oasismc.oasisguild.data.objects.GuildMember;
import top.oasismc.oasisguild.event.player.PlayerQuitGuildEvent;
import top.oasismc.oasisguild.factory.GuildFactory;

import java.util.ArrayList;
import java.util.List;

import static top.oasismc.oasisguild.data.DataHandler.getDataHandler;
import static top.oasismc.oasisguild.factory.GuildFactory.playerQuitGuild;
import static top.oasismc.oasisguild.menu.impl.DefMenuDrawer.getDrawer;
import static top.oasismc.oasisguild.util.MsgSender.sendMsg;

public class GuildCommandManager {

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
        if (getDataHandler().getPlayerJob(gName, player.getName()) != -1) {
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

    public void playerQuitGuildByCmd(Player player) {
        String gName = getDataHandler().getGuildNameByPlayer(player.getName());
        if (gName == null) {
            sendMsg(player, "command.quit.notJoinGuild");
            return;
        }
        if (getDataHandler().getPlayerJob(gName, player.getName()) == -1) {
            sendMsg(player, "command.quit.isLeader");
            return;
        }
        playerQuitGuild(gName, player.getName(), PlayerQuitGuildEvent.QuitReason.QUIT);
        sendMsg(player, "command.quit.success", getDataHandler().getGuildByName(gName));
    }

}
