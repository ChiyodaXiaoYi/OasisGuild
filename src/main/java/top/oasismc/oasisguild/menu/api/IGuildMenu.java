package top.oasismc.oasisguild.menu.api;

import org.bukkit.inventory.Inventory;
import top.oasismc.oasisguild.menu.MenuType;
import top.oasismc.oasisguild.menu.impl.GuildMenuIcon;

import java.util.Map;

public interface IGuildMenu {

    Inventory draw();

    GuildMenuIcon getIcon(int slot);

    Map<Integer, GuildMenuIcon> getIconMap();

    boolean regIcon(IGuildMenuIcon icon, boolean force);

    boolean regIcon(IGuildMenuIcon icon);

    MenuType getMenuType();

}
