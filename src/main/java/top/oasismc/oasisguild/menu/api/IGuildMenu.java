package top.oasismc.oasisguild.menu.api;

import org.bukkit.inventory.Inventory;
import top.oasismc.oasisguild.menu.MenuHolder;
import top.oasismc.oasisguild.menu.MenuType;

import java.util.Map;

public interface IGuildMenu {

    Inventory draw(int page, String guildName);

    IGuildMenuIcon getIcon(int slot);

    Map<Integer, IGuildMenuIcon> getIconMap();

    boolean regIcon(int slot, IGuildMenuIcon icon, boolean force);

    boolean regIcon(int slot, IGuildMenuIcon icon);

    MenuType getMenuType();

    MenuHolder getMenuHolder();

}
