package mcjty.xnet.apiimpl.energy;

import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import mcjty.xnet.apiimpl.ConnectedBlock;
import mcjty.xnet.modules.cables.blocks.ConnectorTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;

public final class EnergyConnectedBlock extends ConnectedBlock<EnergyConnectorSettings> {
    private final int rate;

    public EnergyConnectedBlock(@Nonnull SidedConsumer sidedConsumer, @Nonnull EnergyConnectorSettings settings,
                                @Nonnull BlockPos connectorPos, @Nonnull BlockEntity connectedEntity,
                                @Nonnull ConnectorTileEntity connectorEntity, int rate) {
        super(sidedConsumer, settings, connectorPos, connectedEntity, connectorEntity);
        this.rate = rate;
    }

    public int rate() {return rate;}
}
