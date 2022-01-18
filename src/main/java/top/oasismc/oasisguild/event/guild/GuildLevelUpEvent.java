package top.oasismc.oasisguild.event.guild;

public class GuildLevelUpEvent extends GuildEvent {

    private final int oldLevel;
    private int upNum;

    private GuildLevelUpEvent(String guild, int oldLevel, int upNum) {
        super(guild);
        this.oldLevel = oldLevel;
        this.upNum = upNum;
    }

    public static GuildLevelUpEvent createGuildLevelUpEvent(String guild, int oldLevel, int upNum) {
        return new GuildLevelUpEvent(guild, oldLevel, upNum);
    }

    public int getOldLevel() {
        return oldLevel;
    }

    public int getUpNum() {
        return upNum;
    }

    public void setUpNum(int upNum) {
        this.upNum = upNum;
    }

}
