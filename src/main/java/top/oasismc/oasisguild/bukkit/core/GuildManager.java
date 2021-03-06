package top.oasismc.oasisguild.bukkit.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import top.oasismc.oasisguild.bukkit.api.event.guild.*;
import top.oasismc.oasisguild.bukkit.api.event.player.PlayerApplyGuildEvent;
import top.oasismc.oasisguild.bukkit.api.event.player.PlayerJobChangeEvent;
import top.oasismc.oasisguild.bukkit.api.event.player.PlayerJoinGuildEvent;
import top.oasismc.oasisguild.bukkit.api.event.player.PlayerQuitGuildEvent;
import top.oasismc.oasisguild.bukkit.api.objects.IGuildChunk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static top.oasismc.oasisguild.bukkit.OasisGuild.getPlugin;
import static top.oasismc.oasisguild.bukkit.api.event.guild.GuildChunkChangeEvent.createGuildAddChunkEvent;
import static top.oasismc.oasisguild.bukkit.api.event.guild.GuildCreateEvent.createGuildCreateEvent;
import static top.oasismc.oasisguild.bukkit.api.event.guild.GuildDisbandEvent.createGuildDisbandEvent;
import static top.oasismc.oasisguild.bukkit.api.event.guild.GuildLevelUpEvent.createGuildLevelUpEvent;
import static top.oasismc.oasisguild.bukkit.api.event.guild.GuildLocChangeEvent.createGuildLocChangeEvent;
import static top.oasismc.oasisguild.bukkit.api.event.guild.GuildPvpChangeEvent.createGuildPvpChangeEvent;
import static top.oasismc.oasisguild.bukkit.api.event.player.PlayerApplyGuildEvent.createPlayerApplyGuildEvent;
import static top.oasismc.oasisguild.bukkit.api.event.player.PlayerJobChangeEvent.createPlayerJobChangeEvent;
import static top.oasismc.oasisguild.bukkit.api.job.Jobs.LEADER;
import static top.oasismc.oasisguild.bukkit.data.DataManager.getDataManager;

public class GuildManager {

    private static final Map<String, Boolean> disbandGuildConfirmMap;

    static {
        disbandGuildConfirmMap = new HashMap<>();
    }

