package top.oasismc.oasisguild.bukkit.api.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import top.oasismc.oasisguild.bukkit.menu.MenuHolder;

import java.util.Map;

public interface IGuildMenu {

    Inventory draw(int page, String guildName, Player opener);

    IGuildMenuIcon getIcon(int slot);

    Map<Integer, IGuildMenuIcon> getIconMap();

    boolean regIcon(int slot, IGuildMenuIcon icon, boolean force);

    boolean regIcon(int slot, IGuildMenuIcon icon);

    boolean regIcon(int slot, ItemStack icon);

    MenuHolder getMenuHolder();

}
