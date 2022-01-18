package top.oasismc.oasisguild.event.guild;

import org.bukkit.entity.Player;

public class GuildDisbandEvent extends GuildEvent {

    private final Player disbander;

    private GuildDisbandEvent(String guild, Player disbander) {
        super(guild);
        this.disbander = disbander;
    }

    public static GuildDisbandEvent createGuildDisbandEvent(String guild, Player disbander) {
        return new GuildDisbandEvent(guild, disbander);
    }

    public Player getDisbander() {
        return disbander;
    }

}
