package top.oasismc.oasisguild.event.player;

import top.oasismc.oasisguild.event.guild.GuildEvent;

public class PlayerGuildEvent extends GuildEvent {

    private String player;

    public PlayerGuildEvent(String guild, String player) {
        super(guild);
        this.player = player;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

}
