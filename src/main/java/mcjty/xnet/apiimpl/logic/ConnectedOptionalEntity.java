package mcjty.xnet.apiimpl.logic;

import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import mcjty.xnet.apiimpl.ConnectedBlock;
import mcjty.xnet.modules.cables.blocks.ConnectorTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/*
    A class to interact with a connected block, which could be a BlockEntity, and so we want to keep this entity in the cache.
 */
public class ConnectedOptionalEntity<T> extends ConnectedBlock<T> {

    @Nullable private final BlockEntity connectedEntity;

    public ConnectedOptionalEntity(@Nonnull SidedConsumer sidedConsumer, @Nonnull T settings, @Nonnull BlockPos connectorPos,
                                   @Nonnull BlockPos blockPos, @Nullable BlockEntity connectedEntity,
                                   @Nonnull ConnectorTileEntity connectorEntity) {
        super(sidedConsumer, settings, connectorPos, blockPos, connectorEntity);
        this.connectedEntity = connectedEntity;
    }

    public ConnectedOptionalEntity(@Nonnull ConnectedBlock<T> connectedBlock, @Nullable BlockEntity connectedEntity) {
        super(
                connectedBlock.sidedConsumer(), connectedBlock.settings(), connectedBlock.connectorPos(),
                connectedBlock.getBlockPos(), connectedBlock.getConnectorEntity()
        );
        this.connectedEntity = connectedEntity;
    }

    @Nullable
    public BlockEntity getConnectedEntity() {
        return connectedEntity;
    }
}
