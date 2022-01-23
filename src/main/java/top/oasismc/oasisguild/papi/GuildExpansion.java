package top.oasismc.oasisguild.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import top.oasismc.oasisguild.OasisGuild;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static top.oasismc.oasisguild.data.DataHandler.getDataHandler;
import static top.oasismc.oasisguild.util.MsgSender.getMsgSender;

public class GuildExpansion extends PlaceholderExpansion {

    private final Map<String, Function<String, String>> papiMap;
    private static final GuildExpansion expansion;

    static {
        expansion = new GuildExpansion();
    }

    public static GuildExpansion getExpansion() { return expansion; }

    private GuildExpansion() {
        super();
        papiMap = new HashMap<>();
        regDefParams();
    }

    private void regDefParams() {
        regParam("guildname", pName -> {
            if (getDataHandler().getGuildNameByPlayer(pName) != null) {
                return getDataHandler().getGuildNameByPlayer(pName);
            } else {
                return getMsgSender().getLangFile().getConfig().getString("papi.noGuild");
            }
        });
        regParam("job", pName -> {
            String gName = getDataHandler().getGuildNameByPlayer(pName);
            if (gName != null) {
                int job = getDataHandler().getPlayerJob(gName, pName);
                return getMsgSender().getLangFile().getConfig().getString("job." + job);
            } else {
                return getMsgSender().getLangFile().getConfig().getString("papi.noGuild");
            }
        });
        regParam("guildlevel", pName -> {
            String gName = getDataHandler().getGuildNameByPlayer(pName);
            if (gName == null) {
                return getMsgSender().getLangFile().getConfig().getString("papi.noGuild");
            }
            int level = getDataHandler().getGuildByName(gName).getGuildLevel();
            return level + "";
        });
    }

    public boolean regParam(String param, Function<String, String> function) {
        return regParam(param, function, false);
    }

    public boolean regParam(String param, Function<String, String> function, boolean force) {
        if (papiMap.containsKey(param)) {
            if (!force) {
                return false;
            }
        }
        papiMap.put(param, function);
        return true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "oasisguild";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ChiyodaXiaoYi";
    }

    @Override
    public @NotNull String getVersion() {
        return OasisGuild.getPlugin().getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        return papiMap.getOrDefault(params,
                param -> getMsgSender().getLangFile().getConfig().getString("papi.null")
        ).apply(player.getName());
    }

}
