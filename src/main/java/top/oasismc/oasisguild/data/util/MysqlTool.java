package top.oasismc.oasisguild.data.util;

import org.bukkit.configuration.file.YamlConfiguration;
import top.oasismc.oasisguild.OasisGuild;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static top.oasismc.oasisguild.util.LogWriter.getLogWriter;

public class MysqlTool {

    private String dbUserName, dbPassword, dbUrl;
    private static final MysqlTool mysqlTool;

    static {
        mysqlTool = new MysqlTool();
    }

    private MysqlTool() {
        initDriver();
        loadDatabase();
    }

    public static MysqlTool getMysqlTool() {
        return mysqlTool;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
        } catch (SQLException e) {
            getLogWriter().mysqlWarn(e, this.getClass());
        }
        return null;
    }

    public void initDriver() {
        YamlConfiguration config = (YamlConfiguration) OasisGuild.getPlugin().getConfig();
        String dbHost = config.getString("data.mysql_host");
        String dbPort = config.getString("data.mysql_port");
        String dbName = config.getString("data.mysql_database");
        dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true";
        dbUserName = config.getString("data.mysql_username");
        dbPassword = config.getString("data.mysql_userPwd");
        try {
            String jdbc_driver = "com.mysql.cj.jdbc.Driver";
            Class.forName(jdbc_driver);
        } catch (ClassNotFoundException e) {
            try {
                String jdbc_driver = "com.mysql.jdbc.Driver";
                Class.forName(jdbc_driver);
            } catch (ClassNotFoundException e2) {
                e2.printStackTrace();
            }
        }
    }

    private void loadDatabase() {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "CREATE TABLE IF NOT EXISTS `GuildInfo` (\n" +
                            "`gName` varchar(20) NOT NULL,\n" +
                            "`gIcon` varchar(20) NOT NULL DEFAULT 'STONE',\n" +
                            "`gLevel` int(8) NOT NULL DEFAULT '0',\n" +
                            "`gMaxMember` int(8) NOT NULL DEFAULT '5',\n " +
                            "`gPvp` tinyint(1) NOT NULL DEFAULT '1',\n" +
                            "`gDesc` varchar(255) NOT NULL DEFAULT '',\n" +
                            " PRIMARY KEY (`gName`)) DEFAULT CHARSET=utf8;");
            ps.execute();
            ps = getConnection().prepareStatement(
                    "CREATE TABLE IF NOT EXISTS `GuildMembers` (\n" +
                            "`gName`  varchar(20) NOT NULL ,\n" +
                            "`pName`  varchar(20) NOT NULL ,\n" +
                            "`pJob`  int(8) NOT NULL DEFAULT 0 ,\n" +
                            " PRIMARY KEY (`pName`)) DEFAULT CHARSET=utf8;");
            ps.execute();
            ps = getConnection().prepareStatement(
                    "CREATE TABLE IF NOT EXISTS `GuildApply` (\n" +
                            "`gName`  varchar(20) NOT NULL DEFAULT '' ,\n" +
                            "`pName`  varchar(20) NOT NULL DEFAULT '' ,\n" +
                            "`state`  tinyint(1) NOT NULL DEFAULT 0 \n" +
                            ") DEFAULT CHARSET=utf8;");
            ps.execute();
            ps = getConnection().prepareStatement(
                    "CREATE TABLE IF NOT EXISTS `GuildLocation` (\n" +
                            "`gName`  varchar(20) NOT NULL DEFAULT '' ,\n" +
                            "`gX`  int(8) NOT NULL DEFAULT 0 ,\n" +
                            "`gY`  int(8) NOT NULL DEFAULT 0 ,\n" +
                            "`gZ`  int(8) NOT NULL DEFAULT 0 ,\n" +
                            "`gWorld`  varchar(20) NOT NULL DEFAULT '', \n" +
                            " PRIMARY KEY (`gName`) \n" +
                            ") DEFAULT CHARSET=utf8;");
            ps.execute();
            ps = getConnection().prepareStatement(
                    "CREATE TABLE IF NOT EXISTS `GuildChunks` (\n" +
                            "  `gName` varchar(20) NOT NULL DEFAULT '',\n" +
                            "  `cX` int NOT NULL DEFAULT '0',\n" +
                            "  `cZ` int NOT NULL DEFAULT '0'\n" +
                            ") DEFAULT CHARSET=utf8mb3;"
            );
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
