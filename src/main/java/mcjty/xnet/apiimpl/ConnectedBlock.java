package mcjty.xnet.apiimpl;

import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import mcjty.xnet.modules.cables.blocks.ConnectorTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;


public class ConnectedBlock<T> {
    @Nonnull private final SidedConsumer sidedConsumer;
    @Nonnull private final T settings;
    @Nonnull private final BlockPos connectorPos;
    @Nonnull private final BlockEntity connectedEntity;
    @Nonnull private final ConnectorTileEntity connectorEntity;

    public ConnectedBlock(@Nonnull SidedConsumer sidedConsumer, @Nonnull T settings, @Nonnull BlockPos connectorPos, @Nonnull BlockEntity connectedEntity, @Nonnull ConnectorTileEntity connectorEntity) {
        this.sidedConsumer = sidedConsumer;
        this.settings = settings;
        this.connectorPos = connectorPos;
        this.connectedEntity = connectedEntity;
        this.connectorEntity = connectorEntity;
    }

    @Nonnull
    public SidedConsumer sidedConsumer() {return sidedConsumer;}

    @Nonnull
    public T settings() {return settings;}

    @Nonnull
    public BlockPos connectorPos() {return connectorPos;}

    @Nonnull
    public BlockEntity getConnectedEntity() {
        return connectedEntity;
    }

    @Nonnull
    public ConnectorTileEntity getConnectorEntity() {
        return connectorEntity;
    }

}
