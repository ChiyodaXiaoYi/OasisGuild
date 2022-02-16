package top.oasismc.oasisguild.bukkit.api.objects;

public interface IGuild {
    String getGuildName();

    short getGuildLevel();

    short getMaxMember();

    String getIcon();

    boolean isPvp();

    String getDesc();
}
