package mcjty.xnet.modules.controller;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.OrientationTools;
import mcjty.rftoolsbase.api.xnet.channels.IChannelSettings;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import mcjty.rftoolsbase.api.xnet.keys.ConsumerId;
import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import mcjty.xnet.XNet;
import mcjty.xnet.client.ConnectorInfo;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class ChannelInfo {

    public static final int MAX_CHANNELS = 8;

    private final IChannelType type;
    private final IChannelSettings channelSettings;
    private String channelName;
    private boolean enabled = true;

    private final Map<SidedConsumer, ConnectorInfo> connectors = new HashMap<>();

    public static final ChannelInfo EMPTY = new ChannelInfo(XNet.setup.noneChannelType);

    private static final Codec<IChannelSettings> CHANNEL_SETTINGS_CODEC = Codec.lazyInitialized(() -> Codec.STRING.dispatch("type",
            e -> e.getType().getID(),
            s -> XNet.xNetApi.findType(s).getCodec()));

    public static final Codec<ChannelInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CHANNEL_SETTINGS_CODEC.fieldOf("settings").forGetter(ChannelInfo::getChannelSettings),
            Codec.STRING.fieldOf("name").forGetter(ChannelInfo::getChannelName),
            Codec.BOOL.fieldOf("enabled").forGetter(ChannelInfo::isEnabled),
            Codec.unboundedMap(SidedConsumer.CODEC, ConnectorInfo.CODEC).fieldOf("connectors").forGetter(ChannelInfo::getConnectors)
    ).apply(instance, ChannelInfo::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChannelInfo> STREAM_CODEC = StreamCodec.of(
            (buf, info) -> {
                buf.writeUtf(info.type.getID());
                StreamCodec<RegistryFriendlyByteBuf, IChannelSettings> streamCodec = (StreamCodec<RegistryFriendlyByteBuf, IChannelSettings>) info.type.getStreamCodec();
                StreamCodec<RegistryFriendlyByteBuf, IConnectorSettings> connectorStreamCodec = (StreamCodec<RegistryFriendlyByteBuf, IConnectorSettings>) info.type.getConnectorStreamCodec();
                streamCodec.encode(buf, info.channelSettings);
                buf.writeUtf(info.getChannelName());
                buf.writeBoolean(info.isEnabled());
                buf.writeInt(info.connectors.size());
                for (Map.Entry<SidedConsumer, ConnectorInfo> entry : info.connectors.entrySet()) {
                    ConnectorInfo connectorInfo = entry.getValue();
                    connectorStreamCodec.encode(buf, connectorInfo.getConnectorSettings());
                    SidedConsumer.STREAM_CODEC.encode(buf, entry.getKey());
                }
            },
            buf -> {
                String id = buf.readUtf(32767);
                IChannelType type = XNet.xNetApi.findType(id);
                IChannelSettings settings = type.getStreamCodec().decode(buf);
                String name = buf.readUtf(32767);
                boolean enabled = buf.readBoolean();
                Map<SidedConsumer, ConnectorInfo> connectorMap = new HashMap<>();
                int size = buf.readInt();
                for (int i = 0 ; i < size ; i++) {
                    IConnectorSettings connectorSettings = type.getConnectorStreamCodec().decode(buf);
                    SidedConsumer sidedConsumer = SidedConsumer.STREAM_CODEC.decode(buf);
                    ConnectorInfo connectorInfo = new ConnectorInfo(connectorSettings, sidedConsumer, false);
                    connectorMap.put(sidedConsumer, connectorInfo);
                }
                return new ChannelInfo(settings, name, enabled, connectorMap);
            }
    );

    public ChannelInfo(IChannelType type) {
        this.type = type;
        channelSettings = type.createChannel();
    }

    public ChannelInfo(IChannelSettings settings, String name, boolean enabled, Map<SidedConsumer, ConnectorInfo> connectors) {
        this.type = settings.getType();
        this.channelSettings = settings;
        this.channelName = name;
        this.enabled = enabled;
        this.connectors.putAll(connectors);
    }

    @Nonnull
    public String getChannelName() {
        return channelName == null ? "" : channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public IChannelType getType() {
        return type;
    }

    public IChannelSettings getChannelSettings() {
        return channelSettings;
    }

    public Map<SidedConsumer, ConnectorInfo> getConnectors() {
        return connectors;
    }

    public ConnectorInfo createConnector(SidedConsumer id, boolean advanced) {
        ConnectorInfo info = new ConnectorInfo(type, id, advanced);
        connectors.put(id, info);
        return info;
    }

    public void writeToNBT(CompoundTag tag) {
        channelSettings.writeToNBT(tag);
        tag.putBoolean("enabled", enabled);
        if (channelName != null && !channelName.isEmpty()) {
            tag.putString("name", channelName);
        }
        ListTag conlist = new ListTag();
        for (Map.Entry<SidedConsumer, ConnectorInfo> entry : connectors.entrySet()) {
            CompoundTag tc = new CompoundTag();
            ConnectorInfo connectorInfo = entry.getValue();
            connectorInfo.writeToNBT(tc);
            tc.putInt("consumerId", entry.getKey().consumerId().id());
            tc.putInt("side", entry.getKey().side().ordinal());
            tc.putString("type", connectorInfo.getType().getID());
            tc.putBoolean("advanced", connectorInfo.isAdvanced());
            conlist.add(tc);
        }
        tag.put("connectors", conlist);
    }

    public void readFromNBT(CompoundTag tag) {
        channelSettings.readFromNBT(tag);
        enabled = tag.getBoolean("enabled");
        if (tag.contains("name")) {
            channelName = tag.getString("name");
        } else {
            channelName = null;
        }
        ListTag conlist = tag.getList("connectors", Tag.TAG_COMPOUND);
        for (int i = 0 ; i < conlist.size() ; i++) {
            CompoundTag tc = conlist.getCompound(i);
            String id = tc.getString("type");
            IChannelType type = XNet.xNetApi.findType(id);
            if (type == null) {
                XNet.setup.getLogger().warn("Unsupported type " + id + "!");
                continue;
            }
            if (!getType().equals(type)) {
                XNet.setup.getLogger().warn("Trying to load a connector with non-matching type " + type + "!");
                continue;
            }
            ConsumerId consumerId = new ConsumerId(tc.getInt("consumerId"));
            Direction side = OrientationTools.DIRECTION_VALUES[tc.getInt("side")];
            SidedConsumer key = new SidedConsumer(consumerId, side);
            boolean advanced = tc.getBoolean("advanced");
            ConnectorInfo connectorInfo = new ConnectorInfo(type, key, advanced);
            connectorInfo.readFromNBT(tc);
            connectors.put(key, connectorInfo);
        }
    }
}
