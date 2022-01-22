package top.oasismc.oasisguild.menu.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import top.oasismc.oasisguild.config.ConfigFile;
import top.oasismc.oasisguild.data.objects.GuildMember;

import java.util.List;

public interface IMenuDrawer {

    Inventory drawGuildApplyListMenu(String gName);

    Inventory drawGuildEditMenu(String gName);

    Inventory drawGuildListMenu(int page);

    Inventory drawGuildInfoMenu(List<GuildMember> players, String gName, Player opener);

    void setMenuFile();

    ConfigFile getMenuFile();
}
