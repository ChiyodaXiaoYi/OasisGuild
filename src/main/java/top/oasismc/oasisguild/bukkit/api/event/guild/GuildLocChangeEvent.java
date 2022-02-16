package top.oasismc.oasisguild.bukkit.api.event.guild;


import org.bukkit.Location;

public class GuildLocChangeEvent extends GuildEvent {

    private final Location oldLoc;
    private Location newLoc;

    private GuildLocChangeEvent(String guild, Location oldLoc, Location newLoc) {
        super(guild);
        this.oldLoc = oldLoc;
        this.newLoc = newLoc;
    }

    public static GuildLocChangeEvent createGuildLocChangeEvent(String guild, Location oldLoc, Location newLoc) {
        return new GuildLocChangeEvent(guild, oldLoc, newLoc);
    }

    public Location getOldLoc() {
        return oldLoc;
    }

    public Location getNewLoc() {
        return newLoc;
    }

    public void setNewLoc(Location newLoc) {
        this.newLoc = newLoc;
    }

}
