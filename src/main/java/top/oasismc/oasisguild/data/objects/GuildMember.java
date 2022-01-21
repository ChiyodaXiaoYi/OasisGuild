package top.oasismc.oasisguild.data.objects;

public class GuildMember {

    private final String playerName;
    private int job;

    public GuildMember(String playerName, int job) {
        this.playerName = playerName;
        this.job = job;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getJob() {
        return job;
    }

    public void setJob(int job) {
        this.job = job;
    }
}
