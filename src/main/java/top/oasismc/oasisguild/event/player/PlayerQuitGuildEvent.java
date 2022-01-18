package top.oasismc.oasisguild.event.player;

public class PlayerQuitGuildEvent extends PlayerGuildEvent {

    public final QuitReason reason;

    private PlayerQuitGuildEvent(String guild, String player, QuitReason reason) {
        super(guild, player);
        this.reason = reason;
    }

    public static PlayerQuitGuildEvent createPlayerQuitGuildEvent(String guild, String player, QuitReason reason) {
        return new PlayerQuitGuildEvent(guild, player, reason);
    }

    public enum QuitReason {
        KICK,
        QUIT
    }

    public QuitReason getReason() {
        return reason;
    }
}
