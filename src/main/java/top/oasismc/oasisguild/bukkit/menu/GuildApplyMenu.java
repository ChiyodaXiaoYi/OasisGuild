package top.oasismc.oasisguild.bukkit.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.oasismc.oasisguild.bukkit.api.event.player.PlayerJoinGuildEvent;
import top.oasismc.oasisguild.bukkit.api.objects.IGuildApply;
import top.oasismc.oasisguild.bukkit.core.ConfigFile;
import top.oasismc.oasisguild.bukkit.core.MsgSender;

import java.util.List;
import java.util.function.Consumer;

import static top.oasismc.oasisguild.bukkit.core.GuildManager.playerJoinGuild;
import static top.oasismc.oasisguild.bukkit.core.MsgSender.color;
import static top.oasismc.oasisguild.bukkit.core.MsgSender.sendMsg;
import static top.oasismc.oasisguild.bukkit.data.DataManager.getDataManager;

public final class GuildApplyMenu extends BasicGuildMenu {

    private GuildApplyMenu(MenuHolder menuHolder) {
        super(menuHolder);
    }

    public static GuildApplyMenu createGuildApplyMenu(MenuHolder menuHolder) {
        return new GuildApplyMenu(menuHolder);
    }

    @Override
    public Inventory draw(int page, String guildName, Player opener) {
        ConfigFile menuFile = GuildMenuManager.getMenuManager().getMenuFile();
        String title = menuFile.getConfig().getString("guildApplyList.title", "%guild%");
        title = title.replace("%guild%", guildName);
        Inventory inventory = Bukkit.createInventory(getMenuHolder(), 54, color(title));
        regIcons(page, guildName);
        for (Integer i : getIconMap().keySet()) {
            inventory.setItem(i, getIconMap().get(i).getIcon());
        }
        return inventory;
    }

    private void regIcons(int page, String guildName) {
        regApplyIcons(page, guildName);
        ItemStack frameIcon = GuildMenuManager.getNameOnlyItem("guildApplyList.frame.", "GRAY_STAINED_GLASS_PANE");
        int []frameSlot = {45, 47, 48, 49, 50, 51, 53};
        for (int i : frameSlot)
            regIcon(i, frameIcon);
        ItemStack previousIcon = GuildMenuManager.getNameOnlyItem("guildApplyList.previous.", "PRISMARINE_SHARD");
        regIcon(46, new GuildMenuIcon(previousIcon, event -> {
            event.getWhoClicked().closeInventory();
        }));
        ItemStack nextIcon = GuildMenuManager.getNameOnlyItem("guildApplyList.next.", "PRISMARINE_SHARD");
        regIcon(52, new GuildMenuIcon(nextIcon, event -> {
            event.getWhoClicked().closeInventory();
        }));
    }

    private void regApplyIcons(int page, String guildName) {
        ConfigFile menuFile = GuildMenuManager.getMenuManager().getMenuFile();
        Consumer<InventoryClickEvent> action4Apply = event -> {
            Player clicker = (Player) event.getWhoClicked();
            String gName = getDataManager().getGuildNameByPlayer(clicker.getName());
            String clickedMember = event.getCurrentItem().getItemMeta().getDisplayName().replace("??", "&").substring(2);
            if (event.isLeftClick()) {
                int memberNum = getDataManager().getGuildMembersMap().get(gName).size();
                int maxNum = getDataManager().getGuildByName(gName).getMaxMember();
                if (memberNum < maxNum) {
                    playerJoinGuild(gName, clickedMember, PlayerJoinGuildEvent.JoinReason.ACCEPT);
                    MsgSender.sendMsg4replacePlayer(clicker, "menu.accept.admin", clickedMember);
                    if (Bukkit.getPlayer(clickedMember) != null && Bukkit.getPlayer(clickedMember).isOnline())
                        MsgSender.sendMsg4replaceGuild(Bukkit.getPlayer(clickedMember), "menu.accept.member", getDataManager().getGuildByName(gName));
                } else {
                    sendMsg(clicker, "menu.accept.full");
                }
            } else {
                getDataManager().getGuildDao().handleApply(gName, "deny", clickedMember);
                MsgSender.sendMsg4replacePlayer(clicker, "menu.deny.admin", clickedMember);
                if (Bukkit.getPlayer(clickedMember) != null && Bukkit.getPlayer(clickedMember).isOnline())
                    MsgSender.sendMsg4replaceGuild(Bukkit.getPlayer(clickedMember), "menu.deny.member", getDataManager().getGuildByName(gName));
            }
            event.getWhoClicked().closeInventory();
        };
        List<IGuildApply> applyList = getDataManager().getGuildApplyList(guildName);
        int maxPage = applyList.size() / 45 + 1;
        if (applyList.size() % 45 == 0 && applyList.size() != 0)
            maxPage -= 1;
        if (page >= maxPage)
            page = --maxPage;
        for (int i = 0; i < 45; i++) {
            int j = page * 45 + i;
            if (j >= applyList.size())
                break;
            String material = menuFile.getConfig().getString("guildApplyList.apply.material", "PLAYER_HEAD");
            Material iconMaterial = Material.matchMaterial(material);
            if (iconMaterial == null)
                iconMaterial = Material.PLAYER_HEAD;
            ItemStack icon = new ItemStack(iconMaterial);
            ItemMeta meta = icon.getItemMeta();
            if (meta == null)
                continue;
            meta.setDisplayName(color("&7" + applyList.get(j).getPName()));
            List<String> lore = menuFile.getConfig().getStringList("guildApplyList.apply.lore");
            lore.parallelStream().forEach(l -> {
                int index = lore.indexOf(l);
                lore.set(index, color(l));
            });
            lore.set(lore.size() - 1, color(lore.get(lore.size() - 1)));
            meta.setLore(lore);
            icon.setItemMeta(meta);
            regIcon(i, new GuildMenuIcon(icon, action4Apply));
        }
    }

}
