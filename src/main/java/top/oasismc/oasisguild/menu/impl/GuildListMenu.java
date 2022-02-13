package top.oasismc.oasisguild.menu.impl;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.oasismc.oasisguild.config.ConfigFile;
import top.oasismc.oasisguild.data.DataHandler;
import top.oasismc.oasisguild.objects.api.IGuild;
import top.oasismc.oasisguild.menu.MenuHolder;

import java.util.List;

import static top.oasismc.oasisguild.command.GuildCommand.getGuildCommand;
import static top.oasismc.oasisguild.data.DataHandler.getDataHandler;
import static top.oasismc.oasisguild.menu.impl.GuildMenuManager.*;
import static top.oasismc.oasisguild.util.MsgCatcher.getCatcher;
import static top.oasismc.oasisguild.util.MsgSender.color;
import static top.oasismc.oasisguild.util.MsgSender.sendMsg;

public final class GuildListMenu extends BasicGuildMenu {

    private GuildListMenu(MenuHolder menuHolder) {
        super(menuHolder);
    }

    public static GuildListMenu createGuildListMenu(MenuHolder menuHolder) {
        return new GuildListMenu(menuHolder);
    }

    @Override
    public Inventory draw(int page, String guildName, Player opener) {
        ConfigFile menuFile = getMenuManager().getMenuFile();
        String title = menuFile.getConfig().getString("guildList.title", "Guild List");
        List<IGuild> guilds = DataHandler.getDataHandler().getGuildList();
        Inventory inventory = Bukkit.createInventory(getMenuHolder(), 54, color(title));
        regIcons(guilds, page, menuFile);
        for (Integer i : getIconMap().keySet()) {
            inventory.setItem(i, getIconMap().get(i).getIcon());
        }
        return inventory;
    }

    private void regIcons(List<IGuild> guilds, int page, ConfigFile menuFile) {
        ItemStack frame = getNameOnlyItem("guildList.frame.", "GRAY_STAINED_GLASS_PANE");
        int[] frameSlotList = {0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 47, 48, 50, 51, 53};
        for (int slot : frameSlotList) {
            regIcon(slot, frame);
        }
        ItemStack previous = getNameOnlyItem("guildList.previous.", "PRISMARINE_SHARD");
        regIcon(46, new GuildMenuIcon(previous, event -> {
            getMenuManager().drawGuildListMenu((Player) event.getWhoClicked(), 0);
        }));
        ItemStack next = getNameOnlyItem("guildList.next.", "AMETHYST_SHARD");
        regIcon(52, new GuildMenuIcon(next, event -> {
            getMenuManager().drawGuildListMenu((Player) event.getWhoClicked(), 0);
        }));
        ItemStack create = getNameOnlyItem("guildList.create.", "END_CRYSTAL");
        regIcon(49, new GuildMenuIcon(create, event -> {
            event.getWhoClicked().closeInventory();
            if (getDataHandler().getGuildNameByPlayer(event.getWhoClicked().getName()) != null) {
                sendMsg(event.getWhoClicked(), "command.create.hasGuild");
                return;
            }
            sendMsg(event.getWhoClicked(), "menu.create.needGName");
            getCatcher().startCatch((Player) event.getWhoClicked(), guild -> {
                sendMsg(event.getWhoClicked(), "menu.create.needDesc");
                getCatcher().startCatch((Player) event.getWhoClicked(), desc -> {
                    getGuildCommand().getCommandManager().createGuildByCmd(event.getWhoClicked(), guild, desc);
                    getCatcher().endCatch((Player) event.getWhoClicked());
                });
            });
        }));
        for (int i = 9; i < 45 && i - 9 < guilds.size(); i++) {
            Material material = Material.matchMaterial(guilds.get(i - 9).getIcon());
            if (material == null)
                material = Material.STONE;
            ItemStack guild = new ItemStack(material, 1);
            ItemMeta meta = guild.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(color(guilds.get(i - 9).getGuildName()));
                List<String> lore = menuFile.getConfig().getStringList("guildList.guilds.lore");
                for (int j = 0; j < lore.size(); j++) {
                    String l = replaceOnGuild(lore.get(j), guilds.get(i - 9));
                    l = color(l);
                    lore.set(j, l);
                }
                lore.set(lore.size() - 1, replaceOnGuild(lore.get(lore.size() - 1), guilds.get(i - 9)));
                meta.setLore(lore);
                guild.setItemMeta(meta);
            }
            regIcon(i, new GuildMenuIcon(guild, event -> {
                String gName = event.getCurrentItem().getItemMeta().getDisplayName().replace("§", "&");
                String holderGuildName = getDataHandler().getGuildNameByPlayer(event.getWhoClicked().getName());
                if (holderGuildName != null && holderGuildName.equals(gName)) {
                    getGuildCommand().getCommandManager().openGuildInfoMenu(event.getWhoClicked(), gName);
                } else {
                    if (event.isRightClick()) {
                        getGuildCommand().getCommandManager().playerApplyGuildByCmd(event.getWhoClicked(), gName);
                        event.getWhoClicked().closeInventory();
                    }
                }
            }));
        }
    }

}
