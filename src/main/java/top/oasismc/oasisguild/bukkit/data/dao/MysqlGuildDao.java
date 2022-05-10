package top.oasismc.oasisguild.bukkit.data.dao;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import top.oasismc.oasisguild.bukkit.OasisGuild;
import top.oasismc.oasisguild.bukkit.api.data.IGuildDao;
import top.oasismc.oasisguild.bukkit.api.objects.IGuild;
import top.oasismc.oasisguild.bukkit.api.objects.IGuildApply;
import top.oasismc.oasisguild.bukkit.api.objects.IGuildChunk;
import top.oasismc.oasisguild.bukkit.api.objects.IGuildMember;
import top.oasismc.oasisguild.bukkit.data.DataManager;
import top.oasismc.oasisguild.bukkit.data.MysqlTool;
import top.oasismc.oasisguild.bukkit.objects.Guild;
import top.oasismc.oasisguild.bukkit.objects.GuildApply;
import top.oasismc.oasisguild.bukkit.objects.GuildChunk;
import top.oasismc.oasisguild.bukkit.objects.GuildMember;
import top.oasismc.oasisguild.bungee.listener.BungeeAdapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static top.oasismc.oasisguild.bukkit.OasisGuild.getPlugin;
import static top.oasismc.oasisguild.bukkit.api.job.Jobs.LEADER;
import static top.oasismc.oasisguild.bukkit.api.job.Jobs.NORMAL;
import static top.oasismc.oasisguild.bukkit.core.LogWriter.getLogWriter;

public final class MysqlGuildDao implements IGuildDao {

