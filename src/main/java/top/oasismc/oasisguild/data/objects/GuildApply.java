package top.oasismc.oasisguild.data.objects;

public class GuildApply {

    private String pName;
    private int state;

    public GuildApply(String pName, int state) {
        this.pName = pName;
        this.state = state;
    }

    public String getPName() {
        return pName;
    }

    public void setPName(String pName) {
        this.pName = pName;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
