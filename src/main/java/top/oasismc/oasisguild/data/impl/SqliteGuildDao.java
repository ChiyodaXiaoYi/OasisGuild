package top.oasismc.oasisguild.data.impl;

import org.bukkit.Location;
import top.oasismc.oasisguild.data.api.IGuildDao;
import top.oasismc.oasisguild.objects.api.IGuild;
import top.oasismc.oasisguild.objects.api.IGuildApply;
import top.oasismc.oasisguild.objects.api.IGuildChunk;
import top.oasismc.oasisguild.objects.api.IGuildMember;
import top.oasismc.oasisguild.objects.impl.GuildChunk;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SqliteGuildDao implements IGuildDao {
    @Override
    public List<IGuild> getGuilds() {
        return null;
    }

    @Override
    public Map<String, List<IGuildMember>> getGuildMembers(List<IGuild> guildList) {
        return null;
    }

    @Override
    public Map<String, Location> getGuildLocationMap(List<IGuild> guildList) {
        return null;
    }

    @Override
    public Map<String, List<IGuildApply>> getGuildApplyListMap(List<IGuild> guildList) {
        return null;
    }

    @Override
    public Map<String, Set<IGuildChunk>> getGuildChunkSetMap(List<IGuild> guildList) {
        return null;
    }

    @Override
    public int putApply(String gName, String pName) {
        return 0;
    }

    @Override
    public boolean createGuild(String gName, String pName, String desc, Location loc) {
        return false;
    }

    @Override
    public int addGuildChunk(String gName, List<IGuildChunk> chunkList) {
        return 0;
    }

    @Override
    public boolean disbandGuild(String gName) {
        return false;
    }

    @Override
    public boolean handleApply(String gName, String type, String pName) {
        return false;
    }

    @Override
    public boolean memberQuit(String gName, String pName) {
        return false;
    }

    @Override
    public boolean changePvp(String gName, int pvp) {
        return false;
    }

    @Override
    public boolean changeLoc(String gName, Location loc) {
        return false;
    }

    @Override
    public boolean changeMemberJob(String gName, String pName, int newJob) {
        return false;
    }

    @Override
    public boolean levelUp(String gName, int oldLevel) {
        return false;
    }

    @Override
    public void guildRename(String gName, String newName) {

    }
}
