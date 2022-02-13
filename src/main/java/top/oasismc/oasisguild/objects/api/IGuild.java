package top.oasismc.oasisguild.objects.api;

public interface IGuild {
    String getGuildName();

    short getGuildLevel();

    short getMaxMember();

    String getIcon();

    boolean isPvp();

    String getDesc();
}
