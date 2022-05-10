package top.oasismc.oasisguild.bukkit.api.event.guild;

import top.oasismc.oasisguild.bukkit.api.objects.IGuildChunk;

import java.util.List;

public class GuildChunkChangeEvent extends GuildEvent {

    private List<IGuildChunk> chunkList;
    private Type type;

    private GuildChunkChangeEvent(String guildName, List<IGuildChunk> chunkList, Type type) {
        super(guildName);
        this.chunkList = chunkList;
        this.type = type;
    }

    private GuildChunkChangeEvent(String guildName, List<IGuildChunk> chunkList) {
        this(guildName, chunkList, Type.ADD);
    }

    public static GuildChunkChangeEvent createGuildAddChunkEvent(String guildName, List<IGuildChunk> chunkList) {
        return new GuildChunkChangeEvent(guildName, chunkList);
    }

    public static GuildChunkChangeEvent createGuildAddChunkEvent(String guildName, List<IGuildChunk> chunkList, Type type) {
        return new GuildChunkChangeEvent(guildName, chunkList, type);
    }

    public List<IGuildChunk> getChunkList() {
        return chunkList;
    }

    public void setChunkList(List<IGuildChunk> chunkList) {
        this.chunkList = chunkList;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        ADD, REMOVE;
    }

}
