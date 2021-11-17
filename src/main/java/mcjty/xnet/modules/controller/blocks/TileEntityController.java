package mcjty.xnet.modules.controller.blocks;

import com.google.gson.*;
import mcjty.lib.api.container.DefaultContainerProvider;
import mcjty.lib.blockcommands.Command;
import mcjty.lib.blockcommands.ServerCommand;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.NoDirectionItemHander;
import mcjty.lib.tileentity.Cap;
import mcjty.lib.tileentity.CapType;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.Cached;
import mcjty.lib.varia.OrientationTools;
import mcjty.rftoolsbase.api.xnet.channels.IChannelSettings;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import mcjty.rftoolsbase.api.xnet.channels.IControllerContext;
import mcjty.rftoolsbase.api.xnet.keys.ConsumerId;
import mcjty.rftoolsbase.api.xnet.keys.NetworkId;
import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import mcjty.rftoolsbase.api.xnet.keys.SidedPos;
import mcjty.rftoolsbase.modules.filter.items.FilterModuleItem;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.xnet.XNet;
import mcjty.xnet.client.ChannelClientInfo;
import mcjty.xnet.client.ConnectedBlockClientInfo;
import mcjty.xnet.client.ConnectorClientInfo;
import mcjty.xnet.client.ConnectorInfo;
import mcjty.xnet.compat.XNetTOPDriver;
import mcjty.xnet.logic.LogicTools;
import mcjty.xnet.modules.cables.CableModule;
import mcjty.xnet.modules.cables.blocks.ConnectorBlock;
import mcjty.xnet.modules.cables.blocks.ConnectorTileEntity;
import mcjty.xnet.modules.controller.ChannelInfo;
import mcjty.xnet.modules.controller.ConnectedBlockInfo;
import mcjty.xnet.modules.controller.ControllerModule;
import mcjty.xnet.modules.controller.KnownUnsidedBlocks;
import mcjty.xnet.modules.controller.client.GuiController;
import mcjty.xnet.modules.controller.network.PacketControllerError;
import mcjty.xnet.modules.controller.network.PacketJsonToClipboard;
import mcjty.xnet.multiblock.*;
import mcjty.xnet.setup.Config;
import mcjty.xnet.setup.XNetMessages;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkDirection;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static mcjty.lib.container.ContainerFactory.CONTAINER_CONTAINER;
import static mcjty.lib.container.SlotDefinition.specific;
import static mcjty.xnet.modules.controller.ChannelInfo.MAX_CHANNELS;
import static mcjty.xnet.modules.controller.ControllerModule.TYPE_CONTROLLER;

public final class TileEntityController extends GenericTileEntity implements ITickableTileEntity, IControllerContext {

    public static final String CMD_GETCHANNELS = "getChannelInfo";
    public static final String CLIENTCMD_CHANNELSREADY = "channelsReady";
    public static final String CMD_GETCONNECTEDBLOCKS = "getConnectedBlocks";
    public static final String CLIENTCMD_CONNECTEDBLOCKSREADY = "connectedBlocksReady";

    public static final Key<Integer> PARAM_INDEX = new Key<>("index", Type.INTEGER);
    public static final Key<String> PARAM_TYPE = new Key<>("type", Type.STRING);
    public static final Key<String> PARAM_JSON = new Key<>("json", Type.STRING);
    public static final Key<Integer> PARAM_CHANNEL = new Key<>("channel", Type.INTEGER);
    public static final Key<Integer> PARAM_SIDE = new Key<>("side", Type.INTEGER);
    public static final Key<BlockPos> PARAM_POS = new Key<>("pos", Type.BLOCKPOS);

    public static final BooleanProperty ERROR = BooleanProperty.create("error");

    public static final int SLOT_FILTER = 0;
    public static final int FILTER_SLOTS = 4;

    private final Predicate<ItemStack> filterCaches[] = new Predicate[FILTER_SLOTS];

    public static final Lazy<ContainerFactory> CONTAINER_FACTORY = Lazy.of(() -> new ContainerFactory(FILTER_SLOTS)
            .box(specific(s -> s.getItem() instanceof FilterModuleItem),
                    CONTAINER_CONTAINER, SLOT_FILTER, 17, 5, FILTER_SLOTS, 1)
            .playerSlots(91, 157));

    private NetworkId networkId;

    private final ChannelInfo[] channels = new ChannelInfo[MAX_CHANNELS];
    private int colors = 0;

