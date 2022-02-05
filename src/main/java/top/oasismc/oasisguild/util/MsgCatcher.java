package top.oasismc.oasisguild.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static top.oasismc.oasisguild.OasisGuild.getPlugin;

public final class MsgCatcher implements Listener {

    private final Map<UUID, Boolean> catchSwitchMap;
    private final Map<UUID, Consumer<String>> catchFunMap;
    private static final MsgCatcher catcher;

    static {
        catcher = new MsgCatcher();
    }

    private MsgCatcher() {
        catchSwitchMap = new ConcurrentHashMap<>();
        catchFunMap = new ConcurrentHashMap<>();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void catcher(AsyncPlayerChatEvent event) {
        if (catchSwitchMap.getOrDefault(event.getPlayer().getUniqueId(), false)) {
            event.setCancelled(true);
            Bukkit.getScheduler().callSyncMethod(getPlugin(), () -> {
                catchFunMap.get(event.getPlayer().getUniqueId()).accept(event.getMessage());
                return null;
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void endCatch4PlayerQuit(PlayerQuitEvent event) {
        endCatch(event.getPlayer());
    }

    public static MsgCatcher getCatcher() { return catcher; }

    public void startCatch(Player player, Consumer<String> consumer) {
        catchSwitchMap.put(player.getUniqueId(), true);
        catchFunMap.put(player.getUniqueId(), consumer);
    }

    public void endCatch(Player player) {
        catchSwitchMap.remove(player.getUniqueId());
        catchFunMap.remove(player.getUniqueId());
    }

    public void reloadCatcher() {
        catchFunMap.clear();
        catchSwitchMap.clear();
    }

}
