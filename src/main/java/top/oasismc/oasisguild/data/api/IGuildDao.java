package top.oasismc.oasisguild.data.api;

import org.bukkit.Location;
import top.oasismc.oasisguild.objects.api.IGuild;
import top.oasismc.oasisguild.objects.api.IGuildApply;
import top.oasismc.oasisguild.objects.api.IGuildChunk;
import top.oasismc.oasisguild.objects.api.IGuildMember;
import top.oasismc.oasisguild.objects.impl.GuildChunk;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IGuildDao {

    //select
    List<IGuild> getGuilds();
    Map<String, List<IGuildMember>> getGuildMembers(List<IGuild> guildList);

    Map<String, Location> getGuildLocationMap(List<IGuild> guildList);
    Map<String, List<IGuildApply>> getGuildApplyListMap(List<IGuild> guildList);
    Map<String, Set<IGuildChunk>> getGuildChunkSetMap(List<IGuild> guildList);

    //insert
    int putApply(String gName, String pName);
    boolean createGuild(String gName, String pName, String desc, Location loc);
    int addGuildChunk(String gName, List<IGuildChunk> chunkList);

    //delete
    boolean disbandGuild(String gName);
    boolean handleApply(String gName, String type, String pName);
    boolean memberQuit(String gName, String pName);

    //update
    boolean changePvp(String gName, int pvp);
    boolean changeLoc(String gName, Location loc);
    boolean changeMemberJob(String gName, String pName, int newJob);
    boolean levelUp(String gName, int oldLevel);

    void guildRename(String gName, String newName);
}
