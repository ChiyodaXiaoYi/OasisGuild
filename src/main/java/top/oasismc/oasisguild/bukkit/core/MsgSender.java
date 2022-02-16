package top.oasismc.oasisguild.bukkit.core;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import top.oasismc.oasisguild.bukkit.OasisGuild;
import top.oasismc.oasisguild.bukkit.api.objects.IGuild;

import static org.bukkit.ChatColor.translateAlternateColorCodes;
import static top.oasismc.oasisguild.bukkit.OasisGuild.*;

public final class MsgSender {

    private ConfigFile langFile;
    private static final MsgSender MSG_SENDER;

    static {
        MSG_SENDER = new MsgSender();
    }

    private MsgSender() {
        setLangFile();
    }

    public static String color(String text) {
        return translateAlternateColorCodes('&', text);
    }

    public static void info(String text) {
        Bukkit.getConsoleSender().sendMessage(color("&8[&3Oasis&bGuild&8] &bINFO &8| &r" + text));
    }

    public static void sendMsg(CommandSender sender, String key) {
        sendMsg(sender, key, "", "", false, null);
    }

    public static void sendMsg4replacePlayer(CommandSender sender, String key, String playerName) {
        sendMsg(sender, key, "%player%", playerName, false, null);
    }

    public static void sendMsg4replaceOtherStr(CommandSender sender, String key, String replaceStr, String other) {
        sendMsg(sender, key, replaceStr, other, false, null);
    }

    public static void sendMsg4replaceGuild(CommandSender sender, String key, IGuild guild) {
        sendMsg(sender, key, "", "", true, guild.getGuildName());
    }

    public static void sendMsg(CommandSender sender, String key, String pName, IGuild guild) {
        sendMsg(sender, key, "%player%", pName, true, guild.getGuildName());
    }

    public static void sendMsg(CommandSender sender, String key, String replaceStr, String other, boolean replaceGuild, String gName) {
        String prefix = getMsgSender().langFile.getConfig().getString("messages.prefix", "");
        if (sender == null) {
            return;
        }
        String message = getMsgSender().getLangFile().getConfig().getString("messages." + key, key);
        message = message.replace(replaceStr, other);
        if (replaceGuild) {
            message = message.replace("%guild%", gName);
        }
        message = message.replace("%version%", OasisGuild.getPlugin().getDescription().getVersion());
        sender.sendMessage(color(prefix + message));
    }

    public static MsgSender getMsgSender() {
        return MSG_SENDER;
    }

    public void setLangFile() {
        String lang = getPlugin().getConfig().getString("language", "zh_cn");
        langFile = new ConfigFile("lang/" + lang + ".yml");
    }

    public ConfigFile getLangFile() {
        return langFile;
    }

}