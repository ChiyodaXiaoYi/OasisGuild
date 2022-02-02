package top.oasismc.oasisguild.menu.impl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import top.oasismc.oasisguild.config.ConfigFile;
import top.oasismc.oasisguild.data.DataHandler;
import top.oasismc.oasisguild.data.objects.Guild;
import top.oasismc.oasisguild.data.objects.GuildApply;
import top.oasismc.oasisguild.data.objects.GuildMember;
import top.oasismc.oasisguild.menu.MenuHolder;
import top.oasismc.oasisguild.menu.MenuType;

import java.util.List;

import static top.oasismc.oasisguild.OasisGuild.getPlugin;
import static top.oasismc.oasisguild.data.DataHandler.getDataHandler;
import static top.oasismc.oasisguild.util.MsgSender.color;
import static top.oasismc.oasisguild.util.MsgSender.getMsgSender;
import static top.oasismc.oasisguild.job.Jobs.*;

public class DefMenuDrawer implements top.oasismc.oasisguild.menu.api.IMenuDrawer {

    private static final DefMenuDrawer drawer;
    private ConfigFile menuFile;

    static {
        drawer = new DefMenuDrawer();
    }

    public static DefMenuDrawer getDrawer() {
        return drawer;
    }

    private DefMenuDrawer() {
        setMenuFile();
    }

    @Override
    public Inventory drawGuildApplyListMenu(String gName) {
        MenuHolder holder = new MenuHolder(MenuType.APPLY);
        String title = menuFile.getConfig().getString("guildApplyList.title");
        title = title.replace("%guild%", gName);
        Inventory inventory = Bukkit.createInventory(holder, 54, color(title));
        List<GuildApply> applyList = getDataHandler().getGuildApplyListMap().get(gName);
        for (int i = 0; i < 54 && i < applyList.size(); i++) {
            String material = menuFile.getConfig().getString("guildApplyList.apply.material", "PLAYER_HEAD");
            ItemStack icon = new ItemStack(Material.matchMaterial(material));
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(color("&7" + applyList.get(i).getPName()));
            List<String> lore = menuFile.getConfig().getStringList("guildApplyList.apply.lore");
            lore.parallelStream().forEach(l -> {
                int index = lore.indexOf(l);
                lore.set(index, color(l));
            });
            lore.set(lore.size() - 1, color(lore.get(lore.size() - 1)));
            meta.setLore(lore);
            icon.setItemMeta(meta);
            inventory.setItem(i, icon);
        }
        return inventory;
    }

    @Override
    public Inventory drawGuildEditMenu(String gName) {
        MenuHolder holder = new MenuHolder(MenuType.EDIT);
        String title = menuFile.getConfig().getString("guildEdit.title", "Guild List");
        title = title.replace("%guild%", gName);
        Inventory inventory = Bukkit.createInventory(holder, 45, color(title));
        for (int i = 0; i < 45; i++) {
            switch (i) {
                case 9:
                    ItemStack rename = getNameOnlyItem("guildEdit.rename.", "NAME_TAG");
                    inventory.setItem(i, rename);
                    break;
                case 11:
                    ItemStack setDesc = getNameOnlyItem("guildEdit.setDesc.", "OAK_SIGN");
                    inventory.setItem(i, setDesc);
                    break;
                case 13:
                    ItemStack handleApply = getNameOnlyItem("guildEdit.handleApply.", "BOOK");
                    inventory.setItem(i, handleApply);
                    break;
                case 15:
                    ItemStack setPvp = getSetPvpItem(gName);
                    inventory.setItem(i, setPvp);
                    break;
                case 17:
                    ItemStack setLoc = getNameOnlyItem("guildEdit.setLoc.", "BEACON");
                    inventory.setItem(i, setLoc);
                    break;
                case 27:
                    ItemStack transform = getNameOnlyItem("guildEdit.transform.", "RED_BANNER");
                    inventory.setItem(i, transform);
                    break;
                case 29:
                    break;
                case 31:
                    break;
                case 33:
                    break;
                case 35:
                    break;
                default:
                    ItemStack frame = getNameOnlyItem("guildEdit.frame.", "GRAY_STAINED_GLASS_PANE");
                    inventory.setItem(i, frame);
                    break;
            }
        }
        return inventory;
    }

