package mcjty.xnet.modules.cables.blocks;

import mcjty.lib.McJtyLib;
import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.EnergyTools;
import mcjty.rftoolsbase.api.xnet.channels.IConnectable;
import mcjty.rftoolsbase.api.xnet.keys.ConsumerId;
import mcjty.xnet.XNet;
import mcjty.xnet.setup.Config;
import mcjty.xnet.modules.cables.CableColor;
import mcjty.xnet.modules.cables.CableSetup;
import mcjty.xnet.modules.cables.ConnectorType;
import mcjty.xnet.modules.controller.blocks.TileEntityController;
import mcjty.xnet.modules.facade.FacadeSetup;
import mcjty.xnet.modules.facade.blocks.FacadeItemBlock;
import mcjty.xnet.modules.router.blocks.TileEntityRouter;
import mcjty.xnet.modules.various.blocks.RedstoneProxyBlock;
import mcjty.xnet.modules.wireless.blocks.TileEntityWirelessRouter;
import mcjty.xnet.multiblock.ColorId;
import mcjty.xnet.multiblock.WorldBlob;
import mcjty.xnet.multiblock.XNetBlobData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ConnectorBlock extends GenericCableBlock {

    public ConnectorBlock(CableBlockType type) {
        super(Material.IRON, type);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ConnectorTileEntity();
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof GenericTileEntity) {
                GenericTileEntity genericTileEntity = (GenericTileEntity) te;
                NetworkHooks.openGui((ServerPlayerEntity) player, new INamedContainerProvider() {
                    @Override
                    public ITextComponent getDisplayName() {
                        return new StringTextComponent("Connector");
                    }

                    @Nullable
                    @Override
                    public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
                        return new GenericContainer(CableSetup.CONTAINER_CONNECTOR.get(), id, EmptyContainer.CONTAINER_FACTORY, pos, genericTileEntity);
                    }
                }, pos);
            }
        }
        return true;
    }

    @Override
    public void harvestBlock(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
        if (te instanceof ConnectorTileEntity) {
            // If we are in mimic mode then the drop will be the facade as the connector will remain there
            ConnectorTileEntity connectorTileEntity = (ConnectorTileEntity) te;
            if (connectorTileEntity.getMimicBlock() != null) {
                ItemStack item = new ItemStack(FacadeSetup.FACADE.get());
                FacadeItemBlock.setMimicBlock(item, connectorTileEntity.getMimicBlock());
                connectorTileEntity.setMimicBlock(null);
                spawnAsEntity(worldIn, pos, item);
                return;
            }
        }
        super.harvestBlock(worldIn, player, pos, state, te, stack);
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, IFluidState fluid) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ConnectorTileEntity) {
            ConnectorTileEntity connectorTileEntity = (ConnectorTileEntity) te;
            if (connectorTileEntity.getMimicBlock() == null) {
                this.onBlockHarvested(world, pos, state, player);
                return world.setBlockState(pos, Blocks.AIR.getDefaultState(), world.isRemote ? 11 : 3);
            } else {
                // We are in mimic mode. Don't remove the connector
                this.onBlockHarvested(world, pos, state, player);
                // @todo 1.14
//                if(player.capabilities.isCreativeMode) {
//                    connectorTileEntity.setMimicBlock(null);
//                }
            }
        } else {
            return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
        }
        return true;
    }

// @todo 1.14
//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        TileEntity te = world.getTileEntity(data.getPos());
//        if (te instanceof ConnectorTileEntity) {
//            String name = ((ConnectorTileEntity) te).getConnectorName();
//            if (!name.isEmpty()) {
//                probeInfo.text(TextStyleClass.LABEL + "Name: " + TextStyleClass.INFO + name);
//            }
//        }
//    }



    // @todo 1.14
