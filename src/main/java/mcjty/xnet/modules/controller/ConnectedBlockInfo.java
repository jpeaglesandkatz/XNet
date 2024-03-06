package mcjty.xnet.modules.controller;

import lombok.Getter;
import mcjty.rftoolsbase.api.xnet.keys.SidedPos;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConnectedBlockInfo {

    /// The position of the block we are connecting too
    @Getter(onMethod_ = {@Nonnull})
    private final SidedPos pos;

    @Getter(onMethod_ = {@Nullable})
    private final BlockState state;

    /// The name of the connector
    @Getter(onMethod_ = {@Nonnull})
    private final String name;

    public ConnectedBlockInfo(@Nonnull SidedPos pos, @Nullable BlockState state, @Nonnull String name) {
        this.pos = pos;
        this.state = state;
        this.name = name;
    }


    public boolean isAir() {
        return state == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectedBlockInfo that = (ConnectedBlockInfo) o;

        return pos.equals(that.pos);
    }

    @Override
    public int hashCode() {
        return pos.hashCode();
    }
}
