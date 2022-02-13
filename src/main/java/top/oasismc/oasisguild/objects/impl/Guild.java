package top.oasismc.oasisguild.objects.impl;

import top.oasismc.oasisguild.objects.api.IGuild;

public class Guild implements IGuild {

    private String guildName;
    private short guildLevel;
    private short maxMember;
    private String icon;
    private boolean pvp;
    private String desc;

    public Guild(String guildName, short guildLevel, short maxMember, String icon, byte pvp, String desc) {
        this.setGuildLevel(guildLevel);
        this.setGuildName(guildName);
        this.setMaxMember(maxMember);
        switch (pvp) {
            case 1:
                this.setPvp(true);
                break;
            case 0:
                this.setPvp(false);
                break;
        }
        this.setIcon(icon);
        this.setDesc(desc);
    }

    @Override
    public String getGuildName() {
        return guildName;
    }

    @Override
    public short getGuildLevel() {
        return guildLevel;
    }

    @Override
    public short getMaxMember() {
        return maxMember;
    }

    @Override
    public String getIcon() {
        return icon;
    }

    @Override
    public boolean isPvp() {
        return pvp;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    public void setGuildName(String guildName) {
        this.guildName = guildName;
    }

    public void setGuildLevel(short guildLevel) {
        this.guildLevel = guildLevel;
    }

    public void setMaxMember(short maxMember) {
        this.maxMember = maxMember;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setPvp(boolean pvp) {
        this.pvp = pvp;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
