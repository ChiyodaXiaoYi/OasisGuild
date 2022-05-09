package top.oasismc.oasisguild.bukkit.menu;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import top.oasismc.oasisguild.bukkit.api.event.player.PlayerQuitGuildEvent;
import top.oasismc.oasisguild.bukkit.api.event.player.PlayerTpGuildLocEvent;
import top.oasismc.oasisguild.bukkit.api.objects.IGuild;
import top.oasismc.oasisguild.bukkit.api.objects.IGuildMember;
import top.oasismc.oasisguild.bukkit.core.ConfigFile;
import top.oasismc.oasisguild.bukkit.core.GuildManager;
import top.oasismc.oasisguild.bukkit.core.MsgSender;

import java.util.List;

import static top.oasismc.oasisguild.bukkit.api.event.player.PlayerTpGuildLocEvent.createPlayerTpGuildLocEvent;
import static top.oasismc.oasisguild.bukkit.api.job.Jobs.*;
import static top.oasismc.oasisguild.bukkit.command.GuildCommand.getGuildCommand;
import static top.oasismc.oasisguild.bukkit.core.GuildManager.playerQuitGuild;
import static top.oasismc.oasisguild.bukkit.core.MsgSender.*;
import static top.oasismc.oasisguild.bukkit.data.DataManager.getDataManager;

public final class GuildInfoMenu extends BasicGuildMenu {

    private GuildInfoMenu(MenuHolder menuHolder) {
        super(menuHolder);
    }

    public static GuildInfoMenu createGuildInfoMenu(MenuHolder menuHolder) {
        return new GuildInfoMenu(menuHolder);
    }

    @Override
    public Inventory draw(int page, String guildName, Player opener) {
        ConfigFile menuFile = GuildMenuManager.getMenuManager().getMenuFile();
        String title = menuFile.getConfig().getString("guildInfo.title", "Guild Info");
        title = title.replace("%guild%", guildName);
        int inventoryLength;
        List<IGuildMember> members = getDataManager().getGuildMembers().get(guildName);
        if (members.size() % 9 == 0)
            inventoryLength = 18 + (members.size() / 9) * 9;
        else {
            inventoryLength = 18 + (members.size() / 9 + 1) * 9;
        }

        if (inventoryLength > 54) {
            inventoryLength = 54;
        }
        Inventory inventory = Bukkit.createInventory(getMenuHolder(), inventoryLength, color(title));
        regIcons(guildName, menuFile, opener, members, inventoryLength);
        for (Integer i : getIconMap().keySet()) {
            if (i >= inventoryLength)
                continue;
            inventory.setItem(i, getIconMap().get(i).getIcon());
        }
        return inventory;
    }

