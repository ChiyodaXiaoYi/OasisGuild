package top.oasismc.oasisguild.menu.impl;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import top.oasismc.oasisguild.event.guild.GuildLevelUpEvent;
import top.oasismc.oasisguild.event.guild.GuildLocChangeEvent;
import top.oasismc.oasisguild.event.guild.GuildPvpChangeEvent;
import top.oasismc.oasisguild.event.player.PlayerJoinGuildEvent;
import top.oasismc.oasisguild.event.player.PlayerQuitGuildEvent;
import top.oasismc.oasisguild.event.player.PlayerTpGuildLocEvent;
import top.oasismc.oasisguild.menu.MenuHolder;
import top.oasismc.oasisguild.menu.MenuType;
import top.oasismc.oasisguild.menu.api.IMenuListener;
import top.oasismc.oasisguild.util.MsgTool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static top.oasismc.oasisguild.OasisGuild.getPlugin;
import static top.oasismc.oasisguild.command.GuildCommand.getGuildCommand;
import static top.oasismc.oasisguild.data.DataHandler.getDataHandler;
import static top.oasismc.oasisguild.menu.impl.DefMenuDrawer.getDrawer;
import static top.oasismc.oasisguild.event.player.PlayerTpGuildLocEvent.createPlayerTpGuildLocEvent;
import static top.oasismc.oasisguild.event.player.PlayerQuitGuildEvent.createPlayerQuitGuildEvent;
import static top.oasismc.oasisguild.event.guild.GuildPvpChangeEvent.createGuildPvpChangeEvent;
import static top.oasismc.oasisguild.event.guild.GuildLocChangeEvent.createGuildLocChangeEvent;
import static top.oasismc.oasisguild.event.player.PlayerJoinGuildEvent.createPlayerJoinGuildEvent;
import static top.oasismc.oasisguild.event.guild.GuildLevelUpEvent.createGuildLevelUpEvent;

public class DefMenuListener implements IMenuListener {

    private final Map<MenuType, Consumer<InventoryClickEvent>> menuMap;
    private static final DefMenuListener listener;

    static {
        listener = new DefMenuListener();
    }

    public static DefMenuListener getListener() {
        return listener;
    }

    private DefMenuListener() {
        menuMap = new ConcurrentHashMap<>();
        menuMap.put(MenuType.INFO, this::handleGuildInfoMenuEvent);
        menuMap.put(MenuType.LIST, this::handleGuildListMenuEvent);
        menuMap.put(MenuType.EDIT, this::handleGuildEditMenuEvent);
        menuMap.put(MenuType.APPLY, this::handleGuildApply);
    }

