package top.oasismc.oasisguild.bukkit.menu;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import top.oasismc.oasisguild.bukkit.api.menu.IGuildMenuIcon;

import java.util.function.Consumer;

public class GuildMenuIcon implements IGuildMenuIcon {

    private ItemStack icon;
    private Consumer<InventoryClickEvent> action;

    public GuildMenuIcon(ItemStack icon, Consumer<InventoryClickEvent> action) {
        this.icon = icon;
        this.action = action;
    }

    @Override
    public ItemStack getIcon() {
        return icon;
    }

    @Override
    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }

    @Override
    public Consumer<InventoryClickEvent> getAction() {
        return action;
    }

    @Override
    public void setAction(Consumer<InventoryClickEvent> action) {
        this.action = action;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GuildMenuIcon)) return false;

        GuildMenuIcon guildMenuIcon = (GuildMenuIcon) o;

        if (!icon.equals(guildMenuIcon.icon)) return false;
        return action.equals(guildMenuIcon.action);
    }

    @Override
    public int hashCode() {
        int result = icon.hashCode();
        result = 31 * result + action.hashCode();
        return result;
    }
}
