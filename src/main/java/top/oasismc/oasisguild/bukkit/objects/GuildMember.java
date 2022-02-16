package top.oasismc.oasisguild.bukkit.objects;

import top.oasismc.oasisguild.bukkit.api.objects.IGuildMember;

public class GuildMember implements IGuildMember {

    private final String playerName;
    private int job;

    public GuildMember(String playerName, int job) {
        this.playerName = playerName;
        this.job = job;
    }

    @Override
    public String getPlayerName() {
        return playerName;
    }

    @Override
    public int getJob() {
        return job;
    }

    public void setJob(int job) {
        this.job = job;
    }
}
