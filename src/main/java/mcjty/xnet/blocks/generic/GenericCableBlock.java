package mcjty.xnet.blocks.generic;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import mcjty.xnet.api.keys.ConsumerId;
import mcjty.xnet.api.keys.NetworkId;
import mcjty.xnet.blocks.cables.ConnectorType;
import mcjty.xnet.blocks.facade.FacadeProperty;
import mcjty.xnet.blocks.facade.IFacadeSupport;
import mcjty.xnet.multiblock.BlobId;
import mcjty.xnet.multiblock.ColorId;
import mcjty.xnet.multiblock.WorldBlob;
import mcjty.xnet.multiblock.XNetBlobData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class GenericCableBlock extends Block {

    // Properties that indicate if there is the same block in a certain direction.
    public static final UnlistedPropertyBlockType NORTH = new UnlistedPropertyBlockType("north");
    public static final UnlistedPropertyBlockType SOUTH = new UnlistedPropertyBlockType("south");
    public static final UnlistedPropertyBlockType WEST = new UnlistedPropertyBlockType("west");
    public static final UnlistedPropertyBlockType EAST = new UnlistedPropertyBlockType("east");
    public static final UnlistedPropertyBlockType UP = new UnlistedPropertyBlockType("up");
    public static final UnlistedPropertyBlockType DOWN = new UnlistedPropertyBlockType("down");

    public static final FacadeProperty FACADEID = new FacadeProperty("facadeid");
    public static final EnumProperty<CableColor> COLOR = EnumProperty.<CableColor>create("color", CableColor.class);


    public static final AxisAlignedBB AABB_EMPTY = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    public static final AxisAlignedBB AABB_CENTER = new AxisAlignedBB(.4, .4, .4, .6, .6, .6);

    public static final AxisAlignedBB AABBS[] = new AxisAlignedBB[]{
            new AxisAlignedBB(.4, 0, .4, .6, .4, .6),
            new AxisAlignedBB(.4, .6, .4, .6, 1, .6),
            new AxisAlignedBB(.4, .4, 0, .6, .6, .4),
            new AxisAlignedBB(.4, .4, .6, .6, .6, 1),
            new AxisAlignedBB(0, .4, .4, .4, .6, .6),
            new AxisAlignedBB(.6, .4, .4, 1, .6, .6)
    };

    public static final AxisAlignedBB AABBS_CONNECTOR[] = new AxisAlignedBB[]{
            new AxisAlignedBB(.2, 0, .2, .8, .1, .8),
            new AxisAlignedBB(.2, .9, .2, .8, 1, .8),
            new AxisAlignedBB(.2, .2, 0, .8, .8, .1),
            new AxisAlignedBB(.2, .2, .9, .8, .8, 1),
            new AxisAlignedBB(0, .2, .2, .1, .8, .8),
            new AxisAlignedBB(.9, .2, .2, 1, .8, .8)
    };


    public GenericCableBlock(Material material, String name) {
        super(Properties.create(material)
                .hardnessAndResistance(1.0f)
                .sound(SoundType.METAL)
                .harvestLevel(0)
                .harvestTool(ToolType.PICKAXE)
        );
        setRegistryName(name);
        setDefaultState(getDefaultState().with(COLOR, CableColor.BLUE));
    }

//    public static boolean activateBlock(Block block, World world, BlockPos pos, BlockState state, EntityPlayer player, EnumHand hand, Direction facing, float hitX, float hitY, float hitZ) {
//        return block.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
//    }

    public static Collection<IProperty<?>> getPropertyKeys(BlockState state) {
        return state.getProperties();
    }

    @Override
    public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
        ItemStack item = super.getItem(worldIn, pos, state);
        return updateColorInStack(item, state.get(COLOR));
    }

    protected ItemStack updateColorInStack(ItemStack item, CableColor color) {
        if (color != null) {
            CompoundNBT tag = item.getOrCreateTag();
            // @todo 1.14
//            CompoundNBT display = new CompoundNBT();
//            String unlocname = getUnlocalizedName() + "_" + color.getName() + ".name";
//            display.putString("LocName", unlocname);
//            tag.put("display", display);
        }
        return item;
    }


    // @todo 1.14
