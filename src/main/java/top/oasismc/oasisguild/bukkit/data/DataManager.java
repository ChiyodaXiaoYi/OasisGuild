package top.oasismc.oasisguild.bukkit.data;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import top.oasismc.oasisguild.bukkit.api.data.IDataLoader;
import top.oasismc.oasisguild.bukkit.api.data.IGuildDao;
import top.oasismc.oasisguild.bukkit.api.objects.IGuild;
import top.oasismc.oasisguild.bukkit.api.objects.IGuildApply;
import top.oasismc.oasisguild.bukkit.api.objects.IGuildChunk;
import top.oasismc.oasisguild.bukkit.api.objects.IGuildMember;
import top.oasismc.oasisguild.bukkit.data.dao.SqlGuildDao;
import top.oasismc.oasisguild.bukkit.data.loader.MysqlLoader;
import top.oasismc.oasisguild.bukkit.data.loader.SqliteLoader;
import top.oasismc.oasisguild.bukkit.objects.GuildChunk;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static top.oasismc.oasisguild.bukkit.OasisGuild.getPlugin;

public class DataManager {

    private List<IGuild> guildList;
    private Map<String, List<IGuildMember>> guildMembers;
    private Map<String, Location> guildLocationMap;
    private Map<String, List<IGuildApply>> guildApplyListMap;
    private Map<String, Set<IGuildChunk>> guildChunkSetMap;
    private final Map<String, Supplier<IGuildDao>> guildDataDaoImplMap;
    private final Map<String, Supplier<IDataLoader>> guildDataLoaderImplMap;
    private IGuildDao guildDao;
    private IDataLoader dataLoader;
    private static DataManager dataManager;
    private final String dataType;

    static {
        try {
            dataManager = new DataManager();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private DataManager() throws ClassNotFoundException {
        dataType = getPlugin().getConfig().getString("data.type", "sqlite");
        guildDataDaoImplMap = new ConcurrentHashMap<>();
        guildDataLoaderImplMap = new ConcurrentHashMap<>();
        initDataRegister();
        regDefaultDataDaoImpl();
        loadDataLoaderImpl();
        loadDataDaoImpl();
        reloadData();
    }

    public void regDataDaoImpl(String key, Supplier<IGuildDao> impl) {
        guildDataDaoImplMap.put(key, impl);
    }

    private void regDefaultDataDaoImpl() {
        regDataLoaderImpl("mysql", () -> MysqlLoader.INSTANCE);
        regDataLoaderImpl("sqlite", () -> SqliteLoader.INSTANCE);
        regDataDaoImpl("mysql", () -> SqlGuildDao.INSTANCE);
        regDataDaoImpl("sqlite", () -> SqlGuildDao.INSTANCE);
    }

    private void loadDataDaoImpl() {
        setGuildDao(guildDataDaoImplMap.getOrDefault(dataType, () -> SqlGuildDao.INSTANCE).get());
    }

    private void loadDataLoaderImpl() {
        setDataLoader(guildDataLoaderImplMap.getOrDefault(dataType, () -> MysqlLoader.INSTANCE).get());
    }

    private void initDataRegister() {
        guildList = Collections.synchronizedList(new ArrayList<>());
        guildMembers = new ConcurrentHashMap<>();
        guildLocationMap = new ConcurrentHashMap<>();
        guildApplyListMap = new ConcurrentHashMap<>();
        guildChunkSetMap = new ConcurrentHashMap<>();
    }

    public static DataManager getDataManager() {
        return dataManager;
    }

    public void regDataLoaderImpl(String type, Supplier<IDataLoader> loader) {
        guildDataLoaderImplMap.put(type, loader);
    }


    public synchronized void reloadData() {
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            List<IGuild> tmpGuildList = getGuildDao().getGuilds();
            if (tmpGuildList != null)
                guildList = tmpGuildList;

            Map<String, Location> tmpGuildLocMap = getGuildDao().getGuildLocationMap(guildList);
            if (tmpGuildLocMap != null)
                guildLocationMap = tmpGuildLocMap;

            Map<String, List<IGuildMember>> tmpGuildMembers = getGuildDao().getGuildMembers(guildList);
            if (guildMembers != null)
                guildMembers = tmpGuildMembers;

            Map<String, List<IGuildApply>> tmpGuildApplyListMap = getGuildDao().getGuildApplyListMap(guildList);
            if (guildApplyListMap != null)
                guildApplyListMap = tmpGuildApplyListMap;

            Map<String, Set<IGuildChunk>> tmpGuildChunkSetMap = getGuildDao().getGuildChunkSetMap(guildList);
            if (guildChunkSetMap != null)
                guildChunkSetMap = tmpGuildChunkSetMap;
        });
    }

    public List<IGuild> getGuildList() {
        return guildList;
    }

    public Map<String, List<IGuildMember>> getGuildMembersMap() {
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

    public void setGuildDao(IGuildDao guildDao) {
        this.guildDao = guildDao;
    }

    @Nullable
    public String getGuildNameByPlayer(String pName) {
        String guildName = null;
        for (IGuild guild : guildList) {
            boolean found = false;
            for (IGuildMember player : guildMembers.getOrDefault(guild.getGuildName(), new ArrayList<>())) {
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
        int job = -1;
        List<IGuildMember> playerList = getGuildMembersMap().get(gName);
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

    public IDataLoader getDataLoader() {
        return dataLoader;
    }

    public void setDataLoader(IDataLoader loader) {
        this.dataLoader = loader;
    }

    public String getDataType() {
        return dataType;
    }

}
