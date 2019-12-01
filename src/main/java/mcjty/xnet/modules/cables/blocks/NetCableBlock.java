package mcjty.xnet.modules.cables.blocks;

import mcjty.xnet.modules.cables.ConnectorType;
import mcjty.xnet.modules.cables.CableColor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

public class NetCableBlock extends GenericCableBlock {

    public static final String NETCABLE = "netcable";

    public NetCableBlock(List<Item> items) {
        super(Material.CARPET, NETCABLE, items);
    }

    public NetCableBlock(Material material, String name, List<Item> items) {
        super(material, name, items);
    }

    // @todo 1.14
//    @Override
//    @SideOnly(Side.CLIENT)
//    public void initModel() {
//        super.initModel();
//
//        // To make sure that our ISBM model is chosen for all states we use this custom state mapper:
//        StateMapperBase ignoreState = new StateMapperBase() {
//            @Override
//            protected ModelResourceLocation getModelResourceLocation(BlockState iBlockState) {
//                return GenericCableBakedModel.modelCable;
//            }
//        };
//        ModelLoader.setCustomStateMapper(this, ignoreState);
//    }

//    @Override
//    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
//        for (CableColor value : CableColor.VALUES) {
//            items.add(updateColorInStack(new ItemStack(this, 1, value.ordinal()), value));
//        }
//    }


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
        BlockPos pos = connectorPos.offset(facing);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if ((block instanceof NetCableBlock || block instanceof ConnectorBlock) && state.get(COLOR) == color) {
            return ConnectorType.CABLE;
        } else {
            return ConnectorType.NONE;
        }
    }

}
