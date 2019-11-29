package mcjty.xnet.modules.facade.blocks;

import mcjty.xnet.modules.cables.blocks.NetCableBlock;
import mcjty.xnet.modules.cables.CableSetup;
import mcjty.xnet.modules.cables.CableColor;
import mcjty.xnet.modules.facade.FacadeSetup;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class FacadeBlock extends NetCableBlock {

    public static final String FACADE = "facade";

    public FacadeBlock() {
        super(Material.IRON, FACADE);
        // @todo 1.14
//        setHardness(0.8f);
    }

//    @Override
//    protected ItemBlock createItemBlock() {
//        return new FacadeItemBlock(this);
//    }

//    @Override
//    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
//        items.add(new ItemStack(this));
//    }

//    protected void initTileEntity() {
//        GameRegistry.registerTileEntity(FacadeTileEntity.class, XNet.MODID + ":facade");
//    }

    // @todo 1.14
//    @Nullable
//    @Override
//    public RayTraceResult collisionRayTrace(BlockState blockState, World world, BlockPos pos, Vec3d start, Vec3d end) {
//        // We do not want the raytracing that happens in the GenericCableBlock
//        return super.originalCollisionRayTrace(blockState, world, pos, start, end);
//    }


    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new FacadeTileEntity();
    }

    @Override
    public void harvestBlock(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
        ItemStack item = new ItemStack(FacadeSetup.FACADE);
        BlockState mimicBlock;
        if (te instanceof FacadeTileEntity) {
            mimicBlock = ((FacadeTileEntity) te).getMimicBlock();
        } else {
            mimicBlock = Blocks.COBBLESTONE.getDefaultState();
        }
        FacadeItemBlock.setMimicBlock(item, mimicBlock);

        spawnAsEntity(worldIn, pos, item);
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, IFluidState fluid) {
        CableColor color = state.get(COLOR);
        this.onBlockHarvested(world, pos, state, player);
        return world.setBlockState(pos, CableSetup.NETCABLE.getDefaultState().with(COLOR, color), world.isRemote ? 11 : 3);
    }

    // @todo 1.14
//    @Override
//    public BlockState getExtendedState(BlockState state, IBlockAccess world, BlockPos pos) {
//        IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
//        BlockState mimicBlock = getMimicBlock(world, pos);
//        if (mimicBlock != null) {
//            return extendedBlockState.withProperty(FACADEID, new FacadeBlockId(mimicBlock));
//        } else {
//            return extendedBlockState;
//        }
//    }


    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        // Breaking a facade has no effect on blob network
        super.onReplaced(state, world, pos, newState, isMoving);
    }


// @todo 1.14
//    @Override
//    @SideOnly(Side.CLIENT)
//    public void initModel() {
//        // To make sure that our ISBM model is chosen for all states we use this custom state mapper:
//        StateMapperBase ignoreState = new StateMapperBase() {
//            @Override
//            protected ModelResourceLocation getModelResourceLocation(BlockState iBlockState) {
//                return FacadeBakedModel.modelFacade;
//            }
//        };
//        ModelLoader.setCustomStateMapper(this, ignoreState);
//    }

    // @todo 1.14
//    @Override
//    @SideOnly(Side.CLIENT)
//    public void initItemModel() {
//        // For our item model we want to use a normal json model. This has to be called in
//        // ClientProxy.init (not preInit) so that's why it is a separate method.
//        Item itemBlock = ForgeRegistries.ITEMS.getValue(new ResourceLocation(XNet.MODID, FACADE));
//        ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation(getRegistryName(), "inventory");
//        final int DEFAULT_ITEM_SUBTYPE = 0;
//        Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(itemBlock, DEFAULT_ITEM_SUBTYPE, itemModelResourceLocation);
//    }

    // @todo 1.14
//    @Override
//    @SideOnly(Side.CLIENT)
//    public boolean shouldSideBeRendered(BlockState blockState, IBlockAccess blockAccess, BlockPos pos, Direction side) {
//        BlockState mimicBlock = getMimicBlock(blockAccess, pos);
//        return mimicBlock == null ? true : mimicBlock.shouldSideBeRendered(blockAccess, pos, side);
//    }
//
//    @Override
//    @SideOnly(Side.CLIENT)
//    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
//        return true; // delegated to FacadeBakedModel#getQuads
//    }

    // @todo 1.14
//    @Override
//    public boolean isBlockNormalCube(BlockState blockState) {
//        return true;
//    }
//
//    @Override
//    public boolean isOpaqueCube(BlockState blockState) {
//        return true;
//    }
//
//    @Override
//    public boolean doesSideBlockRendering(BlockState state, IBlockAccess world, BlockPos pos, Direction face) {
//        BlockState mimicBlock = getMimicBlock(world, pos);
//        return mimicBlock == null ? true : mimicBlock.doesSideBlockRendering(world, pos, face);
//    }
//
//    @Override
//    public boolean isFullCube(BlockState state) {
//        return true;
//    }


}
