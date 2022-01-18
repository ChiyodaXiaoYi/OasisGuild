package top.oasismc.oasisguild.event.guild;

import top.oasismc.oasisguild.data.objects.Guild;

public class GuildRenameEvent extends GuildEvent {

    private final String oldName;
    private String newName;

    private GuildRenameEvent(String guild, String oldName, String newName) {
        super(guild);
        this.oldName = oldName;
        this.newName = newName;
    }

    public static GuildRenameEvent createGuildRenameEvent(String guild, String oldName, String newName) {
        return new GuildRenameEvent(guild, oldName, newName);
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

}