//    @Override
//    public int damageDropped(BlockState state) {
//        return state.getValue(COLOR).ordinal();
//    }

//    @SideOnly(Side.CLIENT)
//    public void initModel() {
//        ResourceLocation name = getRegistryName();
//        for (CableColor color : CableColor.VALUES) {
//            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), color.ordinal(), new ModelResourceLocation(new ResourceLocation(name.getResourceDomain(), name.getResourcePath()+"item"), "color=" + color.name()));
//        }
//    }

//    @SideOnly(Side.CLIENT)
//    public void initItemModel() {
//    }

    @Nullable
    protected BlockState getMimicBlock(World blockAccess, BlockPos pos) {
        TileEntity te = blockAccess.getTileEntity(pos);
        if (te instanceof IFacadeSupport) {
            return ((IFacadeSupport) te).getMimicBlock();
        } else {
            return null;
        }
    }

    // @todo 1.14
//    @SideOnly(Side.CLIENT)
//    public void initColorHandler(BlockColors blockColors) {
//        blockColors.registerBlockColorHandler((state, world, pos, tintIndex) -> {
//            BlockState mimicBlock = getMimicBlock(world, pos);
//            return mimicBlock != null ? blockColors.colorMultiplier(mimicBlock, world, pos, tintIndex) : -1;
//        }, this);
//    }

