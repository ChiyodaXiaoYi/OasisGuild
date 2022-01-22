package top.oasismc.oasisguild.util;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MsgCatcher implements Listener {

    private final Map<UUID, Boolean> catchSwitchMap;
    private final Map<UUID, String> catchMsgMap;
    private final Map<UUID, Consumer<String>> catchFunMap;
    private static final MsgCatcher catcher;

    static {
        catcher = new MsgCatcher();
    }

    private MsgCatcher() {
        catchMsgMap = new ConcurrentHashMap<>();
        catchSwitchMap = new ConcurrentHashMap<>();
        catchFunMap = new ConcurrentHashMap<>();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void catcher(AsyncPlayerChatEvent event) {
        if (catchSwitchMap.getOrDefault(event.getPlayer().getUniqueId(), false)) {
            event.setCancelled(true);
            catchMsgMap.put(event.getPlayer().getUniqueId(), event.getMessage());
            catchFunMap.get(event.getPlayer().getUniqueId()).accept(event.getMessage());
        }
    }

    public static MsgCatcher getCatcher() { return catcher; }

    public String getCatchMsg(Player player) {
        return catchMsgMap.getOrDefault(player.getUniqueId(), "");
    }

    public void startCatch(Player player, Consumer<String> consumer) {
        catchSwitchMap.put(player.getUniqueId(), true);
        catchFunMap.put(player.getUniqueId(), consumer);
    }

    public void endCatch(Player player) {
        catchSwitchMap.remove(player.getUniqueId());
        catchMsgMap.remove(player.getUniqueId());
        catchFunMap.remove(player.getUniqueId());
    }

}
