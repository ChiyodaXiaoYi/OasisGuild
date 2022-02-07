package top.oasismc.oasisguild.menu.impl;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import top.oasismc.oasisguild.config.ConfigFile;
import top.oasismc.oasisguild.menu.MenuHolder;
import top.oasismc.oasisguild.menu.MenuType;
import top.oasismc.oasisguild.menu.api.IGuildMenu;
import top.oasismc.oasisguild.menu.api.IGuildMenuIcon;

import java.util.HashMap;
import java.util.Map;

public class BasicGuildMenu implements IGuildMenu {

    private final Map<Integer, IGuildMenuIcon> iconMap;
    private final MenuHolder menuHolder;

    protected BasicGuildMenu(MenuHolder menuHolder) {
        this.menuHolder = menuHolder;
        iconMap = new HashMap<>();
    }

    @Override
    public Inventory draw(int page, String guildName) {
        return Bukkit.createInventory(menuHolder, 54);
    }

    @Override
    public IGuildMenuIcon getIcon(int slot) {
        return iconMap.getOrDefault(slot, new GuildMenuIcon(new ItemStack(Material.AIR), event -> {}));
    }

    @Override
    public Map<Integer, IGuildMenuIcon> getIconMap() {
        return iconMap;
    }

    @Override
    public boolean regIcon(int slot, IGuildMenuIcon icon, boolean force) {
        if (iconMap.containsKey(slot)) {
            if (!force)
                return false;
            else {
                iconMap.put(slot, icon);
            }
        } else {
            iconMap.put(slot, icon);
        }
        return true;
    }

    @Override
    public boolean regIcon(int slot, IGuildMenuIcon icon) {
        return regIcon(slot, icon, false);
    }

    @Override
    public MenuType getMenuType() {
        return menuHolder.getType();
    }

    @Override
    public MenuHolder getMenuHolder() {
        return menuHolder;
    }

}
