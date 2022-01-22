package top.oasismc.oasisguild.data;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import top.oasismc.oasisguild.OasisGuild;
import top.oasismc.oasisguild.data.impl.MysqlGuildDao;
import top.oasismc.oasisguild.data.api.IGuildDao;
import top.oasismc.oasisguild.data.impl.SqliteGuildDao;
import top.oasismc.oasisguild.data.impl.YamlGuildDao;
import top.oasismc.oasisguild.data.objects.Guild;
import top.oasismc.oasisguild.data.objects.GuildApply;
import top.oasismc.oasisguild.data.objects.GuildChunk;
import top.oasismc.oasisguild.data.objects.GuildMember;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static top.oasismc.oasisguild.OasisGuild.*;

public class DataHandler extends BukkitRunnable {

    private List<Guild> guildList;
    private Map<String, List<GuildMember>> guildMembers;
    private Map<String, Location> guildLocationMap;
    private Map<String, List<GuildApply>> guildApplyListMap;
    private Map<String, Set<GuildChunk>> guildChunkSetMap;
    private IGuildDao guildDao;
    private static DataHandler dataHandler;

    static {
        try {
            dataHandler = new DataHandler();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private DataHandler() throws ClassNotFoundException {
        start(OasisGuild.getPlugin().getConfig().getInt("data.dataCacheTime", 2));
        initDataRegister();
        loadDao();
    }

    private void loadDao() throws ClassNotFoundException {
        String dataType = getPlugin().getConfig().getString("data.type", "yaml");
        switch (dataType) {
            case "sqlite":
                guildDao = new SqliteGuildDao();
                break;
            case "mysql":
                Class.forName("top.oasismc.oasisguild.data.util.MysqlTool");
                guildDao = new MysqlGuildDao();
                break;
            default:
                guildDao = new YamlGuildDao();
                break;
        }
    }

    private void initDataRegister() {
        guildList = Collections.synchronizedList(new ArrayList<>());
        guildMembers = new ConcurrentHashMap<>();
        guildLocationMap = new ConcurrentHashMap<>();
        guildApplyListMap = new ConcurrentHashMap<>();
        guildChunkSetMap = new ConcurrentHashMap<>();
    }

    public static DataHandler getDataHandler() {
        return dataHandler;
    }

    @Override
    public void run() {
        getData();
    }

    public void getData() {
        guildList = guildDao.getGuilds();
        guildMembers = guildDao.getGuildMembers(guildList);
        guildLocationMap = guildDao.getGuildLocationMap(guildList);
        guildApplyListMap = guildDao.getGuildApplyListMap(guildList);
        guildChunkSetMap = guildDao.getGuildChunkSetMap(guildList);
    }

    public void start(int interval) {
        this.runTaskTimerAsynchronously(OasisGuild.getPlugin(), interval * 20L, interval * 20L);
    }

    public List<Guild> getGuildList() {
        return guildList;
    }

    public Map<String, List<GuildMember>> getGuildMembers() {
        return guildMembers;
    }

    public Map<String, Location> getGuildLocationMap() {
        return guildLocationMap;
    }

    public Map<String, List<GuildApply>> getGuildApplyListMap() {
        return guildApplyListMap;
    }

    public IGuildDao getGuildDao() {
        return guildDao;
    }

    @Nullable
    public String getGuildNameByPlayer(String pName) {
        String guildName = null;
        for (Guild guild : guildList) {
            boolean found = false;
            for (GuildMember player : guildMembers.get(guild.getGuildName())) {
                if (player.getPlayerName().equals(pName)) {
                    guildName = guild.getGuildName();
                    found = true;
                    break;
                }
            }
            if (found)
                break;
        }
        return guildName;
    }

    public Guild getGuildByName(String guildName) {
        for (Guild guild : guildList) {
            if (guild.getGuildName().equals(guildName)) {
                return guild;
            }
        }
        return null;
    }

    @Nullable
    public String getChunkOwner(Chunk chunk) {
        int x = chunk.getX();
        int z = chunk.getZ();
        String ownerGuild = null;
        GuildChunk tmp = new GuildChunk(x, z);
        Set<String> guilds = guildChunkSetMap.keySet();
        for (String g : guilds) {
            if (guildChunkSetMap.get(g).contains(tmp))
                ownerGuild = g;
        }
        return ownerGuild;
    }

    public int getPlayerJob(String gName, String pName) {
        int job = 0;
        List<GuildMember> playerList = getGuildMembers().get(gName);
        for (GuildMember player : playerList) {
            if (player.getPlayerName().equals(pName)) {
                job = player.getJob();
            }
        }
        return job;
    }

    public List<String> getGuildNameList() {
        List<String> list = new ArrayList<>();
        for (Guild guild : getGuildList()) {
            list.add(guild.getGuildName());
        }
        return list;
    }

}
