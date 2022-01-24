package top.oasismc.oasisguild.menu.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import top.oasismc.oasisguild.event.player.PlayerJoinGuildEvent;
import top.oasismc.oasisguild.event.player.PlayerQuitGuildEvent;
import top.oasismc.oasisguild.factory.GuildFactory;
import top.oasismc.oasisguild.menu.MenuHolder;
import top.oasismc.oasisguild.menu.MenuType;
import top.oasismc.oasisguild.menu.api.IMenuListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static top.oasismc.oasisguild.command.GuildCommand.getGuildCommand;
import static top.oasismc.oasisguild.data.DataHandler.getDataHandler;
import static top.oasismc.oasisguild.factory.GuildFactory.*;
import static top.oasismc.oasisguild.menu.impl.DefMenuDrawer.getDrawer;
import static top.oasismc.oasisguild.util.MsgCatcher.getCatcher;
import static top.oasismc.oasisguild.util.MsgSender.sendMsg;

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

    @Override
    public void handleGuildListMenuEvent(InventoryClickEvent event) {
        if (event.getSlot() >= 9 && event.getSlot() < 45) {
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
        } else if (event.getSlot() == 46) {
            getDrawer().drawGuildListMenu(0);
        } else if (event.getSlot() == 52) {
            getDrawer().drawGuildListMenu(0);
        } else if (event.getSlot() == 49) {
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
        }
    }

    @Override
    public void handleGuildInfoMenuEvent(InventoryClickEvent event) {
        int inventorySize = event.getInventory().getSize();
        int clickSlot = event.getSlot();
        String clickedName = event.getWhoClicked().getName();
        String gName = getDataHandler().getGuildNameByPlayer(clickedName);
        int pJob = getDataHandler().getPlayerJob(gName, event.getWhoClicked().getName());
        if (pJob == -1) {
            if (clickSlot == 4) {
                if (event.isLeftClick()) {
                    playerTpGuildLoc((Player) event.getWhoClicked());
                } else if (event.isRightClick()){
                    guildLevelUpOnMenu((Player) event.getWhoClicked(), gName);
                }
            } else if (clickSlot > 8 && clickSlot < inventorySize - 9) {
                String name = event.getCurrentItem().getItemMeta().getDisplayName().replace("§", "&");
                name = name.substring(2);
                if (!name.equals(event.getWhoClicked().getName()))
                    kickMemberOnMenu(event.getAction(), name, (Player) event.getWhoClicked());
            } else if (clickSlot == inventorySize - 8) {
                event.getWhoClicked().openInventory(getDrawer().drawGuildEditMenu(gName));
            } else if (clickSlot == inventorySize - 2) {
                getGuildCommand().getCommandManager().disbandGuildByCmd((Player) event.getWhoClicked());
                event.getWhoClicked().closeInventory();
            }
        } else {
            if (clickSlot == 4) {
                playerTpGuildLoc((Player) event.getWhoClicked());
            } else if (clickSlot == inventorySize - 5) {
                getGuildCommand().getCommandManager().playerQuitGuildByCmd((Player) event.getWhoClicked());
            }
        }
    }

    @Override
    public void handleGuildEditMenuEvent(InventoryClickEvent event) {
        String gName = getDataHandler().getGuildNameByPlayer(event.getWhoClicked().getName());
        switch (event.getSlot()) {
            case 9:
                event.getWhoClicked().closeInventory();
                sendMsg(event.getWhoClicked(), "menu.rename.needNewName");
                getCatcher().startCatch((Player) event.getWhoClicked(), newName -> {
                    getGuildCommand().getCommandManager().guildRenameByCmd((Player) event.getWhoClicked(), newName);
                    getCatcher().endCatch((Player) event.getWhoClicked());
                });
                break;
            case 11:
                break;//待完成
            case 13:
                event.getWhoClicked().openInventory(getDrawer().drawGuildApplyListMenu(gName));
                break;
            case 15:
                if (changeGuildPvp(gName)) {
                    sendMsg(event.getWhoClicked(), "menu.pvp.open");
                } else {
                    sendMsg(event.getWhoClicked(), "menu.pvp.close");
                }
                event.getWhoClicked().closeInventory();
                break;
            case 17:
                if (changeGuildLoc(gName, event.getWhoClicked().getLocation()))
                    sendMsg(event.getWhoClicked(), "menu.changeLoc.success");
                event.getWhoClicked().closeInventory();
                break;
        }
    }

    @Override
    public void handleGuildApply(InventoryClickEvent event) {
        Player admin = (Player) event.getWhoClicked();
        String gName = getDataHandler().getGuildNameByPlayer(admin.getName());
        String pName = event.getCurrentItem().getItemMeta().getDisplayName().replace("§", "&").substring(2);
        if (event.isLeftClick()) {
            int memberNum = getDataHandler().getGuildMembers().get(gName).size();
            int maxNum = getDataHandler().getGuildByName(gName).getMaxMember();
            if (memberNum < maxNum) {
                playerJoinGuild(gName, pName, PlayerJoinGuildEvent.JoinReason.ACCEPT);
                sendMsg(admin, "menu.accept.admin", pName);
                if (Bukkit.getPlayer(pName) != null && Bukkit.getPlayer(pName).isOnline())
                    sendMsg(Bukkit.getPlayer(pName), "menu.accept.member", getDataHandler().getGuildByName(gName));
            } else {
                sendMsg(admin, "menu.accept.full");
            }
        } else {
            getDataHandler().getGuildDao().handleApply(gName, "deny", pName);
            sendMsg(admin, "menu.deny.admin", pName);
            if (Bukkit.getPlayer(pName) != null && Bukkit.getPlayer(pName).isOnline())
                sendMsg(Bukkit.getPlayer(pName), "menu.deny.member", getDataHandler().getGuildByName(gName));
        }
        event.getWhoClicked().closeInventory();
    }

    private void kickMemberOnMenu(InventoryAction action, String pName, Player click) {
        if (action == InventoryAction.DROP_ONE_SLOT) {
            String gName = getDataHandler().getGuildNameByPlayer(pName);
            playerQuitGuild(gName, pName, PlayerQuitGuildEvent.QuitReason.KICK);
            sendMsg(click, "menu.kick.success", pName);
            Player member = Bukkit.getPlayer(pName);
            if (member != null && member.isOnline()) {
                sendMsg(member, "menu.kick.member", getDataHandler().getGuildByName(gName));
            }
            click.closeInventory();
        }
    }

    private void guildLevelUpOnMenu(Player player, String gName) {
        int gLvl = getDataHandler().getGuildByName(gName).getGuildLevel();
        int code = GuildFactory.guildLevelUp(player, gName, gLvl, 1);
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

}
