package top.oasismc.oasisguild.data.impl;

import org.bukkit.Location;
import top.oasismc.oasisguild.data.api.IGuildDao;
import top.oasismc.oasisguild.data.objects.Guild;
import top.oasismc.oasisguild.data.objects.GuildApply;
import top.oasismc.oasisguild.data.objects.GuildChunk;
import top.oasismc.oasisguild.data.objects.GuildMember;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class YamlGuildDao implements IGuildDao {
    @Override
    public List<Guild> getGuilds() {
        return null;
    }

    @Override
    public Map<String, List<GuildMember>> getGuildMembers(List<Guild> guildList) {
        return null;
    }

    @Override
    public Map<String, Location> getGuildLocationMap(List<Guild> guildList) {
        return null;
    }

    @Override
    public Map<String, List<GuildApply>> getGuildApplyListMap(List<Guild> guildList) {
        return null;
    }

    @Override
    public Map<String, Set<GuildChunk>> getGuildChunkSetMap(List<Guild> guildList) {
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
    public int addGuildChunk(String gName, List<GuildChunk> chunkList) {
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
    public boolean levelUp(String gName, int oldLevel) {
        return false;
    }

    @Override
    public void guildRename(String gName, String newName) {

    }
}
