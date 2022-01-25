package top.oasismc.oasisguild.factory;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import top.oasismc.oasisguild.data.objects.GuildChunk;
import top.oasismc.oasisguild.event.guild.*;
import top.oasismc.oasisguild.event.player.PlayerApplyGuildEvent;
import top.oasismc.oasisguild.event.player.PlayerJoinGuildEvent;
import top.oasismc.oasisguild.event.player.PlayerQuitGuildEvent;
import top.oasismc.oasisguild.event.player.PlayerTpGuildLocEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static top.oasismc.oasisguild.OasisGuild.getPlugin;
import static top.oasismc.oasisguild.data.DataHandler.getDataHandler;
import static top.oasismc.oasisguild.event.guild.GuildAddChunkEvent.createGuildAddChunkEvent;
import static top.oasismc.oasisguild.event.guild.GuildCreateEvent.createGuildCreateEvent;
import static top.oasismc.oasisguild.event.guild.GuildDisbandEvent.createGuildDisbandEvent;
import static top.oasismc.oasisguild.event.guild.GuildLevelUpEvent.createGuildLevelUpEvent;
import static top.oasismc.oasisguild.event.guild.GuildLocChangeEvent.createGuildLocChangeEvent;
import static top.oasismc.oasisguild.event.guild.GuildPvpChangeEvent.createGuildPvpChangeEvent;
import static top.oasismc.oasisguild.event.player.PlayerApplyGuildEvent.createPlayerApplyGuildEvent;
import static top.oasismc.oasisguild.event.player.PlayerTpGuildLocEvent.createPlayerTpGuildLocEvent;
import static top.oasismc.oasisguild.util.MsgSender.color;

public class GuildFactory {

    private static final Map<String, Boolean> disbandGuildConfirmMap;

    static {
        disbandGuildConfirmMap = new HashMap<>();
    }

