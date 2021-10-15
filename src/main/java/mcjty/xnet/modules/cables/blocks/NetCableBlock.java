package mcjty.xnet.modules.cables.blocks;

import mcjty.xnet.modules.cables.CableColor;
import mcjty.xnet.modules.cables.ConnectorType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;

public class NetCableBlock extends GenericCableBlock {

    public NetCableBlock(CableBlockType type) {
        super(Material.METAL, type);
    }

    public NetCableBlock(Material material, CableBlockType type) {
        super(material, type);
    }

//    @Nullable
//    @Override
//    public BlockState getStateForPlacement(BlockItemUseContext context) {
//        return getPlacementState(context);
//
//    }
//
//    public BlockState getPlacementState(BlockItemUseContext context) {
//        // When our block is placed down we force a re-render of adjacent blocks to make sure their baked model is updated
//        World world = context.getWorld();
//        BlockPos pos = context.getPos();
//        BlockState state = world.getBlockState(pos);
//        state = super.getStateForPlacement(context);
//        world.notifyBlockUpdate(pos, state, state, Constants.BlockFlags.BLOCK_UPDATE | Constants.BlockFlags.NOTIFY_NEIGHBORS);
//        return state;
//    }


    @Override
    protected ConnectorType getConnectorType(@Nonnull CableColor color, IBlockReader world, BlockPos connectorPos, Direction facing) {
        BlockPos pos = connectorPos.relative(facing);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if ((block instanceof NetCableBlock || block instanceof ConnectorBlock) && state.getValue(COLOR) == color) {
            return ConnectorType.CABLE;
        } else {
            return ConnectorType.NONE;
        }
    }

}