    private void regIcons(String gName, ConfigFile menuFile, Player opener, List<IGuildMember> members, int inventoryLength) {
        ItemStack frame = GuildMenuManager.getNameOnlyItem("guildInfo.frame.", "GRAY_STAINED_GLASS_PANE");
        int pJob = getDataManager().getPlayerJob(gName, opener.getName());
        int []frameSlots = {0, 1, 2, 3, 5, 6, 7, 8};
        for (int slot : frameSlots) {
            regIcon(slot, frame);
        }

        ItemStack guildInfo = getGuildInfoItem(gName, getDataManager().getPlayerJob(gName, opener.getName()), menuFile);
        regIcon(4, new GuildMenuIcon(guildInfo, event -> {
            if (pJob >= ADVANCED) {
                if (event.isLeftClick()) {
                    playerTpGuildLoc((Player) event.getWhoClicked());
                } else if (event.isRightClick()) {
                    guildLevelUpOnMenu((Player) event.getWhoClicked(), gName);
                }
            } else {
                playerTpGuildLoc((Player) event.getWhoClicked());
            }
        }));

        for(int i = 9, tmp = 0; i < inventoryLength - 9 && tmp < members.size(); i ++) {
            regIcon(i, new GuildMenuIcon(getMemberItem(members.get(tmp), pJob, menuFile), event -> {
                String name = event.getCurrentItem().getItemMeta().getDisplayName().replace("§", "&");
                name = name.substring(2);
                if (pJob > VICE_LEADER)
                    handleGuildMemberAction(event.getClick(), name, (Player) event.getWhoClicked(), pJob);
            }));
            tmp ++;
        }

        int []frameSlots2;
        if (pJob >= MEDIUM) {
            frameSlots2 = new int[]{0, 2, 3, 4, 5, 6, 8};
            ItemStack admin = GuildMenuManager.getNameOnlyItem("guildInfo.admin.", "WRITABLE_BOOK");
            regIcon(inventoryLength - 8, new GuildMenuIcon(admin, event -> {
                event.getWhoClicked().openInventory(GuildMenuManager.getMenuManager().drawGuildEditMenu((Player) event.getWhoClicked(), gName));
            }));
            ItemStack quit;
            if (pJob >= LEADER)
                quit = GuildMenuManager.getNameOnlyItem("guildInfo.disband.", "BARRIER");
            else
                quit = GuildMenuManager.getNameOnlyItem("guildInfo.quit.", "BARRIER");
            regIcon(inventoryLength - 2, new GuildMenuIcon(quit, event -> {
                if (pJob >= LEADER)
                    getGuildCommand().getCommandManager().disbandGuildByCmd((Player) event.getWhoClicked());
                else
                    getGuildCommand().getCommandManager().playerQuitGuildByCmd((Player) event.getWhoClicked());
                event.getWhoClicked().closeInventory();
            }));
        } else {
            frameSlots2 = new int[]{0, 1, 2, 3, 5, 6, 7, 8};
            ItemStack quit = GuildMenuManager.getNameOnlyItem("guildInfo.quit.", "BARRIER");
            regIcon(inventoryLength - 5, new GuildMenuIcon(quit, event -> {
                getGuildCommand().getCommandManager().playerQuitGuildByCmd((Player) event.getWhoClicked());
                event.getWhoClicked().closeInventory();
            }));
        }
        for (int slot : frameSlots2) {
            int i = slot + inventoryLength - 9;
            regIcon(i, frame);
        }
    }

    private ItemStack getGuildInfoItem(String guildName, int pJob, ConfigFile menuFile) {
        String type = menuFile.getConfig().getString("guildInfo.info." + "material", "GREEN_BANNER");
        Material material = Material.matchMaterial(type);
        if (material == null)
            material = Material.GREEN_BANNER;
        ItemStack icon = new ItemStack(material, 1);
        ItemMeta meta = icon.getItemMeta();
        String name = menuFile.getConfig().getString("guildInfo.info." + "name", "&f%guild%");
        name = name.replace("%guild%", guildName);
        if (meta != null) {
            meta.setDisplayName(color(name));
            IGuild guild = getDataManager().getGuildByName(guildName);
            List<String> lore = menuFile.getConfig().getStringList("guildInfo.info." + "lore");
            if (pJob >= ADVANCED) {
                lore.addAll(menuFile.getConfig().getStringList("guildInfo.info." + "lore_admin"));
            }
            for (int i = 0; i < lore.size(); i++) {
                String l = GuildMenuManager.replaceOnGuild(lore.get(i), guild);
                l = color(l);
                lore.set(i, l);
            }
            meta.setLore(lore);
            icon.setItemMeta(meta);
        }
        return icon;
    }

    public ItemStack getMemberItem(IGuildMember member, int pJob, ConfigFile menuFile) {
        String material = menuFile.getConfig().getString("guildInfo.members.material", "PLAYER_HEAD");
        Material iconType = Material.matchMaterial(material);
        if (iconType == null)
            iconType = Material.PLAYER_HEAD;
        ItemStack icon = new ItemStack(iconType, 1);
        ItemMeta meta = icon.getItemMeta();
        String iconName = color("&f" + member.getPlayerName());
        if (meta != null) {
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
        }
        return icon;
    }