    public static void playerJoinGuild(String guildName, String pName, PlayerJoinGuildEvent.JoinReason reason) {
        PlayerJoinGuildEvent event = PlayerJoinGuildEvent.createPlayerJoinGuildEvent(guildName, pName, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;
        getDataHandler().getGuildDao().handleApply(event.getGuildName(), "accept", event.getPlayer());
    }

    public static void playerQuitGuild(String guildName, String pName, PlayerQuitGuildEvent.QuitReason reason) {
        PlayerQuitGuildEvent event = PlayerQuitGuildEvent.createPlayerQuitGuildEvent(guildName, pName, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;
        getDataHandler().getGuildDao().memberQuit(guildName, pName);
    }

    //返回创建是否成功
    public static boolean createGuild(String guildName, Player creator, String desc) {
        GuildCreateEvent event = createGuildCreateEvent(guildName, creator, desc, creator.getLocation());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return false;
        return getDataHandler().getGuildDao().createGuild(event.getGuildName(), event.getCreator().getName(), event.getDesc(), event.getLoc());
    }

    //用于检查公会名字是否符合要求,-2为长度不符合，0为符合，1为颜色代码不符合，-1为已经有同名公会
    public static int checkGuildName(String guildName) {
        //检查是否有同名公会
        if (getDataHandler().getGuildNameList().contains(guildName)) {
            return -1;
        }
        //检查公会名字长度
        int maxNameLength = getPlugin().getConfig().getInt("guildSettings.name.maxLength", 15);
        if (guildName.length() > maxNameLength) {
            return -2;
        }
        //检查公会是否带颜色代码
        if (!color(guildName).startsWith("§")) {
            return 1;
        }
        return 0;
    }

    //返回-1为未加入公会，-2为不是会长，0为正常，1为需要确认，2为被取消
    public static int disbandGuild(Player player) {
        String guildName = getDataHandler().getGuildNameByPlayer(player.getName());
        if (guildName == null) {

            return -1;
        }
        int job = getDataHandler().getPlayerJob(guildName, player.getName());
        if (job != -1) {

            return -2;
        }
        if (disbandGuildConfirmMap.getOrDefault(guildName, false)) {
            GuildDisbandEvent event = createGuildDisbandEvent(guildName, player);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled())
                return 2;
            getDataHandler().getGuildDao().disbandGuild(event.getGuildName());
            return 0;
        } else {
            disbandGuildConfirmMap.put(guildName, true);
            new BukkitRunnable() {
                @Override
                public void run() {
                    disbandGuildConfirmMap.remove(guildName);
                }
            }.runTaskLaterAsynchronously(getPlugin(), 1200);
            return 1;
        }
    }

    public static void playerTpGuildLoc(Player player) {
        String guildName = getDataHandler().getGuildNameByPlayer(player.getName());
        if (guildName == null)
            return;
        Location loc = getDataHandler().getGuildLocationMap().get(guildName);
        PlayerTpGuildLocEvent event = createPlayerTpGuildLocEvent(guildName, player.getName(), loc);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;
        Player tpPlayer = Bukkit.getPlayer(event.getPlayer());
        if (tpPlayer == null)
            return;
        tpPlayer.teleport(event.getLoc());
    }

    //返回是否修改成功
    public static boolean changeGuildLoc(String guildName, Location loc) {
        Location oldLoc = getDataHandler().getGuildLocationMap().get(guildName);
        GuildLocChangeEvent event = createGuildLocChangeEvent(guildName, oldLoc, loc);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return false;
        return getDataHandler().getGuildDao().changeLoc(event.getGuildName(), event.getNewLoc());
    }

    public static boolean changeGuildPvp(String guildName) {
        boolean pvp = getDataHandler().getGuildByName(guildName).isPvp();
        GuildPvpChangeEvent event1 = createGuildPvpChangeEvent(guildName, !pvp);
        Bukkit.getPluginManager().callEvent(event1);
        if (event1.isCancelled())
            return pvp;
        if (event1.isNewPvp()) {
            getDataHandler().getGuildDao().changePvp(guildName, 1);
            return true;
        } else {
            getDataHandler().getGuildDao().changePvp(guildName, 0);
            return false;
        }
    }

    //返回状态码，-1为已经有此名字的公会，0为修改成功，2为被取消，-2为长度不符合要求
    public static int guildRename(String guildName, String newName, Player renamer) {
        switch (checkGuildName(newName)) {
            case -1:
                return -1;
            case -2:
                return -2;
            case 1:
                String defaultColor = getPlugin().getConfig().getString("guildSettings.name.defaultColor", "&f");
                newName = defaultColor + newName;
                break;
        }
        GuildRenameEvent event = GuildRenameEvent.createGuildRenameEvent(guildName, newName, renamer);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return 2;
        getDataHandler().getGuildDao().guildRename(event.getGuildName(), event.getNewName());
        return 0;
    }

    //返回状态码, -1为等级到达上限，1为不能满足升级条件，0为能够升级, 2为被取消
    public static int guildLevelUp(Player player, String guildName, int gLvl, int upNum) {
        int maxLvl = getPlugin().getConfig().getInt("guildSettings.maxLvl", -1);
        if (maxLvl != -1) {
            if (gLvl + 1 >= maxLvl) {
                return -1;
            }
        }
        int needLvl = (int)(Math.pow((gLvl + 1), 1.5));
        if (player.getLevel() < needLvl) {
            return 1;
        }
        GuildLevelUpEvent event = createGuildLevelUpEvent(guildName, gLvl, upNum);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return 2;
        getDataHandler().getGuildDao().levelUp(event.getGuildName(), event.getOldLevel());
        player.setLevel(player.getLevel() - needLvl);
        return 0;
    }

    //返回状态码，为2则被取消
    public static int playerApplyGuild(String guildName, Player player) {
        PlayerApplyGuildEvent event = createPlayerApplyGuildEvent(guildName, player.getName());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return 2;
        return getDataHandler().getGuildDao().putApply(event.getGuildName(), event.getPlayer());
    }

    //返回状态码，为2则被取消，0为正常
    public static void addGuildChunks(String guildName, List<GuildChunk> chunkList) {
        GuildAddChunkEvent event = createGuildAddChunkEvent(guildName, chunkList);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;
        getDataHandler().getGuildDao().addGuildChunk(event.getGuildName(), event.getChunkList());
    }

}
