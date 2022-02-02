package top.oasismc.oasisguild.event.player;

public class PlayerJobChangeEvent extends PlayerGuildEvent{

    private final String leader;
    private final int oldJob;
    private int newJob;

    private PlayerJobChangeEvent(String guild, String player, String leader, int oldJob, int newJob) {
        super(guild, player);
        this.leader = leader;
        this.oldJob = oldJob;
        this.newJob = newJob;
    }

    public static PlayerJobChangeEvent createPlayerJobChangeEvent(String guild, String player, String leader, int oldJob, int newJob) {
        return new PlayerJobChangeEvent(guild, player, leader, oldJob, newJob);
    }

    public String getLeader() {
        return leader;
    }

    public int getOldJob() {
        return oldJob;
    }

    public int getNewJob() {
        return newJob;
    }

    public void setNewJob(int newJob) {
        this.newJob = newJob;
    }

}
