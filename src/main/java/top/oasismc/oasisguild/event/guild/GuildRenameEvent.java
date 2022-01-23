package top.oasismc.oasisguild.event.guild;

public class GuildRenameEvent extends GuildEvent {

    private String newName;

    private GuildRenameEvent(String guild, String newName) {
        super(guild);
        this.newName = newName;
    }

    public static GuildRenameEvent createGuildRenameEvent(String guild, String newName) {
        return new GuildRenameEvent(guild, newName);
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

}