    @Override
    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        if (event.getClickedInventory() == null) { return; }
        if (!(event.getClickedInventory().getHolder() instanceof MenuHolder)) {
            return;
        }
        event.setCancelled(true);
        MenuHolder holder = (MenuHolder) event.getClickedInventory().getHolder();
        try {
            menuMap.get(holder.getType()).accept(event);
        } catch (NullPointerException ignored) {
//            ignored.printStackTrace();
        }
    }

    private void handleGuildListMenuEvent(InventoryClickEvent event) {
        String gName = event.getCurrentItem().getItemMeta().getDisplayName().replace("§", "&");
        String holderGuildName = getDataHandler().getGuildNameByPlayer(event.getWhoClicked().getName());
        if (holderGuildName != null && holderGuildName.equals(gName)) {
            getGuildCommand().getCommandManager().openGuildInfoMenu(event.getWhoClicked(), gName);
        } else {
            if (event.isRightClick()) {
                getGuildCommand().getCommandManager().applyGuild(event.getWhoClicked(), gName);
                event.getWhoClicked().closeInventory();
            }
        }
    }

    private void handleGuildInfoMenuEvent(InventoryClickEvent event) {
        int inventorySize = event.getInventory().getSize();
        int clickSlot = event.getSlot();
        String clickedName = event.getWhoClicked().getName();
        String gName = getDataHandler().getGuildNameByPlayer(clickedName);
        int pJob = getDataHandler().getPlayerJob(gName, event.getWhoClicked().getName());
        if (pJob == -1) {
            if (clickSlot == 4) {
                if (event.isLeftClick()) {
                    playerTpGuildLoc(gName, (Player) event.getWhoClicked(), getDataHandler().getGuildLocationMap().get(gName));
                } else if (event.isRightClick()){
                    guildLevelUp((Player) event.getWhoClicked(), gName);
                }
            } else if (clickSlot > 8 && clickSlot < inventorySize - 9) {
                String name = event.getCurrentItem().getItemMeta().getDisplayName().replace("§", "&");
                name = name.substring(2);
                if (!name.equals(event.getWhoClicked().getName()))
                    kickMember(event.getAction(), name, (Player) event.getWhoClicked());
            } else if (clickSlot == inventorySize - 8) {
                event.getWhoClicked().openInventory(getDrawer().drawGuildEditMenu(gName));
            } else if (clickSlot == inventorySize - 2) {
                getGuildCommand().getCommandManager().disbandGuild((Player) event.getWhoClicked());
                event.getWhoClicked().closeInventory();
            }
        } else {
            if (clickSlot == 4) {
                playerTpGuildLoc(gName, (Player) event.getWhoClicked(), getDataHandler().getGuildLocationMap().get(gName));
            } else if (clickSlot == inventorySize - 5) {
                String pName = event.getWhoClicked().getName();
                PlayerQuitGuildEvent event1 = createPlayerQuitGuildEvent(gName, pName, PlayerQuitGuildEvent.QuitReason.QUIT);
                Bukkit.getPluginManager().callEvent(event1);
                if (event1.isCancelled())
                    return;
                getDataHandler().getGuildDao().memberQuit(event1.getGuildName(), event1.getPlayer());
                MsgTool.sendMsg(event.getWhoClicked(), "command.quit.success", getDataHandler().getGuildByName(gName));
            }
        }
    }

    private void playerTpGuildLoc(String guild, Player player, Location loc) {
        PlayerTpGuildLocEvent event = createPlayerTpGuildLocEvent(guild, player.getName(), loc);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;
        Player tpPlayer = Bukkit.getPlayer(event.getPlayer());
        if (tpPlayer == null)
            return;
        tpPlayer.teleport(event.getLoc());
    }

    private void handleGuildEditMenuEvent(InventoryClickEvent event) {
        String gName = getDataHandler().getGuildNameByPlayer(event.getWhoClicked().getName());
        switch (event.getSlot()) {
            case 9:
            case 11:
                break;//待完成
            case 13:
                event.getWhoClicked().openInventory(getDrawer().drawGuildApplyListMenu(gName));
                break;
            case 15:
                boolean pvp = getDataHandler().getGuildByName(gName).isPvp();
                GuildPvpChangeEvent event1 = createGuildPvpChangeEvent(gName, !pvp);
                Bukkit.getPluginManager().callEvent(event1);
                if (event1.isCancelled())
                    return;
                if (event1.isNewPvp()) {
                    getDataHandler().getGuildDao().changePvp(gName, 1);
                    MsgTool.sendMsg(event.getWhoClicked(), "command.pvp.open");
                } else {
                    getDataHandler().getGuildDao().changePvp(gName, 0);
                    MsgTool.sendMsg(event.getWhoClicked(), "command.pvp.close");
                }
                event.getWhoClicked().closeInventory();
                break;
            case 17:
                Location oldLoc = getDataHandler().getGuildLocationMap().get(gName);
                GuildLocChangeEvent event2 = createGuildLocChangeEvent(gName, oldLoc, event.getWhoClicked().getLocation());
                Bukkit.getPluginManager().callEvent(event2);
                if (event2.isCancelled())
                    return;
                if (getDataHandler().getGuildDao().changeLoc(event2.getGuildName(), event2.getNewLoc()))
                    MsgTool.sendMsg(event.getWhoClicked(), "command.changeLoc.success");
                event.getWhoClicked().closeInventory();
                break;
        }
    }

    private void kickMember(InventoryAction action, String pName, Player click) {
        if (action == InventoryAction.DROP_ONE_SLOT) {
            String gName = getDataHandler().getGuildNameByPlayer(pName);
            PlayerQuitGuildEvent event = createPlayerQuitGuildEvent(gName, pName, PlayerQuitGuildEvent.QuitReason.KICK);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled())
                return;
            if (getDataHandler().getGuildDao().memberQuit(event.getGuildName(), event.getPlayer())) {
                MsgTool.sendMsg(click, "command.kick.success", pName);
                Player member = Bukkit.getPlayer(pName);
                if (member != null && member.isOnline()) {
                    MsgTool.sendMsg(member, "command.kick.member", getDataHandler().getGuildByName(gName));
                }
                click.closeInventory();
            }
        }
    }

    private void handleGuildApply(InventoryClickEvent event) {
        Player admin = (Player) event.getWhoClicked();
        String gName = getDataHandler().getGuildNameByPlayer(admin.getName());
        String pName = event.getCurrentItem().getItemMeta().getDisplayName().replace("§", "&").substring(2);
        if (event.isLeftClick()) {
            int memberNum = getDataHandler().getGuildMembers().get(gName).size();
            int maxNum = getDataHandler().getGuildByName(gName).getMaxMember();
            if (memberNum < maxNum) {
                PlayerJoinGuildEvent event1 = createPlayerJoinGuildEvent(gName, pName, PlayerJoinGuildEvent.JoinReason.ACCEPT);
                Bukkit.getPluginManager().callEvent(event1);
                if (event1.isCancelled())
                    return;
                getDataHandler().getGuildDao().handleApply(event1.getGuildName(), "accept", event1.getPlayer());
                MsgTool.sendMsg(admin, "command.accept.admin", pName);
                if (Bukkit.getPlayer(pName) != null && Bukkit.getPlayer(pName).isOnline())
                    MsgTool.sendMsg(Bukkit.getPlayer(pName), "command.accept.member", getDataHandler().getGuildByName(gName));
            } else {
                MsgTool.sendMsg(admin, "command.accept.full");
            }
        } else {
            getDataHandler().getGuildDao().handleApply(gName, "deny", pName);
            MsgTool.sendMsg(admin, "command.deny.admin", pName);
            if (Bukkit.getPlayer(pName) != null && Bukkit.getPlayer(pName).isOnline())
                MsgTool.sendMsg(Bukkit.getPlayer(pName), "command.deny.member", getDataHandler().getGuildByName(gName));
        }
        event.getWhoClicked().closeInventory();
    }

    private void guildLevelUp(Player player, String gName) {
        int gLvl = getDataHandler().getGuildByName(gName).getGuildLevel();
        int maxLvl = getPlugin().getConfig().getInt("guildSettings.maxLvl", -1);
        if (maxLvl != -1) {
            if (gLvl + 1 >= maxLvl) {
                MsgTool.sendMsg(player, "command.levelUp.limit");
                player.closeInventory();
                return;
            }
        }
        int needLvl = (int)(Math.pow((gLvl + 1), 1.5));
        if (player.getLevel() < needLvl) {
            MsgTool.sendMsg(player, "command.levelUp.lackLvl");
        } else {
            player.setLevel(player.getLevel() - needLvl);
            MsgTool.sendMsg(player, "command.levelUp.success");
            GuildLevelUpEvent event = createGuildLevelUpEvent(gName, gLvl, 1);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled())
                return;
            getDataHandler().getGuildDao().levelUp(event.getGuildName(), event.getOldLevel());
        }
        player.closeInventory();
    }

}
