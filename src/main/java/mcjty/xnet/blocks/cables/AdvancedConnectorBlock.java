package mcjty.xnet.blocks.cables;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class AdvancedConnectorBlock extends ConnectorBlock {

    public static final String ADVANCED_CONNECTOR = "advanced_connector";

    public AdvancedConnectorBlock() {
        super(ADVANCED_CONNECTOR);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new AdvancedConnectorTileEntity();
    }

    @Override
    public boolean isAdvancedConnector() {
        return true;
    }
}
