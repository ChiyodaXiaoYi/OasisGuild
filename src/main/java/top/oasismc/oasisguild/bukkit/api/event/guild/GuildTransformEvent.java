package top.oasismc.oasisguild.bukkit.api.event.guild;

public class GuildTransformEvent extends GuildEvent {

    private final String operator;
    private String newLeader;

    private GuildTransformEvent(String guildName, String operator, String newLeader) {
        super(guildName);
        this.operator = operator;
        this.newLeader = newLeader;
    }

    public static GuildTransformEvent createGuildTransformEvent(String guildName, String operator, String newLeader) {
        return new GuildTransformEvent(guildName, operator, newLeader);
    }

    public String getNewLeader() {
        return newLeader;
    }

    public void setNewLeader(String newLeader) {
        this.newLeader = newLeader;
    }

    public String getOperator() {
        return operator;
    }

}
