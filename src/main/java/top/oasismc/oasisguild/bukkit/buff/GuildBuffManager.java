package top.oasismc.oasisguild.bukkit.buff;

import org.bukkit.scheduler.BukkitRunnable;

import static top.oasismc.oasisguild.bukkit.OasisGuild.getPlugin;

public class GuildBuffManager {

    private BukkitRunnable runnable;

    private GuildBuffManager() {

    }

    private void resetRunnable() {
        if (runnable != null)
            runnable.cancel();
        long period = getPlugin().getConfig().getInt("guildBuffSettings.periodSecond", 10) * 20L;
        runnable = new BukkitRunnable() {
            @Override
            public void run() {

            }
        };
        runnable.runTaskTimerAsynchronously(getPlugin(), 0, period);
    }

}
