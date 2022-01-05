package mcjty.xnet.modules.cables.blocks;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;

public class AdvancedConnectorBlock extends ConnectorBlock {

    public AdvancedConnectorBlock(CableBlockType type) {
        super(type);
    }

    @Nullable
    @Override
    public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
        return new AdvancedConnectorTileEntity();
    }

    @Override
    public boolean isAdvancedConnector() {
        return true;
    }
}
