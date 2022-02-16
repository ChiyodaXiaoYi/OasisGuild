package top.oasismc.oasisguild.bukkit.api.event.guild;

import static top.oasismc.oasisguild.bukkit.data.DataManager.getDataManager;

public class GuildPvpChangeEvent extends GuildEvent {

    private final boolean oldPvp;
    private boolean newPvp;

    private GuildPvpChangeEvent(String guild, boolean newPvp) {
        super(guild);
        oldPvp = getDataManager().getGuildByName(guild).isPvp();
        this.newPvp = newPvp;
    }

    public static GuildPvpChangeEvent createGuildPvpChangeEvent(String guild, boolean newPvp) {
        return new GuildPvpChangeEvent(guild, newPvp);
    }

    public boolean isOldPvp() {
        return oldPvp;
    }

    public boolean isNewPvp() {
        return newPvp;
    }

    public void setNewPvp(boolean newPvp) {
        this.newPvp = newPvp;
    }

}
