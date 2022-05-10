package top.oasismc.oasisguild.bukkit.core;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import top.oasismc.oasisguild.bukkit.api.event.guild.*;
import top.oasismc.oasisguild.bukkit.api.event.player.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import static top.oasismc.oasisguild.bukkit.OasisGuild.getPlugin;

public final class LogWriter implements Listener {

    private BufferedWriter writer;
    private int logNum;
    private long currentLogTime;
    private long lastMysqlWarnTime;
    private final File logFolder;
    private final SimpleDateFormat logFormat;
    private final SimpleDateFormat fileFormat;
    private static final LogWriter logWriter;

    static {
        logWriter = new LogWriter();
    }

    private LogWriter() {
        logNum = 0;
        currentLogTime = System.currentTimeMillis();
        lastMysqlWarnTime = System.currentTimeMillis();
        fileFormat = new SimpleDateFormat("yyyy-MM-dd");
        logFormat = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");
        logFolder = new File(getPlugin().getDataFolder(), "log");
        if (!logFolder.exists())
            logFolder.mkdir();
        resetLogFile();
    }

    public void resetLogFile() {
        File logFile = new File(logFolder, fileFormat.format(System.currentTimeMillis()) + ".txt");
        if (!logFile.exists()) {
            try {
                if (logFile.createNewFile()) {
                    currentLogTime = System.currentTimeMillis();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            if (writer != null)
                writer.close();
            writer = Files.newBufferedWriter(logFile.toPath(), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write2LogFile(String text) {
        if (!fileFormat.format(System.currentTimeMillis()).equals(fileFormat.format(currentLogTime)))
            resetLogFile();
        try {
            writer.write(logFormat.format(System.currentTimeMillis()) + " " + text);
            writer.newLine();
            if (logNum >= getPlugin().getConfig().getInt("log.writePreLine", 100)) {
                writer.flush();
                logNum = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BufferedWriter getWriter() {
        return writer;
    }

    public void mysqlWarn(SQLException e, Class<?> stack) {
        long time = System.currentTimeMillis() - lastMysqlWarnTime;
        if (time >= 7200000) {
            lastMysqlWarnTime = System.currentTimeMillis();
        }
        e.printStackTrace();
        write2LogFile(e.getMessage());
        write2LogFile("at " + stack.getName());
    }

    public static LogWriter getLogWriter() { return logWriter; }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGuildCreate(GuildCreateEvent event) {
        write2LogFile("Guild Create | "
                + "Guild: " + event.getGuildName()
                + "; Desc: " + event.getDesc()
                + "; Creator: " + event.getCreator().getName()
                + "; Location: " + getLocationText(event.getLoc())
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGuildDisband(GuildDisbandEvent event) {
        write2LogFile("Guild Disband | " + "Guild: " + event.getGuildName() + "; Disbander: " + event.getDisbander().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGuildLocChange(GuildLocChangeEvent event) {
        write2LogFile("Guild Location Change | "
                + "Guild: " + event.getGuildName()
                + "; Old Location: " + getLocationText(event.getOldLoc())
                + "; New Location: " + getLocationText(event.getNewLoc())
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGuildLevelUpEvent(GuildLevelUpEvent event) {
        write2LogFile("Guild Level Up | "
                + "Guild: " + event.getGuildName()
                + "; Old Level: " + event.getOldLevel()
                + "; Up Level: " + event.getUpNum()
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGuildPvpChangeEvent(GuildPvpChangeEvent event) {
        write2LogFile("Guild Pvp Change | " + "Guild: " + event.getGuildName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGuildChunkChange(GuildChunkChangeEvent event) {
        write2LogFile("Guild Chunk Change | " + "Guild: " + event.getGuildName() + " Type: " + event.getType());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGuildRename(GuildRenameEvent event) {
        write2LogFile("Guild Rename | " + "Old Name: " + event.getGuildName() + "; New Name: " + event.getNewName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoinGuild(PlayerJoinGuildEvent event) {
        write2LogFile("Player Join Guild | " + "Guild: " + event.getGuildName() + "; Player: " + event.getPlayer() + "; Reason: " + event.getReason());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerApplyGuild(PlayerApplyGuildEvent event) {
        write2LogFile("Player Apply Guild | " + "Guild: " + event.getGuildName() + "; Player: " + event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuitGuild(PlayerQuitGuildEvent event) {
        write2LogFile("Player Quit Guild |　" + "Guild: " + event.getGuildName() + "; Player: " + event.getPlayer() + "; Reason: " + event.getReason());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTpGuildLoc(PlayerTpGuildLocEvent event) {
        write2LogFile("Player Tp Guild Location | "
                + "Guild: " + event.getGuildName()
                + "; Player: " + event.getPlayer()
                + "; Location: " + getLocationText(event.getLoc())
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJobChange(PlayerJobChangeEvent event) {
        write2LogFile("Player Job Change | "
                + "Guild: " + event.getGuildName()
                + "; Player: " + event.getPlayer()
                + "; Old Job: " + event.getOldJob()
                + "; New Job: " + event.getNewJob()
                + "; Leader: " + event.getLeader()
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGuildTransform(GuildTransformEvent event) {
        write2LogFile("Guild Transform | "
                + "Guild: " + event.getGuildName()
                + "; Operator: " + event.getOperator()
                + "; New Leader: " + event.getNewLeader()
        );
    }

    public String getLocationText(Location loc) {
        return loc.getWorld().getName() + ", " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }

}
