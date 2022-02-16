package top.oasismc.oasisguild.bukkit.api.objects;

import org.bukkit.Chunk;

public interface IGuildChunk {
    boolean hasOwner(Chunk chunk);

    int getX();

    int getZ();

    String getWorld();
}