    public static void playerJoinGuild(String guildName, String pName, PlayerJoinGuildEvent.JoinReason reason) {
        PlayerJoinGuildEvent event = PlayerJoinGuildEvent.createPlayerJoinGuildEvent(guildName, pName, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;
        getDataManager().getGuildDao().handleApply(event.getGuildName(), "accept", event.getPlayer());
    }

    public static void playerQuitGuild(String guildName, String pName, PlayerQuitGuildEvent.QuitReason reason) {
        PlayerQuitGuildEvent event = PlayerQuitGuildEvent.createPlayerQuitGuildEvent(guildName, pName, reason);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;
        getDataManager().getGuildDao().memberQuit(guildName, pName);
    }

    //????????????????????????
    public static boolean createGuild(String guildName, Player creator, String desc) {
        GuildCreateEvent event = createGuildCreateEvent(guildName, creator, desc, creator.getLocation());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return false;
        return getDataManager().getGuildDao().createGuild(event.getGuildName(), event.getCreator().getName(), event.getDesc(), event.getLoc());
    }

    //??????????????????????????????????????????,-2?????????????????????0????????????1???????????????????????????-1????????????????????????
    public static int checkGuildName(String guildName) {
        //???????????????????????????
        if (getDataManager().getGuildNameList().contains(guildName)) {
            return -1;
        }
        //????????????????????????
        int maxNameLength = getPlugin().getConfig().getInt("guildSettings.name.maxLength", 15);
        if (guildName.length() > maxNameLength) {
            return -2;
        }
        //?????????????????????????????????
        if (!MsgSender.color(guildName).startsWith("??")) {
            return 1;
        }
        return 0;
    }

    //??????-1?????????????????????-2??????????????????0????????????1??????????????????2????????????
    public static int disbandGuild(Player player, String guildName) {
        if (guildName == null) {
            return -1;
        }
        int job = getDataManager().getPlayerJob(guildName, player.getName());
        if (job < LEADER) {
            if (!player.hasPermission("oasis.guild.admin"))
                return -2;
        }
        if (disbandGuildConfirmMap.getOrDefault(guildName, false)) {
            GuildDisbandEvent event = createGuildDisbandEvent(guildName, player);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled())
                return 2;
            getDataManager().getGuildDao().disbandGuild(event.getGuildName());
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

    //????????????????????????
    public static boolean changeGuildLoc(String guildName, Location loc) {
        Location oldLoc = getDataManager().getGuildLocationMap().get(guildName);
        GuildLocChangeEvent event = createGuildLocChangeEvent(guildName, oldLoc, loc);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return false;
        return getDataManager().getGuildDao().changeLoc(event.getGuildName(), event.getNewLoc());
    }

    public static boolean changeGuildPvp(String guildName) {
        boolean pvp = getDataManager().getGuildByName(guildName).isPvp();
        GuildPvpChangeEvent event1 = createGuildPvpChangeEvent(guildName, !pvp);
        Bukkit.getPluginManager().callEvent(event1);
        if (event1.isCancelled())
            return pvp;
        if (event1.isNewPvp()) {
            getDataManager().getGuildDao().changePvp(guildName, 1);
            return true;
        } else {
            getDataManager().getGuildDao().changePvp(guildName, 0);
            return false;
        }
    }

    //??????????????????-1?????????????????????????????????0??????????????????2???????????????-2????????????????????????
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
        getDataManager().getGuildDao().guildRename(event.getGuildName(), event.getNewName());
        return 0;
    }

    public static int guildResetDesc(String guildName, String newDesc) {
        GuildResetDescEvent event = new GuildResetDescEvent(guildName, newDesc);
        if (event.isCancelled()) {
            return 2;
        }
        if (newDesc.length() > getPlugin().getConfig().getInt("guildSettings.desc.maxLength"))
            return 1;
        getDataManager().getGuildDao().guildResetDesc(event.getGuildName(), event.getNewDesc());
        return 0;
    }

    //???????????????, -1????????????????????????1??????????????????????????????0???????????????, 2????????????
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
        getDataManager().getGuildDao().levelUp(event.getGuildName(), event.getOldLevel());
        player.setLevel(player.getLevel() - needLvl);
        return 0;
    }

    //?????????????????????2????????????
    public static int playerApplyGuild(String guildName, Player player) {
        PlayerApplyGuildEvent event = createPlayerApplyGuildEvent(guildName, player.getName());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return 2;
        return getDataManager().getGuildDao().putApply(event.getGuildName(), event.getPlayer());
    }

    //?????????????????????2???????????????0?????????
    public static void addGuildChunks(String guildName, List<IGuildChunk> chunkList) {
        GuildChunkChangeEvent event = createGuildAddChunkEvent(guildName, chunkList);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;
        getDataManager().getGuildDao().addGuildChunk(event.getGuildName(), event.getChunkList());
    }

    public static void removeGuildChunks(String guildName, List<IGuildChunk> chunkList) {
        GuildChunkChangeEvent event = createGuildAddChunkEvent(guildName, chunkList, GuildChunkChangeEvent.Type.REMOVE);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;
        getDataManager().getGuildDao().removeGuildChunk(event.getGuildName(), event.getChunkList());
    }

    public static void memberJobChange(String guildName, String member, String leader, int oldJob, int newJob) {
        PlayerJobChangeEvent event = createPlayerJobChangeEvent(guildName, member, leader, oldJob, newJob);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;
        getDataManager().getGuildDao().changeMemberJob(guildName, member, newJob);
    }

}
