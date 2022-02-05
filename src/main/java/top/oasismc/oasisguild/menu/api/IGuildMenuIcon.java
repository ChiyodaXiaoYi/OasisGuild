package top.oasismc.oasisguild.menu.api;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public interface IGuildMenuIcon {
    ItemStack getIcon();

    void setIcon(ItemStack icon);

    Consumer<InventoryClickEvent> getAction();

    void setAction(Consumer<InventoryClickEvent> action);
}
