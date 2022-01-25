package top.oasismc.oasisguild.listener;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import top.oasismc.oasisguild.data.objects.GuildChunk;
import top.oasismc.oasisguild.util.MsgSender;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static top.oasismc.oasisguild.OasisGuild.getPlugin;
import static top.oasismc.oasisguild.data.DataHandler.getDataHandler;
import static top.oasismc.oasisguild.util.MsgSender.sendMsg;
import static top.oasismc.oasisguild.util.MsgSender.sendMsg4replaceOtherStr;

public class GuildChunkListener implements Listener {

    private static final GuildChunkListener LISTENER;

    static {
        LISTENER = new GuildChunkListener();
    }

    private final Map<UUID, Boolean> chunkSelSwitchMap;
    private final Map<String, List<GuildChunk>> selChunkMap;

    public static GuildChunkListener getListener() {
        return LISTENER;
    }

    private GuildChunkListener() {
        chunkSelSwitchMap = new ConcurrentHashMap<>();
        selChunkMap = new ConcurrentHashMap<>();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void chunkSelectListener(PlayerInteractEvent event) {
        if (event.getHand() == (EquipmentSlot.OFF_HAND))
            return;
        if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_AIR))
            return;
        if (chunkSelSwitchMap.getOrDefault(event.getPlayer().getUniqueId(), false)) {
            event.setCancelled(true);
            Chunk chunk = Objects.requireNonNull(event.getClickedBlock()).getChunk();
            GuildChunk gChunk = new GuildChunk(chunk.getX(), chunk.getZ(), chunk.getWorld().getName());
            String gName = getDataHandler().getGuildNameByPlayer(event.getPlayer().getName());

            //检查该世界是否能创建公会区块
            if (!getPlugin().getConfig().getStringList("guildSettings.guildChunks.canSetWorlds").contains(chunk.getWorld().getName())) {
                sendMsg(event.getPlayer(), "command.chunk.notAllowedWorld");
                return;
            }

            //检查是否有主人
            if (getDataHandler().getChunkOwner(chunk) != null) {
                sendMsg(event.getPlayer(), "command.chunk.hasOwner");
                return;
            }

            if (!selChunkMap.containsKey(gName)) {
                selChunkMap.put(gName, new ArrayList<>());
            }

            //检查是否到达公会区块上限
            int defaultMaxChunk = getPlugin().getConfig().getInt("guildSettings.guildChunks.default", 36);
            int perLvlAdd = getPlugin().getConfig().getInt("guildSettings.guildChunks.perLvlAdd", 8);
            int maxChunks = getDataHandler().getGuildByName(gName).getGuildLevel() * perLvlAdd + defaultMaxChunk;
            int nowChunkNum = selChunkMap.get(gName).size() + getDataHandler().getGuildChunkSet(gName).size();
            if (nowChunkNum >= maxChunks) {
                sendMsg(event.getPlayer(), "command.chunk.limit");
                return;
            }

            if (!selChunkMap.get(gName).contains(gChunk)) {
                selChunkMap.get(gName).add(gChunk);
            }
            sendMsg4replaceOtherStr(event.getPlayer(), "command.chunk.click", "%chunk%", chunk.getX() + ", " + chunk.getZ());
        }
    }

    public void startChunkSelect(Player player) {
        chunkSelSwitchMap.put(player.getUniqueId(), true);
    }

    public void endChunkSelect(Player player) {
        if (chunkSelSwitchMap.get(player.getUniqueId()) != null) {
            chunkSelSwitchMap.remove(player.getUniqueId());
        }
        String gName = getDataHandler().getGuildNameByPlayer(player.getName());
        if (selChunkMap.get(gName) != null) {
            selChunkMap.remove(gName);
        }
    }

    public Map<String, List<GuildChunk>> getSelChunkMap() {
        return selChunkMap;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        String toGName = getDataHandler().getChunkOwner(event.getTo().getChunk());
        String fromGName = getDataHandler().getChunkOwner(event.getFrom().getChunk());
        if (toGName == null) {
            if (fromGName != null) {
                MsgSender.sendMsg4replaceGuild(event.getPlayer(), "chunk.leave", getDataHandler().getGuildByName(fromGName));
            }
        } else {
            if (fromGName == null) {
                MsgSender.sendMsg4replaceGuild(event.getPlayer(), "chunk.join", getDataHandler().getGuildByName(toGName));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDestroyBlock(BlockBreakEvent event) {
        String blockGName = getDataHandler().getChunkOwner(event.getBlock().getChunk());
        if (blockGName == null)
            return;
        String playerGName = getDataHandler().getGuildNameByPlayer(event.getPlayer().getName());
        if (!blockGName.equals(playerGName)) {
            event.setCancelled(true);
            MsgSender.sendMsg4replaceGuild(event.getPlayer(), "chunk.destroy", getDataHandler().getGuildByName(blockGName));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPlaceBlock(BlockPlaceEvent event) {
        String blockGName = getDataHandler().getChunkOwner(event.getBlock().getChunk());
        if (blockGName == null)
            return;
        String playerGName = getDataHandler().getGuildNameByPlayer(event.getPlayer().getName());
        if (!blockGName.equals(playerGName)) {
            event.setCancelled(true);
            MsgSender.sendMsg4replaceGuild(event.getPlayer(), "chunk.place", getDataHandler().getGuildByName(blockGName));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerFertilize(BlockFertilizeEvent event) {
        String blockGName = getDataHandler().getChunkOwner(event.getBlock().getChunk());
        if (blockGName == null)
            return;
        String playerGName = getDataHandler().getGuildNameByPlayer(event.getPlayer().getName());
        if (!blockGName.equals(playerGName)) {
            event.setCancelled(true);
            MsgSender.sendMsg4replaceGuild(event.getPlayer(), "chunk.use", getDataHandler().getGuildByName(blockGName));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerClickArmorStand(PlayerArmorStandManipulateEvent event) {
        Chunk chunk = event.getRightClicked().getLocation().getChunk();
        String blockGName = getDataHandler().getChunkOwner(chunk);
        if (blockGName == null)
            return;
        String playerGName = getDataHandler().getGuildNameByPlayer(event.getPlayer().getName());
        if (!blockGName.equals(playerGName)) {
            event.setCancelled(true);
            MsgSender.sendMsg4replaceGuild(event.getPlayer(), "chunk.use", getDataHandler().getGuildByName(blockGName));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
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
            MsgSender.sendMsg4replaceGuild(event.getPlayer(), "chunk.container", getDataHandler().getGuildByName(blockGName));
        }
    }

}
