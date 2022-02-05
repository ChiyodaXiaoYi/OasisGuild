package top.oasismc.oasisguild.menu.impl;

import org.bukkit.inventory.Inventory;
import top.oasismc.oasisguild.menu.MenuType;
import top.oasismc.oasisguild.menu.api.IGuildMenu;
import top.oasismc.oasisguild.menu.api.IGuildMenuIcon;

import java.util.Map;

public class GuildInfoMenu implements IGuildMenu {
    @Override
    public Inventory draw() {
        return null;
    }

    @Override
    public GuildMenuIcon getIcon(int slot) {
        return null;
    }

    @Override
    public Map<Integer, GuildMenuIcon> getIconMap() {
        return null;
    }

    @Override
    public boolean regIcon(IGuildMenuIcon icon, boolean force) {
        return false;
    }

    @Override
    public boolean regIcon(IGuildMenuIcon icon) {
        return false;
    }

    @Override
    public MenuType getMenuType() {
        return null;
    }
}
