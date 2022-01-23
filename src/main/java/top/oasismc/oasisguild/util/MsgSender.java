package top.oasismc.oasisguild.util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import top.oasismc.oasisguild.OasisGuild;
import top.oasismc.oasisguild.config.ConfigFile;
import top.oasismc.oasisguild.data.objects.Guild;

import static org.bukkit.ChatColor.translateAlternateColorCodes;
import static top.oasismc.oasisguild.OasisGuild.*;

public class MsgSender {

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
        sendMsg(sender, key, false, null, false, null);
    }

    public static void sendMsg(CommandSender sender, String key, String pName) {
        sendMsg(sender, key, true, pName, false, null);
    }

    public static void sendMsg(CommandSender sender, String key, Guild guild) {
        sendMsg(sender, key, false, null, true, guild.getGuildName());
    }

    public static void sendMsg(CommandSender sender, String key, String pName, Guild guild) {
        sendMsg(sender, key, true, pName, true, guild.getGuildName());
    }

    public static void sendMsg(CommandSender sender, String key, boolean replaceOther, String other, boolean replaceGuild, String gName) {
        String prefix = getMsgSender().langFile.getConfig().getString("messages.prefix", "");
        if (sender == null) {
            return;
        }
        String message = getMsgSender().getLangFile().getConfig().getString("messages." + key, key);
        if (!replaceOther) {
            message = message.replace("%player%", sender.getName());
        } else {
            message = message.replace("%player%", other);
        }
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