//    @Override
//    @SideOnly(Side.CLIENT)
//    public void initModel() {
//        super.initModel();
//        // To make sure that our ISBM model is chosen for all states we use this custom state mapper:
//        StateMapperBase ignoreState = new StateMapperBase() {
//            @Override
//            protected ModelResourceLocation getModelResourceLocation(BlockState iBlockState) {
//                return GenericCableBakedModel.modelConnector;
//            }
//        };
//        ModelLoader.setCustomStateMapper(this, ignoreState);
//    }


    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        checkRedstone(world, pos);
    }

    @Override
    public void onNeighborChange(BlockState state, IWorldReader blockAccess, BlockPos pos, BlockPos neighbor) {
        if (blockAccess instanceof World) {
            World world = (World) blockAccess;
            if (!world.isRemote) {
                TileEntity te = world.getTileEntity(pos);
                if (te instanceof ConnectorTileEntity) {
                    ConnectorTileEntity connector = (ConnectorTileEntity) te;
                    connector.possiblyMarkNetworkDirty(neighbor);
                }
            }
        }
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, IWorldReader world, BlockPos pos, Direction side) {
        return false;
    }

    private void checkRedstone(World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ConnectorTileEntity) {
//            int powered = world.isBlockIndirectlyGettingPowered(pos);
            int powered = world.getRedstonePowerFromNeighbors(pos); // @todo 1.14 check
            ConnectorTileEntity genericTileEntity = (ConnectorTileEntity) te;
            genericTileEntity.setPowerInput(powered);
        }
    }

    @Override
    public boolean canProvidePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        return getRedstoneOutput(state, world, pos, side);
    }

    @Override
    public int getStrongPower(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        return getRedstoneOutput(state, world, pos, side);
    }

    protected int getRedstoneOutput(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        TileEntity te = world.getTileEntity(pos);
        if (state.getBlock() instanceof ConnectorBlock && te instanceof ConnectorTileEntity) {
            ConnectorTileEntity connector = (ConnectorTileEntity) te;
            return connector.getPowerOut(side.getOpposite());
        }
        return 0;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
//        World world = context.getWorld();
//        BlockPos pos = context.getPos();
//        BlockState state = world.getBlockState(pos);
//        world.notifyBlockUpdate(pos, state, state, Constants.BlockFlags.BLOCK_UPDATE | Constants.BlockFlags.NOTIFY_NEIGHBORS);
        return super.getStateForPlacement(context);
    }

    @Override
    protected ConnectorType getConnectorType(@Nonnull CableColor color, IBlockReader world, BlockPos connectorPos, Direction facing) {
        BlockPos pos = connectorPos.offset(facing);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if ((block instanceof NetCableBlock || block instanceof ConnectorBlock) && state.get(COLOR) == color) {
            return ConnectorType.CABLE;
        } else if (isConnectable(world, connectorPos, facing) && color != CableColor.ROUTING) {
            return ConnectorType.BLOCK;
        } else if (isConnectableRouting(world, pos) && color == CableColor.ROUTING) {
            return ConnectorType.BLOCK;
        } else {
            return ConnectorType.NONE;
        }
    }

    public static boolean isConnectableRouting(IBlockReader world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te == null) {
            return false;
        }
        if (te instanceof TileEntityRouter || te instanceof TileEntityWirelessRouter) {
            return true;
        }
        return false;
    }

    public static boolean isConnectable(IBlockReader world, BlockPos connectorPos, Direction facing) {

        BlockPos pos = connectorPos.offset(facing);
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block.isAir(state, world, pos)) {
            return false;
        }

        ConnectorTileEntity connectorTE = (ConnectorTileEntity) world.getTileEntity(connectorPos);
        if (connectorTE == null) {
            return false;
        }

        if (!connectorTE.isEnabled(facing)) {
            return false;
        }


        TileEntity te = world.getTileEntity(pos);

        if (block instanceof IConnectable) {
            IConnectable.ConnectResult result = ((IConnectable) block).canConnect(world, connectorPos, pos, te, facing);
            if (result == IConnectable.ConnectResult.NO) {
                return false;
            } else if (result == IConnectable.ConnectResult.YES) {
                return true;
            }
        }
        for (IConnectable connectable : XNet.xNetApi.getConnectables()) {
            IConnectable.ConnectResult result = connectable.canConnect(world, connectorPos, pos, te, facing);
            if (result == IConnectable.ConnectResult.NO) {
                return false;
            } else if (result == IConnectable.ConnectResult.YES) {
                return true;
            }
        }

        if (block instanceof ConnectorBlock) {
            return false;
        }
        if (block instanceof RedstoneProxyBlock || block == Blocks.REDSTONE_LAMP ||
                block == Blocks.PISTON || block == Blocks.STICKY_PISTON) {
            return true;
        }
        if (block.canConnectRedstone(state, world, pos, null) || state.canProvidePower()) {
            return true;
        }
        if (te == null) {
            return false;
        }
        if (te instanceof IInventory) {
            return true;
        }
        if (EnergyTools.isEnergyTE(te, null)) {
            return true;
        }
        if (te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
            return true;
        }
        if (te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).isPresent()) {
            return true;
        }
        if (te instanceof TileEntityController) {
            return true;
        }
        if (te instanceof TileEntityRouter) {
            return true;
        }
        return false;
    }

    // @todo 1.14
