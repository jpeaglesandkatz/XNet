package mcjty.xnet.modules.router.blocks;

import mcjty.lib.api.container.CapabilityContainerProvider;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.OrientationTools;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import mcjty.rftoolsbase.api.xnet.keys.NetworkId;
import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import mcjty.xnet.client.ControllerChannelClientInfo;
import mcjty.xnet.compat.XNetTOPDriver;
import mcjty.xnet.logic.LogicTools;
import mcjty.xnet.modules.cables.CableColor;
import mcjty.xnet.modules.controller.ChannelInfo;
import mcjty.xnet.modules.router.LocalChannelId;
import mcjty.xnet.modules.router.RouterModule;
import mcjty.xnet.modules.router.client.GuiRouter;
import mcjty.xnet.multiblock.ColorId;
import mcjty.xnet.multiblock.WirelessChannelKey;
import mcjty.xnet.multiblock.WorldBlob;
import mcjty.xnet.multiblock.XNetBlobData;
import mcjty.xnet.setup.Config;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static mcjty.lib.builder.TooltipBuilder.header;
import static mcjty.lib.builder.TooltipBuilder.key;
import static mcjty.xnet.modules.controller.ChannelInfo.MAX_CHANNELS;
import static mcjty.xnet.modules.controller.blocks.TileEntityController.ERROR;
import static mcjty.xnet.modules.router.RouterModule.TYPE_ROUTER;

public final class TileEntityRouter extends GenericTileEntity {

    public static final String CMD_UPDATENAME = "router.updateName";
    public static final Key<BlockPos> PARAM_POS = new Key<>("pos", Type.BLOCKPOS);
    public static final Key<Integer> PARAM_CHANNEL = new Key<>("channel", Type.INTEGER);
    public static final Key<String> PARAM_NAME = new Key<>("name", Type.STRING);

    public static final String CMD_GETCHANNELS = "getChannelInfo";
    public static final String CLIENTCMD_CHANNELSREADY = "channelsReady";
    public static final String CMD_GETREMOTECHANNELS = "getRemoteChannelInfo";
    public static final String CLIENTCMD_CHANNELSREMOTEREADY = "channelsRemoteReady";

    private final Map<LocalChannelId, String> publishedChannels = new HashMap<>();
    private int channelCount = 0;

