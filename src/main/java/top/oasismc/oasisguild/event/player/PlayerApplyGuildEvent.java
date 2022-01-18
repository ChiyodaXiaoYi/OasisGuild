package top.oasismc.oasisguild.event.player;

public class PlayerApplyGuildEvent extends PlayerGuildEvent{

    private PlayerApplyGuildEvent(String guild, String player) {
        super(guild, player);
    }

    public static PlayerApplyGuildEvent createPlayerApplyGuildEvent(String guild, String player) {
        return new PlayerApplyGuildEvent(guild, player);
    }

}
