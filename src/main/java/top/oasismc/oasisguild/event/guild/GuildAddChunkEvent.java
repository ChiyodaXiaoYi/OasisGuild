package top.oasismc.oasisguild.event.guild;

import top.oasismc.oasisguild.data.objects.GuildChunk;

import java.util.List;

public class GuildAddChunkEvent extends GuildEvent {

    private List<GuildChunk> chunkList;

    private GuildAddChunkEvent(String guildName, List<GuildChunk> chunkList) {
        super(guildName);
        this.chunkList = chunkList;
    }

    public static GuildAddChunkEvent createGuildAddChunkEvent(String guildName, List<GuildChunk> chunkList) {
        return new GuildAddChunkEvent(guildName, chunkList);
    }

    public List<GuildChunk> getChunkList() {
        return chunkList;
    }

    public void setChunkList(List<GuildChunk> chunkList) {
        this.chunkList = chunkList;
    }

}
