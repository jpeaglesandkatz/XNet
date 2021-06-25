package mcjty.xnet.modules.facade.blocks;

import mcjty.xnet.modules.cables.CableColor;
import mcjty.xnet.modules.cables.CableModule;
import mcjty.xnet.modules.cables.blocks.NetCableBlock;
import mcjty.xnet.modules.facade.FacadeModule;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.BlockFlags;

import javax.annotation.Nullable;

import mcjty.xnet.modules.cables.blocks.GenericCableBlock.CableBlockType;

public class FacadeBlock extends NetCableBlock {

    public FacadeBlock(CableBlockType type) {
        super(Material.METAL, type);
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
//    public RayTraceResult collisionRayTrace(BlockState blockState, World world, BlockPos pos, Vector3d start, Vector3d end) {
//        // We do not want the raytracing that happens in the GenericCableBlock
//        return super.originalCollisionRayTrace(blockState, world, pos, start, end);
//    }

    // @todo 1.15
//    @Override
//    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
//        return true;    // delegated to GenericCableBakedModel#getQuads
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
    public void playerDestroy(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
        ItemStack item = new ItemStack(FacadeModule.FACADE.get());
        BlockState mimicBlock;
        if (te instanceof FacadeTileEntity) {
            mimicBlock = ((FacadeTileEntity) te).getMimicBlock();
        } else {
            mimicBlock = Blocks.COBBLESTONE.defaultBlockState();
        }
        FacadeBlockItem.setMimicBlock(item, mimicBlock);

        popResource(worldIn, pos, item);
    }

    private boolean replaceWithCable(IWorld world, BlockPos pos, BlockState state) {
        CableColor color = state.getValue(COLOR);
        BlockState defaultState = CableModule.NETCABLE.get().defaultBlockState().setValue(COLOR, color);
        BlockState newState = this.calculateState(world, pos, defaultState);
        return world.setBlock(pos, newState, world.isClientSide()
                ? BlockFlags.BLOCK_UPDATE + BlockFlags.NOTIFY_NEIGHBORS + BlockFlags.RERENDER_MAIN_THREAD
                : BlockFlags.BLOCK_UPDATE + BlockFlags.NOTIFY_NEIGHBORS);
    }

    @Override
    public void destroy(IWorld world, BlockPos pos, BlockState state) {
        if (world.isClientSide()) {
            replaceWithCable(world, pos, state);
        }
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
        return replaceWithCable(world, pos, state);
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
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        // Breaking a facade has no effect on blob network
        super.onRemove(state, world, pos, newState, isMoving);
    }


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