    private final LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<GenericContainer>("Router")
            .containerSupplier((windowId,player) -> new GenericContainer(RouterModule.CONTAINER_ROUTER.get(), windowId, EmptyContainer.CONTAINER_FACTORY.get(), getPos(), TileEntityRouter.this)));

    public TileEntityRouter() {
        super(TYPE_ROUTER.get());
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .topDriver(XNetTOPDriver.DRIVER)
                .tileEntitySupplier(TileEntityRouter::new)
                .info(key("message.xnet.shiftmessage"))
                .infoShift(header())
        ) {
            @Override
            protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
                super.fillStateContainer(builder);
                builder.add(ERROR);
            }
        };
    }

    public void addPublishedChannels(Set<String> channels) {
        channels.addAll(publishedChannels.values());
    }

    public int countPublishedChannelsOnNet() {
        Set<String> channels = new HashSet<>();
        NetworkId networkId = findRoutingNetwork();
        if (networkId != null) {
            LogicTools.routers(world, networkId)
                    .forEach(router -> router.addPublishedChannels(channels));
        }
        return channels.size();
    }

    public boolean inError() {
        return channelCount > Config.maxPublishedChannels.get();
    }

    public int getChannelCount() {
        return channelCount;
    }

    public void setChannelCount(int cnt) {
        if (channelCount == cnt) {
            return;
        }
        channelCount = cnt;
        BlockState state = world.getBlockState(pos);
        if (inError()) {
            if (!state.get(ERROR)) {
                world.setBlockState(pos, state.with(ERROR, true), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
            }
        } else {
            if (state.get(ERROR)) {
                world.setBlockState(pos, state.with(ERROR, false), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
            }
        }
        markDirtyQuick();
    }

    @Override
    public void writeInfo(CompoundNBT tagCompound) {
        super.writeInfo(tagCompound);
        CompoundNBT info = getOrCreateInfo(tagCompound);
        info.putInt("chancnt", channelCount);
        ListNBT published = new ListNBT();
        for (Map.Entry<LocalChannelId, String> entry : publishedChannels.entrySet()) {
            CompoundNBT tc = new CompoundNBT();
            BlockPosTools.write(tc, "pos", entry.getKey().getControllerPos());
            tc.putInt("index", entry.getKey().getIndex());
            tc.putString("name", entry.getValue());
            published.add(tc);
        }
        info.put("published", published);
    }

    @Override
    public void readInfo(CompoundNBT tagCompound) {
        super.readInfo(tagCompound);
        CompoundNBT info = tagCompound.getCompound("Info");
        channelCount = info.getInt("chancnt");
        ListNBT published = info.getList("published", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < published.size(); i++) {
            CompoundNBT tc = published.getCompound(i);
            LocalChannelId id = new LocalChannelId(BlockPosTools.read(tc, "pos"), tc.getInt("index"));
            String name = tc.getString("name");
            publishedChannels.put(id, name);
        }
    }

    public Stream<Pair<String, IChannelType>> publishedChannelStream() {
        return LogicTools.connectors(world, pos)
                .map(connectorPos -> LogicTools.getControllerForConnector(world, connectorPos))
                .filter(Objects::nonNull)
                .flatMap(controller -> IntStream.range(0, MAX_CHANNELS)
                        .mapToObj(i -> {
                            ChannelInfo channelInfo = controller.getChannels()[i];
                            if (channelInfo != null && !channelInfo.getChannelName().isEmpty()) {
                                LocalChannelId id = new LocalChannelId(controller.getPos(), i);
                                String publishedName = publishedChannels.get(id);
                                if (publishedName != null && !publishedName.isEmpty()) {
                                    return Pair.of(publishedName, channelInfo.getType());
                                }
                            }
                            return null;
                        })
                        .filter(Objects::nonNull));
    }

    public void findLocalChannelInfo(List<ControllerChannelClientInfo> list, boolean onlyPublished, boolean remote) {
        LogicTools.connectors(world, getPos())
                .map(connectorPos -> LogicTools.getControllerForConnector(world, connectorPos))
                .filter(Objects::nonNull)
                .forEach(controller -> {
                    for (int i = 0; i < MAX_CHANNELS; i++) {
                        ChannelInfo channelInfo = controller.getChannels()[i];
                        if (channelInfo != null && !channelInfo.getChannelName().isEmpty()) {
                            LocalChannelId id = new LocalChannelId(controller.getPos(), i);
                            String publishedName = publishedChannels.get(id);
                            if (publishedName == null) {
                                publishedName = "";
                            }
                            if ((!onlyPublished) || !publishedName.isEmpty()) {
                                ControllerChannelClientInfo ci = new ControllerChannelClientInfo(channelInfo.getChannelName(), publishedName, controller.getPos(), channelInfo.getType(), remote, i);
                                if (list.stream().noneMatch(ii -> Objects.equals(ii.getPublishedName(), ci.getPublishedName())
                                        && Objects.equals(ii.getChannelName(), ci.getChannelName())
                                        && Objects.equals(ii.getChannelType(), ci.getChannelType())
                                        && Objects.equals(ii.getPos(), ci.getPos()))) {
                                    list.add(ci);
                                }
                            }
                        }
                    }
                });
    }

    private void findRemoteChannelInfo(List<ControllerChannelClientInfo> list) {
        NetworkId networkId = findRoutingNetwork();
        if (networkId != null) {
            // For each consumer on this network:
            LogicTools.consumers(world, networkId)
                    .forEach(consumerPos -> {
                        // Find all routers connected to this network and add their published local channels
                        LogicTools.routers(world, consumerPos)
                                .filter(r -> r != this)
                                .forEach(router -> router.findLocalChannelInfo(list, true, false));
                        // Find all wireless routers connected to this network and add the public or private
                        // channels that can be reached by them
                        LogicTools.wirelessRouters(world, consumerPos)
                                .filter(router -> !router.inError())
                                .forEach(router -> router.findRemoteChannelInfo(list));
                    });
        }
    }

    @Nullable
    public NetworkId findRoutingNetwork() {
        WorldBlob worldBlob = XNetBlobData.get(world).getWorldBlob(world);
        return LogicTools.routingConnectors(world, getPos())
                .findFirst()
                .map(worldBlob::getNetworkAt)
                .orElse(null);
    }

    public void addRoutedConnectors(Map<SidedConsumer, IConnectorSettings> connectors, @Nonnull BlockPos controllerPos, int channel, IChannelType type,
                                    Map<WirelessChannelKey, Integer> wirelessVersions) {
        if (inError()) {
            // We are in error. Don't do anything
            return;
        }
        LocalChannelId id = new LocalChannelId(controllerPos, channel);
        String publishedName = publishedChannels.get(id);
        if (publishedName != null && !publishedName.isEmpty()) {
            NetworkId networkId = findRoutingNetwork();
            if (networkId != null) {
                LogicTools.consumers(world, networkId)
                        .forEach(consumerPos -> {
                            LogicTools.routers(world, consumerPos)
                                    .forEach(router -> router.addConnectorsFromConnectedNetworks(connectors, publishedName, type));
                            // First public channels
                            LogicTools.wirelessRouters(world, consumerPos)
                                    .filter(router -> !router.inError())
                                    .forEach(router -> {
                                        // First public
                                        router.addWirelessConnectors(connectors, publishedName, type, null, wirelessVersions);
                                        // Now private
                                        router.addWirelessConnectors(connectors, publishedName, type, getOwnerUUID(), wirelessVersions);
                                    });
                        });
            } else {
                // If there is no routing network that means we have a local network only
                addConnectorsFromConnectedNetworks(connectors, publishedName, type);
            }
        }
    }

    public boolean addConnectorsFromConnectedNetworks(Map<SidedConsumer, IConnectorSettings> connectors, String channelName, IChannelType type) {
        AtomicBoolean rc = new AtomicBoolean(false);
        LogicTools.connectors(world, getPos())
                .map(connectorPos -> LogicTools.getControllerForConnector(world, connectorPos))
                .filter(Objects::nonNull)
                .forEach(controller -> {
                    for (int i = 0; i < MAX_CHANNELS; i++) {
                        ChannelInfo info = controller.getChannels()[i];
                        if (info != null) {
                            String publishedName = publishedChannels.get(new LocalChannelId(controller.getPos(), i));
                            if (publishedName != null && !publishedName.isEmpty()) {
                                if (channelName.equals(publishedName) && type.equals(info.getType())) {
                                    connectors.putAll(controller.getConnectors(i));
                                    rc.set(true);
                                }
                            }
                        }
                    }
                });
        return rc.get();
    }

    private void updatePublishName(@Nonnull BlockPos controllerPos, int channel, String name) {
        LocalChannelId id = new LocalChannelId(controllerPos, channel);
        if (name == null || name.isEmpty()) {
            publishedChannels.remove(id);
        } else {
            publishedChannels.put(id, name);
        }
        int number = countPublishedChannelsOnNet();
        WorldBlob worldBlob = XNetBlobData.get(world).getWorldBlob(world);
        NetworkId networkId = findRoutingNetwork();
        if (networkId != null) {
            if (number != channelCount) {
                LogicTools.routers(world, networkId)
                        .forEach(router -> router.setChannelCount(number));
            }
            worldBlob.markNetworkDirty(networkId); // Force a recalc of affected networks
        }
        for (NetworkId net : worldBlob.getNetworksAt(pos)) {
            worldBlob.markNetworkDirty(net);
        }
        for (Direction facing : OrientationTools.DIRECTION_VALUES) {
            for (NetworkId net : worldBlob.getNetworksAt(pos.offset(facing))) {
                worldBlob.markNetworkDirty(net);
            }
        }


        markDirtyQuick();
    }

    @Override
    public boolean execute(PlayerEntity playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_UPDATENAME.equals(command)) {
            BlockPos controllerPos = params.get(PARAM_POS);
            int channel = params.get(PARAM_CHANNEL);
            String name = params.get(PARAM_NAME);
            updatePublishName(controllerPos, channel, name);
            return true;
        }

        return false;
    }

    @Nonnull
    @Override
    public <T> List<T> executeWithResultList(String command, TypedMap args, Type<T> type) {
        List<T> rc = super.executeWithResultList(command, args, type);
        if (!rc.isEmpty()) {
            return rc;
        }
        if (CMD_GETCHANNELS.equals(command)) {
            List<ControllerChannelClientInfo> list = new ArrayList<>();
            findLocalChannelInfo(list, false, false);
            return type.convert(list);
        } else if (CMD_GETREMOTECHANNELS.equals(command)) {
            List<ControllerChannelClientInfo> list = new ArrayList<>();
            findRemoteChannelInfo(list);
            return type.convert(list);
        }
        return Collections.emptyList();
    }


    @Override
    public <T> boolean receiveListFromServer(String command, List<T> list, Type<T> type) {
        boolean rc = super.receiveListFromServer(command, list, type);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_CHANNELSREADY.equals(command)) {
            GuiRouter.fromServer_localChannels = new ArrayList<>(Type.create(ControllerChannelClientInfo.class).convert(list));
            return true;
        } else if (CLIENTCMD_CHANNELSREMOTEREADY.equals(command)) {
            GuiRouter.fromServer_remoteChannels = new ArrayList<>(Type.create(ControllerChannelClientInfo.class).convert(list));
            return true;
        }
        return false;
    }

    @Override
    public void onReplaced(World world, BlockPos pos, BlockState state, BlockState newstate) {
        if (state.getBlock() == newstate.getBlock()) {
            return;
        }
        if (!this.world.isRemote) {
            XNetBlobData blobData = XNetBlobData.get(this.world);
            WorldBlob worldBlob = blobData.getWorldBlob(this.world);
            worldBlob.removeCableSegment(pos);
            blobData.save();
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (!world.isRemote) {
            XNetBlobData blobData = XNetBlobData.get(world);
            WorldBlob worldBlob = blobData.getWorldBlob(world);
            NetworkId networkId = worldBlob.newNetwork();
            worldBlob.createNetworkProvider(pos, new ColorId(CableColor.ROUTING.ordinal() + 1), networkId);
            blobData.save();
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
        if (cap == CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY) {
            return screenHandler.cast();
        }
        return super.getCapability(cap, facing);
    }
}
