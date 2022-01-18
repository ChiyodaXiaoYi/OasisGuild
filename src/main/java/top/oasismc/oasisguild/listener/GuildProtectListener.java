package top.oasismc.oasisguild.listener;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Objects;

import static top.oasismc.oasisguild.OasisGuild.getPlugin;
import static top.oasismc.oasisguild.data.DataHandler.getDataHandler;
import static top.oasismc.oasisguild.util.MsgTool.sendMsg;

public class GuildProtectListener implements Listener {

    private static final GuildProtectListener LISTENER;

    static {
        LISTENER = new GuildProtectListener();
    }

    public static GuildProtectListener getListener() {
        return LISTENER;
    }

    private GuildProtectListener() {
        getConfigNum();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        String toGName = getDataHandler().getChunkOwner(event.getTo().getChunk());
        String fromGName = getDataHandler().getChunkOwner(event.getFrom().getChunk());
        if (toGName == null) {
            if (fromGName != null) {
                sendMsg(event.getPlayer(), "protect.leave", getDataHandler().getGuildByName(fromGName));
            }
        } else {
            if (fromGName == null) {
                sendMsg(event.getPlayer(), "protect.join", getDataHandler().getGuildByName(toGName));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDestroyBlock(BlockBreakEvent event) {
        String blockGName = getDataHandler().getChunkOwner(event.getBlock().getChunk());
        if (blockGName == null)
            return;
        String playerGName = getDataHandler().getGuildNameByPlayer(event.getPlayer().getName());
        if (!blockGName.equals(playerGName)) {
            event.setCancelled(true);
            sendMsg(event.getPlayer(), "protect.destroy", getDataHandler().getGuildByName(blockGName));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPlaceBlock(BlockPlaceEvent event) {
        String blockGName = getDataHandler().getChunkOwner(event.getBlock().getChunk());
        if (blockGName == null)
            return;
        String playerGName = getDataHandler().getGuildNameByPlayer(event.getPlayer().getName());
        if (!blockGName.equals(playerGName)) {
            event.setCancelled(true);
            sendMsg(event.getPlayer(), "protect.place", getDataHandler().getGuildByName(blockGName));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerFertilize(BlockFertilizeEvent event) {
        String blockGName = getDataHandler().getChunkOwner(event.getBlock().getChunk());
        if (blockGName == null)
            return;
        String playerGName = getDataHandler().getGuildNameByPlayer(event.getPlayer().getName());
        if (!blockGName.equals(playerGName)) {
            event.setCancelled(true);
            sendMsg(event.getPlayer(), "protect.use", getDataHandler().getGuildByName(blockGName));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerClickArmorStand(PlayerArmorStandManipulateEvent event) {
        Chunk chunk = event.getRightClicked().getLocation().getChunk();
        String blockGName = getDataHandler().getChunkOwner(chunk);
        if (blockGName == null)
            return;
        String playerGName = getDataHandler().getGuildNameByPlayer(event.getPlayer().getName());
        if (!blockGName.equals(playerGName)) {
            event.setCancelled(true);
            sendMsg(event.getPlayer(), "protect.use", getDataHandler().getGuildByName(blockGName));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerOpenContainer(InventoryOpenEvent event) {
        Location invLoc = event.getInventory().getLocation();
        if (invLoc == null)
            return;
        String blockGName = getDataHandler().getChunkOwner(invLoc.getChunk());
        if (blockGName == null)
            return;
        String playerGName = getDataHandler().getGuildNameByPlayer(event.getPlayer().getName());
        if (!blockGName.equals(playerGName)) {
            event.setCancelled(true);
            sendMsg(event.getPlayer(), "protect.container", getDataHandler().getGuildByName(blockGName));
        }
    }

    public void getConfigNum() {
    }

}
