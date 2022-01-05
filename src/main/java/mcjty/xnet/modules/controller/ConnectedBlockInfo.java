package mcjty.xnet.modules.controller;

import mcjty.rftoolsbase.api.xnet.keys.SidedPos;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConnectedBlockInfo {

    /// The position of the block we are connecting too
    @Nonnull
    private final SidedPos pos;

    @Nullable
    private final BlockState state;

    /// The name of the connector
    @Nonnull private final String name;

    public ConnectedBlockInfo(@Nonnull SidedPos pos, @Nullable BlockState state, @Nonnull String name) {
        this.pos = pos;
        this.state = state;
        this.name = name;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public SidedPos getPos() {
        return pos;
    }

    @Nullable
    public BlockState getConnectedState() {
        return state;
    }

    public boolean isAir() {
        return state == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectedBlockInfo that = (ConnectedBlockInfo) o;

        if (!pos.equals(that.pos)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return pos.hashCode();
    }
}
