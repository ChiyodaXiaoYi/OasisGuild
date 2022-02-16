package top.oasismc.oasisguild.bukkit.listener;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import static top.oasismc.oasisguild.bukkit.data.DataManager.*;

public final class GuildPvpListener implements Listener {

    private static final GuildPvpListener listener;

    static {
        listener = new GuildPvpListener();
    }

    public static GuildPvpListener getListener() {
        return listener;
    }

    private GuildPvpListener() {}

    @EventHandler
    public void checkGuildPvp(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Player damager;
            if (event.getDamager() instanceof Player) {
                damager = (Player) event.getDamager();
            } else if (event.getDamager() instanceof Projectile) {
                if (((Projectile) event.getDamager()).getShooter() instanceof Player) {
                    damager = (Player) ((Projectile) event.getDamager()).getShooter();
                } else {
                    return;
                }
            } else {
                return;
            }
            String gName1 = getDataManager().getGuildNameByPlayer(player.getName());
            String gName2 = getDataManager().getGuildNameByPlayer(damager.getName());
            if (gName1 == null || gName2 == null) {
                return;
            }
            if (gName1.equals(gName2)) {
                if (!getDataManager().getGuildByName(gName1).isPvp()) {
                    event.setCancelled(true);
                }
            }
        }
    }

}
