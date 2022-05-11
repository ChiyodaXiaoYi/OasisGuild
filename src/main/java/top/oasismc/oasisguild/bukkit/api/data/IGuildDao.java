package top.oasismc.oasisguild.bukkit.api.data;

import org.bukkit.Location;
import top.oasismc.oasisguild.bukkit.api.objects.IGuild;
import top.oasismc.oasisguild.bukkit.api.objects.IGuildApply;
import top.oasismc.oasisguild.bukkit.api.objects.IGuildChunk;
import top.oasismc.oasisguild.bukkit.api.objects.IGuildMember;

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
    boolean removeGuildChunk(String gName, List<IGuildChunk> chunkList);

    //update
    boolean changePvp(String gName, int pvp);
    boolean changeLoc(String gName, Location loc);
    boolean changeMemberJob(String gName, String pName, int newJob);
    boolean levelUp(String gName, int oldLevel);
    void setGuildLevel(String gName, int lvl);
    void setGuildMaxMember(String gName, int maxMember);
    boolean transformGuild(String gName, String oldLeader, String pName);
    void setGuildIcon(String gName, String icon);

    void guildRename(String gName, String newName);
    void guildResetDesc(String gName, String newDesc);
}
