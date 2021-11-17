package mcjty.xnet.modules.wireless.blocks;

import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.bindings.Val;
import mcjty.lib.bindings.Value;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.LevelTools;
import mcjty.lib.varia.Sync;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import mcjty.rftoolsbase.api.xnet.keys.NetworkId;
import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.xnet.XNet;
import mcjty.xnet.client.ControllerChannelClientInfo;
import mcjty.xnet.compat.XNetTOPDriver;
import mcjty.xnet.logic.LogicTools;
import mcjty.xnet.modules.cables.CableColor;
import mcjty.xnet.modules.router.blocks.TileEntityRouter;
import mcjty.xnet.modules.wireless.WirelessRouterModule;
import mcjty.xnet.multiblock.*;
import mcjty.xnet.setup.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static mcjty.xnet.modules.controller.blocks.TileEntityController.ERROR;
import static mcjty.xnet.modules.wireless.WirelessRouterModule.TYPE_WIRELESS_ROUTER;

public final class TileEntityWirelessRouter extends GenericTileEntity implements ITickableTileEntity {

    public static final int TIER_INVALID = -1;
    public static final int TIER_1 = 0;
    public static final int TIER_2 = 1;
    public static final int TIER_INF = 2;

    @Val
    public static final Value<?, Boolean> VALUE_PUBLIC = Value.<TileEntityWirelessRouter, Boolean>create("public", Type.BOOLEAN, TileEntityWirelessRouter::isPublicAccess, TileEntityWirelessRouter::setPublicAccess);


    private boolean error = false;
    private int counter = 10;
    private boolean publicAccess = false;

    private int globalChannelVersion = -1;      // Used to detect if a wireless channel has been published and we might need to recheck

