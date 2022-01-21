package top.oasismc.oasisguild.data.impl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import top.oasismc.oasisguild.OasisGuild;
import top.oasismc.oasisguild.data.api.IGuildDao;
import top.oasismc.oasisguild.data.objects.Guild;
import top.oasismc.oasisguild.data.objects.GuildApply;
import top.oasismc.oasisguild.data.objects.GuildChunk;
import top.oasismc.oasisguild.data.objects.GuildMember;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static top.oasismc.oasisguild.OasisGuild.getPlugin;
import static top.oasismc.oasisguild.data.DataHandler.getDataHandler;
import static top.oasismc.oasisguild.data.util.MysqlTool.getMysqlTool;
import static top.oasismc.oasisguild.util.LogWriter.getLogWriter;
import static top.oasismc.oasisguild.util.MsgTool.color;
import static top.oasismc.oasisguild.util.MsgTool.info;

public class MysqlGuildDao implements IGuildDao {

    @Override
    public List<Guild> getGuilds() {
        List<Guild> guilds = new ArrayList<>();
        Connection conn = getMysqlTool().getConnection();
        PreparedStatement ps = null;
        try {
            if (conn.isClosed())
                conn = getMysqlTool().getConnection();
            ps = conn.prepareStatement(
                    "SELECT * FROM `GuildInfo` ORDER BY `gLevel` DESC",
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = ps.executeQuery();
            guilds = createGuildList(resultSet);
            ps.close();
        } catch (SQLException e) {
            getLogWriter().mysqlWarn(e, this.getClass());
            closeStatement(ps);
        }

        closeConnection(conn);
        return guilds;
    }

    @Override
    public Map<String, List<GuildMember>> getGuildMembers(List<Guild> guildList) {
        Connection conn = getMysqlTool().getConnection();
        Map<String, List<GuildMember>> guildMemberMap = new HashMap<>();

        for (int i = 0; i < guildList.size(); i++) {
            List<GuildMember> players;
            PreparedStatement ps = null;
            try {
                if (conn.isClosed())
                    conn = getMysqlTool().getConnection();
                conn = getMysqlTool().getConnection();
                ps = conn.prepareStatement(
                        "SELECT * FROM `GuildMembers` WHERE `gName` = ? ORDER BY `pJob`;",
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);
                ps.setString(1, guildList.get(i).getGuildName());
                ResultSet resultSet = ps.executeQuery();
                players = createPlayerList(resultSet);
                guildMemberMap.put(guildList.get(i).getGuildName(), players);
                ps.close();
            } catch (SQLException e) {
                getLogWriter().mysqlWarn(e, this.getClass());
                closeStatement(ps);
            }
        }

        closeConnection(conn);
        return guildMemberMap;
    }

    @Override
    public Map<String, Location> getGuildLocationMap(List<Guild> guildList) {
        Connection conn = getMysqlTool().getConnection();
        Map<String, Location> guildLocationMap = new HashMap<>();
        
        for (int i = 0; i < guildList.size(); i++) {
            Location location;
            PreparedStatement ps = null;
            try {
                if (conn.isClosed())
                    conn = getMysqlTool().getConnection();
                ps = conn.prepareStatement(
                        "SELECT * FROM `GuildLocation` WHERE `gName` = ?;",
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);
                ps.setString(1, guildList.get(i).getGuildName());
                ResultSet resultSet = ps.executeQuery();
                resultSet.first();
                location = new Location(Bukkit.getWorld(
                        resultSet.getString("gWorld")), resultSet.getInt("gX"), resultSet.getInt("gY"), resultSet.getInt("gZ")
                );
                guildLocationMap.put(guildList.get(i).getGuildName(), location);
                ps.close();
            } catch (SQLException e) {
                getLogWriter().mysqlWarn(e, this.getClass());
                closeStatement(ps);
            }
        }

        closeConnection(conn);
        return guildLocationMap;
    }

    @Override
    public Map<String, List<GuildApply>> getGuildApplyListMap(List<Guild> guildList) {
        Connection conn = getMysqlTool().getConnection();
        Map<String, List<GuildApply>> applyListMap = new HashMap<>();
        for (int i = 0; i < guildList.size(); i++) {
            List<GuildApply> applyList;
            PreparedStatement ps = null;
            try {
                if (conn.isClosed())
                    conn = getMysqlTool().getConnection();
                conn = getMysqlTool().getConnection();
                ps = conn.prepareStatement("SELECT * FROM `GuildApply` WHERE `gName` = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ps.setString(1, guildList.get(i).getGuildName());
                ResultSet set = ps.executeQuery();
                applyList = createGuildApplyList(set);
                applyListMap.put(guildList.get(i).getGuildName(), applyList);
                ps.close();
            } catch (SQLException e) {
                getLogWriter().mysqlWarn(e, this.getClass());
                closeStatement(ps);
            }
        }

        closeConnection(conn);
        return applyListMap;
    }

    @Override
    public Map<String, Set<GuildChunk>> getGuildChunkSetMap(List<Guild> guildList) {
        Connection conn = getMysqlTool().getConnection();
        Map<String, Set<GuildChunk>> guildChunkSetMap = new HashMap<>();

        for (int i = 0; i < guildList.size(); i++) {
            Set<GuildChunk> chunkSet;
            PreparedStatement ps = null;
            try {
                if (conn.isClosed())
                    conn = getMysqlTool().getConnection();
                ps = conn.prepareStatement("SELECT * FROM `GuildChunks` WHERE `gName` = ?", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ps.setString(1, guildList.get(i).getGuildName());
                ResultSet set = ps.executeQuery();
                chunkSet = createGuildChunkSet(set);
                guildChunkSetMap.put(guildList.get(i).getGuildName(), chunkSet);
                ps.close();
            } catch (SQLException e) {
                getLogWriter().mysqlWarn(e, this.getClass());
                closeStatement(ps);
            }
        }

        closeConnection(conn);
        return guildChunkSetMap;
    }

    @Override
    public int putApply(String gName, String pName) {
        AtomicInteger canPut = new AtomicInteger(0);
        if (getDataHandler().getGuildByName(gName) == null) {
            canPut.set(1);
        }
        List<GuildApply> guildApplyList = getDataHandler().getGuildApplyListMap().get(gName);
        if (guildApplyList != null) {
            guildApplyList.parallelStream().forEach(apply -> {
                if (apply.getPName().equals(pName)) {
                    canPut.set(1);
                }
            });
        } else {
            canPut.set(1);
        }
        if (getDataHandler().getGuildNameByPlayer(pName) != null) {
            canPut.set(-1);
        }
        if (canPut.get() == 0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Connection conn = getMysqlTool().getConnection();
                    PreparedStatement ps = null;
                    try {
                        ps = conn.prepareStatement("INSERT INTO `GuildApply`(`gName`, `pName`, `state`) VALUES (?, ?, ?);");
                        ps.setString(1, gName);
                        ps.setString(2, pName);
                        ps.setInt(3, 0);
                        ps.executeUpdate();
                        ps.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        closeStatement(ps);
                    }

                    closeConnection(conn);
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
                Connection conn = getMysqlTool().getConnection();
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
                    ps.close();
                    ps = conn.prepareStatement("INSERT INTO `GuildMembers`(`gName`, `pName`, `pJob`) VALUES (?, ?, ?)");
                    ps.setString(1, gName);
                    ps.setString(2, pName);
                    ps.setInt(3, -1);
                    ps.executeUpdate();
                    ps.close();
                    ps = conn.prepareStatement("INSERT INTO `GuildLocation`(`gName`, `gX`, `gY`, `gZ`, `gWorld`)" +
                                                    "VALUES (?, ?, ?, ?, ?)");
                    ps.setString(1, gName);
                    ps.setInt(2, (int) loc.getX());
                    ps.setInt(3, (int) loc.getY());
                    ps.setInt(4, (int) loc.getZ());
                    ps.setString(5, loc.getWorld().getName());
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    closeStatement(ps);
                }

                closeConnection(conn);
            }
        }.runTaskAsynchronously(OasisGuild.getPlugin());
        return true;
    }

    @Override
    public boolean disbandGuild(String gName) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection conn = getMysqlTool().getConnection();
                try {
                    execSql4disband("DELETE FROM `GuildInfo` WHERE `gName` = ?;", gName, conn);
                    execSql4disband("DELETE FROM `GuildMembers` WHERE `gName` = ?;", gName, conn);
                    execSql4disband("DELETE FROM `GuildApply` WHERE `gName` = ?;", gName, conn);
                    execSql4disband("DELETE FROM `GuildLocation` WHERE `gName` = ?;", gName, conn);
                    execSql4disband("DELETE FROM `GuildChunks` WHERE `gName` = ?;", gName, conn);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                closeConnection(conn);
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
                Connection conn = getMysqlTool().getConnection();
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
                        ps.setInt(3, 0);
                    } else {
                        ps = conn.prepareStatement("DELETE FROM `GuildApply` WHERE `pName` = ? AND `gName` = ?;");
                        ps.setString(1, pName);
                        ps.setString(2, gName);
                    }
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    closeStatement(ps);
                }

