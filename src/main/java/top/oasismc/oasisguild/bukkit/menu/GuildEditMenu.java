package top.oasismc.oasisguild.bukkit.menu;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.oasismc.oasisguild.bukkit.core.ConfigFile;

import static top.oasismc.oasisguild.bukkit.api.job.Jobs.*;
import static top.oasismc.oasisguild.bukkit.command.GuildCommand.getGuildCommand;
import static top.oasismc.oasisguild.bukkit.data.DataManager.getDataManager;
import static top.oasismc.oasisguild.bukkit.core.GuildManager.changeGuildLoc;
import static top.oasismc.oasisguild.bukkit.core.GuildManager.changeGuildPvp;
import static top.oasismc.oasisguild.bukkit.util.MsgCatcher.getCatcher;
import static top.oasismc.oasisguild.bukkit.core.MsgSender.color;
import static top.oasismc.oasisguild.bukkit.core.MsgSender.sendMsg;

public final class GuildEditMenu extends BasicGuildMenu {

    private GuildEditMenu(MenuHolder menuHolder) {
        super(menuHolder);
    }

    public static GuildEditMenu createGuildEditMenu(MenuHolder menuHolder) {
        return new GuildEditMenu(menuHolder);
    }

    @Override
    public Inventory draw(int page, String guildName, Player opener) {
        ConfigFile menuFile = GuildMenuManager.getMenuManager().getMenuFile();
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
        ItemStack rename = GuildMenuManager.getNameOnlyItem("guildEdit.rename.", "NAME_TAG");
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
        ItemStack setDesc = GuildMenuManager.getNameOnlyItem("guildEdit.setDesc.", "OAK_SIGN");
        regIcon(11, new GuildMenuIcon(setDesc, event -> {
            if (getJob(event) < VICE_LEADER) {
                sendMsg(event.getWhoClicked(), "noPerm");
                event.getWhoClicked().closeInventory();
            }
        }));
        ItemStack handleApply = GuildMenuManager.getNameOnlyItem("guildEdit.handleApply.", "BOOK");
        regIcon(13, new GuildMenuIcon(handleApply, event -> {
            if (getJob(event) >= ADVANCED)
                event.getWhoClicked().openInventory(GuildMenuManager.getMenuManager().drawGuildApplyListMenu((Player) event.getWhoClicked(), gName, 0));
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
        ItemStack setLoc = GuildMenuManager.getNameOnlyItem("guildEdit.setLoc.", "BEACON");
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
        ItemStack transform = GuildMenuManager.getNameOnlyItem("guildEdit.transform.", "RED_BANNER");
        regIcon(27, new GuildMenuIcon(transform, event -> {}));
    }

    private int getJob(InventoryClickEvent event) {
        String gName = getDataManager().getGuildNameByPlayer(event.getWhoClicked().getName());
        return getDataManager().getPlayerJob(gName, event.getWhoClicked().getName());
    }

    public ItemStack getSetPvpItem(String gName) {
        ConfigFile menuFile = GuildMenuManager.getMenuManager().getMenuFile();
        ItemStack setPvp = GuildMenuManager.getNameOnlyItem("guildEdit.setPvp.", "DIAMOND_SWORD");
        ItemMeta meta = setPvp.getItemMeta();
        if (meta != null) {
            if (getDataManager().getGuildByName(gName).isPvp()) {
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