    @Cap(type = CapType.ENERGY)
    private final LazyOptional<GenericEnergyStorage> energyHandler = LazyOptional.of(() -> new GenericEnergyStorage(this, true, Config.wirelessRouterMaxRF.get(), Config.wirelessRouterRfPerTick.get()));
    @Cap(type = CapType.CONTAINER)
    private final LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<GenericContainer>("Wireless Router")
            .containerSupplier((windowId,player) -> new GenericContainer(WirelessRouterModule.CONTAINER_WIRELESS_ROUTER.get(), windowId, ContainerFactory.EMPTY.get(), getBlockPos(), TileEntityWirelessRouter.this))
            .dataListener(Sync.values(new ResourceLocation(XNet.MODID, "data"), this)));

    public TileEntityWirelessRouter() {
        super(TYPE_WIRELESS_ROUTER.get());
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .topDriver(XNetTOPDriver.DRIVER)
                .tileEntitySupplier(TileEntityWirelessRouter::new)
                .manualEntry(ManualHelper.create("xnet:simple/wireless/wireless_router"))
                .info(TooltipBuilder.key("message.xnet.shiftmessage"))
                .infoShift(TooltipBuilder.header(), TooltipBuilder.gold())
        ) {
            @Override
            protected void createBlockStateDefinition(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
                super.createBlockStateDefinition(builder);
                builder.add(ERROR);
            }
        };
    }

    public boolean isPublicAccess() {
        return publicAccess;
    }

    public void setPublicAccess(boolean publicAccess) {
        this.publicAccess = publicAccess;
        setChanged();
    }

    @Override
    public void tick() {
        if (!level.isClientSide) {
            counter--;
            if (counter > 0) {
                return;
            }
            counter = 10;

            int version = XNetWirelessChannels.get(level).getGlobalChannelVersion();
            if (globalChannelVersion != version) {
                globalChannelVersion = version;
                NetworkId networkId = findRoutingNetwork();
                if (networkId != null) {
                    WorldBlob worldBlob = XNetBlobData.get(level).getWorldBlob(level);
                    worldBlob.markNetworkDirty(networkId);
                }
            }

            boolean err = false;
            int range = getAntennaRange();
            if (range < 0) {
                err = true;
            }

            if (!err) {
                NetworkId networkId = findRoutingNetwork();
                if (networkId != null) {
                    LogicTools.consumers(level, networkId)
                            .forEach(consumerPos -> LogicTools.routers(level, consumerPos)
                                    .forEach(r -> publishChannels(r, networkId)));
                }
            }

            setError(err);
        }
    }

    private int getAntennaTier() {
        if (level.getBlockState(worldPosition.above()).getBlock() != WirelessRouterModule.ANTENNA_BASE.get()) {
            return TIER_INVALID;
        }
        Block aboveAntenna = level.getBlockState(worldPosition.above(2)).getBlock();
        if (aboveAntenna == WirelessRouterModule.ANTENNA_DISH.get()) {
            return TIER_INF;
        }
        if (aboveAntenna != WirelessRouterModule.ANTENNA.get()) {
            return TIER_INVALID;
        }
        if (level.getBlockState(worldPosition.above(3)).getBlock() == WirelessRouterModule.ANTENNA.get()) {
            return TIER_2;
        } else {
            return TIER_1;
        }
    }

    private int getAntennaRange() {
        int tier = getAntennaTier();
        switch (tier) {
            case TIER_INVALID:
                return -1;
            case TIER_1:
                return Config.antennaTier1Range.get();
            case TIER_2:
                return Config.antennaTier2Range.get();
            case TIER_INF:
                return Integer.MAX_VALUE;
        }
        return -1;
    }

    private boolean inRange(TileEntityWirelessRouter otherRouter) {
        // Both wireless routers have to support the range
        int thisRange = getAntennaRange();
        int otherRange = otherRouter.getAntennaRange();
        if (thisRange >= Integer.MAX_VALUE && otherRange >= Integer.MAX_VALUE) {
            return true;
        }
        if (thisRange <= 0 || otherRange <= 0) {
            return false;
        }

        // If the dimension is different at this point there is no connection
        if (!level.dimension().equals(otherRouter.level.dimension())) {
            return false;
        }

        double maxSqdist = Math.min(thisRange, otherRange);
        maxSqdist *= maxSqdist;
        double sqdist = worldPosition.distSqr(otherRouter.worldPosition);
        return sqdist <= maxSqdist;
    }

    private boolean inRange(XNetWirelessChannels.WirelessRouterInfo wirelessRouter) {
        World otherWorld = LevelTools.getLevel(level, wirelessRouter.getCoordinate().dimension());
        if (otherWorld == null) {
            return false;
        }
        return LogicTools.consumers(otherWorld, wirelessRouter.getNetworkId())
                .filter(consumerPos -> LevelTools.isLoaded(otherWorld, consumerPos))
                .anyMatch(consumerPos -> LogicTools.wirelessRouters(otherWorld, consumerPos)
                        .anyMatch(this::inRange));
    }

    public void findRemoteChannelInfo(List<ControllerChannelClientInfo> list) {
        NetworkId network = findRoutingNetwork();
        if (network == null) {
            return;
        }

        XNetWirelessChannels wirelessData = XNetWirelessChannels.get(level);
        wirelessData.findChannels(getOwnerUUID())
                .forEach(channel -> {
                    // Find all wireless routers on a given channel but remove the ones on our own channel
                    channel.getRouters().values().stream()
                            // Make sure this is not the same router
                            .filter(routerInfo -> isDifferentRouter(network, routerInfo))
                            .filter(this::inRange)
                            .forEach(routerInfo -> {
                                // Find all routers on this network
                                World otherWorld = LevelTools.getLevel(level, routerInfo.getCoordinate().dimension());
                                LogicTools.consumers(otherWorld, routerInfo.getNetworkId())
                                        .filter(otherWorld::hasChunkAt)
                                        // Range check not needed here since the check is already done on the wireless router
                                        .forEach(consumerPos -> LogicTools.routers(otherWorld, consumerPos)
                                                .forEach(router -> {
                                                    router.findLocalChannelInfo(list, true, true);
                                                }));

                            });
                });
    }

    // Test if the given router is not a router on this network
    private boolean isDifferentRouter(NetworkId thisNetwork, XNetWirelessChannels.WirelessRouterInfo routerInfo) {
        return !routerInfo.getCoordinate().dimension().equals(level.dimension()) || !thisNetwork.equals(routerInfo.getNetworkId());
    }

