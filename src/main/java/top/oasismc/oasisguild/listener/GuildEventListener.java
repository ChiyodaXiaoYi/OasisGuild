package top.oasismc.oasisguild.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.oasismc.oasisguild.event.guild.GuildCreateEvent;
import top.oasismc.oasisguild.event.guild.GuildDisbandEvent;
import top.oasismc.oasisguild.event.guild.GuildRenameEvent;
import top.oasismc.oasisguild.util.GuildManager;
import top.oasismc.oasisguild.util.LoreTool;

import java.util.List;

import static top.oasismc.oasisguild.OasisGuild.getPlugin;
import static top.oasismc.oasisguild.data.DataHandler.getDataHandler;
import static top.oasismc.oasisguild.util.MsgSender.color;
import static top.oasismc.oasisguild.util.MsgSender.sendMsg;

public final class GuildEventListener implements Listener {

    private static final GuildEventListener listener;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreateGuild(GuildCreateEvent event) {
        Player creator = event.getCreator();
        String gName = event.getGuildName();
        String desc = event.getDesc();
        String world = creator.getWorld().getName();
        List<String> canSetWorlds = getPlugin().getConfig().getStringList("guildSettings.canSetWorlds");
        if (!canSetWorlds.contains(world)) {
            sendMsg(creator, "command.create.notAllowedWorld");
            event.setCancelled(true);
            return;
        }

        //检查玩家是否已经有公会
        if (getDataHandler().getGuildNameByPlayer(creator.getName()) != null) {
            sendMsg(creator, "command.create.hasGuild");
            event.setCancelled(true);
            return;
        }

        switch (GuildManager.checkGuildName(gName)) {
            case -1:
                sendMsg(creator, "command.create.sameName");
                event.setCancelled(true);
                return;
            case -2:
                sendMsg(creator, "command.create.nameTooLong");
                event.setCancelled(true);
                return;
            case 1:
                String defaultColor = getPlugin().getConfig().getString("guildSettings.name.defaultColor", "&f");
                gName = defaultColor + gName;
                event.setGuildName(gName);
        }

        //检查公会描述长度
        int maxDescLength = getPlugin().getConfig().getInt("guildSettings.desc.maxLength", 15);
        if (desc.length() > maxDescLength) {
            sendMsg(creator, "command.create.descTooLong");
            event.setCancelled(true);
            return;
        }

        //检查玩家是否有创建所需物品
        ItemStack item = creator.getInventory().getItemInMainHand();
        boolean needItem = getPlugin().getConfig().getBoolean("conditions.create.item.enable");
        if (needItem) {
            String material = getPlugin().getConfig().getString("conditions.create.item.material");
            String name = color(getPlugin().getConfig().getString("conditions.create.item.name"));
            String lore = color(getPlugin().getConfig().getString("conditions.create.item.lore"));
            if (item.getType() != Material.getMaterial(material)) {
                sendMsg(creator, "command.create.needItem");
                event.setCancelled(true);
                return;
            }
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                sendMsg(creator, "command.create.needItem");
                event.setCancelled(true);
                return;
            }
            if (!meta.hasDisplayName()) {
                sendMsg(creator, "command.create.needItem");
                event.setCancelled(true);
                return;
            }
            if (!meta.getDisplayName().equals(name)) {
                sendMsg(creator, "command.create.needItem");
                event.setCancelled(true);
                return;
            }
            if (!LoreTool.hasLore(lore, meta.getLore())) {
                sendMsg(creator, "command.create.needItem");
                event.setCancelled(true);
                return;
            }
        }

        //检查权限
        String perm = getPlugin().getConfig().getString("conditions.create.perm");
        if (perm != null && !perm.equals("")) {
            if (!creator.hasPermission(perm)) {
                sendMsg(creator, "noPerm");
                event.setCancelled(true);
                return;
            }
        }

        //检查等级
        int needLvl = getPlugin().getConfig().getInt("conditions.create.exp", 100);
        if (needLvl != 0) {
            if (creator.getLevel() < needLvl) {
                sendMsg(creator, "command.create.needLvl");
                event.setCancelled(true);
                return;
            }
        }

        if (needItem)
            item.setAmount(item.getAmount() - 1);
        creator.getInventory().setItemInMainHand(item);
        creator.setLevel(creator.getLevel() - needLvl);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDisbandGuild(GuildDisbandEvent event) {
        if (!event.getDisbander().hasPermission("oasis.guild.disband")) {
            sendMsg(event.getDisbander(), "noPerm");
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGuildRename(GuildRenameEvent event) {
        if (!event.getRenamer().hasPermission("oasis.guild.rename")) {
            sendMsg(event.getRenamer(), "noPerm");
            event.setCancelled(true);
        }
    }

    static {
        listener = new GuildEventListener();
    }

    private GuildEventListener() {}

    public static GuildEventListener getListener() {
        return listener;
    }

}
