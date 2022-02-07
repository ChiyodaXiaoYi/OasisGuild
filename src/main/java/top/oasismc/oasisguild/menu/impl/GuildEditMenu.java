package top.oasismc.oasisguild.menu.impl;

import org.bukkit.inventory.Inventory;
import top.oasismc.oasisguild.menu.MenuHolder;
import top.oasismc.oasisguild.menu.MenuType;
import top.oasismc.oasisguild.menu.api.IGuildMenuIcon;

import java.util.Map;

public class GuildEditMenu extends BasicGuildMenu {

    private GuildEditMenu(MenuHolder menuHolder) {
        super(menuHolder);
    }

    public static GuildEditMenu createGuildEditMenu(MenuHolder menuHolder) {
        return new GuildEditMenu(menuHolder);
    }

    @Override
    public Inventory draw(int page, String guildName) {
        return null;
    }

    @Override
    public IGuildMenuIcon getIcon(int slot) {
        return null;
    }

    @Override
    public Map<Integer, IGuildMenuIcon> getIconMap() {
        return null;
    }

    @Override
    public boolean regIcon(int slot, IGuildMenuIcon icon, boolean force) {
        return false;
    }

    @Override
    public boolean regIcon(int slot, IGuildMenuIcon icon) {
        return false;
    }

    @Override
    public MenuType getMenuType() {
        return null;
    }

    @Override
    public MenuHolder getMenuHolder() {
        return super.getMenuHolder();
    }
}
