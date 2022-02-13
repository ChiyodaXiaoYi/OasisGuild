package top.oasismc.oasisguild.data;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import top.oasismc.oasisguild.OasisGuild;
import top.oasismc.oasisguild.data.api.IGuildDao;
import top.oasismc.oasisguild.data.impl.MysqlGuildDao;
import top.oasismc.oasisguild.data.impl.SqliteGuildDao;
import top.oasismc.oasisguild.objects.api.IGuild;
import top.oasismc.oasisguild.objects.api.IGuildApply;
import top.oasismc.oasisguild.objects.api.IGuildChunk;
import top.oasismc.oasisguild.objects.api.IGuildMember;
import top.oasismc.oasisguild.objects.impl.Guild;
import top.oasismc.oasisguild.objects.impl.GuildChunk;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static top.oasismc.oasisguild.OasisGuild.getPlugin;

public class DataHandler extends BukkitRunnable {

    private List<IGuild> guildList;
    private Map<String, List<IGuildMember>> guildMembers;
    private Map<String, Location> guildLocationMap;
    private Map<String, List<IGuildApply>> guildApplyListMap;
    private Map<String, Set<IGuildChunk>> guildChunkSetMap;
    private final Map<String, Supplier<IGuildDao>> guildDataImplMap;
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
        guildDataImplMap = new ConcurrentHashMap<>();
        start(OasisGuild.getPlugin().getConfig().getInt("data.dataCacheTime", 2));
        initDataRegister();
        regDefaultDataImpl();
        loadDao();
    }

    public void regDataImpl(String key, Supplier<IGuildDao> impl) {
        guildDataImplMap.put(key, impl);
    }

    private void regDefaultDataImpl() {
        regDataImpl("mysql", () -> {
            try {
                Class.forName("top.oasismc.oasisguild.data.util.MysqlTool");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            if (!getPlugin().isEnabled())
                return null;
            return new MysqlGuildDao();
        });
        regDataImpl("sqlite", SqliteGuildDao::new);
    }

    private void loadDao() {
        String dataType = getPlugin().getConfig().getString("data.type", "sqlite");
        guildDao = guildDataImplMap.getOrDefault(dataType, SqliteGuildDao::new).get();
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

    public synchronized void getData() {
        guildList = guildDao.getGuilds();
        guildMembers = guildDao.getGuildMembers(guildList);
        guildLocationMap = guildDao.getGuildLocationMap(guildList);
        guildApplyListMap = guildDao.getGuildApplyListMap(guildList);
        guildChunkSetMap = guildDao.getGuildChunkSetMap(guildList);
    }

    public void start(int interval) {
        this.runTaskTimerAsynchronously(OasisGuild.getPlugin(), interval * 20L, interval * 20L);
    }

    public List<IGuild> getGuildList() {
        return guildList;
    }

    public Map<String, List<IGuildMember>> getGuildMembers() {
        return guildMembers;
    }

    public Map<String, Location> getGuildLocationMap() {
        return guildLocationMap;
    }

    public Map<String, List<IGuildApply>> getGuildApplyListMap() {
        return guildApplyListMap;
    }

    public List<IGuildApply> getGuildApplyList(String gName) {
        return guildApplyListMap.getOrDefault(gName, new ArrayList<>());
    }

    public IGuildDao getGuildDao() {
        return guildDao;
    }

    @Nullable
    public String getGuildNameByPlayer(String pName) {
        String guildName = null;
        for (IGuild guild : guildList) {
            boolean found = false;
            for (IGuildMember player : guildMembers.get(guild.getGuildName())) {
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

    public IGuild getGuildByName(String guildName) {
        for (IGuild guild : guildList) {
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
        String world = chunk.getWorld().getName();
        String ownerGuild = null;
        GuildChunk tmp = new GuildChunk(x, z, world);
        Set<String> guilds = guildChunkSetMap.keySet();
        for (String g : guilds) {
            if (guildChunkSetMap.get(g).contains(tmp))
                ownerGuild = g;
        }
        return ownerGuild;
    }

    public int getPlayerJob(String gName, String pName) {
        int job = 0;
        List<IGuildMember> playerList = getGuildMembers().get(gName);
        for (IGuildMember player : playerList) {
            if (player.getPlayerName().equals(pName)) {
                job = player.getJob();
                break;
            }
        }
        return job;
    }

    public List<String> getGuildNameList() {
        List<String> list = new ArrayList<>();
        for (IGuild guild : getGuildList()) {
            list.add(guild.getGuildName());
        }
        return list;
    }

    public Set<IGuildChunk> getGuildChunkSet(String guildName) {
        return guildChunkSetMap.getOrDefault(guildName, new HashSet<>());
    }
}
