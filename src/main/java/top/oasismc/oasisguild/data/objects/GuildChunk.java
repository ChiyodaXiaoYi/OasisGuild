package top.oasismc.oasisguild.data.objects;

import org.bukkit.Chunk;

import java.util.Objects;

public class GuildChunk {

    private final int x;
    private final int z;
    private final String world;

    public GuildChunk(int x, int z, String world) {
        this.x = x;
        this.z = z;
        this.world = world;
    }

    public boolean hasOwner(Chunk chunk) {
        return chunk.getX() == x && chunk.getZ() == z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GuildChunk)) return false;
        GuildChunk that = (GuildChunk) o;
        return x == that.x && z == that.z && world.equals(that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z, world);
    }

    public String getWorld() {
        return world;
    }
}