    // Cached/transient data
    private final Map<SidedConsumer, IConnectorSettings> cachedConnectors[] = new Map[MAX_CHANNELS];
    private final Map<SidedConsumer, IConnectorSettings> cachedRoutedConnectors[] = new Map[MAX_CHANNELS];
    private final Map<WirelessChannelKey, Integer> wirelessVersions = new HashMap<>();

    @Cap(type = CapType.ITEMS_AUTOMATION)
    private final NoDirectionItemHander items = createItemHandler();

    private final GenericEnergyStorage energyStorage = new GenericEnergyStorage(this, true, Config.controllerMaxRF.get(), Config.controllerRfPerTick.get());
    @Cap(type = CapType.CONTAINER)
    private final LazyOptional<GenericEnergyStorage> energyHandler = LazyOptional.of(() -> energyStorage);
    @Cap(type = CapType.CONTAINER)
    private final LazyOptional<INamedContainerProvider> screenHandler = LazyOptional.of(() -> new DefaultContainerProvider<GenericContainer>("Controller")
            .containerSupplier((windowId,player) -> new GenericContainer(ControllerModule.CONTAINER_CONTROLLER.get(), windowId, CONTAINER_FACTORY.get(), getBlockPos(), TileEntityController.this))
            .itemHandler(() -> items)
            .energyHandler(() -> energyStorage));

    private final Cached<NetworkChecker> networkChecker = Cached.of(this::createNetworkChecker);

    public TileEntityController() {
        super(TYPE_CONTROLLER.get());
        for (int i = 0; i < MAX_CHANNELS; i++) {
            channels[i] = null;
        }
    }

    @Nonnull
    @Override
    public Predicate<ItemStack> getIndexedFilter(int idx) {
        if (idx < 0 || idx >= FILTER_SLOTS) {
            return stack -> false;
        }
        if (filterCaches[idx] == null) {
            ItemStack stack = items.getStackInSlot(idx);
            if (stack.getItem() instanceof FilterModuleItem) {
                filterCaches[idx] = FilterModuleItem.getCache(stack);
            } else {
                filterCaches[idx] = s -> false;
            }
        }
        return filterCaches[idx];
    }

    public static BaseBlock createBlock() {
        return new BaseBlock(new BlockBuilder()
                .topDriver(XNetTOPDriver.DRIVER)
                .tileEntitySupplier(TileEntityController::new)
                .manualEntry(ManualHelper.create("xnet:simple/controller"))
                .info(TooltipBuilder.key("message.xnet.shiftmessage"))
                .infoShift(TooltipBuilder.header())
        ) {
            @Override
            protected void createBlockStateDefinition(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
                super.createBlockStateDefinition(builder);
                builder.add(ERROR);
            }
        };
    }

    private NetworkChecker createNetworkChecker() {
        NetworkChecker checker = new NetworkChecker();
        checker.add(networkId);
        WorldBlob worldBlob = XNetBlobData.get(level).getWorldBlob(level);
        LogicTools.routers(level, networkId)
                .forEach(router -> {
                    checker.add(worldBlob.getNetworksAt(router.getBlockPos()));
                    // We're only interested in one network. The other router networks are all same topology
                    NetworkId routerNetwork = worldBlob.getNetworkAt(router.getBlockPos());
                    if (routerNetwork != null) {
                        LogicTools.routers(level, routerNetwork)
                                .filter(r -> router != r)
                                .forEach(r -> LogicTools.connectors(level, r.getBlockPos())
                                        .forEach(connectorPos -> checker.add(worldBlob.getNetworkAt(connectorPos))));
                    }
                });
        return checker;
    }

    @Override
    public World getControllerWorld() {
        return level;
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
        networkChecker.clear();
        this.networkId = networkId;
        markDirtyQuick();
    }

    public ChannelInfo[] getChannels() {
        return channels;
    }

    public Cached<NetworkChecker> getNetworkChecker() {
        return networkChecker;
    }

