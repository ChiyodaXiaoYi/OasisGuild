package top.oasismc.oasisguild.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import top.oasismc.oasisguild.bukkit.command.GuildCommand;
import top.oasismc.oasisguild.bukkit.data.MysqlTool;
import top.oasismc.oasisguild.bukkit.listener.GuildChunkListener;
import top.oasismc.oasisguild.bukkit.listener.GuildEventListener;
import top.oasismc.oasisguild.bukkit.listener.GuildPvpListener;
import top.oasismc.oasisguild.bukkit.menu.GuildMenuManager;
import top.oasismc.oasisguild.bukkit.papi.GuildExpansion;
import top.oasismc.oasisguild.bukkit.core.LogWriter;
import top.oasismc.oasisguild.bukkit.util.MsgCatcher;

import java.io.IOException;
import java.sql.SQLException;

import static top.oasismc.oasisguild.bukkit.core.MsgSender.info;

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
        if (getConfig().getString("data.type", "sqlite").equals("mysql")) {
            if (MysqlTool.getMysqlTool() != null && MysqlTool.getMysqlTool().getConnection() != null) {
                try {
                    MysqlTool.getMysqlTool().getConnection().close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            LogWriter.getLogWriter().getWriter().flush();
            LogWriter.getLogWriter().getWriter().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        info("&cPlugin Disabled");
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
        loadStaticClasses();
        if (!this.isEnabled())
            return;
        loadCommands();
        loadListeners();
        regPapiParams();
    }

    private void loadListeners() {
        Bukkit.getPluginManager().registerEvents(GuildMenuManager.getMenuManager(), this);
        Bukkit.getPluginManager().registerEvents(GuildPvpListener.getListener(), this);
        Bukkit.getPluginManager().registerEvents(GuildChunkListener.getListener(), this);
        Bukkit.getPluginManager().registerEvents(LogWriter.getLogWriter(), this);
        Bukkit.getPluginManager().registerEvents(MsgCatcher.getCatcher(), this);
        Bukkit.getPluginManager().registerEvents(GuildEventListener.getListener(), this);
    }

    private void loadCommands() {
        PluginCommand guildCommand = Bukkit.getPluginCommand("guild");
        if (guildCommand == null)
            return;
        guildCommand.setExecutor(GuildCommand.getGuildCommand());
        guildCommand.setTabCompleter(GuildCommand.getGuildCommand());
    }

    public void loadStaticClasses() {
        try {
            Class.forName("top.oasismc.oasisguild.bukkit.data.DataManager");
            Class.forName("top.oasismc.oasisguild.bukkit.core.MsgSender");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}