package top.oasismc.oasisguild.command;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import top.oasismc.oasisguild.data.objects.GuildMember;
import top.oasismc.oasisguild.event.guild.GuildCreateEvent;
import top.oasismc.oasisguild.event.guild.GuildDisbandEvent;
import top.oasismc.oasisguild.event.player.PlayerApplyGuildEvent;
import top.oasismc.oasisguild.util.LoreTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static top.oasismc.oasisguild.util.MsgTool.*;
import static top.oasismc.oasisguild.OasisGuild.*;
import static top.oasismc.oasisguild.data.DataHandler.*;
import static top.oasismc.oasisguild.menu.impl.DefMenuDrawer.*;
import static top.oasismc.oasisguild.event.guild.GuildCreateEvent.createGuildCreateEvent;
import static top.oasismc.oasisguild.event.guild.GuildDisbandEvent.createGuildDisbandEvent;
import static top.oasismc.oasisguild.event.player.PlayerApplyGuildEvent.createPlayerApplyGuildEvent;

public class GuildCommandManager {

    private final Map<String, Boolean> disbandGuildConfirmMap;

    public GuildCommandManager() {
        disbandGuildConfirmMap = new HashMap<>();
    }

    public void openGuildListMenu(CommandSender sender) {
        Inventory inventory = getDrawer().drawGuildListMenu();
        ((Player) sender).openInventory(inventory);
    }

    public void applyGuild(CommandSender sender, String guildName) {
        PlayerApplyGuildEvent event = createPlayerApplyGuildEvent(guildName, sender.getName());
        if (event.isCancelled())
            return;
        int success = getDataHandler().getGuildDao().putApply(event.getGuildName(), event.getPlayer());
        if (success == 1) {
            sendMsg(sender, "command.apply.failed");
        } else if (success == 0){
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

    public void createGuild(CommandSender sender, String gName, String desc) {
        //检查当前世界能否创建公会
        String world = ((Player) sender).getWorld().getName();
        List<String> canSetWorlds = getPlugin().getConfig().getStringList("guildSettings.canSetWorlds");
        if (!canSetWorlds.contains(world)) {
            sendMsg(sender, "command.create.notAllowedWorld");
            return;
        }

        //检查是否有同名公会
        if (getDataHandler().getGuildByName(gName) != null) {
            sendMsg(sender, "command.create.sameName");
            return;
        }

        //检查玩家是否已经有公会
        if (getDataHandler().getGuildNameByPlayer(sender.getName()) != null) {
            sendMsg(sender, "command.create.hasGuild");
            return;
        }

        //检查公会名字长度
        int maxNameLength = getPlugin().getConfig().getInt("guildSettings.name.maxLength", 15);
        if (gName.length() > maxNameLength) {
            sendMsg(sender, "command.create.nameTooLong");
            return;
        }

        //检查公会描述长度
        int maxDescLength = getPlugin().getConfig().getInt("guildSettings.desc.maxLength", 15);
        if (desc.length() > maxDescLength) {
            sendMsg(sender, "command.create.descTooLong");
            return;
        }

        //检查公会是否带颜色代码
        if (!gName.startsWith("&")) {
            String defaultColor = getPlugin().getConfig().getString("guildSettings.name.defaultColor", "&f");
            gName = defaultColor + gName;
        }

        //检查玩家是否能够创建
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (getPlugin().getConfig().getBoolean("conditions.create.item.enable")) {
            String material = getPlugin().getConfig().getString("conditions.create.item.material");
            String name = color(getPlugin().getConfig().getString("conditions.create.item.name"));
            String lore = color(getPlugin().getConfig().getString("conditions.create.item.lore"));
            if (item.getType() != Material.getMaterial(material)) {
                sendMsg(sender, "command.create.needItem");
                return;
            }
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                sendMsg(sender, "command.create.needItem");
                return;
            }
            if (!meta.hasDisplayName()) {
                sendMsg(sender, "command.create.needItem");
                return;
            }
            if (!meta.getDisplayName().equals(name)) {
                sendMsg(sender, "command.create.needItem");
                return;
            }
            if (!LoreTool.hasLore(lore, meta.getLore())) {
                sendMsg(sender, "command.create.needItem");
                return;
            }
        }
        //检查权限
        String perm = getPlugin().getConfig().getString("conditions.create.perm");
        if (perm != null && !perm.equals("")) {
            if (!player.hasPermission(perm)) {
                sendMsg(sender, "command.create.needPerm");
                return;
            }
        }

        //检查等级
        int needLvl = getPlugin().getConfig().getInt("conditions.create.exp", 100);
        if (needLvl != 0) {
            if (player.getLevel() < needLvl) {
                sendMsg(sender, "command.create.needLvl");
                return;
            }
        }

        GuildCreateEvent event = createGuildCreateEvent(gName, player, desc, player.getLocation());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        if (getDataHandler().getGuildDao().createGuild(event.getGuildName(), event.getCreator().getName(), event.getDesc(), event.getLoc())) {
            item.setAmount(item.getAmount() - 1);
            player.getInventory().setItemInMainHand(item);
            player.setLevel(player.getLevel() - needLvl);
            sendMsg(sender, "command.create.success");
        } else {
            sendMsg(sender, "command.create.error");
        }
    }

    public void disbandGuild(Player player) {
        String guildName = getDataHandler().getGuildNameByPlayer(player.getName());
        if (!disbandGuildConfirmMap.getOrDefault(guildName, false)) {
            if (guildName == null) {
                sendMsg(player, "command.disband.notJoinGuild");
                return;
            }
            int job = getDataHandler().getPlayerJob(guildName, player.getName());
            if (job != -1) {
                sendMsg(player, "command.disband.notLeader");
                return;
            }
            sendMsg(player, "command.disband.confirm");
            disbandGuildConfirmMap.put(guildName, true);
            new BukkitRunnable() {
                @Override
                public void run() {
                    disbandGuildConfirmMap.remove(guildName);
                }
            }.runTaskLaterAsynchronously(getPlugin(), 1200);
            return;
        }

        GuildDisbandEvent event = createGuildDisbandEvent(guildName, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;
        getDataHandler().getGuildDao().disbandGuild(event.getGuildName());
        sendMsg(player, "command.disband.success");
    }

}
