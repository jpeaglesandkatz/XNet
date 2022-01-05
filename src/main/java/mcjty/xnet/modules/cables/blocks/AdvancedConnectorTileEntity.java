package mcjty.xnet.modules.cables.blocks;

import mcjty.xnet.setup.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import static mcjty.xnet.modules.cables.CableModule.TYPE_ADVANCED_CONNECTOR;

public class AdvancedConnectorTileEntity extends ConnectorTileEntity {

    public AdvancedConnectorTileEntity(BlockPos pos, BlockState state) {
        super(TYPE_ADVANCED_CONNECTOR.get(), pos, state);
    }

    @Override
    public int getMaxEnergy() {
        return Config.maxRfAdvancedConnector.get();
    }
}
