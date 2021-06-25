package mcjty.xnet.modules.cables.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

import mcjty.xnet.modules.cables.blocks.GenericCableBlock.CableBlockType;

public class AdvancedConnectorBlock extends ConnectorBlock {

    public AdvancedConnectorBlock(CableBlockType type) {
        super(type);
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
