package top.oasismc.oasisguild.event.guild;

import org.bukkit.entity.Player;

public class GuildRenameEvent extends GuildEvent {

    private String newName;
    private Player renamer;

    private GuildRenameEvent(String guild, String newName, Player renamer) {
        super(guild);
        this.newName = newName;
        this.renamer = renamer;
    }

    public static GuildRenameEvent createGuildRenameEvent(String guild, String newName, Player renamer) {
        return new GuildRenameEvent(guild, newName, renamer);
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public Player getRenamer() {
        return renamer;
    }

    public void setRenamer(Player renamer) {
        this.renamer = renamer;
    }
}
