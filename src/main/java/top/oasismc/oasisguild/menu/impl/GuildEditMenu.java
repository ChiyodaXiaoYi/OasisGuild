package top.oasismc.oasisguild.menu.impl;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.oasismc.oasisguild.config.ConfigFile;
import top.oasismc.oasisguild.menu.MenuHolder;

import static top.oasismc.oasisguild.command.GuildCommand.getGuildCommand;
import static top.oasismc.oasisguild.data.DataHandler.getDataHandler;
import static top.oasismc.oasisguild.util.GuildManager.changeGuildLoc;
import static top.oasismc.oasisguild.util.GuildManager.changeGuildPvp;
import static top.oasismc.oasisguild.job.Jobs.*;
import static top.oasismc.oasisguild.menu.impl.GuildMenuManager.getMenuManager;
import static top.oasismc.oasisguild.menu.impl.GuildMenuManager.getNameOnlyItem;
import static top.oasismc.oasisguild.util.MsgCatcher.getCatcher;
import static top.oasismc.oasisguild.util.MsgSender.color;
import static top.oasismc.oasisguild.util.MsgSender.sendMsg;

public final class GuildEditMenu extends BasicGuildMenu {

    private GuildEditMenu(MenuHolder menuHolder) {
        super(menuHolder);
    }

    public static GuildEditMenu createGuildEditMenu(MenuHolder menuHolder) {
        return new GuildEditMenu(menuHolder);
    }

    @Override
    public Inventory draw(int page, String guildName, Player opener) {
        ConfigFile menuFile = getMenuManager().getMenuFile();
        String title = menuFile.getConfig().getString("guildEdit.title", "%guild%");
        title = title.replace("%guild%", guildName);
        Inventory inventory = Bukkit.createInventory(getMenuHolder(), 45, color(title));
        regIcons(guildName);
        for (Integer i : getIconMap().keySet()) {
            inventory.setItem(i, getIconMap().get(i).getIcon());
        }
        return inventory;
    }

    private void regIcons(String gName) {
        ItemStack rename = getNameOnlyItem("guildEdit.rename.", "NAME_TAG");
        regIcon(9, new GuildMenuIcon(rename, event -> {
            if (getJob(event) < VICE_LEADER) {
                sendMsg(event.getWhoClicked(), "noPerm");
                event.getWhoClicked().closeInventory();
                return;
            }
            event.getWhoClicked().closeInventory();
            sendMsg(event.getWhoClicked(), "menu.rename.needNewName");
            getCatcher().startCatch((Player) event.getWhoClicked(), newName -> {
                getGuildCommand().getCommandManager().guildRenameByCmd((Player) event.getWhoClicked(), newName);
                getCatcher().endCatch((Player) event.getWhoClicked());
            });
        }));
        ItemStack setDesc = getNameOnlyItem("guildEdit.setDesc.", "OAK_SIGN");
        regIcon(11, new GuildMenuIcon(setDesc, event -> {
            if (getJob(event) < VICE_LEADER) {
                sendMsg(event.getWhoClicked(), "noPerm");
                event.getWhoClicked().closeInventory();
            }
        }));
        ItemStack handleApply = getNameOnlyItem("guildEdit.handleApply.", "BOOK");
        regIcon(13, new GuildMenuIcon(handleApply, event -> {
            if (getJob(event) >= ADVANCED)
                event.getWhoClicked().openInventory(getMenuManager().drawGuildApplyListMenu((Player) event.getWhoClicked(), gName, 0));
            else {
                sendMsg(event.getWhoClicked(), "noPerm");
                event.getWhoClicked().closeInventory();
            }
        }));
        ItemStack setPvp = getSetPvpItem(gName);
        regIcon(15, new GuildMenuIcon(setPvp, event -> {
            if (getJob(event) < MEDIUM) {
                sendMsg(event.getWhoClicked(), "noPerm");
                event.getWhoClicked().closeInventory();
                return;
            }
            if (changeGuildPvp(gName)) {
                sendMsg(event.getWhoClicked(), "menu.pvp.open");
            } else {
                sendMsg(event.getWhoClicked(), "menu.pvp.close");
            }
            event.getWhoClicked().closeInventory();
        }));
        ItemStack setLoc = getNameOnlyItem("guildEdit.setLoc.", "BEACON");
        regIcon(17, new GuildMenuIcon(setLoc, event -> {
            if (getJob(event) < VICE_LEADER) {
                sendMsg(event.getWhoClicked(), "noPerm");
                event.getWhoClicked().closeInventory();
                return;
            }
            if (changeGuildLoc(gName, event.getWhoClicked().getLocation()))
                sendMsg(event.getWhoClicked(), "menu.changeLoc.success");
            event.getWhoClicked().closeInventory();
        }));
        ItemStack transform = getNameOnlyItem("guildEdit.transform.", "RED_BANNER");
        regIcon(27, new GuildMenuIcon(transform, event -> {}));
    }

    private int getJob(InventoryClickEvent event) {
        String gName = getDataHandler().getGuildNameByPlayer(event.getWhoClicked().getName());
        return getDataHandler().getPlayerJob(gName, event.getWhoClicked().getName());
    }

    public ItemStack getSetPvpItem(String gName) {
        ConfigFile menuFile = GuildMenuManager.getMenuManager().getMenuFile();
        ItemStack setPvp = getNameOnlyItem("guildEdit.setPvp.", "DIAMOND_SWORD");
        ItemMeta meta = setPvp.getItemMeta();
        if (meta != null) {
            if (getDataHandler().getGuildByName(gName).isPvp()) {
                String name = menuFile.getConfig().getString("guildEdit.setPvp.open.name", "&cClose");
                meta.setDisplayName(color(name));
                if (menuFile.getConfig().getBoolean("guildEdit.setPvp.open.light", true)) {
                    meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
            } else {
                String name = menuFile.getConfig().getString("guildEdit.setPvp.close.name", "&cOpen");
                meta.setDisplayName(color(name));
                if (menuFile.getConfig().getBoolean("guildEdit.setPvp.close.light", true)) {
                    meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
            }
            setPvp.setItemMeta(meta);
        }
        return setPvp;
    }

}
