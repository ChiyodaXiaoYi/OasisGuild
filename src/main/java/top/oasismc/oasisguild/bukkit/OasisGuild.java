package top.oasismc.oasisguild.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import top.oasismc.oasisguild.bukkit.command.GuildAdminCommand;
import top.oasismc.oasisguild.bukkit.command.GuildCommand;
import top.oasismc.oasisguild.bukkit.core.LogWriter;
import top.oasismc.oasisguild.bukkit.core.MsgSender;
import top.oasismc.oasisguild.bukkit.data.loader.MysqlLoader;
import top.oasismc.oasisguild.bukkit.listener.GuildChunkListener;
import top.oasismc.oasisguild.bukkit.listener.GuildEventListener;
import top.oasismc.oasisguild.bukkit.listener.GuildPvpListener;
import top.oasismc.oasisguild.bukkit.menu.GuildMenuManager;
import top.oasismc.oasisguild.bukkit.papi.GuildExpansion;
import top.oasismc.oasisguild.bukkit.util.MsgCatcher;
import top.oasismc.oasisguild.bungee.listener.BungeeAdapter;

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
            if (MysqlLoader.getMysqlTool() != null && MysqlLoader.getMysqlTool().getConnection() != null) {
                try {
                    MysqlLoader.getMysqlTool().getConnection().close();
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
        loadBungeeSupport();
    }

    private void loadBungeeSupport() {
        if (getConfig().getBoolean("bungee_support", false)) {
            this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
            this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", BungeeAdapter.INSTANCE);
            Bukkit.getPluginManager().registerEvents(BungeeAdapter.INSTANCE, this);
            MsgSender.info("&3Enable Bungee support");
        }
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

        PluginCommand guildAdminCommand = Bukkit.getPluginCommand("guildadmin");
        if (guildAdminCommand == null)
            return;
        guildAdminCommand.setExecutor(GuildAdminCommand.INSTANCE);
        guildAdminCommand.setTabCompleter(GuildAdminCommand.INSTANCE);
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
