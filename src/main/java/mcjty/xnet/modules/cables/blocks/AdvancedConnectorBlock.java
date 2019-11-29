package mcjty.xnet.modules.cables.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;
import java.util.List;

public class AdvancedConnectorBlock extends ConnectorBlock {

    public static final String ADVANCED_CONNECTOR = "advanced_connector";

    public AdvancedConnectorBlock(List<Item> items) {
        super(ADVANCED_CONNECTOR, items);
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
