package mcjty.xnet.apiimpl;

import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import mcjty.xnet.modules.cables.blocks.ConnectorTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;

public class ConnectedInventory<T, H> extends ConnectedBlock<T> {
    @Nonnull private final H handler;

    public ConnectedInventory(@Nonnull SidedConsumer sidedConsumer, @Nonnull T settings, @Nonnull BlockPos connectorPos,
                              @Nonnull BlockEntity connectedEntity, @Nonnull ConnectorTileEntity connectorEntity,
                              @Nonnull H handler) {
        super(sidedConsumer, settings, connectorPos, connectedEntity, connectorEntity);
        this.handler = handler;
    }

    @Nonnull
    public H getHandler() {
        return handler;
    }
}
