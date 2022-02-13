package top.oasismc.oasisguild.event.guild;

import top.oasismc.oasisguild.objects.api.IGuildChunk;
import top.oasismc.oasisguild.objects.impl.GuildChunk;

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
