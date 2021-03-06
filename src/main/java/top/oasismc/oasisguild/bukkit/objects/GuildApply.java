package top.oasismc.oasisguild.bukkit.objects;

import top.oasismc.oasisguild.bukkit.api.objects.IGuildApply;

public class GuildApply implements IGuildApply {

    private String pName;
    private int state;

    public GuildApply(String pName, int state) {
        this.pName = pName;
        this.state = state;
    }

    @Override
    public String getPName() {
        return pName;
    }

    public void setPName(String pName) {
        this.pName = pName;
    }

    @Override
    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