//    @Override
//    @SideOnly(Side.CLIENT)
//    public AxisAlignedBB getSelectedBoundingBox(BlockState state, World worldIn, BlockPos pos) {
//        return AABB_EMPTY;
//    }



    @Nullable
    @Override
    public RayTraceResult collisionRayTrace(BlockState blockState, World world, BlockPos pos, Vec3d start, Vec3d end) {
        if (getMimicBlock(world, pos) != null) {
            // In mimic mode we use original raytrace mode
            return originalCollisionRayTrace(blockState, world, pos, start, end);
        }
        Vec3d vec3d = start.subtract(pos.getX(), pos.getY(), pos.getZ());
        Vec3d vec3d1 = end.subtract(pos.getX(), pos.getY(), pos.getZ());
        RayTraceResult rc = checkIntersect(pos, vec3d, vec3d1, AABB_CENTER);
        if (rc != null) {
            return rc;
        }
        CableColor color = blockState.getValue(COLOR);

        for (Direction facing : Direction.VALUES) {
            ConnectorType type = getConnectorType(color, world, pos, facing);
            if (type != ConnectorType.NONE) {
                rc = checkIntersect(pos, vec3d, vec3d1, AABBS[facing.ordinal()]);
                if (rc != null) {
                    return rc;
                }
            }
            if (type == ConnectorType.BLOCK) {
                rc = checkIntersect(pos, vec3d, vec3d1, AABBS_CONNECTOR[facing.ordinal()]);
                if (rc != null) {
                    return rc;
                }
            }
        }
        return null;
    }

    private RayTraceResult checkIntersect(BlockPos pos, Vec3d vec3d, Vec3d vec3d1, AxisAlignedBB boundingBox) {
        RayTraceResult raytraceresult = boundingBox.calculateIntercept(vec3d, vec3d1);
        return raytraceresult == null ? null : new RayTraceResult(raytraceresult.hitVec.addVector(pos.getX(), pos.getY(), pos.getZ()), raytraceresult.sideHit, pos);
    }

    protected RayTraceResult originalCollisionRayTrace(BlockState blockState, World world, BlockPos pos, Vec3d start, Vec3d end) {
        return super.collisionRayTrace(blockState, world, pos, start, end);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @Optional.Method(modid = "waila")
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        return currenttip;
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, BlockState blockState, IProbeHitData data) {
        WorldBlob worldBlob = XNetBlobData.getBlobData(world).getWorldBlob(world);

        if (mode == ProbeMode.DEBUG) {
            BlobId blobId = worldBlob.getBlobAt(data.getPos());
            if (blobId != null) {
                probeInfo.text(TextStyleClass.LABEL + "Blob: " + TextStyleClass.INFO + blobId.getId());
            }
            ColorId colorId = worldBlob.getColorAt(data.getPos());
            if (colorId != null) {
                probeInfo.text(TextStyleClass.LABEL + "Color: " + TextStyleClass.INFO + colorId.getId());
            }
        }

        Set<NetworkId> networks = worldBlob.getNetworksAt(data.getPos());
        for (NetworkId network : networks) {
            if (mode == ProbeMode.DEBUG) {
                probeInfo.text(TextStyleClass.LABEL + "Network: " + TextStyleClass.INFO + network.getId() + ", V: " +
                    worldBlob.getNetworkVersion(network));
            } else {
                probeInfo.text(TextStyleClass.LABEL + "Network: " + TextStyleClass.INFO + network.getId());
            }
        }

        ConsumerId consumerId = worldBlob.getConsumerAt(data.getPos());
        if (consumerId != null) {
            probeInfo.text(TextStyleClass.LABEL + "Consumer: " + TextStyleClass.INFO + consumerId.getId());
        }
    }

    public boolean isAdvancedConnector() {
        return false;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, EntityLivingBase placer, ItemStack stack) {
        originalOnBlockPlacedBy(world, pos, state, placer, stack);
        if (!world.isRemote) {
            createCableSegment(world, pos, stack);
        }
    }

    protected void originalOnBlockPlacedBy(World world, BlockPos pos, BlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
    }

    public void createCableSegment(World world, BlockPos pos, ItemStack stack) {
        XNetBlobData blobData = XNetBlobData.getBlobData(world);
        WorldBlob worldBlob = blobData.getWorldBlob(world);
        CableColor color = world.getBlockState(pos).getValue(COLOR);
        worldBlob.createCableSegment(pos, new ColorId(color.ordinal()+1));
        blobData.save();
    }

    @Override
    public void breakBlock(World world, BlockPos pos, BlockState state) {
        unlinkBlock(world, pos);
        originalBreakBlock(world, pos, state);
    }

    public void unlinkBlock(World world, BlockPos pos) {
        if (!world.isRemote) {
            XNetBlobData blobData = XNetBlobData.getBlobData(world);
            WorldBlob worldBlob = blobData.getWorldBlob(world);
            worldBlob.removeCableSegment(pos);
            blobData.save();
        }
    }

    protected void originalBreakBlock(World world, BlockPos pos, BlockState state) {
        super.breakBlock(world, pos, state);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(BlockState blockState, IBlockAccess blockAccess, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    public boolean isBlockNormalCube(BlockState blockState) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(BlockState blockState) {
        return false;
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(COLOR, CableColor.VALUES[meta]);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(COLOR).ordinal();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        IProperty<?>[] listedProperties = new IProperty<?>[] { COLOR };
        IUnlistedProperty<?>[] unlistedProperties = new IUnlistedProperty<?>[] { NORTH, SOUTH, WEST, EAST, UP, DOWN,
            FACADEID};
        return new ExtendedBlockState(this, listedProperties, unlistedProperties);
    }

    @Override
    public BlockState getExtendedState(BlockState state, IBlockAccess world, BlockPos pos) {
        return getStateInternal(state, world, pos);
    }

    public BlockState getStateInternal(BlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
        CableColor color = state.getValue(COLOR);

        ConnectorType north = getConnectorType(color, world, pos, Direction.NORTH);
        ConnectorType south = getConnectorType(color, world, pos, Direction.SOUTH);
        ConnectorType west = getConnectorType(color, world, pos, Direction.WEST);
        ConnectorType east = getConnectorType(color, world, pos, Direction.EAST);
        ConnectorType up = getConnectorType(color, world, pos, Direction.UP);
        ConnectorType down = getConnectorType(color, world, pos, Direction.DOWN);

        return extendedBlockState
                .withProperty(NORTH, north)
                .withProperty(SOUTH, south)
                .withProperty(WEST, west)
                .withProperty(EAST, east)
                .withProperty(UP, up)
                .withProperty(DOWN, down);
    }

    protected abstract ConnectorType getConnectorType(@Nonnull CableColor thisColor, IBlockAccess world, BlockPos pos, Direction facing);
}