                closeConnection(conn);
            }
        }.runTaskAsynchronously(getPlugin());
        return true;
    }

    @Override
    public boolean memberQuit(String gName, String pName) {
        boolean canKick = false;
        if (getDataHandler().getGuildNameByPlayer(pName).equals(gName)) {
            canKick = true;
            new BukkitRunnable() {
                @Override
                public void run() {
                    Connection conn = getMysqlTool().getConnection();
                    PreparedStatement ps = null;
                    try {
                        ps = conn.prepareStatement("DELETE FROM `GuildMembers` WHERE `pName` = ?;");
                        ps.setString(1, pName);
                        ps.executeUpdate();
                        ps.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        closeStatement(ps);
                    }

                    closeConnection(conn);
                }
            }.runTaskAsynchronously(OasisGuild.getPlugin());
        }
        return canKick;
    }

    @Override
    public boolean changePvp(String gName, int pvp) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection conn = getMysqlTool().getConnection();
                PreparedStatement ps = null;
                try {
                    ps = conn.prepareStatement("UPDATE `GuildInfo` SET `gPvp` = ? WHERE `gName` = ?;");
                    ps.setInt(1, pvp);
                    ps.setString(2, gName);
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    closeStatement(ps);
                }

                closeConnection(conn);
            }
        }.runTaskAsynchronously(OasisGuild.getPlugin());
        return true;
    }

    @Override
    public boolean changeLoc(String gName, Location loc) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection conn = getMysqlTool().getConnection();
                PreparedStatement ps = null;
                try {
                    ps = conn.prepareStatement("UPDATE `GuildLocation` SET `gX` = ?, `gY` = ?, `gZ` = ?, `gWorld` = ? WHERE `gName` = ?;");
                    ps.setInt(1, (int) loc.getX());
                    ps.setInt(2, (int) loc.getY());
                    ps.setInt(3, (int) loc.getZ());
                    ps.setString(4, loc.getWorld().getName());
                    ps.setString(5, gName);
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    closeStatement(ps);
                }

                closeConnection(conn);
            }
        }.runTaskAsynchronously(OasisGuild.getPlugin());
        return true;
    }

    @Override
    public boolean levelUp(String gName, int oldLvl) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection conn = getMysqlTool().getConnection();
                PreparedStatement ps = null;
                try {
                    ps = conn.prepareStatement("UPDATE `GuildInfo` SET `gLevel` = ?, `gMaxMember` = ? WHERE `gName` = ?;");
                    ps.setInt(1, oldLvl + 1);
                    int perLvlAddMaxMember = getPlugin().getConfig().getInt("guildSettings.maxMemberNum.perLvlAdd", 1);
                    ps.setInt(2, getDataHandler().getGuildByName(gName).getMaxMember() + perLvlAddMaxMember);
                    ps.setString(3, gName);
                    ps.executeUpdate();
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    closeStatement(ps);
                }

                closeConnection(conn);
            }
        }.runTaskAsynchronously(OasisGuild.getPlugin());
        return false;
    }

    private List<GuildApply> createGuildApplyList(ResultSet resultSet) throws SQLException {
        List<GuildApply> applyList = new ArrayList<>();
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

    private List<GuildMember> createPlayerList(ResultSet resultSet) throws SQLException {
        List<GuildMember> players = new ArrayList<>();
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
    
    private List<Guild> createGuildList(ResultSet resultSet) throws SQLException {
        List<Guild> guilds = new ArrayList<>();
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

    private Set<GuildChunk> createGuildChunkSet(ResultSet resultSet) throws SQLException {
        Set<GuildChunk> chunkSet = new HashSet<>();
        if (resultSet.next()) {
            resultSet.first();
        } else {
            return chunkSet;
        }
        int cX = resultSet.getInt("cX");
        int cZ = resultSet.getInt("cZ");
        chunkSet.add(new GuildChunk(cX, cZ));
        while (resultSet.next()) {
            cX = resultSet.getInt("cX");
            cZ = resultSet.getInt("cZ");
            chunkSet.add(new GuildChunk(cX, cZ));
        }
        return chunkSet;
    }
    
    private void closeConnection(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            getLogWriter().mysqlWarn(e, this.getClass());
        }
    }
    
    private void closeStatement(PreparedStatement statement) {
        if (statement == null) {
            return;
        }
        try {
            statement.close();
        } catch (SQLException e) {
            getLogWriter().mysqlWarn(e, this.getClass());
        }
    }
    
}