    private void checkNetwork(WorldBlob worldBlob) {
        if (networkId != null && networkChecker.get().isDirtyAndMarkClean(worldBlob)) {
            cleanCaches();
            return;
        }

        // Check wireless
        for (Map.Entry<WirelessChannelKey, Integer> entry : wirelessVersions.entrySet()) {
            XNetWirelessChannels channels = XNetWirelessChannels.get(level);
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

    public int getColors() {
        return colors;
    }

    @Override
    public void tick() {
        if (!level.isClientSide) {
            WorldBlob worldBlob = XNetBlobData.get(level).getWorldBlob(level);

            BlockState state = level.getBlockState(worldPosition);
            if (worldBlob.getNetworksAt(getBlockPos()).size() > 1) {
                if (!state.getValue(ERROR)) {
                    level.setBlock(worldPosition, state.setValue(ERROR, true), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
                }
                return;
            } else {
                if (state.getValue(ERROR)) {
                    level.setBlock(worldPosition, state.setValue(ERROR, false), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
                }
            }

            checkNetwork(worldBlob);

            if (!checkAndConsumeRF(Config.controllerRFT.get())) {
                return;
            }

            boolean dirty = false;
            int newcolors = 0;
            for (int i = 0; i < MAX_CHANNELS; i++) {
                if (channels[i] != null && channels[i].isEnabled()) {
                    if (checkAndConsumeRF(Config.controllerChannelRFT.get())) {
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
        if (rft > 0) {
            if (energyStorage.getEnergy() < rft) {
                // Not enough energy
                return false;
            }
            energyStorage.consumeEnergy(rft);
            markDirtyQuick();
        }
        return true;
    }

    private void networkDirty() {
        if (networkId != null) {
            XNetBlobData.get(level).getWorldBlob(level).markNetworkDirty(networkId);
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
            WorldBlob worldBlob = XNetBlobData.get(level).getWorldBlob(level);
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
                LogicTools.routers(level, networkId)
                        .forEach(router -> router.addRoutedConnectors(cachedRoutedConnectors[channel], getBlockPos(),
                                channel, channels[channel].getType(),
                                wirelessVersions));
            }
        }
        return cachedRoutedConnectors[channel];
    }

    @Nonnull
    @Override
    public CompoundNBT save(@Nonnull CompoundNBT tagCompound) {
        if (networkId != null) {
            tagCompound.putInt("networkId", networkId.getId());
        }
        return super.save(tagCompound);
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
        WorldBlob worldBlob = XNetBlobData.get(level).getWorldBlob(level);
        return findConsumerPosition(worldBlob, consumerId);
    }

    @Nullable
    private BlockPos findConsumerPosition(@Nonnull WorldBlob worldBlob, @Nonnull ConsumerId consumerId) {
        return worldBlob.getConsumerPosition(consumerId);
    }

    @Override
    public List<SidedPos> getConnectedBlockPositions() {
        WorldBlob worldBlob = XNetBlobData.get(level).getWorldBlob(level);

        List<SidedPos> result = new ArrayList<>();
        Set<ConnectedBlockClientInfo> set = new HashSet<>();
        Stream<BlockPos> consumers = getConsumerStream(worldBlob);
        consumers.forEach(consumerPos -> {
            String name = "";
            TileEntity te = level.getBlockEntity(consumerPos);
            if (te instanceof ConnectorTileEntity) {
                // Should always be the case. @todo error?
                name = ((ConnectorTileEntity) te).getConnectorName();
            } else {
                XNet.setup.getLogger().warn("What? The connector at " + BlockPosTools.toString(consumerPos) + " is not a connector?");
            }
            for (Direction facing : OrientationTools.DIRECTION_VALUES) {
                if (ConnectorBlock.isConnectable(level, consumerPos, facing)) {
                    BlockPos pos = consumerPos.relative(facing);
                    SidedPos sidedPos = new SidedPos(pos, facing.getOpposite());
                    result.add(sidedPos);
                }
            }
        });

        return result;
    }

    @Nonnull
    private List<ConnectedBlockClientInfo> findConnectedBlocksForClient() {
        WorldBlob worldBlob = XNetBlobData.get(level).getWorldBlob(level);

        Set<ConnectedBlockClientInfo> set = new HashSet<>();
        Stream<BlockPos> consumers = getConsumerStream(worldBlob);
        consumers.forEach(consumerPos -> {
            String name = "";
            TileEntity te = level.getBlockEntity(consumerPos);
            if (te instanceof ConnectorTileEntity) {
                // Should always be the case. @todo error?
                name = ((ConnectorTileEntity) te).getConnectorName();
            } else {
                XNet.setup.getLogger().warn("What? The connector at " + BlockPosTools.toString(consumerPos) + " is not a connector?");
            }
            for (Direction facing : OrientationTools.DIRECTION_VALUES) {
                if (ConnectorBlock.isConnectable(level, consumerPos, facing)) {
                    BlockPos pos = consumerPos.relative(facing);
                    SidedPos sidedPos = new SidedPos(pos, facing.getOpposite());
                    BlockState state = level.getBlockState(pos);
                    ItemStack item = state.getBlock().getCloneItemStack(level, pos, state);
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
                .map(provider -> provider.getConsumers(level, worldBlob, getNetworkId()).stream())
                .flatMap(s -> s);
    }

    @Nonnull
    private List<ChannelClientInfo> findChannelInfo() {
        WorldBlob worldBlob = XNetBlobData.get(level).getWorldBlob(level);

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
                            SidedPos pos = new SidedPos(consumerPos.relative(sidedConsumer.getSide()), sidedConsumer.getSide().getOpposite());
                            boolean advanced = level.getBlockState(consumerPos).getBlock() == CableModule.ADVANCED_CONNECTOR.get();
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
        WorldBlob worldBlob = XNetBlobData.get(level).getWorldBlob(level);
        ConsumerId consumerId = worldBlob.getConsumerAt(pos.getPos().relative(pos.getSide()));
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
        WorldBlob worldBlob = XNetBlobData.get(level).getWorldBlob(level);
        ConsumerId consumerId = worldBlob.getConsumerAt(pos.getPos().relative(pos.getSide()));
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
        WorldBlob worldBlob = XNetBlobData.get(level).getWorldBlob(level);
        BlockPos consumerPos = pos.getPos().relative(pos.getSide());
        ConsumerId consumerId = worldBlob.getConsumerAt(consumerPos);
        if (consumerId == null) {
            throw new RuntimeException("What?");
        }
        SidedConsumer id = new SidedConsumer(consumerId, pos.getSide().getOpposite());
        boolean advanced = level.getBlockState(consumerPos).getBlock() == CableModule.ADVANCED_CONNECTOR.get();
        ConnectorInfo info = channels[channel].createConnector(id, advanced);
        markAsDirty();
        return info;
    }

    private IConnectorSettings findConnectorSettings(ChannelInfo channel, SidedPos p) {
        WorldBlob worldBlob = XNetBlobData.get(level).getWorldBlob(level);

        for (Map.Entry<SidedConsumer, ConnectorInfo> entry : channel.getConnectors().entrySet()) {
            SidedConsumer sidedConsumer = entry.getKey();
            ConnectorInfo info = entry.getValue();
            if (info.getConnectorSettings() != null) {
                BlockPos consumerPos = findConsumerPosition(worldBlob, sidedConsumer.getConsumerId());
                if (consumerPos != null) {
                    SidedPos pos = new SidedPos(consumerPos.relative(sidedConsumer.getSide()), sidedConsumer.getSide().getOpposite());
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
        WorldBlob worldBlob = XNetBlobData.get(level).getWorldBlob(level);

        Set<ConnectedBlockInfo> set = new HashSet<>();
        Stream<BlockPos> consumers = getConsumerStream(worldBlob);
        consumers.forEach(consumerPos -> {
            String name = "";
            TileEntity te = level.getBlockEntity(consumerPos);
            if (te instanceof ConnectorTileEntity) {
                // Should always be the case. @todo error?
                name = ((ConnectorTileEntity) te).getConnectorName();
            } else {
                XNet.setup.getLogger().warn("What? The connector at " + BlockPosTools.toString(consumerPos) + " is not a connector?");
            }
            for (Direction facing : OrientationTools.DIRECTION_VALUES) {
                if (ConnectorBlock.isConnectable(level, consumerPos, facing)) {
                    BlockPos pos = consumerPos.relative(facing);
                    SidedPos sidedPos = new SidedPos(pos, facing.getOpposite());
                    BlockState state = level.getBlockState(pos);
                    state = state.getBlock().isAir(state, level, pos) ? null : state;
                    ConnectedBlockInfo info = new ConnectedBlockInfo(sidedPos, state, name);
                    set.add(info);
                }
            }
        });
        return set;
    }

    private void copyConnector(PlayerEntity player, int index, SidedPos sidedPos) {
        ChannelInfo channel = channels[index];
        IChannelSettings settings = channel.getChannelSettings();
        JsonObject parent = new JsonObject();
        IConnectorSettings connectorSettings = findConnectorSettings(channel, sidedPos);
        if (connectorSettings != null) {
            JsonObject object = connectorSettings.writeToJson();
            if (object != null) {
                parent.add("type", new JsonPrimitive(channel.getType().getID()));
                parent.add("connector", object);
                boolean advanced = ConnectorBlock.isAdvancedConnector(level, sidedPos.getPos().relative(sidedPos.getSide()));
                parent.add("advanced", new JsonPrimitive(advanced));

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(parent);

                XNetMessages.INSTANCE.sendTo(new PacketJsonToClipboard(json), ((ServerPlayerEntity)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                return;
            }
        }
        XNetMessages.INSTANCE.sendTo(new PacketControllerError("Error copying connector!"), ((ServerPlayerEntity)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    private void copyChannel(PlayerEntity player, int index) {
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
                        boolean advanced = ConnectorBlock.isAdvancedConnector(level, sidedPos.getPos().relative(sidedPos.getSide()));
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

            XNetMessages.INSTANCE.sendTo(new PacketJsonToClipboard(json), ((ServerPlayerEntity)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        } else {
            XNetMessages.INSTANCE.sendTo(new PacketControllerError("Channel does not support this!"), ((ServerPlayerEntity)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
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
        if (!type.supportsBlock(level, blockPos, facing)) {
            score -= 1000;
        }

        ResourceLocation infoBlock = info.getConnectedState().getBlock().getRegistryName();

        // If the side doesn't match we give a bad penalty
        if (!KnownUnsidedBlocks.isUnsided(infoBlock) && !facingOverride.equals(facing)) {
            score -= 1000;
        }

        boolean infoAdvanced = ConnectorBlock.isAdvancedConnector(level, blockPos.relative(facing));
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

    private void pasteConnector(PlayerEntity player, int channel, SidedPos sidedPos, String json) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(json).getAsJsonObject();

            if (!root.has("connector") || !root.has("type")) {
                XNetMessages.INSTANCE.sendTo(new PacketControllerError("Invalid connector json!"), ((ServerPlayerEntity)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                return;
            }

            String typeId = root.get("type").getAsString();
            IChannelType type = XNet.xNetApi.findType(typeId);
            if (type != channels[channel].getType()) {
                XNetMessages.INSTANCE.sendTo(new PacketControllerError("Wrong channel type!"), ((ServerPlayerEntity)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                return;
            }
            boolean advanced = root.get("advanced").getAsBoolean();
            JsonObject connectorObject = root.get("connector").getAsJsonObject();
            boolean advancedNeeded = connectorObject.get("advancedneeded").getAsBoolean();

            BlockPos blockPos = sidedPos.getPos();
            Direction facing = sidedPos.getSide();

            Direction side = Direction.byName(connectorObject.get("side").getAsString());
            Direction facingOverride = connectorObject.has("facingoverride") ? Direction.byName(connectorObject.get("facingoverride").getAsString()) : side;
            boolean infoAdvanced = ConnectorBlock.isAdvancedConnector(level, blockPos.relative(facing));
            if (advanced) {
                if (!infoAdvanced) {
                    // If advanced is desired but our actual connector is not advanced then we give a penalty. The penalty is big
                    // if we can't match with the actual side or if we actually need advanced
                    if (advancedNeeded || !facingOverride.equals(facing)) {
                        XNetMessages.INSTANCE.sendTo(new PacketControllerError("Advanced connector is needed!"), ((ServerPlayerEntity)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
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
            XNetMessages.INSTANCE.sendTo(new PacketControllerError("Error pasting clipboard data!"), ((ServerPlayerEntity)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
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

    private void pasteChannel(PlayerEntity player, int channel, String json) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(json).getAsJsonObject();
            if (!root.has("channel") || !root.has("type") || !root.has("name")) {
                XNetMessages.INSTANCE.sendTo(new PacketControllerError("Invalid channel json!"), ((ServerPlayerEntity)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
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
                boolean infoAdvanced = ConnectorBlock.isAdvancedConnector(level, info.getPos().getPos());

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
                XNetMessages.INSTANCE.sendTo(new PacketControllerError("Not everything could be pasted!"), ((ServerPlayerEntity)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            }
        } catch (JsonSyntaxException e) {
            XNetMessages.INSTANCE.sendTo(new PacketControllerError("Error pasting clipboard data!"), ((ServerPlayerEntity)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }

        markAsDirty();
    }

    @ServerCommand
    public static final Command<?> CMD_CREATECONNECTOR = Command.<TileEntityController>create("controller.createConnector",
        (te, player, params) -> te.createConnector(params.get(PARAM_CHANNEL), new SidedPos(params.get(PARAM_POS), OrientationTools.DIRECTION_VALUES[params.get(PARAM_SIDE)])));
    @ServerCommand
    public static final Command<?> CMD_REMOVECONNECTOR = Command.<TileEntityController>create("controller.removeConnector",
        (te, player, params) -> te.removeConnector(params.get(PARAM_CHANNEL), new SidedPos(params.get(PARAM_POS), OrientationTools.DIRECTION_VALUES[params.get(PARAM_SIDE)])));
    @ServerCommand
    public static final Command<?> CMD_UPDATECONNECTOR = Command.<TileEntityController>create("controller.updateConnector",
        (te, player, params) -> te.updateConnector(params.get(PARAM_CHANNEL), new SidedPos(params.get(PARAM_POS), OrientationTools.DIRECTION_VALUES[params.get(PARAM_SIDE)]), params));
    @ServerCommand
    public static final Command<?> CMD_CREATECHANNEL = Command.<TileEntityController>create("controller.createChannel",
        (te, player, params) -> te.createChannel(params.get(PARAM_INDEX), params.get(PARAM_TYPE)));
    @ServerCommand
    public static final Command<?> CMD_PASTECHANNEL = Command.<TileEntityController>create("controller.pasteChannel",
        (te, player, params) -> te.pasteChannel(player, params.get(PARAM_INDEX), params.get(PARAM_JSON)));
    @ServerCommand
    public static final Command<?> CMD_COPYCHANNEL = Command.<TileEntityController>create("controller.copyChannel",
        (te, player, params) -> te.copyChannel(player, params.get(PARAM_INDEX)));
    @ServerCommand
    public static final Command<?> CMD_PASTECONNECTOR = Command.<TileEntityController>create("controller.pasteConnector",
        (te, player, params) -> te.pasteConnector(player, params.get(PARAM_INDEX), new SidedPos(params.get(PARAM_POS), OrientationTools.DIRECTION_VALUES[params.get(PARAM_SIDE)]), params.get(PARAM_JSON)));
    @ServerCommand
    public static final Command<?> CMD_COPYCONNECTOR = Command.<TileEntityController>create("controller.copyConnector",
        (te, player, params) -> te.copyConnector(player, params.get(PARAM_INDEX), new SidedPos(params.get(PARAM_POS), OrientationTools.DIRECTION_VALUES[params.get(PARAM_SIDE)])));
    @ServerCommand
    public static final Command<?> CMD_REMOVECHANNEL = Command.<TileEntityController>create("controller.removeChannel",
        (te, player, params) -> te.removeChannel(params.get(PARAM_INDEX)));
    @ServerCommand
    public static final Command<?> CMD_UPDATECHANNEL = Command.<TileEntityController>create("controller.updateChannel",
        (te, player, params) -> te.updateChannel(params.get(PARAM_CHANNEL), params));

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
    public void onReplaced(World world, BlockPos pos, BlockState state, BlockState newstate) {
        if (state.getBlock() == newstate.getBlock()) {
            return;
        }
        XNetBlobData blobData = XNetBlobData.get(this.level);
        WorldBlob worldBlob = blobData.getWorldBlob(this.level);
        worldBlob.removeCableSegment(pos);
        blobData.save();
    }

    @Override
    public void checkRedstone(World world, BlockPos pos) {
        // We abuse the redstone check for something else
        if (!world.isClientSide) {
            findNeighbourConnector(world, pos);
        }
    }

    // Check neighbour blocks for a connector and inherit the color from that
    private void findNeighbourConnector(World world, BlockPos pos) {
        if (world.isClientSide) {
            return;
        }
        XNetBlobData blobData = XNetBlobData.get(world);
        WorldBlob worldBlob = blobData.getWorldBlob(world);
        ColorId oldColor = worldBlob.getColorAt(pos);
        ColorId newColor = null;
        for (Direction facing : OrientationTools.DIRECTION_VALUES) {
            if (world.getBlockState(pos.relative(facing)).getBlock() instanceof ConnectorBlock) {
                ColorId color = worldBlob.getColorAt(pos.relative(facing));
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

            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof TileEntityController) {
                ((TileEntityController) te).setNetworkId(networkId);
            }
        }
    }

    private NoDirectionItemHander createItemHandler() {
        return new NoDirectionItemHander(TileEntityController.this, CONTAINER_FACTORY.get()) {

            @Override
            protected void onUpdate(int index) {
                super.onUpdate(index);
                for (int i = 0 ; i < FILTER_SLOTS ; i++) {
                    filterCaches[i] = null;
                }
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.getItem() instanceof FilterModuleItem;
            }
        };
    }
}
