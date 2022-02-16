package top.oasismc.oasisguild.bukkit.api.event.guild;

import top.oasismc.oasisguild.bukkit.api.objects.IGuildChunk;

import java.util.List;

public class GuildAddChunkEvent extends GuildEvent {

    private List<IGuildChunk> chunkList;

    private GuildAddChunkEvent(String guildName, List<IGuildChunk> chunkList) {
        super(guildName);
        this.chunkList = chunkList;
    }

    public static GuildAddChunkEvent createGuildAddChunkEvent(String guildName, List<IGuildChunk> chunkList) {
        return new GuildAddChunkEvent(guildName, chunkList);
    }

    public List<IGuildChunk> getChunkList() {
        return chunkList;
    }

    public void setChunkList(List<IGuildChunk> chunkList) {
        this.chunkList = chunkList;
    }

}
