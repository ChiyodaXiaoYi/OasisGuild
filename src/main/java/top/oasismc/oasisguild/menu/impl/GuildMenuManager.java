package top.oasismc.oasisguild.menu.impl;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.oasismc.oasisguild.config.ConfigFile;
import top.oasismc.oasisguild.data.DataHandler;
import top.oasismc.oasisguild.data.objects.Guild;
import top.oasismc.oasisguild.menu.MenuHolder;
import top.oasismc.oasisguild.menu.api.IGuildMenu;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static top.oasismc.oasisguild.OasisGuild.getPlugin;
import static top.oasismc.oasisguild.data.DataHandler.getDataHandler;
import static top.oasismc.oasisguild.util.MsgSender.color;

public class GuildMenuManager implements Listener {

    private static final GuildMenuManager MANAGER;
    private static ConfigFile menuFile;
    private final Map<UUID, IGuildMenu> playerOpenedMenuMap;

    static {
        MANAGER = new GuildMenuManager();
    }

    public static GuildMenuManager getMenuManager() {
        return MANAGER;
    }

    private GuildMenuManager() {
        setMenuFile();
        playerOpenedMenuMap = new HashMap<>();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerOpenedMenuMap.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        if (event.getClickedInventory() == null) { return; }
        if (event.getView().getTopInventory().getHolder() instanceof MenuHolder) {
            event.setCancelled(true);
        }
        if (!(event.getClickedInventory().getHolder() instanceof MenuHolder)) {
            return;
        }
        int slot = event.getSlot();
        Player player = (Player) event.getWhoClicked();
        try {
            GuildMenuManager.getMenuManager()
                    .getPlayerOpenedMenu(player)
                    .getIcon(slot)
                    .getAction()
                    .accept(event);
        } catch (NullPointerException ignored) {}
    }

    public Inventory drawGuildApplyListMenu(Player opener, String gName, int page) {
        GuildApplyMenu menu = GuildApplyMenu.createGuildApplyMenu(new MenuHolder());
        playerOpenedMenuMap.put(opener.getUniqueId(), menu);
        return menu.draw(page, gName, opener);
    }

    public Inventory drawGuildEditMenu(Player opener, String gName) {
        GuildEditMenu menu = GuildEditMenu.createGuildEditMenu(new MenuHolder());
        playerOpenedMenuMap.put(opener.getUniqueId(), menu);
        return menu.draw(0, gName, opener);
    }

    public Inventory drawGuildListMenu(Player opener, int page) {
        GuildListMenu menu = GuildListMenu.createGuildListMenu(new MenuHolder());
        playerOpenedMenuMap.put(opener.getUniqueId(), menu);
        String gName = getDataHandler().getGuildNameByPlayer(opener.getName());
        return menu.draw(page, gName, opener);
    }

    public Inventory drawGuildInfoMenu(String gName, Player opener) {
        GuildInfoMenu menu = GuildInfoMenu.createGuildInfoMenu(new MenuHolder());
        playerOpenedMenuMap.put(opener.getUniqueId(), menu);
        return menu.draw(0, gName, opener);
    }

    public static ItemStack getNameOnlyItem(String key, String defaultMaterial) {
        String materialStr = menuFile.getConfig().getString(key + "material", defaultMaterial);
        Material material = Material.matchMaterial(materialStr);
        if (material == null)
            material = Material.STONE;
        ItemStack itemStack = new ItemStack(material, 1);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            String name = menuFile.getConfig().getString(key + "name", "&fFrame");
            meta.setDisplayName(color(name));
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    public static String replaceOnGuild(String str, Guild guild) {
        int guildMemberNum = DataHandler.getDataHandler().getGuildMembers().get(guild.getGuildName()).size();
        Location gLoc = getDataHandler().getGuildLocationMap().get(guild.getGuildName());
        str = str.replace("%desc%", guild.getDesc());
        str = str.replace("%memberNum%", guildMemberNum + "");
        str = str.replace("%maxMemberNum%", guild.getMaxMember() + "");
        str = str.replace("%level%", guild.getGuildLevel() + "");
        str = str.replace("%pvp%", guild.isPvp() + "");
        World world = gLoc.getWorld();
        if (world == null)
            str = str.replace("%world%", "world");
        else
            str = str.replace("%world%", world.getName());
        str = str.replace("%x%", (int) gLoc.getX() + "");
        str = str.replace("%y%", (int) gLoc.getY() + "");
        str = str.replace("%z%", (int) gLoc.getZ() + "");
        str = str.replace("%exp%", (int)(Math.pow((guild.getGuildLevel() + 1), 1.5)) + "");
        str = color(str);
        return str;
    }


    public void setMenuFile() {
        String lang = getPlugin().getConfig().getString("language", "zh_cn");
        menuFile = new ConfigFile("menus/menu_" + lang + ".yml");
    }

    public IGuildMenu getPlayerOpenedMenu(Player player) {
        return playerOpenedMenuMap.get(player.getUniqueId());
    }

    public ConfigFile getMenuFile() {
        return menuFile;
    }

}
