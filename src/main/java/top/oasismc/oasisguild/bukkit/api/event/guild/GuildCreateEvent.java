package top.oasismc.oasisguild.bukkit.api.event.guild;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class GuildCreateEvent extends GuildEvent {

    private final Player creator;
    private String desc;
    private Location loc;

    private GuildCreateEvent(String guild, Player creator, String desc, Location loc) {
        super(guild);
        this.creator = creator;
        this.desc = desc;
        this.loc = loc;
    }

    public static GuildCreateEvent createGuildCreateEvent(String guild, Player creator, String desc, Location loc) {
        return new GuildCreateEvent(guild, creator, desc, loc);
    }

    public Player getCreator() {
        return creator;
    }

    public String getDesc() {
        return desc;
    }

    public Location getLoc() {
        return loc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

}
