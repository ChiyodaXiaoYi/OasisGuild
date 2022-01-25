package top.oasismc.oasisguild.data.api;

import org.bukkit.Location;
import top.oasismc.oasisguild.data.objects.Guild;
import top.oasismc.oasisguild.data.objects.GuildApply;
import top.oasismc.oasisguild.data.objects.GuildChunk;
import top.oasismc.oasisguild.data.objects.GuildMember;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IGuildDao {

    //select
    List<Guild> getGuilds();
    Map<String, List<GuildMember>> getGuildMembers(List<Guild> guildList);

    Map<String, Location> getGuildLocationMap(List<Guild> guildList);
    Map<String, List<GuildApply>> getGuildApplyListMap(List<Guild> guildList);
    Map<String, Set<GuildChunk>> getGuildChunkSetMap(List<Guild> guildList);

    //insert
    int putApply(String gName, String pName);
    boolean createGuild(String gName, String pName, String desc, Location loc);
    int addGuildChunk(String gName, List<GuildChunk> chunkList);

    //delete
    boolean disbandGuild(String gName);
    boolean handleApply(String gName, String type, String pName);
    boolean memberQuit(String gName, String pName);

    //update
    boolean changePvp(String gName, int pvp);
    boolean changeLoc(String gName, Location loc);
    boolean levelUp(String gName, int oldLevel);

    void guildRename(String gName, String newName);
}
