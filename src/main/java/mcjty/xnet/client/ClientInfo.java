package mcjty.xnet.client;

import net.minecraft.util.math.BlockPos;

/**
 * This class holds information on client-side only which are global to the mod.
 */
public class ClientInfo {
    private BlockPos hilightedBlock = null;
    private long expireHilight = 0;

    public void hilightBlock(BlockPos c, long expireHilight) {
        hilightedBlock = c;
        this.expireHilight = expireHilight;
    }

    public BlockPos getHilightedBlock() {
        return hilightedBlock;
    }

    public long getExpireHilight() {
        return expireHilight;
    }
}
