package top.oasismc.oasisguild.menu.api;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public interface IMenuListener extends Listener {
    @EventHandler
    void onMenuClick(InventoryClickEvent event);
}
