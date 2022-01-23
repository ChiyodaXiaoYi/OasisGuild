package top.oasismc.oasisguild;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import top.oasismc.oasisguild.command.GuildCommand;
import top.oasismc.oasisguild.data.util.MysqlTool;
import top.oasismc.oasisguild.listener.GuildProtectListener;
import top.oasismc.oasisguild.listener.GuildPvpListener;
import top.oasismc.oasisguild.menu.impl.DefMenuListener;
import top.oasismc.oasisguild.papi.GuildExpansion;
import top.oasismc.oasisguild.util.LogWriter;
import top.oasismc.oasisguild.util.MsgCatcher;

import java.io.IOException;
import java.sql.SQLException;

import static top.oasismc.oasisguild.util.MsgSender.info;

public final class OasisGuild extends JavaPlugin {

    private static OasisGuild plugin;

    public OasisGuild() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadModules();
        info("&3Plugin Enabled");
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        try {
            LogWriter.getLogWriter().getWriter().flush();
            LogWriter.getLogWriter().getWriter().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            MysqlTool.getMysqlTool().getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        info("Plugin Disabled");
    }

    public static OasisGuild getPlugin() {
        return plugin;
    }

    private void regPapiParams() {
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            GuildExpansion.getExpansion().register();
        }
    }

    private void loadModules() {
        loadCommands();
        loadStaticClasses();
        loadListeners();
        regPapiParams();
    }

    private void loadListeners() {
        Bukkit.getPluginManager().registerEvents(DefMenuListener.getListener(), this);
        Bukkit.getPluginManager().registerEvents(GuildPvpListener.getListener(), this);
        Bukkit.getPluginManager().registerEvents(GuildProtectListener.getListener(), this);
        Bukkit.getPluginManager().registerEvents(LogWriter.getLogWriter(), this);
        Bukkit.getPluginManager().registerEvents(MsgCatcher.getCatcher(), this);
    }

    private void loadCommands() {
        Bukkit.getPluginCommand("guild").setExecutor(GuildCommand.getGuildCommand());
        Bukkit.getPluginCommand("guild").setTabCompleter(GuildCommand.getGuildCommand());
    }

    public void loadStaticClasses() {
        try {
            Class.forName("top.oasismc.oasisguild.data.DataHandler");
            Class.forName("top.oasismc.oasisguild.menu.impl.DefMenuDrawer");
            Class.forName("top.oasismc.oasisguild.util.MsgSender");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


}
