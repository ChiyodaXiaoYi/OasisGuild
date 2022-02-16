package top.oasismc.oasisguild.bukkit.api.event.player;

public class PlayerJoinGuildEvent extends PlayerGuildEvent {

    private final JoinReason reason;

    private PlayerJoinGuildEvent(String guild, String player, JoinReason reason) {
        super(guild, player);
        this.reason = reason;
    }

    public static PlayerJoinGuildEvent createPlayerJoinGuildEvent(String guild, String player, JoinReason reason) {
        return new PlayerJoinGuildEvent(guild, player, reason);
    }

    public enum JoinReason {
        INVITE,
        ACCEPT,
    }

    public JoinReason getReason() {
        return reason;
    }

}
