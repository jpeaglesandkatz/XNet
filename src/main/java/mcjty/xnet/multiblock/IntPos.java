package mcjty.xnet.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;

/**
 * A local position in a chunk can be represented with a single int like done
 * in this class.
 */
public record IntPos(int pos) {

    public IntPos(BlockPos pos) {
        this(toInt(pos));
    }

    public int[] getSidePositions() {
        return new int[] { posDown(), posUp(), posEast(), posWest(), posSouth(), posNorth() };
    }

    public boolean isBorder() {
        return getX() == 0 || getX() == 15 || getZ() == 0 || getZ() == 15;
    }

    public boolean isBorder(Direction facing) {
        return switch (facing) {
            case DOWN, UP -> false;
            case NORTH -> getZ() == 0;
            case SOUTH -> getZ() == 15;
            case WEST -> getX() == 0;
            case EAST -> getX() == 15;
        };
    }

    public IntPos otherSide(Direction facing) {
        return switch (facing) {
            case DOWN, UP -> this;
            case NORTH -> new IntPos(pos + (15 << 12));
            case SOUTH -> new IntPos(pos - (15 << 12));
            case WEST -> new IntPos(pos + 15);
            case EAST -> new IntPos(pos - 15);
        };
    }

    public int getX() {
        return pos & 0xf;
    }

    public int getY() {
        return (pos >> 4) & 0xff;
    }

    public int getZ() {
        return (pos >> 12) & 0xf;
    }

    public int posSouth() {
        if (getZ() >= 15) {
            return -1;
        }
        return pos + (1<<12);
    }

    public int posNorth() {
        if (getZ() < 1) {
            return -1;
        }
        return pos - (1<<12);
    }

    public int posEast() {
        if (getX() >= 15) {
            return -1;
        }
        return pos+1;
    }

    public int posWest() {
        if (getX() < 1) {
            return -1;
        }
        return pos-1;
    }

    public int posUp() {
        if (getY() >= 255) {
            return -1;
        }
        return pos + (1<<4);
    }

    public int posDown() {
        if (getY() < 1) {
            return -1;
        }
        return pos - (1<<4);
    }

    private static int toInt(BlockPos pos) {
        int dx = pos.getX() & 0xf;
        int dy = pos.getY();
        int dz = pos.getZ() & 0xf;
        return dz << 12 | dy << 4 | dx;
    }

    public BlockPos toBlockPos(ChunkPos cpos) {
        int dx = getX();
        int dy = getY();
        int dz = getZ();
        return new BlockPos((cpos.x << 4) + dx, dy, (cpos.z << 4) + dz);
//        return cpos.getBlock(dx, dy, dz);
    }
}
