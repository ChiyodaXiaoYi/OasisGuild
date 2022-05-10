package top.oasismc.oasisguild.bukkit.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.oasismc.oasisguild.bukkit.api.objects.IGuild;
import top.oasismc.oasisguild.bukkit.core.ConfigFile;
import top.oasismc.oasisguild.bukkit.data.DataManager;

import java.util.List;

import static top.oasismc.oasisguild.bukkit.command.GuildCommand.getGuildCommand;
import static top.oasismc.oasisguild.bukkit.core.MsgSender.color;
import static top.oasismc.oasisguild.bukkit.core.MsgSender.sendMsg;
import static top.oasismc.oasisguild.bukkit.data.DataManager.getDataManager;
import static top.oasismc.oasisguild.bukkit.util.MsgCatcher.getCatcher;

public final class GuildListMenu extends BasicGuildMenu {

    private GuildListMenu(MenuHolder menuHolder) {
        super(menuHolder);
    }

    public static GuildListMenu createGuildListMenu(MenuHolder menuHolder) {
        return new GuildListMenu(menuHolder);
    }

    @Override
    public Inventory draw(int page, String guildName, Player opener) {
        ConfigFile menuFile = GuildMenuManager.getMenuManager().getMenuFile();
        String title = menuFile.getConfig().getString("guildList.title", "Guild List");
        List<IGuild> guilds = DataManager.getDataManager().getGuildList();
        Inventory inventory = Bukkit.createInventory(getMenuHolder(), 54, color(title));
        guilds = guilds.subList(page * 36, guilds.size());
        regIcons(guilds, page, menuFile);
        for (Integer i : getIconMap().keySet()) {
            inventory.setItem(i, getIconMap().get(i).getIcon());
        }
        return inventory;
    }

    private void regIcons(List<IGuild> guilds, int page, ConfigFile menuFile) {
        ItemStack frame = GuildMenuManager.getNameOnlyItem("guildList.frame.", "GRAY_STAINED_GLASS_PANE");
        int[] frameSlotList = {0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 47, 48, 50, 51, 53};
        for (int slot : frameSlotList) {
            regIcon(slot, frame);
        }

        ItemStack previous = GuildMenuManager.getNameOnlyItem("guildList.previous.", "PRISMARINE_SHARD");
        regIcon(46, new GuildMenuIcon(previous, event -> {
            Inventory previousPage;
            if (page > 0)
                previousPage = GuildMenuManager.getMenuManager().drawGuildListMenu((Player) event.getWhoClicked(), page - 1);
            else
                previousPage = GuildMenuManager.getMenuManager().drawGuildListMenu((Player) event.getWhoClicked(), 0);
            event.getWhoClicked().openInventory(previousPage);
        }));
        ItemStack next = GuildMenuManager.getNameOnlyItem("guildList.next.", "AMETHYST_SHARD");
        int size = guilds.size();
        regIcon(52, new GuildMenuIcon(next, event -> {
            Inventory nextPage;
            if (size > 36)
                nextPage = GuildMenuManager.getMenuManager().drawGuildListMenu((Player) event.getWhoClicked(), page + 1);
            else
                nextPage = GuildMenuManager.getMenuManager().drawGuildListMenu((Player) event.getWhoClicked(), page);
            event.getWhoClicked().openInventory(nextPage);
        }));

        ItemStack create = GuildMenuManager.getNameOnlyItem("guildList.create.", "END_CRYSTAL");
        regIcon(49, new GuildMenuIcon(create, event -> {
            if (getDataManager().getGuildNameByPlayer(event.getWhoClicked().getName()) != null) {
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
                    String l = GuildMenuManager.replaceOnGuild(lore.get(j), guilds.get(i - 9));
                    l = color(l);
                    lore.set(j, l);
                }
                lore.set(lore.size() - 1, GuildMenuManager.replaceOnGuild(lore.get(lore.size() - 1), guilds.get(i - 9)));
                meta.setLore(lore);
                guild.setItemMeta(meta);
            }
            regIcon(i, new GuildMenuIcon(guild, event -> {
                String gName = event.getCurrentItem().getItemMeta().getDisplayName().replace("§", "&");
                String holderGuildName = getDataManager().getGuildNameByPlayer(event.getWhoClicked().getName());
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
