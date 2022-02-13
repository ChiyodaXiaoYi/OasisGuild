package top.oasismc.oasisguild.objects.api;

import org.bukkit.Chunk;

public interface IGuildChunk {
    boolean hasOwner(Chunk chunk);

    int getX();

    int getZ();

    String getWorld();
}
