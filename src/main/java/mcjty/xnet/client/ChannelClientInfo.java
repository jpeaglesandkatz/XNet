package mcjty.xnet.client;

import mcjty.lib.blockcommands.ISerializer;
import mcjty.rftoolsbase.api.xnet.channels.IChannelSettings;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import mcjty.xnet.XNet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * This class is used to communicate channels/connectors to the client (GUI)
 */
public class ChannelClientInfo {

    @Nonnull private final IChannelType type;
    @Nonnull private final IChannelSettings channelSettings;
    @Nonnull private final String channelName;
    private final boolean enabled;

    private final Map<SidedConsumer, ConnectorClientInfo> connectors = new HashMap<>();

    public static class Serializer implements ISerializer<ChannelClientInfo> {
        @Override
        public Function<RegistryFriendlyByteBuf, ChannelClientInfo> getDeserializer() {
            return buf -> {
                if (buf.readBoolean()) {
                    return STREAM_CODEC.decode(buf);
                } else {
                    return null;
                }
            };
        }

        @Override
        public BiConsumer<RegistryFriendlyByteBuf, ChannelClientInfo> getSerializer() {
            return (buf, info) -> {
                if (info == null) {
                    buf.writeBoolean(false);
                } else {
                    buf.writeBoolean(true);
                    STREAM_CODEC.encode(buf, info);
                }
            };
        }
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ChannelClientInfo> STREAM_CODEC = StreamCodec.of(
            (buf, info) -> {
                buf.writeUtf(info.type.getID());
                StreamCodec<RegistryFriendlyByteBuf, IChannelSettings> streamCodec = (StreamCodec<RegistryFriendlyByteBuf, IChannelSettings>) info.type.getStreamCodec();
                streamCodec.encode(buf, info.channelSettings);
                buf.writeUtf(info.getChannelName());
                buf.writeBoolean(info.isEnabled());
                buf.writeInt(info.connectors.size());
                for (Map.Entry<SidedConsumer, ConnectorClientInfo> pair : info.connectors.entrySet()) {
                    SidedConsumer.STREAM_CODEC.encode(buf, pair.getKey());
                    ConnectorClientInfo.STREAM_CODEC.encode(buf, pair.getValue());
                }
            },
            buf -> {
                String id = buf.readUtf(32767);
                IChannelType type = XNet.xNetApi.findType(id);
                IChannelSettings settings = type.getStreamCodec().decode(buf);
                String name = buf.readUtf(32767);
                boolean enabled = buf.readBoolean();
                Map<SidedConsumer, ConnectorClientInfo> connectors = new HashMap<>();
                int size = buf.readInt();
                for (int i = 0 ; i < size ; i++) {
                    SidedConsumer key = SidedConsumer.STREAM_CODEC.decode(buf);
                    connectors.put(key, ConnectorClientInfo.STREAM_CODEC.decode(buf));
                }
                ChannelClientInfo info = new ChannelClientInfo(name, type, settings, enabled);
                info.connectors.clear();
                info.connectors.putAll(connectors);
                return info;
            }
    );

    public ChannelClientInfo(@Nonnull String channelName, @Nonnull IChannelType type, @Nonnull IChannelSettings channelSettings, boolean enabled) {
        this.channelName = channelName;
        this.type = type;
        this.channelSettings = channelSettings;
        this.enabled = enabled;
    }

//    public ChannelClientInfo(@Nonnull RegistryFriendlyByteBuf buf) {
//        channelName = NetworkTools.readStringUTF8(buf);
//        enabled = buf.readBoolean();
//        String id = buf.readUtf(32767);
//        IChannelType t = XNet.xNetApi.findType(id);
//        if (t == null) {
//            throw new RuntimeException("Bad type: " + id);
//        }
//        type = t;
//        StreamCodec<RegistryFriendlyByteBuf, IChannelSettings> codec = (StreamCodec<RegistryFriendlyByteBuf, IChannelSettings>) t.getStreamCodec();
//        channelSettings = codec.decode(buf);
//
//        int size = buf.readInt();
//        for (int i = 0 ; i < size ; i++) {
//            SidedConsumer key = new SidedConsumer(new ConsumerId(buf.readInt()), OrientationTools.DIRECTION_VALUES[buf.readByte()]);
//            ConnectorClientInfo info = new ConnectorClientInfo(buf);
//            connectors.put(key, info);
//        }
//    }

//    public void writeToNBT(@Nonnull RegistryFriendlyByteBuf buf) {
//        NetworkTools.writeStringUTF8(buf, channelName);
//        buf.writeBoolean(enabled);
//        buf.writeUtf(type.getID());
//        StreamCodec<RegistryFriendlyByteBuf, IChannelSettings> codec = (StreamCodec<RegistryFriendlyByteBuf, IChannelSettings>) channelSettings.getType().getStreamCodec();
//        codec.encode(buf, channelSettings);
//        buf.writeInt(connectors.size());
//        for (Map.Entry<SidedConsumer, ConnectorClientInfo> entry : connectors.entrySet()) {
//            SidedConsumer key = entry.getKey();
//            ConnectorClientInfo info = entry.getValue();
//
//            buf.writeInt(key.consumerId().id());
//            buf.writeByte(key.side().ordinal());
//
//            info.writeToBuf(buf);
//        }
//    }

    public boolean isEnabled() {
        return enabled;
    }

    @Nonnull
    public String getChannelName() {
        return channelName;
    }

    @Nonnull
    public IChannelType getType() {
        return type;
    }

    @Nonnull
    public IChannelSettings getChannelSettings() {
        return channelSettings;
    }

    @Nonnull
    public Map<SidedConsumer, ConnectorClientInfo> getConnectors() {
        return connectors;
    }
}
