package top.oasismc.oasisguild.objects.impl;

public class GuildMember implements top.oasismc.oasisguild.objects.api.IGuildMember {

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
