package top.oasismc.oasisguild.bukkit.data.loader;

import org.bukkit.Bukkit;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import top.oasismc.oasisguild.bukkit.OasisGuild;
import top.oasismc.oasisguild.bukkit.api.data.IDataLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static top.oasismc.oasisguild.bukkit.core.LogWriter.getLogWriter;

public enum SqliteLoader implements IDataLoader {

    INSTANCE;

    SqliteLoader() {
        loadTables();
    }

    @Override
    public Connection getConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Connection conn = null;
        SQLiteConfig config = new SQLiteConfig();
        config.setSharedCache(true);
        config.enableRecursiveTriggers(true);
        SQLiteDataSource dataSource = new SQLiteDataSource(config);
        String path = OasisGuild.getPlugin().getDataFolder().getPath();
        dataSource.setUrl("jdbc:sqlite:" + path + "/data.db");
        try {
            conn = dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    @Override
    public void loadTables() {
        Bukkit.getScheduler().runTaskAsynchronously(OasisGuild.getPlugin(), () -> {
            Connection conn = getConnection();
            PreparedStatement ps = null;
            try {
                ps = conn.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS `GuildInfo` (\n" +
                                "`gName` varchar(20) NOT NULL,\n" +
                                "`gIcon` varchar(20) NOT NULL,\n" +
                                "`gLevel` int(8) NOT NULL,\n" +
                                "`gMaxMember` int(8) NOT NULL,\n " +
                                "`gPvp` tinyint(1) NOT NULL,\n" +
                                "`gDesc` varchar(255) NOT NULL,\n" +
                                " PRIMARY KEY (`gName`));");
                ps.executeUpdate();
                ps = getConnection().prepareStatement(
                        "CREATE TABLE IF NOT EXISTS `GuildMembers` (\n" +
                                "`gName`  varchar(20) NOT NULL ,\n" +
                                "`pName`  varchar(20) NOT NULL ,\n" +
                                "`pJob`  int(8) NOT NULL,\n" +
                                " PRIMARY KEY (`pName`));");
                ps.executeUpdate();
                ps = getConnection().prepareStatement(
                        "CREATE TABLE IF NOT EXISTS `GuildApply` (\n" +
                                "`gName`  varchar(20) NOT NULL,\n" +
                                "`pName`  varchar(20) NOT NULL,\n" +
                                "`state`  tinyint(1) NOT NULL\n" +
                                ")");
                ps.executeUpdate();
                ps = getConnection().prepareStatement(
                        "CREATE TABLE IF NOT EXISTS `GuildLocation` (\n" +
                                "`gName`  varchar(20) NOT NULL,\n" +
                                "`gX`  int(8) NOT NULL,\n" +
                                "`gY`  int(8) NOT NULL,\n" +
                                "`gZ`  int(8) NOT NULL,\n" +
                                "`gWorld`  varchar(20) NOT NULL, \n" +
                                " PRIMARY KEY (`gName`) \n" +
                                ");");
                ps.executeUpdate();
                ps = getConnection().prepareStatement(
                        "CREATE TABLE IF NOT EXISTS `GuildChunks` (\n" +
                                "  `gName` varchar(20) NOT NULL,\n" +
                                "  `cX` int NOT NULL,\n" +
                                "  `cZ` int NOT NULL,\n" +
                                "  `cWorld` VARCHAR(50) NOT NULL" +
                                ");"
                );
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                closeStatement(ps, conn);
            }
        });
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