    @Override
    public List<IGuild> getGuilds() {
        List<IGuild> guilds = new ArrayList<>();
        Connection conn = MysqlTool.getMysqlTool().getConnection();
        PreparedStatement ps = null;
        try {
            if (conn.isClosed())
                conn = MysqlTool.getMysqlTool().getConnection();
            ps = conn.prepareStatement(
                    "SELECT * FROM `GuildInfo` ORDER BY `gLevel` DESC",
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = ps.executeQuery();
            guilds = createGuildList(resultSet);
        } catch (SQLException e) {
            getLogWriter().mysqlWarn(e, this.getClass());
        } finally {
            closeStatement(ps, conn);
        }
        return guilds;
    }

    @Override
    public Map<String, List<IGuildMember>> getGuildMembers(List<IGuild> guildList) {
        Connection conn = MysqlTool.getMysqlTool().getConnection();
        Map<String, List<IGuildMember>> guildMemberMap = new ConcurrentHashMap<>();
        PreparedStatement ps = null;
        try {
            for (IGuild guild : guildList) {
                List<IGuildMember> players;
                ps = conn.prepareStatement(
                        "SELECT * FROM `GuildMembers` WHERE `gName` = ? ORDER BY `pJob` DESC;",
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);
                ps.setString(1, guild.getGuildName());
                ResultSet resultSet = ps.executeQuery();
                players = createPlayerList(resultSet);
                guildMemberMap.put(guild.getGuildName(), players);
            }
        } catch (SQLException e) {
            getLogWriter().mysqlWarn(e, this.getClass());
        } finally {
            closeStatement(ps, conn);
        }

        return guildMemberMap;
    }

    @Override
    public Map<String, Location> getGuildLocationMap(List<IGuild> guildList) {
        Connection conn = MysqlTool.getMysqlTool().getConnection();
        Map<String, Location> guildLocationMap = new ConcurrentHashMap<>();

        PreparedStatement ps = null;
        try {
            for (IGuild guild : guildList) {
                Location location;
                ps = conn.prepareStatement(
                        "SELECT * FROM `GuildLocation` WHERE `gName` = ?;",
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);
                ps.setString(1, guild.getGuildName());
                ResultSet resultSet = ps.executeQuery();
                resultSet.first();
                location = new Location(Bukkit.getWorld(
                        resultSet.getString("gWorld")), resultSet.getInt("gX"), resultSet.getInt("gY"), resultSet.getInt("gZ")
                );
                guildLocationMap.put(guild.getGuildName(), location);
            }
        } catch (SQLException e) {
            getLogWriter().mysqlWarn(e, this.getClass());
        } finally {
            closeStatement(ps, conn);
        }
        return guildLocationMap;
    }

    @Override
    public Map<String, List<IGuildApply>> getGuildApplyListMap(List<IGuild> guildList) {
        Connection conn = MysqlTool.getMysqlTool().getConnection();
        Map<String, List<IGuildApply>> applyListMap = new ConcurrentHashMap<>();
        PreparedStatement ps = null;
        try {
            for (IGuild guild : guildList) {
                List<IGuildApply> applyList;
                conn = MysqlTool.getMysqlTool().getConnection();
                ps = conn.prepareStatement("SELECT * FROM `GuildApply` WHERE `gName` = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ps.setString(1, guild.getGuildName());
                ResultSet set = ps.executeQuery();
                applyList = createGuildApplyList(set);
                applyListMap.put(guild.getGuildName(), applyList);
            }
        } catch (SQLException e) {
            getLogWriter().mysqlWarn(e, this.getClass());
        } finally {
            closeStatement(ps, conn);
        }
        return applyListMap;
    }

    @Override
    public Map<String, Set<IGuildChunk>> getGuildChunkSetMap(List<IGuild> guildList) {
        Connection conn = MysqlTool.getMysqlTool().getConnection();
        Map<String, Set<IGuildChunk>> guildChunkSetMap = new ConcurrentHashMap<>();

        PreparedStatement ps = null;
        try {
            for (IGuild guild : guildList) {
                Set<IGuildChunk> chunkSet;
                ps = conn.prepareStatement("SELECT * FROM `GuildChunks` WHERE `gName` = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ps.setString(1, guild.getGuildName());
                ResultSet set = ps.executeQuery();
                chunkSet = createGuildChunkSet(set);
                guildChunkSetMap.put(guild.getGuildName(), chunkSet);
            }
        } catch (SQLException e) {
            getLogWriter().mysqlWarn(e, this.getClass());
        } finally {
            closeStatement(ps, conn);
        }
        return guildChunkSetMap;
    }

    @Override
    public int putApply(String gName, String pName) {
        AtomicInteger canPut = new AtomicInteger(0);
        if (DataManager.getDataManager().getGuildByName(gName) == null) {
            canPut.set(1);
        }
        List<IGuildApply> guildApplyList = DataManager.getDataManager().getGuildApplyListMap().get(gName);
        if (guildApplyList != null) {
            guildApplyList.parallelStream().forEach(apply -> {
                if (apply.getPName().equals(pName)) {
                    canPut.set(1);
                }
            });
        } else {
            canPut.set(1);
        }
        if (DataManager.getDataManager().getGuildNameByPlayer(pName) != null) {
            canPut.set(-1);
        }
        if (canPut.get() == 0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Connection conn = MysqlTool.getMysqlTool().getConnection();
                    PreparedStatement ps = null;
                    try {
                        ps = conn.prepareStatement("INSERT INTO `GuildApply`(`gName`, `pName`, `state`) VALUES (?, ?, ?);");
                        ps.setString(1, gName);
                        ps.setString(2, pName);
                        ps.setInt(3, 0);
                        ps.executeUpdate();
                        DataManager.getDataManager().reloadData();
                        BungeeAdapter.INSTANCE.sendUpdateDataMsg();
                    } catch (SQLException e) {
                        getLogWriter().mysqlWarn(e, this.getClass());
                    } finally {
                        closeStatement(ps, conn);
                    }
                }
            }.runTaskAsynchronously(OasisGuild.getPlugin());
        }
        return canPut.get();
    }

    @Override
    public boolean createGuild(String gName, String pName, String desc, Location loc) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection conn = MysqlTool.getMysqlTool().getConnection();
                PreparedStatement ps = null;
                try {
                    ps = conn.prepareStatement("INSERT " +
                            "INTO `GuildInfo`(`gName`, `gIcon`, `gLevel`, `gPvp`, `gMaxMember`, `gDesc`)" +
                            "VALUES (?, ?, ?, ?, ? ,?);");
                    ps.setString(1, gName);
                    ps.setString(2, getPlugin().getConfig().getString("guildSettings.defaultIcon", "WHITE_BANNER"));
                    ps.setInt(3, 0);
                    if (getPlugin().getConfig().getBoolean("guildSettings.pvp")) {
                        ps.setInt(4, 1);
                    } else {
                        ps.setInt(4, 0);
                    }
                    ps.setInt(5, getPlugin().getConfig().getInt("guildSettings.maxMemberNum.default"));
                    ps.setString(6, desc);
                    ps.executeUpdate();
                    ps = conn.prepareStatement("INSERT INTO `GuildMembers`(`gName`, `pName`, `pJob`) VALUES (?, ?, ?)");
                    ps.setString(1, gName);
                    ps.setString(2, pName);
                    ps.setInt(3, LEADER);
                    ps.executeUpdate();
                    ps = conn.prepareStatement("INSERT INTO `GuildLocation`(`gName`, `gX`, `gY`, `gZ`, `gWorld`)" +
                                                    "VALUES (?, ?, ?, ?, ?)");
                    ps.setString(1, gName);
                    ps.setInt(2, (int) loc.getX());
                    ps.setInt(3, (int) loc.getY());
                    ps.setInt(4, (int) loc.getZ());
                    ps.setString(5, loc.getWorld().getName());
                    ps.executeUpdate();
                    DataManager.getDataManager().reloadData();
                    BungeeAdapter.INSTANCE.sendUpdateDataMsg();
                } catch (SQLException e) {
                    getLogWriter().mysqlWarn(e, this.getClass());
                } finally {
                    closeStatement(ps, conn);
                }
            }
        }.runTaskAsynchronously(OasisGuild.getPlugin());
        return true;
    }

    @Override
    public int addGuildChunk(String gName, List<IGuildChunk> chunkList) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection conn = MysqlTool.getMysqlTool().getConnection();
                chunkList.parallelStream().forEach((chunk) -> {
                    PreparedStatement ps = null;
                    try {
                        ps = conn.prepareStatement("INSERT INTO `GuildChunks`(`gName`, `cX`, `cZ`, `cWorld`) VALUES (?, ?, ?, ?);");
                        ps.setString(1, gName);
                        ps.setInt(2, chunk.getX());
                        ps.setInt(3, chunk.getZ());
                        ps.setString(4, chunk.getWorld());
                        ps.executeUpdate();
                        DataManager.getDataManager().reloadData();
                        BungeeAdapter.INSTANCE.sendUpdateDataMsg();
                    } catch (SQLException e) {
                        getLogWriter().mysqlWarn(e, this.getClass());
                    } finally {
                        closeStatement(ps, conn);
                    }
                });
            }
        }.runTaskAsynchronously(getPlugin());
        return 0;
    }

    @Override
    public boolean disbandGuild(String gName) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection conn = MysqlTool.getMysqlTool().getConnection();
                try {
                    execSql4disband("DELETE FROM `GuildInfo` WHERE `gName` = ?;", gName, conn);
                    execSql4disband("DELETE FROM `GuildMembers` WHERE `gName` = ?;", gName, conn);
                    execSql4disband("DELETE FROM `GuildApply` WHERE `gName` = ?;", gName, conn);
                    execSql4disband("DELETE FROM `GuildLocation` WHERE `gName` = ?;", gName, conn);
                    execSql4disband("DELETE FROM `GuildChunks` WHERE `gName` = ?;", gName, conn);
                    conn.close();
                } catch (SQLException e) {
                    getLogWriter().mysqlWarn(e, this.getClass());
                }
                DataManager.getDataManager().reloadData();
                BungeeAdapter.INSTANCE.sendUpdateDataMsg();
            }
        }.runTaskAsynchronously(OasisGuild.getPlugin());
        return true;
    }
    private void execSql4disband(String sql, String gName, Connection conn) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, gName);
        ps.executeUpdate();
        ps.close();
    }

    @Override
    public boolean handleApply(String gName, String type, String pName) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection conn = MysqlTool.getMysqlTool().getConnection();
                PreparedStatement ps = null;
                try {
                    if ("accept".equals(type)) {
                        ps = conn.prepareStatement("DELETE FROM `GuildApply` WHERE `pName` = ?;");
                        ps.setString(1, pName);
                        ps.executeUpdate();
                        ps = conn.prepareStatement("INSERT INTO `GuildMembers`(`gName`, `pName`, `pJob`)" +
                                "VALUES (?, ?, ?);");
                        ps.setString(1, gName);
                        ps.setString(2, pName);
                        ps.setInt(3, NORMAL);
                    } else {
                        ps = conn.prepareStatement("DELETE FROM `GuildApply` WHERE `pName` = ? AND `gName` = ?;");
                        ps.setString(1, pName);
                        ps.setString(2, gName);
                    }
                    ps.executeUpdate();
                    DataManager.getDataManager().reloadData();
                    BungeeAdapter.INSTANCE.sendUpdateDataMsg();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    closeStatement(ps, conn);
                }
            }
        }.runTaskAsynchronously(getPlugin());
        return true;
    }

    @Override
    public boolean memberQuit(String gName, String pName) {
        boolean canKick = false;
        if (DataManager.getDataManager().getGuildNameByPlayer(pName).equals(gName)) {
            canKick = true;
            new BukkitRunnable() {
                @Override
                public void run() {
                    Connection conn = MysqlTool.getMysqlTool().getConnection();
                    PreparedStatement ps = null;
                    try {
                        ps = conn.prepareStatement("DELETE FROM `GuildMembers` WHERE `pName` = ?;");
                        ps.setString(1, pName);
                        ps.executeUpdate();
                        DataManager.getDataManager().reloadData();
                        BungeeAdapter.INSTANCE.sendUpdateDataMsg();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        closeStatement(ps, conn);
                    }
                }
            }.runTaskAsynchronously(OasisGuild.getPlugin());
        }
        return canKick;
    }

    @Override
    public boolean removeGuildChunk(String gName, List<IGuildChunk> chunkList) {
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            Connection conn = MysqlTool.getMysqlTool().getConnection();
            chunkList.parallelStream().forEach((chunk) -> {
                PreparedStatement ps = null;
                try {
                    ps = conn.prepareStatement("delete from GuildChunks where gName = ? and cWorld = ? and cX = ? and cZ = ?;");
                    ps.setString(1, gName);
                    ps.setString(2, chunk.getWorld());
                    ps.setInt(3, chunk.getX());
                    ps.setInt(4, chunk.getZ());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    getLogWriter().mysqlWarn(e, this.getClass());
                } finally {
                    closeStatement(ps, conn);
                }
            });
            DataManager.getDataManager().reloadData();
            BungeeAdapter.INSTANCE.sendUpdateDataMsg();
        });
        return true;
    }

    @Override
    public boolean changePvp(String gName, int pvp) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection conn = MysqlTool.getMysqlTool().getConnection();
                PreparedStatement ps = null;
                try {
                    ps = conn.prepareStatement("UPDATE `GuildInfo` SET `gPvp` = ? WHERE `gName` = ?;");
                    ps.setInt(1, pvp);
                    ps.setString(2, gName);
                    ps.executeUpdate();
                    DataManager.getDataManager().reloadData();
                    BungeeAdapter.INSTANCE.sendUpdateDataMsg();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    closeStatement(ps, conn);
                }
            }
        }.runTaskAsynchronously(OasisGuild.getPlugin());
        return true;
    }

    @Override
    public boolean changeLoc(String gName, Location loc) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection conn = MysqlTool.getMysqlTool().getConnection();
                PreparedStatement ps = null;
                try {
                    ps = conn.prepareStatement("UPDATE `GuildLocation` SET `gX` = ?, `gY` = ?, `gZ` = ?, `gWorld` = ? WHERE `gName` = ?;");
                    ps.setInt(1, (int) loc.getX());
                    ps.setInt(2, (int) loc.getY());
                    ps.setInt(3, (int) loc.getZ());
                    ps.setString(4, loc.getWorld().getName());
                    ps.setString(5, gName);
                    ps.executeUpdate();
                    DataManager.getDataManager().reloadData();
                    BungeeAdapter.INSTANCE.sendUpdateDataMsg();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    closeStatement(ps, conn);
                }
            }
        }.runTaskAsynchronously(OasisGuild.getPlugin());
        return true;
    }

    @Override
    public boolean changeMemberJob(String gName, String pName, int newJob) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection conn = MysqlTool.getMysqlTool().getConnection();
                PreparedStatement ps = null;
                try {
                    ps = conn.prepareStatement("UPDATE `GuildMembers` SET `pJob` = ? WHERE `pName` = ? AND `gName` = ?;");
                    ps.setInt(1, newJob);
                    ps.setString(2, pName);
                    ps.setString(3, gName);
                    ps.executeUpdate();
                    ps.close();
                    conn.close();
                    DataManager.getDataManager().reloadData();
                    BungeeAdapter.INSTANCE.sendUpdateDataMsg();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    closeStatement(ps, conn);
                }
            }
        }.runTaskAsynchronously(getPlugin());
        return true;
    }

    @Override
    public boolean levelUp(String gName, int oldLvl) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection conn = MysqlTool.getMysqlTool().getConnection();
                PreparedStatement ps = null;
                try {
                    ps = conn.prepareStatement("UPDATE `GuildInfo` SET `gLevel` = ?, `gMaxMember` = ? WHERE `gName` = ?;");
                    ps.setInt(1, oldLvl + 1);
                    int perLvlAddMaxMember = getPlugin().getConfig().getInt("guildSettings.maxMemberNum.perLvlAdd", 1);
                    ps.setInt(2, DataManager.getDataManager().getGuildByName(gName).getMaxMember() + perLvlAddMaxMember);
                    ps.setString(3, gName);
                    ps.executeUpdate();
                    DataManager.getDataManager().reloadData();
                    BungeeAdapter.INSTANCE.sendUpdateDataMsg();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    closeStatement(ps, conn);
                }
            }
        }.runTaskAsynchronously(OasisGuild.getPlugin());
        return true;
    }

    @Override
    public boolean transformGuild(String gName, String oldLeader, String pName) {
        Bukkit.getScheduler().runTaskAsynchronously(OasisGuild.getPlugin(), () -> {
            Connection conn = MysqlTool.getMysqlTool().getConnection();
            PreparedStatement ps = null;
            try {
                ps = conn.prepareStatement("UPDATE `GuildMembers` SET `pJob` = ? WHERE `pName` = ?");
                ps.setInt(1, LEADER);
                ps.setString(2, pName);
                ps.executeUpdate();
                ps.setInt(1, NORMAL);
                ps.setString(2, oldLeader);
                ps.executeUpdate();
                DataManager.getDataManager().reloadData();
                BungeeAdapter.INSTANCE.sendUpdateDataMsg();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                closeStatement(ps, conn);
            }
        });
        return false;
    }

    @Override
    public void guildRename(String gName, String newName) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection conn = MysqlTool.getMysqlTool().getConnection();
                PreparedStatement ps = null;
                try {
                    String []tableNameList = {"GuildInfo", "GuildMembers", "GuildApply", "GuildChunks", "GuildLocation"};
                    for (String table : tableNameList) {
                        ps = conn.prepareStatement("UPDATE `" + table + "` SET `gName` = ? WHERE `gName` = ?;");
                        ps.setString(1, newName);
                        ps.setString(2, gName);
                        ps.executeUpdate();
                    }
                    DataManager.getDataManager().reloadData();
                    BungeeAdapter.INSTANCE.sendUpdateDataMsg();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    closeStatement(ps, conn);
                }
            }
        }.runTaskAsynchronously(getPlugin());
    }

    @Override
    public void guildResetDesc(String gName, String newDesc) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection conn = MysqlTool.getMysqlTool().getConnection();
                PreparedStatement ps = null;
                try {
                    ps = conn.prepareStatement("UPDATE `GuildInfo` SET `gDesc` = ? WHERE `gName` = ?;");
                    ps.setString(1, newDesc);
                    ps.setString(2, gName);
                    ps.executeUpdate();
                    DataManager.getDataManager().reloadData();
                    BungeeAdapter.INSTANCE.sendUpdateDataMsg();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    closeStatement(ps, conn);
                }
            }
        }.runTaskAsynchronously(getPlugin());
    }

    private List<IGuildApply> createGuildApplyList(ResultSet resultSet) throws SQLException {
        List<IGuildApply> applyList = new ArrayList<>();
        if (resultSet.next()) {
            resultSet.first();
        } else {
            return applyList;
        }
        String pName = resultSet.getString("pName");
        int state = resultSet.getInt("state");
        applyList.add(new GuildApply(pName, state));
        while (resultSet.next()) {
            pName = resultSet.getString("pName");
            state = resultSet.getInt("state");
            applyList.add(new GuildApply(pName, state));
        }
        return applyList;
    }

    private List<IGuildMember> createPlayerList(ResultSet resultSet) throws SQLException {
        List<IGuildMember> players = new ArrayList<>();
        if (resultSet.next()) {
            resultSet.first();
        } else {
            return players;
        }
        String pName = resultSet.getString("pName");
        int pJob = resultSet.getInt("pJob");
        players.add(new GuildMember(pName, pJob));
        while (resultSet.next()) {
            pName = resultSet.getString("pName");
            pJob = resultSet.getInt("pJob");
            players.add(new GuildMember(pName, pJob));
        }
        return players;
    }
    
    private List<IGuild> createGuildList(ResultSet resultSet) throws SQLException {
        List<IGuild> guilds = new ArrayList<>();
        if (resultSet.next()) {
            resultSet.first();
        } else {
            return guilds;
        }
        String gName = resultSet.getString("gName");
        String icon = resultSet.getString("gIcon");
        short maxMember = resultSet.getShort("gMaxMember");
        short level = resultSet.getShort("gLevel");
        byte pvp = resultSet.getByte("gPvp");
        String desc = resultSet.getString("gDesc");
        guilds.add(new Guild(gName, level, maxMember, icon, pvp, desc));
        while (resultSet.next()) {
            gName = resultSet.getString("gName");
            icon = resultSet.getString("gIcon");
            maxMember = resultSet.getShort("gMaxMember");
            level = resultSet.getShort("gLevel");
            pvp = resultSet.getByte("gPvp");
            desc = resultSet.getString("gDesc");
            guilds.add(new Guild(gName, level, maxMember, icon, pvp, desc));
        }
        return guilds;
    }

    private Set<IGuildChunk> createGuildChunkSet(ResultSet resultSet) throws SQLException {
        Set<IGuildChunk> chunkSet = new HashSet<>();
        if (resultSet.next()) {
            resultSet.first();
        } else {
            return chunkSet;
        }
        int cX = resultSet.getInt("cX");
        int cZ = resultSet.getInt("cZ");
        String cWorld = resultSet.getString("cWorld");
        chunkSet.add(new GuildChunk(cX, cZ, cWorld));
        while (resultSet.next()) {
            cX = resultSet.getInt("cX");
            cZ = resultSet.getInt("cZ");
            cWorld = resultSet.getString("cWorld");
            chunkSet.add(new GuildChunk(cX, cZ, cWorld));
        }
        return chunkSet;
    }
    
    private void closeStatement(PreparedStatement statement, Connection conn) {
        try {
            if (statement != null)
                statement.close();
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
            getLogWriter().mysqlWarn(e, this.getClass());
        }
    }

}
