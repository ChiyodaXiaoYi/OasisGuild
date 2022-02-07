package top.oasismc.oasisguild.menu.impl;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.oasismc.oasisguild.config.ConfigFile;
import top.oasismc.oasisguild.data.objects.GuildApply;
import top.oasismc.oasisguild.menu.MenuHolder;
import top.oasismc.oasisguild.menu.MenuType;
import top.oasismc.oasisguild.menu.api.IGuildMenuIcon;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static top.oasismc.oasisguild.OasisGuild.getPlugin;
import static top.oasismc.oasisguild.data.DataHandler.getDataHandler;
import static top.oasismc.oasisguild.util.MsgSender.color;

public class GuildApplyMenu extends BasicGuildMenu {

    private GuildApplyMenu(MenuHolder menuHolder) {
        super(menuHolder);
    }

    public static GuildApplyMenu createGuildApplyMenu(MenuHolder menuHolder) {
        return new GuildApplyMenu(menuHolder);
    }

    @Override
    public Inventory draw(int page, String guildName) {
        ConfigFile menuFile = getPlugin().getMenuFactory().getMenuFile();
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
        getIconMap().clear();
        ConfigFile menuFile = getPlugin().getMenuFactory().getMenuFile();
        Consumer<InventoryClickEvent> action4Apply = event -> {
            int slot = event.getSlot();
            //TODO
        };
        List<GuildApply> applyList = getDataHandler().getGuildApplyList(guildName);
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
        ItemStack frameIcon = GuildMenuFactory.getNameOnlyItem("guildApplyList.frame.", "GRAY_STAINED_GLASS_PANE");
        int []frameSlot = {45, 47, 48, 49, 50, 51, 53};
        for (int i : frameSlot)
            regIcon(i, new GuildMenuIcon(frameIcon, event -> {}));
        ItemStack previousIcon = GuildMenuFactory.getNameOnlyItem("guildApplyList.previous.", "PRISMARINE_SHARD");
        regIcon(46, new GuildMenuIcon(previousIcon, event -> {
            //TODO
        }));
        ItemStack nextIcon = GuildMenuFactory.getNameOnlyItem("guildApplyList.next.", "PRISMARINE_SHARD");
        regIcon(52, new GuildMenuIcon(nextIcon, event -> {
            //TODO
        }));
    }

    @Override
    public IGuildMenuIcon getIcon(int slot) {
        return getIconMap().get(slot);
    }

    @Override
    public Map<Integer, IGuildMenuIcon> getIconMap() {
        return super.getIconMap();
    }

    @Override
    public boolean regIcon(int slot, IGuildMenuIcon icon, boolean force) {
        return super.regIcon(slot, icon, force);
    }

    @Override
    public boolean regIcon(int slot, IGuildMenuIcon icon) {
        return super.regIcon(slot, icon);
    }

    @Override
    public MenuType getMenuType() {
        return getMenuHolder().getType();
    }

    @Override
    public MenuHolder getMenuHolder() {
        return super.getMenuHolder();
    }

}