    public String replaceOnMember(String str, IGuildMember player) {
        String gJob = getMsgSender().getLangFile().getConfig().getString("job." + player.getJob(), player.getJob() + "");
        str = str.replace("%job%", gJob);
        str = color(str);
        return str;
    }


    private void guildLevelUpOnMenu(Player player, String gName) {
        int gLvl = getDataManager().getGuildByName(gName).getGuildLevel();
        int code = GuildManager.guildLevelUp(player, gName, gLvl, 1);
        switch (code) {
            case -1:
                sendMsg(player, "menu.levelUp.limit");
                break;
            case 0:
                sendMsg(player, "menu.levelUp.success");
                break;
            case 1:
                sendMsg(player, "menu.levelUp.lackLvl");
                break;
        }
        player.closeInventory();
    }

    public static void playerTpGuildLoc(Player player) {
        String guildName = getDataManager().getGuildNameByPlayer(player.getName());
        if (guildName == null)
            return;
        Location loc = getDataManager().getGuildLocationMap().get(guildName);
        PlayerTpGuildLocEvent event = createPlayerTpGuildLocEvent(guildName, player.getName(), loc);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;
        Player tpPlayer = Bukkit.getPlayer(event.getPlayer());
        if (tpPlayer == null)
            return;
        tpPlayer.teleport(event.getLoc());
    }


    private void handleGuildMemberAction(ClickType action, String pName, Player clicker, int clickerJob) {
        String gName = getDataManager().getGuildNameByPlayer(pName);
        int pJob = getDataManager().getPlayerJob(gName, pName);
        switch (action) {
            case DROP:
                if (clickerJob <= pJob)
                    return;
                playerQuitGuild(gName, pName, PlayerQuitGuildEvent.QuitReason.KICK);
                MsgSender.sendMsg4replacePlayer(clicker, "menu.kick.success", pName);
                Player member = Bukkit.getPlayer(pName);
                if (member != null && member.isOnline()) {
                    MsgSender.sendMsg4replaceGuild(member, "menu.kick.member", getDataManager().getGuildByName(gName));
                }
                clicker.closeInventory();
                break;
            case SHIFT_LEFT:
                int newJob;
                if (pJob < MEDIUM) {
                    newJob = 149;
                } else if (pJob < ADVANCED) {
                    newJob = 199;
                } else if (pJob < VICE_LEADER) {
                    newJob = 249;
                } else if (pJob < LEADER) {
                    newJob = 299;
                } else {
                    sendMsg4replacePlayer(clicker, "menu.jobChange.highest", pName);
                    clicker.closeInventory();
                    return;
                }
                if (newJob >= clickerJob) {
                    sendMsg(clicker, "noPerm");
                    clicker.closeInventory();
                    return;
                }
                GuildManager.memberJobChange(gName, pName, clicker.getName(), pJob, newJob);
                sendMsg4replacePlayer(clicker, "menu.jobChange.up", pName);
                clicker.closeInventory();
                break;
            case SHIFT_RIGHT:
                newJob = 0;
                if (clickerJob <= pJob) {
                    sendMsg(clicker, "noPerm");
                    clicker.closeInventory();
                    return;
                }
                if (pJob >= NORMAL && pJob < MEDIUM) {
                    sendMsg4replacePlayer(clicker, "menu.jobChange.lowest", pName);
                    clicker.closeInventory();
                    return;
                } else if (pJob >= ADVANCED && pJob < VICE_LEADER) {
                    newJob = 149;
                } else if (pJob >= VICE_LEADER && pJob < LEADER) {
                    newJob = 199;
                }
                GuildManager.memberJobChange(gName, pName, clicker.getName(), pJob, newJob);
                sendMsg4replacePlayer(clicker, "menu.jobChange.down", pName);
                clicker.closeInventory();
                break;
        }
    }

}
