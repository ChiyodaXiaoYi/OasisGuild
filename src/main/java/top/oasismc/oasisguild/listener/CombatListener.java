package top.oasismc.oasisguild.listener;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import static top.oasismc.oasisguild.data.DataHandler.*;

public class CombatListener implements Listener {

    private static final CombatListener listener;

    static {
        listener = new CombatListener();
    }

    public static CombatListener getListener() {
        return listener;
    }

    private CombatListener() {}

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
            String gName1 = getDataHandler().getGuildNameByPlayer(player.getName());
            String gName2 = getDataHandler().getGuildNameByPlayer(damager.getName());
            if (gName1 == null || gName2 == null) {
                return;
            }
            if (gName1.equals(gName2)) {
                if (!getDataHandler().getGuildByName(gName1).isPvp()) {
                    event.setCancelled(true);
                }
            }
        }
    }

}
