package top.oasismc.oasisguild.bukkit.api.event.guild;

public class GuildResetDescEvent extends GuildEvent {

    private String newDesc;

    public GuildResetDescEvent(String guildName, String newDesc) {
        super(guildName);
        this.newDesc = newDesc;
    }

    public String getNewDesc() {
        return newDesc;
    }

    public void setNewDesc(String newDesc) {
        this.newDesc = newDesc;
    }

}