    @Override
    public Inventory drawGuildListMenu(int page) {
        MenuHolder holder = new MenuHolder(MenuType.LIST);
        String title = menuFile.getConfig().getString("guildList.title", "Guild List");
        List<Guild> guilds = DataHandler.getDataHandler().getGuildList();
        Inventory inventory = Bukkit.createInventory(holder, 54, color(title));
        ItemStack frame = getNameOnlyItem("guildList.frame.", "GRAY_STAINED_GLASS_PANE");
        int[] frameSlotList = {0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 47, 48, 50, 51, 53};
        for (int k : frameSlotList) {
            inventory.setItem(k, frame);
        }
        ItemStack previous, next, create;
        previous = getNameOnlyItem("guildList.previous.", "PRISMARINE_SHARD");
        next = getNameOnlyItem("guildList.next.", "AMETHYST_SHARD");
        create = getNameOnlyItem("guildList.create.", "END_CRYSTAL");
        inventory.setItem(46, previous);
        inventory.setItem(49, create);
        inventory.setItem(52, next);
        for (int i = 9; i < 45 && i - 9 < guilds.size(); i++) {
            ItemStack guild = new ItemStack(Material.matchMaterial(guilds.get(i - 9).getIcon()), 1);
            ItemMeta meta = guild.getItemMeta();
            meta.setDisplayName(color(guilds.get(i - 9).getGuildName()));
            List<String> lore = menuFile.getConfig().getStringList("guildList.guilds.lore");
            for (int j = 0; j < lore.size(); j++) {
                String l = replaceOnGuild(lore.get(j), guilds.get(i - 9));
                l = color(l);
                lore.set(j, l);
            }
            lore.set(lore.size() - 1, getDrawer().replaceOnGuild(lore.get(lore.size() - 1), guilds.get(i - 9)));
            meta.setLore(lore);
            guild.setItemMeta(meta);
            inventory.setItem(i, guild);
        }
        return inventory;
    }