//    @Override
//    @SideOnly(Side.CLIENT)
//    public boolean shouldSideBeRendered(BlockState blockState, IBlockAccess blockAccess, BlockPos pos, Direction side) {
//        BlockState mimicBlock = getMimicBlock(blockAccess, pos);
//        if (mimicBlock == null) {
//            return false;
//        } else {
//            return mimicBlock.shouldSideBeRendered(blockAccess, pos, side);
//        }
//    }
//
//    @Override
//    @SideOnly(Side.CLIENT)
//    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
//        return true; // delegated to GenericCableBakedModel#getQuads
//    }


    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        ServerWorld world = builder.getWorld();
        for (ItemStack drop : drops) {
            WorldBlob worldBlob = XNetBlobData.get(world).getWorldBlob(world);
            ConsumerId consumer = worldBlob.getConsumerAt(builder.get(LootParameters.POSITION));
            if (consumer != null) {
                drop.getOrCreateTag().putInt("consumerId", consumer.getId());
            }
        }
        return drops;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        if (McJtyLib.proxy.isShiftKeyDown()) {
            tooltip.add(new StringTextComponent(TextFormatting.BLUE + "Place connector next to block or"));
            tooltip.add(new StringTextComponent(TextFormatting.BLUE + "machine that should be connected"));
            tooltip.add(new StringTextComponent(TextFormatting.BLUE + "to the network"));
            boolean advanced = this == CableSetup.ADVANCED_CONNECTOR.get();
            int maxrf = advanced ? Config.maxRfAdvancedConnector.get() : Config.maxRfConnector.get();
            tooltip.add(new StringTextComponent(TextFormatting.GRAY + "" + TextFormatting.BOLD + "Max RF: " + TextFormatting.WHITE + maxrf));
            if (advanced) {
                tooltip.add(new StringTextComponent(TextFormatting.GRAY + "Allow access to different sides"));
                tooltip.add(new StringTextComponent(TextFormatting.GRAY + "Supports faster item transfer"));
            }
        } else {
            tooltip.add(new StringTextComponent(TextFormatting.WHITE + "<Press Shift>"));
        }

    }

    @Override
    public void createCableSegment(World world, BlockPos pos, ItemStack stack) {
        ConsumerId consumer;
        if (!stack.isEmpty() && stack.hasTag() && stack.getTag().contains("consumerId")) {
            consumer = new ConsumerId(stack.getTag().getInt("consumerId"));
        } else {
            XNetBlobData blobData = XNetBlobData.get(world);
            WorldBlob worldBlob = blobData.getWorldBlob(world);
            consumer = worldBlob.newConsumer();
        }
        createCableSegment(world, pos, consumer);
    }

    public void createCableSegment(World world, BlockPos pos, ConsumerId consumer) {
        XNetBlobData blobData = XNetBlobData.get(world);
        WorldBlob worldBlob = blobData.getWorldBlob(world);
        CableColor color = world.getBlockState(pos).get(COLOR);
        worldBlob.createNetworkConsumer(pos, new ColorId(color.ordinal()+1), consumer);
        blobData.save();
    }

    public static boolean isAdvancedConnector(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof GenericCableBlock) {
            return ((GenericCableBlock) block).isAdvancedConnector();
        }
        return false;
    }

    @Override
    public boolean isAdvancedConnector() {
        return false;
    }
}