//    private long getStoredPower() {
//        return energyHandler.map(h -> h.getEnergy()).orElse(0L);
//    }

    private void publishChannels(TileEntityRouter router, NetworkId networkId) {
        int tier = getAntennaTier();
        UUID ownerUUID = publicAccess ? null : getOwnerUUID();
        XNetWirelessChannels wirelessData = XNetWirelessChannels.get(level);
        energyHandler.ifPresent(h -> {

            router.publishedChannelStream()
                    .forEach(pair -> {
                        String name = pair.getKey();
                        IChannelType channelType = pair.getValue();
                        long energyStored = h.getEnergy();
                        if (Config.wirelessRouterRfPerChannel[tier].get() <= energyStored) {
                            h.consumeEnergy(Config.wirelessRouterRfPerChannel[tier].get());
                            wirelessData.transmitChannel(name, channelType, ownerUUID, level.dimension(),
                                    worldPosition, networkId);
                        }
                    });
        });
    }

    public void addWirelessConnectors(Map<SidedConsumer, IConnectorSettings> connectors, String channelName, IChannelType type,
                                      @Nullable UUID owner, @Nonnull Map<WirelessChannelKey, Integer> wirelessVersions) {
        WirelessChannelKey key = new WirelessChannelKey(channelName, type, owner);
        XNetWirelessChannels.WirelessChannelInfo info = XNetWirelessChannels.get(level).findChannel(key);
        if (info != null) {
            info.getRouters().keySet().stream()
                    // Don't do this for ourselves
                    .filter(routerPos -> !routerPos.dimension().equals(level.dimension()) || !routerPos.pos().equals(worldPosition))
                    .filter(routerPos -> LevelTools.isLoaded(LevelTools.getLevel(level, routerPos.dimension()), routerPos.pos()))
                    .forEach(routerPos -> {
                        ServerWorld otherWorld = LevelTools.getLevel(level, routerPos.dimension());
                        TileEntity otherTE = otherWorld.getBlockEntity(routerPos.pos());
                        if (otherTE instanceof TileEntityWirelessRouter) {
                            TileEntityWirelessRouter otherRouter = (TileEntityWirelessRouter) otherTE;
                            if (inRange(otherRouter) && !otherRouter.inError()) {
                                NetworkId routingNetwork = otherRouter.findRoutingNetwork();
                                if (routingNetwork != null) {
                                    LogicTools.consumers(level, routingNetwork)
                                            .forEach(consumerPos -> LogicTools.routers(otherWorld, consumerPos).
                                                    forEach(router -> {
                                                        if (router.addConnectorsFromConnectedNetworks(connectors, channelName, type)) {
                                                            wirelessVersions.put(key, info.getVersion());
                                                        }
                                                    }));
                                }
                            }
                        }

                    });
        }
    }


    private void setError(boolean err) {
        if (error != err) {
            error = err;
            BlockState state = level.getBlockState(worldPosition);
            if (error) {
                if (!state.getValue(ERROR)) {
                    level.setBlock(worldPosition, state.setValue(ERROR, true), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
                }
            } else {
                if (state.getValue(ERROR)) {
                    level.setBlock(worldPosition, state.setValue(ERROR, false), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
                }
            }
            markDirtyQuick();
        }
    }

    public boolean inError() {
        return error;
    }

    @Nullable
    public NetworkId findRoutingNetwork() {
        WorldBlob worldBlob = XNetBlobData.get(level).getWorldBlob(level);
        return LogicTools.routingConnectors(level, getBlockPos())
                .findFirst()
                .map(worldBlob::getNetworkAt)
                .orElse(null);
    }


    @Nonnull
    @Override
    public CompoundNBT save(@Nonnull CompoundNBT tagCompound) {
        tagCompound.putBoolean("error", error);
        return super.save(tagCompound);
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        error = tagCompound.getBoolean("error");
    }

    @Override
    public void writeInfo(CompoundNBT tagCompound) {
        super.writeInfo(tagCompound);
        CompoundNBT info = getOrCreateInfo(tagCompound);
        info.putBoolean("publicAcc", publicAccess);
    }

    @Override
    public void readInfo(CompoundNBT tagCompound) {
        super.readInfo(tagCompound);
        CompoundNBT info = tagCompound.getCompound("Info");
        publicAccess = info.getBoolean("publicAcc");
    }

    @Override
    public void onReplaced(World world, BlockPos pos, BlockState state, BlockState newstate) {
        if (state.getBlock() == newstate.getBlock()) {
            return;
        }
        if (!this.level.isClientSide) {
            XNetBlobData blobData = XNetBlobData.get(this.level);
            WorldBlob worldBlob = blobData.getWorldBlob(this.level);
            worldBlob.removeCableSegment(pos);
            blobData.save();
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (!world.isClientSide) {
            XNetBlobData blobData = XNetBlobData.get(world);
            WorldBlob worldBlob = blobData.getWorldBlob(world);
            NetworkId networkId = worldBlob.newNetwork();
            worldBlob.createNetworkProvider(pos, new ColorId(CableColor.ROUTING.ordinal() + 1), networkId);
            blobData.save();
        }
    }


    // @todo 1.14
    public BlockState getActualState(BlockState state) {
        return state.setValue(ERROR, inError());
    }
}
