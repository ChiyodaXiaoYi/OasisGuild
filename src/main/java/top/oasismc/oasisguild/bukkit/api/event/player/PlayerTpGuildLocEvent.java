package top.oasismc.oasisguild.bukkit.api.event.player;

import org.bukkit.Location;

public class PlayerTpGuildLocEvent extends PlayerGuildEvent {

    private final Location loc;

    private PlayerTpGuildLocEvent(String guild, String player, Location loc) {
        super(guild, player);
        this.loc = loc;
    }

    public static PlayerTpGuildLocEvent createPlayerTpGuildLocEvent(String guild, String player, Location loc) {
        return new PlayerTpGuildLocEvent(guild, player, loc);
    }

    public Location getLoc() {
        return loc;
    }

}
