package mcjty.xnet.blocks.controller;

import com.google.gson.*;
import mcjty.lib.api.container.CapabilityContainerProvider;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.OrientationTools;
import mcjty.xnet.XNet;
import mcjty.rftoolsbase.api.xnet.channels.IChannelSettings;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import mcjty.rftoolsbase.api.xnet.channels.IControllerContext;
import mcjty.rftoolsbase.api.xnet.keys.ConsumerId;
import mcjty.rftoolsbase.api.xnet.keys.NetworkId;
import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import mcjty.rftoolsbase.api.xnet.keys.SidedPos;
import mcjty.xnet.blocks.cables.ConnectorBlock;
import mcjty.xnet.blocks.cables.ConnectorTileEntity;
import mcjty.xnet.blocks.cables.NetCableSetup;
import mcjty.xnet.blocks.controller.gui.GuiController;
import mcjty.xnet.clientinfo.ChannelClientInfo;
import mcjty.xnet.clientinfo.ConnectedBlockClientInfo;
import mcjty.xnet.clientinfo.ConnectorClientInfo;
import mcjty.xnet.clientinfo.ConnectorInfo;
import mcjty.xnet.config.ConfigSetup;
import mcjty.xnet.init.ModBlocks;
import mcjty.xnet.logic.ChannelInfo;
import mcjty.xnet.logic.LogicTools;
import mcjty.xnet.multiblock.*;
import mcjty.xnet.network.PacketControllerError;
import mcjty.xnet.network.PacketJsonToClipboard;
import mcjty.xnet.network.XNetMessages;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.BooleanProperty;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.network.NetworkDirection;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static mcjty.xnet.init.ModBlocks.TYPE_CONTROLLER;
import static mcjty.xnet.logic.ChannelInfo.MAX_CHANNELS;

public final class TileEntityController extends GenericTileEntity implements ITickableTileEntity, IControllerContext {

    public static final String CMD_GETCHANNELS = "getChannelInfo";
    public static final String CLIENTCMD_CHANNELSREADY = "channelsReady";
    public static final String CMD_GETCONNECTEDBLOCKS = "getConnectedBlocks";
    public static final String CLIENTCMD_CONNECTEDBLOCKSREADY = "connectedBlocksReady";

    public static final String CMD_CREATECONNECTOR = "controller.createConnector";
    public static final String CMD_REMOVECONNECTOR = "controller.removeConnector";
    public static final String CMD_UPDATECONNECTOR = "controller.updateConnector";
    public static final String CMD_CREATECHANNEL = "controller.createChannel";
    public static final String CMD_PASTECHANNEL = "controller.pasteChannel";
    public static final String CMD_COPYCHANNEL = "controller.copyChannel";
    public static final String CMD_PASTECONNECTOR = "controller.pasteConnector";
    public static final String CMD_COPYCONNECTOR = "controller.copyConnector";
    public static final String CMD_REMOVECHANNEL = "controller.removeChannel";
    public static final String CMD_UPDATECHANNEL = "controller.updateChannel";

    public static final Key<Integer> PARAM_INDEX = new Key<>("index", Type.INTEGER);
    public static final Key<String> PARAM_TYPE = new Key<>("type", Type.STRING);
    public static final Key<String> PARAM_JSON = new Key<>("json", Type.STRING);
    public static final Key<Integer> PARAM_CHANNEL = new Key<>("channel", Type.INTEGER);
    public static final Key<Integer> PARAM_SIDE = new Key<>("side", Type.INTEGER);
    public static final Key<BlockPos> PARAM_POS = new Key<>("pos", Type.BLOCKPOS);

    public static final BooleanProperty ERROR = BooleanProperty.create("error");

    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(0) {
        @Override
        protected void setup() {
            playerSlots(91, 157);
        }
    };

    private NetworkId networkId;

    private final ChannelInfo[] channels = new ChannelInfo[MAX_CHANNELS];
    private int colors = 0;

    // Client side only
    private boolean error = false;

    // Cached/transient data
    private Map<SidedConsumer, IConnectorSettings> cachedConnectors[] = new Map[MAX_CHANNELS];
    private Map<SidedConsumer, IConnectorSettings> cachedRoutedConnectors[] = new Map[MAX_CHANNELS];
    private Map<WirelessChannelKey, Integer> wirelessVersions = new HashMap<>();

