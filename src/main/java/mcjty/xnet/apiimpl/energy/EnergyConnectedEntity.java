package mcjty.xnet.apiimpl.energy;

import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import mcjty.xnet.apiimpl.logic.ConnectedEntity;
import mcjty.xnet.modules.cables.blocks.ConnectorTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;

public final class EnergyConnectedEntity extends ConnectedEntity<EnergyConnectorSettings> {
    private final int rate;

    public EnergyConnectedEntity(@Nonnull SidedConsumer sidedConsumer, @Nonnull EnergyConnectorSettings settings,
                                 @Nonnull BlockPos connectorPos, @Nonnull BlockPos blockPos, @Nonnull BlockEntity connectedEntity,
                                 @Nonnull ConnectorTileEntity connectorEntity, int rate) {
        super(sidedConsumer, settings, connectorPos, blockPos, connectedEntity, connectorEntity);
        this.rate = rate;
    }

    public int rate() {return rate;}
}
