package top.oasismc.oasisguild.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatListener implements Listener {

    private final Map<UUID, Boolean> catchMap;
    private final Map<UUID, String> catchStrMap;
    private static final ChatListener listener;

    static {
        listener = new ChatListener();
    }

    public static ChatListener getListener() {
        return listener;
    }

    private ChatListener() {
        catchMap = new HashMap<>();
        catchStrMap = new HashMap<>();
    }

    @EventHandler
    public void catcher(AsyncPlayerChatEvent event) {
        if (!catchMap.getOrDefault(event.getPlayer().getUniqueId(), false)) {
            return;
        }
        event.setCancelled(true);
        catchStrMap.put(event.getPlayer().getUniqueId(), event.getMessage());
        catchMap.remove(event.getPlayer().getUniqueId());
    }

    public String getCatchStr(Player player) {
        return catchStrMap.getOrDefault(player.getUniqueId(), null);
    }

    public void startCatch(Player player) {
        catchMap.put(player.getUniqueId(), true);
    }

}