    @Override
    public Inventory drawGuildInfoMenu(List<GuildMember> players, String gName, Player opener) {
        MenuHolder holder = new MenuHolder(MenuType.INFO);
        String title = menuFile.getConfig().getString("guildInfo.title", "Guild Info");
        title = title.replace("%guild%", gName);

        int inventoryLength;
        if (players.size() % 9 == 0)
            inventoryLength = 18 + (players.size() / 9) * 9;
        else {
            inventoryLength = 18 + (players.size() / 9 + 1) * 9;
        }

        if (inventoryLength > 54) {
            inventoryLength = 54;
        }
        Inventory inventory = Bukkit.createInventory(holder, inventoryLength, color(title));
        ItemStack frame = getNameOnlyItem("guildInfo.frame.", "GRAY_STAINED_GLASS_PANE");
        for(int i = 0; i < 4; i++) {
            inventory.setItem(i, frame);
        }
        ItemStack guildInfo = getGuildInfoItem("guildInfo.info.", gName, getDataHandler().getPlayerJob(gName, opener.getName()));
        inventory.setItem(4, guildInfo);
        for (int i = 5; i < 9; i++) {
            inventory.setItem(i, frame);
        }
        int tmp = 0;
        int pJob = getDataHandler().getPlayerJob(gName, opener.getName());
        for(int i = 9; i < inventoryLength - 9 && tmp < players.size(); i ++) {
            inventory.setItem(i, getMemberItem(players.get(tmp), pJob));
            tmp ++;
        }
        for (int i = inventoryLength - 9; i < inventoryLength; i ++) {
            int j = i - inventoryLength + 9;
            if (pJob >= MEDIUM) {
                switch (j) {
                    case 0:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 8:
                        inventory.setItem(i, getNameOnlyItem("guildInfo.frame.", "GRAY_STAINED_GLASS_PANE"));
                        break;
                    case 1: 
                        inventory.setItem(i, getNameOnlyItem("guildInfo.admin.", "WRITABLE_BOOK"));
                        break;
                    case 7:
                        if (pJob >= 250)
                            inventory.setItem(i, getNameOnlyItem("guildInfo.disband.", "BARRIER"));
                        else
                            inventory.setItem(i, getNameOnlyItem("guildInfo.quit.", "BARRIER"));
                        break;
                }
            }
            else {
                switch (j) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                            inventory.setItem(i, getNameOnlyItem("guildInfo.frame.", "GRAY_STAINED_GLASS_PANE"));
                            break;
                    case 4: 
                            inventory.setItem(i, getNameOnlyItem("guildInfo.quit.", "WRITABLE_BOOK"));
                            break;
                }
            }
        }
        return inventory;
    }

    public ItemStack getNameOnlyItem(String key, String defaultMaterial) {
        String material = menuFile.getConfig().getString(key + "material", defaultMaterial);
        ItemStack itemStack = new ItemStack(Material.matchMaterial(material), 1);
        ItemMeta meta = itemStack.getItemMeta();
        String name = menuFile.getConfig().getString(key + "name", "&fFrame");
        meta.setDisplayName(color(name));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public ItemStack getGuildInfoItem(String key, String guildName, int pJob) {
        String type = menuFile.getConfig().getString(key + "material", "GREEN_BANNER");
        ItemStack icon = new ItemStack(Material.matchMaterial(type), 1);
        ItemMeta meta = icon.getItemMeta();
        String name = menuFile.getConfig().getString(key + "name", "&f%guild%");
        name = name.replace("%guild%", guildName);
        meta.setDisplayName(color(name));
        Guild guild = getDataHandler().getGuildByName(guildName);
        List<String> lore = menuFile.getConfig().getStringList(key + "lore");
        if (pJob >= ADVANCED) {
            lore.addAll(menuFile.getConfig().getStringList(key + "lore_admin"));
        }
        for (int i = 0; i < lore.size(); i++) {
            String l = replaceOnGuild(lore.get(i), guild);
            l = color(l);
            lore.set(i, l);
        }
        meta.setLore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    public ItemStack getMemberItem(GuildMember member, int pJob) {
        String material = menuFile.getConfig().getString("guildInfo.members.material");
        Material iconType = Material.matchMaterial(material);
        if (iconType == null)
            iconType = Material.PLAYER_HEAD;
        ItemStack icon = new ItemStack(iconType, 1);
        ItemMeta meta = icon.getItemMeta();
        String iconName = color("&f" + member.getPlayerName());
        meta.setDisplayName(iconName);
        List<String> lore;
        if (pJob < VICE_LEADER || member.getJob() >= VICE_LEADER) {
            lore = menuFile.getConfig().getStringList("guildInfo.members.lore");
        } else {
            lore = menuFile.getConfig().getStringList("guildInfo.members.lore_admin");
        }
        for (int i = 0; i < lore.size(); i++) {
            String l = replaceOnMember(lore.get(i), member);
            l = color(l);
            lore.set(i, l);
        }
        meta.setLore(lore);
        if (member.getJob() >= VICE_LEADER) {
            icon.setAmount(2);
        }
        if (iconType == Material.PLAYER_HEAD)
            ((SkullMeta) meta).setOwningPlayer(Bukkit.getOfflinePlayer(member.getPlayerName()));
        icon.setItemMeta(meta);
        return icon;
    }

    public String replaceOnGuild(String str, Guild guild) {
        int guildMemberNum = DataHandler.getDataHandler().getGuildMembers().get(guild.getGuildName()).size();
        Location gLoc = getDataHandler().getGuildLocationMap().get(guild.getGuildName());
        str = str.replace("%desc%", guild.getDesc());
        str = str.replace("%memberNum%", guildMemberNum + "");
        str = str.replace("%maxMemberNum%", guild.getMaxMember() + "");
        str = str.replace("%level%", guild.getGuildLevel() + "");
        str = str.replace("%pvp%", guild.isPvp() + "");
        str = str.replace("%world%", gLoc.getWorld().getName() + "");
        str = str.replace("%x%", (int) gLoc.getX() + "");
        str = str.replace("%y%", (int) gLoc.getY() + "");
        str = str.replace("%z%", (int) gLoc.getZ() + "");
        str = str.replace("%exp%", (int)(Math.pow((guild.getGuildLevel() + 1), 1.5)) + "");
        str = color(str);
        return str;
    }

    public String replaceOnMember(String str, GuildMember player) {
        String gJob = getMsgSender().getLangFile().getConfig().getString("job." + player.getJob(), player.getJob() + "");
        str = str.replace("%job%", gJob);
        str = color(str);
        return str;
    }

    public ItemStack getSetPvpItem(String gName) {
        ItemStack setPvp = getNameOnlyItem("guildEdit.setPvp.", "DIAMOND_SWORD");
        ItemMeta meta = setPvp.getItemMeta();
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
        return setPvp;
    }

    @Override
    public void setMenuFile() {
        String lang = getPlugin().getConfig().getString("language", "zh_cn");
        menuFile = new ConfigFile("menus/menu_" + lang + ".yml");
    }

    @Override
    public ConfigFile getMenuFile() {
        return menuFile;
    }

}
