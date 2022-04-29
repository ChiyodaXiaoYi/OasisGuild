package top.oasismc.oasisguild.bukkit.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import top.oasismc.oasisguild.bukkit.core.MsgSender;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static top.oasismc.oasisguild.bukkit.OasisGuild.getPlugin;
import static top.oasismc.oasisguild.bukkit.core.LogWriter.getLogWriter;

public class MysqlTool {

    private String dbUserName, dbPassword, dbUrl;
    private static final MysqlTool mysqlTool;

    static {
        mysqlTool = new MysqlTool();
    }

    private MysqlTool() {
        initDriver();
        getConnection();
        loadDatabase();
    }

    public static MysqlTool getMysqlTool() {
        return mysqlTool;
    }

    public Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
        } catch (SQLException e) {
            getLogWriter().mysqlWarn(e, this.getClass());
        }
        return conn;
    }

    public void initDriver() {
        setConnectDBInfo();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e2) {
                e2.printStackTrace();
            }
        }
    }

    public void setConnectDBInfo() {
        YamlConfiguration config = (YamlConfiguration) getPlugin().getConfig();
        String dbHost = config.getString("data.mysql_host");
        String dbPort = config.getString("data.mysql_port");
        String dbName = config.getString("data.mysql_database");
        dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true";
        dbUserName = config.getString("data.mysql_username");
        dbPassword = config.getString("data.mysql_userPwd");
    }

    private void loadDatabase() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Connection conn = getConnection();
                PreparedStatement ps = null;
                try {
                    ps = conn.prepareStatement(
                            "CREATE TABLE IF NOT EXISTS `GuildInfo` (\n" +
                                    "`gName` varchar(20) NOT NULL,\n" +
                                    "`gIcon` varchar(20) NOT NULL DEFAULT 'STONE',\n" +
                                    "`gLevel` int(8) NOT NULL DEFAULT '0',\n" +
                                    "`gMaxMember` int(8) NOT NULL DEFAULT '5',\n " +
                                    "`gPvp` tinyint(1) NOT NULL DEFAULT '1',\n" +
                                    "`gDesc` varchar(255) NOT NULL DEFAULT '',\n" +
                                    " PRIMARY KEY (`gName`)) DEFAULT CHARSET=utf8;");
                    ps.executeUpdate();
                    ps = getConnection().prepareStatement(
                            "CREATE TABLE IF NOT EXISTS `GuildMembers` (\n" +
                                    "`gName`  varchar(20) NOT NULL ,\n" +
                                    "`pName`  varchar(20) NOT NULL ,\n" +
                                    "`pJob`  int(8) NOT NULL DEFAULT 0 ,\n" +
                                    " PRIMARY KEY (`pName`)) DEFAULT CHARSET=utf8;");
                    ps.executeUpdate();
                    ps = getConnection().prepareStatement(
                            "CREATE TABLE IF NOT EXISTS `GuildApply` (\n" +
                                    "`gName`  varchar(20) NOT NULL DEFAULT '' ,\n" +
                                    "`pName`  varchar(20) NOT NULL DEFAULT '' ,\n" +
                                    "`state`  tinyint(1) NOT NULL DEFAULT 0 \n" +
                                    ") DEFAULT CHARSET=utf8;");
                    ps.executeUpdate();
                    ps = getConnection().prepareStatement(
                            "CREATE TABLE IF NOT EXISTS `GuildLocation` (\n" +
                                    "`gName`  varchar(20) NOT NULL DEFAULT '' ,\n" +
                                    "`gX`  int(8) NOT NULL DEFAULT 0 ,\n" +
                                    "`gY`  int(8) NOT NULL DEFAULT 0 ,\n" +
                                    "`gZ`  int(8) NOT NULL DEFAULT 0 ,\n" +
                                    "`gWorld`  varchar(20) NOT NULL DEFAULT '', \n" +
                                    " PRIMARY KEY (`gName`) \n" +
                                    ") DEFAULT CHARSET=utf8;");
                    ps.executeUpdate();
                    ps = getConnection().prepareStatement(
                            "CREATE TABLE IF NOT EXISTS `GuildChunks` (\n" +
                                    "  `gName` varchar(20) NOT NULL DEFAULT '',\n" +
                                    "  `cX` int NOT NULL DEFAULT '0',\n" +
                                    "  `cZ` int NOT NULL DEFAULT '0',\n" +
                                    "  `cWorld` VARCHAR(50) NOT NULL DEFAULT ''" +
                                    ") DEFAULT CHARSET=utf8mb3;"
                    );
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (ps != null) {
                            ps.close();
                        }
                        if (conn != null) {
                            conn.close();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.runTaskAsynchronously(getPlugin());
    }

}