    private LazyOptional<GenericEnergyStorage> energyHandler = LazyOptional.of(() -> new GenericEnergyStorage(this, true, ConfigSetup.controllerMaxRF.get(), ConfigSetup.controllerRfPerTick.get()));
    private LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<GenericContainer>("Controller")
            .containerSupplier((windowId,player) -> new GenericContainer(ModBlocks.CONTAINER_CONTROLLER, windowId, CONTAINER_FACTORY, getPos(), TileEntityController.this))
            .energyHandler(energyHandler));

    private NetworkChecker networkChecker = null;

    public TileEntityController() {
        super(TYPE_CONTROLLER);
        for (int i = 0; i < MAX_CHANNELS; i++) {
            channels[i] = null;
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        boolean oldError = error;

        super.onDataPacket(net, packet);

        if (world.isRemote) {
            // If needed send a render update.
            if (oldError != error) {
                BlockState state = world.getBlockState(pos);
                world.notifyBlockUpdate(pos, state, state, Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
            }
        }
    }


    @Nonnull
    public NetworkChecker getNetworkChecker() {
        if (networkChecker == null) {
            networkChecker = new NetworkChecker();
            networkChecker.add(networkId);
            WorldBlob worldBlob = XNetBlobData.getBlobData(world).getWorldBlob(world);
            LogicTools.routers(world, networkId)
                    .forEach(router -> {
                        networkChecker.add(worldBlob.getNetworksAt(router.getPos()));
                        // We're only interested in one network. The other router networks are all same topology
                        NetworkId routerNetwork = worldBlob.getNetworkAt(router.getPos());
                        if (routerNetwork != null) {
                            LogicTools.routers(world, routerNetwork)
                                    .filter(r -> router != r)
                                    .forEach(r -> LogicTools.connectors(world, r.getPos())
                                            .forEach(connectorPos -> networkChecker.add(worldBlob.getNetworkAt(connectorPos))));
                        }
                    });

//            networkChecker.dump();
        }
        return networkChecker;
    }

    @Override
    public World getControllerWorld() {
        return world;
    }

    @Override
    public NetworkId getNetworkId() {
        return networkId;
    }

    public void setNetworkId(NetworkId networkId) {
        if (networkId == null && this.networkId == null) {
            return;
        }
        if (networkId != null && networkId.equals(this.networkId)) {
            return;
        }
        networkChecker = null;
        this.networkId = networkId;
        markDirtyQuick();
    }

    public ChannelInfo[] getChannels() {
        return channels;
    }

    private void checkNetwork(WorldBlob worldBlob) {
        if (networkId != null && getNetworkChecker().isDirtyAndMarkClean(worldBlob)) {
            cleanCaches();
            return;
        }

        // Check wireless
        for (Map.Entry<WirelessChannelKey, Integer> entry : wirelessVersions.entrySet()) {
            XNetWirelessChannels channels = XNetWirelessChannels.getWirelessChannels(world);
            XNetWirelessChannels.WirelessChannelInfo channel = channels.findChannel(entry.getKey());
            if (channel == null) {
                cleanCaches();
                return;
            }
            if (channel.getVersion() != entry.getValue()) {
                cleanCaches();
                return;
            }
        }
    }

    private void cleanCaches() {
        for (int i = 0; i < MAX_CHANNELS; i++) {
            if (channels[i] != null) {
                cleanCache(i);
            }
        }
    }

    @Override
    public boolean matchColor(int colorMask) {
        return (colors & colorMask) == colorMask;
    }

    public boolean inError() {
        if (world.isRemote) {
            return error;
        } else {
            WorldBlob worldBlob = XNetBlobData.getBlobData(world).getWorldBlob(world);
            return worldBlob.getNetworksAt(getPos()).size() > 1;
        }
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            WorldBlob worldBlob = XNetBlobData.getBlobData(world).getWorldBlob(world);

            if (worldBlob.getNetworksAt(getPos()).size() > 1) {
                // Error situation!
                markDirtyClient();
                return;
            }

            checkNetwork(worldBlob);

            if (!checkAndConsumeRF(ConfigSetup.controllerRFT.get())) {
                return;
            }

            boolean dirty = false;
            int newcolors = 0;
            for (int i = 0; i < MAX_CHANNELS; i++) {
                if (channels[i] != null && channels[i].isEnabled()) {
                    if (checkAndConsumeRF(ConfigSetup.controllerChannelRFT.get())) {
                        channels[i].getChannelSettings().tick(i, this);
                    }
                    newcolors |= channels[i].getChannelSettings().getColors();
                    dirty = true;
                }
            }
            if (newcolors != colors) {
                dirty = true;
                colors = newcolors;
            }
            if (dirty) {
                markDirtyQuick();
            }
        }
    }

    @Override
    public boolean checkAndConsumeRF(int rft) {
        return energyHandler.map(h -> {
            if (rft > 0) {
                if (h.getEnergy() < rft) {
                    // Not enough energy
                    return false;
                }
                h.consumeEnergy(rft);
                markDirtyQuick();
            }
            return true;
        }).orElse(false);
    }

    private void networkDirty() {
        if (networkId != null) {
            XNetBlobData.getBlobData(world).getWorldBlob(world).markNetworkDirty(networkId);
        }
    }

    private void cleanCache(int channel) {
        cachedConnectors[channel] = null;
        cachedRoutedConnectors[channel] = null;
        channels[channel].getChannelSettings().cleanCache();
    }

    @Override
    @Nonnull
    public Map<SidedConsumer, IConnectorSettings> getConnectors(int channel) {
        if (cachedConnectors[channel] == null) {
            WorldBlob worldBlob = XNetBlobData.getBlobData(world).getWorldBlob(world);
            cachedConnectors[channel] = new HashMap<>();
            for (Map.Entry<SidedConsumer, ConnectorInfo> entry : channels[channel].getConnectors().entrySet()) {
                SidedConsumer sidedConsumer = entry.getKey();
                BlockPos pos = findConsumerPosition(sidedConsumer.getConsumerId());
                if (pos != null && worldBlob.getNetworksAt(pos).contains(getNetworkId())) {
                    cachedConnectors[channel].put(sidedConsumer, entry.getValue().getConnectorSettings());
                }
            }
        }
        return cachedConnectors[channel];
    }

    @Override
    @Nonnull
    public Map<SidedConsumer, IConnectorSettings> getRoutedConnectors(int channel) {
        if (cachedRoutedConnectors[channel] == null) {
            cachedRoutedConnectors[channel] = new HashMap<>();

            wirelessVersions.clear();
            if (!channels[channel].getChannelName().isEmpty()) {
                LogicTools.routers(world, networkId)
                        .forEach(router -> router.addRoutedConnectors(cachedRoutedConnectors[channel], getPos(),
                                channel, channels[channel].getType(),
                                wirelessVersions));
            }
        }
        return cachedRoutedConnectors[channel];
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        if (networkId != null) {
            tagCompound.putInt("networkId", networkId.getId());
        }
        return super.write(tagCompound);
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        if (tagCompound.contains("networkId")) {
            networkId = new NetworkId(tagCompound.getInt("networkId"));
        } else {
            networkId = null;
        }
    }

    @Override
    public void writeClientDataToNBT(CompoundNBT tagCompound) {
        super.writeClientDataToNBT(tagCompound);
        if (!world.isRemote) {
            tagCompound.putBoolean("error", inError());
        }
    }

    @Override
    public void readClientDataFromNBT(CompoundNBT tagCompound) {
        super.readClientDataFromNBT(tagCompound);
        error = tagCompound.getBoolean("error");
    }

    @Override
    protected void writeInfo(CompoundNBT tagCompound) {
        super.writeInfo(tagCompound);
        CompoundNBT info = getOrCreateInfo(tagCompound);
        info.putInt("colors", colors);

        for (int i = 0; i < MAX_CHANNELS; i++) {
            if (channels[i] != null) {
                CompoundNBT tc = new CompoundNBT();
                tc.putString("type", channels[i].getType().getID());
                channels[i].writeToNBT(tc);
                info.put("channel" + i, tc);
            }
        }
    }

    @Override
    public void readInfo(CompoundNBT tagCompound) {
        super.readInfo(tagCompound);
        CompoundNBT info = tagCompound.getCompound("Info");
        colors = info.getInt("colors");
        for (int i = 0; i < MAX_CHANNELS; i++) {
            if (info.contains("channel" + i)) {
                CompoundNBT tc = info.getCompound("channel" + i);
                String id = tc.getString("type");
                IChannelType type = XNet.xNetApi.findType(id);
                if (type == null) {
                    XNet.setup.getLogger().warn("Unsupported type " + id + "!");
                    continue;
                }
                channels[i] = new ChannelInfo(type);
                channels[i].readFromNBT(tc);
            } else {
                channels[i] = null;
            }
        }
    }

    @Nullable
    @Override
    public BlockPos findConsumerPosition(@Nonnull ConsumerId consumerId) {
        WorldBlob worldBlob = XNetBlobData.getBlobData(world).getWorldBlob(world);
        return findConsumerPosition(worldBlob, consumerId);
    }

    @Nullable
    private BlockPos findConsumerPosition(@Nonnull WorldBlob worldBlob, @Nonnull ConsumerId consumerId) {
        return worldBlob.getConsumerPosition(consumerId);
    }

    @Override
    public List<SidedPos> getConnectedBlockPositions() {
        WorldBlob worldBlob = XNetBlobData.getBlobData(world).getWorldBlob(world);

        List<SidedPos> result = new ArrayList<>();
        Set<ConnectedBlockClientInfo> set = new HashSet<>();
        Stream<BlockPos> consumers = getConsumerStream(worldBlob);
        consumers.forEach(consumerPos -> {
            String name = "";
            TileEntity te = world.getTileEntity(consumerPos);
            if (te instanceof ConnectorTileEntity) {
                // Should always be the case. @todo error?
                name = ((ConnectorTileEntity) te).getConnectorName();
            } else {
                XNet.setup.getLogger().warn("What? The connector at " + BlockPosTools.toString(consumerPos) + " is not a connector?");
            }
            for (Direction facing : OrientationTools.DIRECTION_VALUES) {
                if (ConnectorBlock.isConnectable(world, consumerPos, facing)) {
                    BlockPos pos = consumerPos.offset(facing);
                    SidedPos sidedPos = new SidedPos(pos, facing.getOpposite());
                    result.add(sidedPos);
                }
            }
        });

        return result;
    }

    @Nonnull
    private List<ConnectedBlockClientInfo> findConnectedBlocksForClient() {
        WorldBlob worldBlob = XNetBlobData.getBlobData(world).getWorldBlob(world);

        Set<ConnectedBlockClientInfo> set = new HashSet<>();
        Stream<BlockPos> consumers = getConsumerStream(worldBlob);
        consumers.forEach(consumerPos -> {
            String name = "";
            TileEntity te = world.getTileEntity(consumerPos);
            if (te instanceof ConnectorTileEntity) {
                // Should always be the case. @todo error?
                name = ((ConnectorTileEntity) te).getConnectorName();
            } else {
                XNet.setup.getLogger().warn("What? The connector at " + BlockPosTools.toString(consumerPos) + " is not a connector?");
            }
            for (Direction facing : OrientationTools.DIRECTION_VALUES) {
                if (ConnectorBlock.isConnectable(world, consumerPos, facing)) {
                    BlockPos pos = consumerPos.offset(facing);
                    SidedPos sidedPos = new SidedPos(pos, facing.getOpposite());
                    BlockState state = world.getBlockState(pos);
                    ItemStack item = state.getBlock().getItem(world, pos, state);
                    ConnectedBlockClientInfo info = new ConnectedBlockClientInfo(sidedPos, item, name);
                    set.add(info);
                }
            }
        });
        List<ConnectedBlockClientInfo> list = new ArrayList<>(set);
        list.sort(Comparator.comparing(ConnectedBlockClientInfo::getBlockUnlocName)
                .thenComparing(ConnectedBlockClientInfo::getPos));
        return list;
    }

    private Stream<BlockPos> getConsumerStream(WorldBlob worldBlob) {
        return XNet.xNetApi.getConsumerProviders().stream()
                .map(provider -> provider.getConsumers(world, worldBlob, getNetworkId()).stream())
                .flatMap(s -> s);
    }

    @Nonnull
    private List<ChannelClientInfo> findChannelInfo() {
        WorldBlob worldBlob = XNetBlobData.getBlobData(world).getWorldBlob(world);

        List<ChannelClientInfo> chanList = new ArrayList<>();
        for (ChannelInfo channel : channels) {
            if (channel != null) {
                ChannelClientInfo clientInfo = new ChannelClientInfo(channel.getChannelName(), channel.getType(),
                        channel.getChannelSettings(), channel.isEnabled());

                for (Map.Entry<SidedConsumer, ConnectorInfo> entry : channel.getConnectors().entrySet()) {
                    SidedConsumer sidedConsumer = entry.getKey();
                    ConnectorInfo info = entry.getValue();
                    if (info.getConnectorSettings() != null) {
                        BlockPos consumerPos = findConsumerPosition(worldBlob, sidedConsumer.getConsumerId());
                        if (consumerPos != null) {
                            SidedPos pos = new SidedPos(consumerPos.offset(sidedConsumer.getSide()), sidedConsumer.getSide().getOpposite());
                            boolean advanced = world.getBlockState(consumerPos).getBlock() == NetCableSetup.advancedConnectorBlock;
                            ConnectorClientInfo ci = new ConnectorClientInfo(pos, sidedConsumer.getConsumerId(), channel.getType(), info.getConnectorSettings());
                            clientInfo.getConnectors().put(sidedConsumer, ci);
                        } else {
                            // Consumer was possibly removed. We might want to remove the entry from our list here?
                            // @todo
                        }
                    }
                }

                chanList.add(clientInfo);
            } else {
                chanList.add(null);
            }
        }
        return chanList;
    }

    private void updateChannel(int channel, TypedMap params) {
        Map<String, Object> data = new HashMap<>();
        for (Key<?> key : params.getKeys()) {
            data.put(key.getName(), params.get(key));
        }
        channels[channel].getChannelSettings().update(data);

        Boolean enabled = (Boolean) data.get(GuiController.TAG_ENABLED);
        channels[channel].setEnabled(Boolean.TRUE.equals(enabled));

        String name = (String) data.get(GuiController.TAG_NAME);
        channels[channel].setChannelName(name);

        markAsDirty();
    }

    public void markAsDirty() {
        networkDirty();
        markDirtyQuick();
    }

    private void removeChannel(int channel) {
        channels[channel] = null;
        cachedConnectors[channel] = null;
        cachedRoutedConnectors[channel] = null;
        markAsDirty();
    }

    private void createChannel(int channel, String typeId) {
        IChannelType type = XNet.xNetApi.findType(typeId);
        channels[channel] = new ChannelInfo(type);
        markAsDirty();
    }

    private void updateConnector(int channel, SidedPos pos, TypedMap params) {
        WorldBlob worldBlob = XNetBlobData.getBlobData(world).getWorldBlob(world);
        ConsumerId consumerId = worldBlob.getConsumerAt(pos.getPos().offset(pos.getSide()));
        for (Map.Entry<SidedConsumer, ConnectorInfo> entry : channels[channel].getConnectors().entrySet()) {
            SidedConsumer key = entry.getKey();
            if (key.getConsumerId().equals(consumerId) && key.getSide().getOpposite().equals(pos.getSide())) {
                Map<String, Object> data = new HashMap<>();
                for (Key<?> k : params.getKeys()) {
                    data.put(k.getName(), params.get(k));
                }
                channels[channel].getConnectors().get(key).getConnectorSettings().update(data);
                markAsDirty();
                return;
            }
        }
    }

    private void removeConnector(int channel, SidedPos pos) {
        WorldBlob worldBlob = XNetBlobData.getBlobData(world).getWorldBlob(world);
        ConsumerId consumerId = worldBlob.getConsumerAt(pos.getPos().offset(pos.getSide()));
        SidedConsumer toremove = null;
        for (Map.Entry<SidedConsumer, ConnectorInfo> entry : channels[channel].getConnectors().entrySet()) {
            SidedConsumer key = entry.getKey();
            if (key.getSide().getOpposite().equals(pos.getSide())) {
                if (key.getConsumerId().equals(consumerId)) {
                    toremove = key;
                    break;
                }
            }
        }
        if (toremove != null) {
            channels[channel].getConnectors().remove(toremove);
            markAsDirty();
        }
    }

    private ConnectorInfo createConnector(int channel, SidedPos pos) {
        WorldBlob worldBlob = XNetBlobData.getBlobData(world).getWorldBlob(world);
        BlockPos consumerPos = pos.getPos().offset(pos.getSide());
        ConsumerId consumerId = worldBlob.getConsumerAt(consumerPos);
        if (consumerId == null) {
            throw new RuntimeException("What?");
        }
        SidedConsumer id = new SidedConsumer(consumerId, pos.getSide().getOpposite());
        boolean advanced = world.getBlockState(consumerPos).getBlock() == NetCableSetup.advancedConnectorBlock;
        ConnectorInfo info = channels[channel].createConnector(id, advanced);
        markAsDirty();
        return info;
    }

    private IConnectorSettings findConnectorSettings(ChannelInfo channel, SidedPos p) {
        WorldBlob worldBlob = XNetBlobData.getBlobData(world).getWorldBlob(world);

        for (Map.Entry<SidedConsumer, ConnectorInfo> entry : channel.getConnectors().entrySet()) {
            SidedConsumer sidedConsumer = entry.getKey();
            ConnectorInfo info = entry.getValue();
            if (info.getConnectorSettings() != null) {
                BlockPos consumerPos = findConsumerPosition(worldBlob, sidedConsumer.getConsumerId());
                if (consumerPos != null) {
                    SidedPos pos = new SidedPos(consumerPos.offset(sidedConsumer.getSide()), sidedConsumer.getSide().getOpposite());
                    if (pos.equals(p)) {
                        return info.getConnectorSettings();
                    }
                }
            }
        }
        return null;
    }

    @Nonnull
    private Set<ConnectedBlockInfo> findConnectedBlocks() {
        WorldBlob worldBlob = XNetBlobData.getBlobData(world).getWorldBlob(world);

        Set<ConnectedBlockInfo> set = new HashSet<>();
        Stream<BlockPos> consumers = getConsumerStream(worldBlob);
        consumers.forEach(consumerPos -> {
            String name = "";
            TileEntity te = world.getTileEntity(consumerPos);
            if (te instanceof ConnectorTileEntity) {
                // Should always be the case. @todo error?
                name = ((ConnectorTileEntity) te).getConnectorName();
            } else {
                XNet.setup.getLogger().warn("What? The connector at " + BlockPosTools.toString(consumerPos) + " is not a connector?");
            }
            for (Direction facing : OrientationTools.DIRECTION_VALUES) {
                if (ConnectorBlock.isConnectable(world, consumerPos, facing)) {
                    BlockPos pos = consumerPos.offset(facing);
                    SidedPos sidedPos = new SidedPos(pos, facing.getOpposite());
                    BlockState state = world.getBlockState(pos);
                    state = state.getBlock().isAir(state, world, pos) ? null : state;
                    ConnectedBlockInfo info = new ConnectedBlockInfo(sidedPos, state, name);
                    set.add(info);
                }
            }
        });
        return set;
    }

    private void copyConnector(ServerPlayerEntity player, int index, SidedPos sidedPos) {
        ChannelInfo channel = channels[index];
        IChannelSettings settings = channel.getChannelSettings();
        JsonObject parent = new JsonObject();
        IConnectorSettings connectorSettings = findConnectorSettings(channel, sidedPos);
        if (connectorSettings != null) {
            JsonObject object = connectorSettings.writeToJson();
            if (object != null) {
                parent.add("type", new JsonPrimitive(channel.getType().getID()));
                parent.add("connector", object);
                boolean advanced = ConnectorBlock.isAdvancedConnector(world, sidedPos.getPos().offset(sidedPos.getSide()));
                parent.add("advanced", new JsonPrimitive(advanced));

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(parent);

                XNetMessages.INSTANCE.sendTo(new PacketJsonToClipboard(json), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
                return;
            }
        }
        XNetMessages.INSTANCE.sendTo(new PacketControllerError("Error copying connector!"), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }

    private void copyChannel(ServerPlayerEntity player, int index) {
        ChannelInfo channel = channels[index];
        IChannelSettings settings = channel.getChannelSettings();
        JsonObject parent = new JsonObject();
        JsonObject channelObject = settings.writeToJson();

        if (channelObject != null) {
            parent.add("type", new JsonPrimitive(channel.getType().getID()));
            parent.add("name", new JsonPrimitive(channel.getChannelName()));
            parent.add("channel", channelObject);

            JsonArray connectors = new JsonArray();

            Set<ConnectedBlockInfo> connectedBlocks = findConnectedBlocks();
            for (ConnectedBlockInfo connectedBlock : connectedBlocks) {
                SidedPos sidedPos = connectedBlock.getPos();
                IConnectorSettings connectorSettings = findConnectorSettings(channel, sidedPos);
                if (connectorSettings != null) {
                    JsonObject object = connectorSettings.writeToJson();
                    if (object != null) {
                        JsonObject connectorObject = new JsonObject();
                        connectorObject.add("connector", object);
                        connectorObject.add("name", new JsonPrimitive(connectedBlock.getName()));
                        boolean advanced = ConnectorBlock.isAdvancedConnector(world, sidedPos.getPos().offset(sidedPos.getSide()));
                        connectorObject.add("advanced", new JsonPrimitive(advanced));
                        if (!connectedBlock.isAir()) {
                            BlockState state = connectedBlock.getConnectedState();
                            connectorObject.add("block", new JsonPrimitive(state.getBlock().getRegistryName().toString()));
                        }

                        connectors.add(connectorObject);
                    }
                }
            }

            parent.add("connectors", connectors);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(parent);

            XNetMessages.INSTANCE.sendTo(new PacketJsonToClipboard(json), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        } else {
            XNetMessages.INSTANCE.sendTo(new PacketControllerError("Channel does not support this!"), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    private int calculateMatchingScore(IChannelType type, ConnectedBlockInfo info, String name, ResourceLocation block,
                                       @Nonnull Direction side, @Nonnull Direction facingOverride, boolean advanced,
                                       boolean advancedNeeded) {
        int score = 0;

        String infoName = info.getName();
        if (!name.isEmpty() && Objects.equals(name, infoName)) {
            score += 100;
        }

        BlockPos blockPos = info.getPos().getPos();
        Direction facing = info.getPos().getSide();

        // This block doesn't support this type. So bad score
        if (!type.supportsBlock(world, blockPos, facing)) {
            score -= 1000;
        }

        ResourceLocation infoBlock = info.getConnectedState().getBlock().getRegistryName();

        // If the side doesn't match we give a bad penalty
        if (!KnownUnsidedBlocks.isUnsided(infoBlock) && !facingOverride.equals(facing)) {
            score -= 1000;
        }

        boolean infoAdvanced = ConnectorBlock.isAdvancedConnector(world, blockPos.offset(facing));
        if (advanced) {
            if (infoAdvanced) {
                score += 50;
            } else {
                // If advanced is desired but our actual connector is not advanced then we give a penalty. The penalty is big
                // if we can't match with the actual side or if we actually need advanced
                if (advancedNeeded) {
                    score -= 1000;
                } else {
                    score -= 40;
                }
            }
        } else {
            // If we don't need advanced then we add a small penalty if it is advanced
            if (infoAdvanced) {
                score--;
            }
        }

        if (!info.isAir()) {
            if (Objects.equals(infoBlock, block)) {
                score += 200;
            }
        }

        if (facing.equals(side)) {
            score += 2;
        }

        return score;
    }

    private void pasteConnector(ServerPlayerEntity player, int channel, SidedPos sidedPos, String json) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(json).getAsJsonObject();

            if (!root.has("connector") || !root.has("type")) {
                XNetMessages.INSTANCE.sendTo(new PacketControllerError("Invalid connector json!"), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
                return;
            }

            String typeId = root.get("type").getAsString();
            IChannelType type = XNet.xNetApi.findType(typeId);
            if (type != channels[channel].getType()) {
                XNetMessages.INSTANCE.sendTo(new PacketControllerError("Wrong channel type!"), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
                return;
            }
            boolean advanced = root.get("advanced").getAsBoolean();
            JsonObject connectorObject = root.get("connector").getAsJsonObject();
            boolean advancedNeeded = connectorObject.get("advancedneeded").getAsBoolean();

            BlockPos blockPos = sidedPos.getPos();
            Direction facing = sidedPos.getSide();

            Direction side = Direction.byName(connectorObject.get("side").getAsString());
            Direction facingOverride = connectorObject.has("facingoverride") ? Direction.byName(connectorObject.get("facingoverride").getAsString()) : side;
            boolean infoAdvanced = ConnectorBlock.isAdvancedConnector(world, blockPos.offset(facing));
            if (advanced) {
                if (!infoAdvanced) {
                    // If advanced is desired but our actual connector is not advanced then we give a penalty. The penalty is big
                    // if we can't match with the actual side or if we actually need advanced
                    if (advancedNeeded || !facingOverride.equals(facing)) {
                        XNetMessages.INSTANCE.sendTo(new PacketControllerError("Advanced connector is needed!"), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
                        return;
                    }
                }
            }
            if (!infoAdvanced) {
                // Remove the facingoverride
                connectorObject.remove("facingoverride");
            }

            ConnectorInfo info = createConnector(channel, sidedPos);
            info.getConnectorSettings().readFromJson(connectorObject);
        } catch (JsonSyntaxException e) {
            XNetMessages.INSTANCE.sendTo(new PacketControllerError("Error pasting clipboard data!"), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        }

        markAsDirty();

    }

    private static class PossibleConnection {
        private final JsonObject connector;
        private List<Pair<ConnectedBlockInfo, Integer>> sortedMatches;

        public PossibleConnection(JsonObject connector, List<Pair<ConnectedBlockInfo, Integer>> sortedMatches) {
            this.connector = connector;
            this.sortedMatches = sortedMatches;
        }
    }

    private void pasteChannel(ServerPlayerEntity player, int channel, String json) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(json).getAsJsonObject();
            if (!root.has("channel") || !root.has("type") || !root.has("name")) {
                XNetMessages.INSTANCE.sendTo(new PacketControllerError("Invalid channel json!"), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
                return;
            }
            String typeId = root.get("type").getAsString();
            IChannelType type = XNet.xNetApi.findType(typeId);
            channels[channel] = new ChannelInfo(type);
            channels[channel].setChannelName(root.get("name").getAsString());
            channels[channel].getChannelSettings().readFromJson(root.get("channel").getAsJsonObject());
            channels[channel].setEnabled(false);

            // Find all blocks connected to this controller. We'll try to match and paste the json data
            // to these blocks in the best way possible
            Set<ConnectedBlockInfo> connectedBlocks = findConnectedBlocks();

            // We try to paste the best matches first. If there are any connectors in the clip that can't
            // be pasted we'll give a warning to the user
            boolean notEnoughConnectors = false;

            // First scan all connectors from the json clip and from that make a list of possible connections
            // on the in-world blocks
            JsonArray connectors = root.get("connectors").getAsJsonArray();
            List<PossibleConnection> connections = new ArrayList<>();
            for (JsonElement con : connectors) {
                JsonObject connector = con.getAsJsonObject();

                // Fetch some useful information from the connector as it is defined in the json. Basically we
                // need the connector name, wether or not the original connector (we copied from) was advanced
                // and also the name of the block we were connecting too
                String name = connector.get("name").getAsString();
                boolean advanced = connector.get("advanced").getAsBoolean();
                ResourceLocation block = connector.has("block") ? new ResourceLocation(connector.get("block").getAsString()) : null;

                // Also get some useful settings from the connector data itself. Using these we can estimate a
                // matching score to see how well the destination connector matches with this one
                JsonObject connectorSettings = connector.get("connector").getAsJsonObject();
                Direction side = Direction.byName(connectorSettings.get("side").getAsString());
                Direction facingOverride = connectorSettings.has("facingoverride") ? Direction.byName(connectorSettings.get("facingoverride").getAsString()) : side;

                // 'advancedNeeded' is true if the connector settings are such that they only work in an advanced connector. This
                // is unrelated to the actual 'side' differing from the side that is set in the connector (only advanced connectors
                // can change that) as it is still possible that in the pasted setup the side happens to be at the right side and
                // then we don't need an advanced connector
                boolean advancedNeeded = connectorSettings.get("advancedneeded").getAsBoolean();

                // Given these desired settings from the json connector we calculate a sorted list of connection
                // candidates. If there are good candidates in this list we remember this sorted list and
                // add it to our list of possible connections
                List<Pair<ConnectedBlockInfo, Integer>> sortedMatches = connectedBlocks.stream()
                        .map(info -> Pair.of(info, calculateMatchingScore(type, info, name, block, side, facingOverride, advanced, advancedNeeded)))
                        .sorted((p1, p2) -> Integer.compare(p2.getRight(), p1.getRight()))
                        .collect(Collectors.toList());
                if (!sortedMatches.isEmpty() && sortedMatches.get(0).getRight() > -50) {
                    connections.add(new PossibleConnection(connector, sortedMatches));
                } else {
                    notEnoughConnectors = true;
                }
            }

            // Now we go over all possible connections by always selecting the best one from the list. The best
            // possible connection is one that has a connection candidate with the highest score on its first spot
            // (remember that the list of connection candidates is itself sorted with the highest scored candidates
            // in front of the list)
            while (!connections.isEmpty()) {
                connections.sort((p1, p2) -> Integer.compare(p2.sortedMatches.get(0).getRight(), p1.sortedMatches.get(0).getRight()));

                // Get rid of the highest priority one at the front of the list
                // This is the best match we have at this moment
                PossibleConnection pair = connections.remove(0);
                JsonObject connector = pair.connector;
                if (pair.sortedMatches.isEmpty()) {
                    // This connector has no more valid candidates so that means all remaining connectors
                    // are bad as well. We just stop here
                    notEnoughConnectors = true;
                    break;
                }

                // 'info' refers to the real block on this local network
                ConnectedBlockInfo info = pair.sortedMatches.get(0).getKey();
                boolean infoAdvanced = ConnectorBlock.isAdvancedConnector(world, info.getPos().getPos());

                JsonObject connectorSettings = connector.get("connector").getAsJsonObject();
                if (!infoAdvanced) {
                    // Remove the facingoverride because this is not an advanced connector so it doesn't
                    // support different facings
                    connectorSettings.remove("facingoverride");
                }

                // Actually create the connector and paste the connector settings
                ResourceLocation block = connector.has("block") ? new ResourceLocation(connector.get("block").getAsString()) : null;
                System.out.println("Pasting " + info.getName() + " (" + block.toString() + " into " + info.getConnectedState().getBlock().getRegistryName().toString() + ") with score = " + pair.sortedMatches.get(0).getRight());
                ConnectorInfo connectorInfo = createConnector(channel, info.getPos());
                connectorInfo.getConnectorSettings().readFromJson(connectorSettings);

                // Remove the connected block info we just used from all remaining connection proposals
                for (PossibleConnection connection : connections) {
                    List<Pair<ConnectedBlockInfo, Integer>> newMatches = new ArrayList<>();
                    for (Pair<ConnectedBlockInfo, Integer> match : connection.sortedMatches) {
                        if (match.getLeft() != info) {
                            newMatches.add(match);
                        }
                    }
                    connection.sortedMatches = newMatches;
                }
            }

            if (notEnoughConnectors) {
                XNetMessages.INSTANCE.sendTo(new PacketControllerError("Not everything could be pasted!"), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
            }
        } catch (JsonSyntaxException e) {
            XNetMessages.INSTANCE.sendTo(new PacketControllerError("Error pasting clipboard data!"), player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        }

        markAsDirty();
    }


    @Override
    public boolean execute(PlayerEntity player, String command, TypedMap params) {
        boolean rc = super.execute(player, command, params);
        if (rc) {
            return true;
        }
        ServerPlayerEntity playerMP = (ServerPlayerEntity) player;
        if (CMD_CREATECHANNEL.equals(command)) {
            int index = params.get(PARAM_INDEX);
            String typeId = params.get(PARAM_TYPE);
            createChannel(index, typeId);
            return true;
        } else if (CMD_PASTECHANNEL.equals(command)) {
            int index = params.get(PARAM_INDEX);
            String json = params.get(PARAM_JSON);
            pasteChannel(playerMP, index, json);
            return true;
        } else if (CMD_PASTECONNECTOR.equals(command)) {
            int index = params.get(PARAM_INDEX);
            SidedPos pos = new SidedPos(params.get(PARAM_POS), OrientationTools.DIRECTION_VALUES[params.get(PARAM_SIDE)]);
            String json = params.get(PARAM_JSON);
            pasteConnector(playerMP, index, pos, json);
            return true;
        } else if (CMD_COPYCHANNEL.equals(command)) {
            int index = params.get(PARAM_INDEX);
            copyChannel(playerMP, index);
            return true;
        } else if (CMD_COPYCONNECTOR.equals(command)) {
            int index = params.get(PARAM_INDEX);
            SidedPos pos = new SidedPos(params.get(PARAM_POS), OrientationTools.DIRECTION_VALUES[params.get(PARAM_SIDE)]);
            copyConnector(playerMP, index, pos);
            return true;
        } else if (CMD_CREATECONNECTOR.equals(command)) {
            int channel = params.get(PARAM_CHANNEL);
            SidedPos pos = new SidedPos(params.get(PARAM_POS), OrientationTools.DIRECTION_VALUES[params.get(PARAM_SIDE)]);
            createConnector(channel, pos);
            return true;
        } else if (CMD_REMOVECHANNEL.equals(command)) {
            int index = params.get(PARAM_INDEX);
            removeChannel(index);
            return true;
        } else if (CMD_REMOVECONNECTOR.equals(command)) {
            SidedPos pos = new SidedPos(params.get(PARAM_POS), OrientationTools.DIRECTION_VALUES[params.get(PARAM_SIDE)]);
            int channel = params.get(PARAM_CHANNEL);
            removeConnector(channel, pos);
            return true;
        } else if (CMD_UPDATECONNECTOR.equals(command)) {
            SidedPos pos = new SidedPos(params.get(PARAM_POS), OrientationTools.DIRECTION_VALUES[params.get(PARAM_SIDE)]);
            int channel = params.get(PARAM_CHANNEL);
            updateConnector(channel, pos, params);
            return true;
        } else if (CMD_UPDATECHANNEL.equals(command)) {
            int channel = params.get(PARAM_CHANNEL);
            updateChannel(channel, params);
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
            return type.convert(findChannelInfo());
        } else if (CMD_GETCONNECTEDBLOCKS.equals(command)) {
            return type.convert(findConnectedBlocksForClient());
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
            GuiController.fromServer_channels = new ArrayList<>(Type.create(ChannelClientInfo.class).convert(list));
            return true;
        } else if (CLIENTCMD_CONNECTEDBLOCKSREADY.equals(command)) {
            GuiController.fromServer_connectedBlocks = new ArrayList<>(Type.create(ConnectedBlockClientInfo.class).convert(list));
            return true;
        }
        return false;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        findNeighbourConnector(world, pos);
    }

    @Override
    public void onReplaced(World world, BlockPos pos, BlockState state) {
        super.onReplaced(world, pos, state);
        XNetBlobData blobData = XNetBlobData.getBlobData(this.world);
        WorldBlob worldBlob = blobData.getWorldBlob(this.world);
        worldBlob.removeCableSegment(pos);
        blobData.save();
    }

    // @todo 1.14
//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//
//        WorldBlob worldBlob = XNetBlobData.getBlobData(world).getWorldBlob(world);
//
//        NetworkId networkId = getNetworkId();
//        if (networkId != null) {
//            if (mode == ProbeMode.DEBUG) {
//                probeInfo.text(TextStyleClass.LABEL + "Network: " + TextStyleClass.INFO + networkId.getId() + ", V: " +
//                        worldBlob.getNetworkVersion(networkId));
//            } else {
//                probeInfo.text(TextStyleClass.LABEL + "Network: " + TextStyleClass.INFO + networkId.getId());
//            }
//        }
//
//        if (mode == ProbeMode.DEBUG) {
//            String s = "";
//            for (NetworkId id : getNetworkChecker().getAffectedNetworks()) {
//                s += id.getId() + " ";
//                if (s.length() > 15) {
//                    probeInfo.text(TextStyleClass.LABEL + "InfNet: " + TextStyleClass.INFO + s);
//                    s = "";
//                }
//            }
//            if (!s.isEmpty()) {
//                probeInfo.text(TextStyleClass.LABEL + "InfNet: " + TextStyleClass.INFO + s);
//            }
//        }
//        if (inError()) {
//            probeInfo.text(TextStyleClass.ERROR + "Too many controllers on network!");
//        }
//
//        if (mode == ProbeMode.DEBUG) {
//            BlobId blobId = worldBlob.getBlobAt(data.getPos());
//            if (blobId != null) {
//                probeInfo.text(TextStyleClass.LABEL + "Blob: " + TextStyleClass.INFO + blobId.getId());
//            }
//            ColorId colorId = worldBlob.getColorAt(data.getPos());
//            if (colorId != null) {
//                probeInfo.text(TextStyleClass.LABEL + "Color: " + TextStyleClass.INFO + colorId.getId());
//            }
//
//            probeInfo.text(TextStyleClass.LABEL + "Color mask: " + colors);
//        }
//    }

    @Override
    public BlockState getActualState(BlockState state) {
        // @todo 1.14?
        return state.with(ERROR, inError());
    }

    @Override
    public void checkRedstone(World world, BlockPos pos) {
        // We abuse the redstone check for something else
        if (!world.isRemote) {
            findNeighbourConnector(world, pos);
        }
    }

    // Check neighbour blocks for a connector and inherit the color from that
    private void findNeighbourConnector(World world, BlockPos pos) {
        if (world.isRemote) {
            return;
        }
        XNetBlobData blobData = XNetBlobData.getBlobData(world);
        WorldBlob worldBlob = blobData.getWorldBlob(world);
        ColorId oldColor = worldBlob.getColorAt(pos);
        ColorId newColor = null;
        for (Direction facing : OrientationTools.DIRECTION_VALUES) {
            if (world.getBlockState(pos.offset(facing)).getBlock() instanceof ConnectorBlock) {
                ColorId color = worldBlob.getColorAt(pos.offset(facing));
                if (color != null) {
                    if (color == oldColor) {
                        return; // Nothing to do
                    }
                    newColor = color;
                }
            }
        }
        if (newColor != null) {
            if (worldBlob.getBlobAt(pos) != null) {
                worldBlob.removeCableSegment(pos);
            }
            NetworkId networkId = worldBlob.newNetwork();
            worldBlob.createNetworkProvider(pos, newColor, networkId);
            blobData.save();

            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityController) {
                ((TileEntityController) te).setNetworkId(networkId);
            }
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
//        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
//            return itemHandler.cast();
//        }
        if (cap == CapabilityEnergy.ENERGY) {
            return energyHandler.cast();
        }
        if (cap == CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY) {
            return screenHandler.cast();
        }
        return super.getCapability(cap, facing);
    }
}
