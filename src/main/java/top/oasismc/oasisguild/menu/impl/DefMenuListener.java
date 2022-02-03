package top.oasismc.oasisguild.menu.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import top.oasismc.oasisguild.event.player.PlayerJoinGuildEvent;
import top.oasismc.oasisguild.event.player.PlayerQuitGuildEvent;
import top.oasismc.oasisguild.factory.GuildFactory;
import top.oasismc.oasisguild.job.Jobs;
import top.oasismc.oasisguild.menu.MenuHolder;
import top.oasismc.oasisguild.menu.MenuType;
import top.oasismc.oasisguild.menu.api.IMenuListener;
import top.oasismc.oasisguild.util.MsgSender;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static top.oasismc.oasisguild.command.GuildCommand.getGuildCommand;
import static top.oasismc.oasisguild.data.DataHandler.getDataHandler;
import static top.oasismc.oasisguild.factory.GuildFactory.*;
import static top.oasismc.oasisguild.menu.impl.DefMenuDrawer.getDrawer;
import static top.oasismc.oasisguild.util.MsgCatcher.getCatcher;
import static top.oasismc.oasisguild.util.MsgSender.sendMsg;
import static top.oasismc.oasisguild.job.Jobs.*;
import static top.oasismc.oasisguild.util.MsgSender.sendMsg4replacePlayer;

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
    @EventHandler(priority = EventPriority.HIGH)
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        if (event.getClickedInventory() == null) { return; }
        if (event.getView().getTopInventory().getHolder() instanceof MenuHolder) {
            event.setCancelled(true);
        }
        if (!(event.getClickedInventory().getHolder() instanceof MenuHolder)) {
            return;
        }
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

        if (pJob >= MEDIUM) { //大于等于优秀成员时显示的是带公会管理的界面
            if (clickSlot == 4) {
                if (pJob >= ADVANCED) {
                    if (event.isLeftClick()) {
                        playerTpGuildLoc((Player) event.getWhoClicked());
                    } else if (event.isRightClick()) {
                        guildLevelUpOnMenu((Player) event.getWhoClicked(), gName);
                    }
                } else {
                    playerTpGuildLoc((Player) event.getWhoClicked());
                }
            } else if (clickSlot > 8 && clickSlot < inventorySize - 9) {
                String name = event.getCurrentItem().getItemMeta().getDisplayName().replace("§", "&");
                name = name.substring(2);
                if (pJob > VICE_LEADER)
                    handleGuildMemberAction(event.getClick(), name, (Player) event.getWhoClicked(), pJob);
            } else if (clickSlot == inventorySize - 8) {
                event.getWhoClicked().openInventory(getDrawer().drawGuildEditMenu(gName));
            } else if (clickSlot == inventorySize - 2) {
                if (pJob >= LEADER)
                    getGuildCommand().getCommandManager().disbandGuildByCmd((Player) event.getWhoClicked());
                else
                    getGuildCommand().getCommandManager().playerQuitGuildByCmd((Player) event.getWhoClicked());
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
        int pJob = getDataHandler().getPlayerJob(gName, event.getWhoClicked().getName());
        switch (event.getSlot()) {
            case 9:
                if (pJob < VICE_LEADER) {
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
                break;
            case 11:
                if (pJob < VICE_LEADER) {
                    sendMsg(event.getWhoClicked(), "noPerm");
                    event.getWhoClicked().closeInventory();
                    return;
                }
                break;//待完成
            case 13:
                if (pJob >= ADVANCED)
                    event.getWhoClicked().openInventory(getDrawer().drawGuildApplyListMenu(gName));
                else {
                    sendMsg(event.getWhoClicked(), "noPerm");
                    event.getWhoClicked().closeInventory();
                }
                break;
            case 15:
                if (pJob < MEDIUM) {
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
                break;
            case 17:
                if (pJob < VICE_LEADER) {
                    sendMsg(event.getWhoClicked(), "noPerm");
                    event.getWhoClicked().closeInventory();
                    return;
                }
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
                MsgSender.sendMsg4replacePlayer(admin, "menu.accept.admin", pName);
                if (Bukkit.getPlayer(pName) != null && Bukkit.getPlayer(pName).isOnline())
                    MsgSender.sendMsg4replaceGuild(Bukkit.getPlayer(pName), "menu.accept.member", getDataHandler().getGuildByName(gName));
            } else {
                sendMsg(admin, "menu.accept.full");
            }
        } else {
            getDataHandler().getGuildDao().handleApply(gName, "deny", pName);
            MsgSender.sendMsg4replacePlayer(admin, "menu.deny.admin", pName);
            if (Bukkit.getPlayer(pName) != null && Bukkit.getPlayer(pName).isOnline())
                MsgSender.sendMsg4replaceGuild(Bukkit.getPlayer(pName), "menu.deny.member", getDataHandler().getGuildByName(gName));
        }
        event.getWhoClicked().closeInventory();
    }

    private void handleGuildMemberAction(ClickType action, String pName, Player clicker, int clickerJob) {
        String gName = getDataHandler().getGuildNameByPlayer(pName);
        int pJob = getDataHandler().getPlayerJob(gName, pName);
        switch (action) {
            case DROP:
                if (clickerJob <= pJob)
                    return;
                playerQuitGuild(gName, pName, PlayerQuitGuildEvent.QuitReason.KICK);
                MsgSender.sendMsg4replacePlayer(clicker, "menu.kick.success", pName);
                Player member = Bukkit.getPlayer(pName);
                if (member != null && member.isOnline()) {
                    MsgSender.sendMsg4replaceGuild(member, "menu.kick.member", getDataHandler().getGuildByName(gName));
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
                    sendMsg(clicker, "menu.jobChange.highest");
                    clicker.closeInventory();
                    return;
                }
                if (newJob >= clickerJob) {
                    sendMsg(clicker, "noPerm");
                    clicker.closeInventory();
                    return;
                }
                GuildFactory.memberJobChange(gName, pName, clicker.getName(), pJob, newJob);
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
                GuildFactory.memberJobChange(gName, pName, clicker.getName(), pJob, newJob);
                sendMsg4replacePlayer(clicker, "menu.jobChange.down", pName);
                clicker.closeInventory();
                break;
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
