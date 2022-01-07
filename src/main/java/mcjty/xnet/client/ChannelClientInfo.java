package mcjty.xnet.client;

import mcjty.lib.blockcommands.ISerializer;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.OrientationTools;
import mcjty.rftoolsbase.api.xnet.channels.IChannelSettings;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.keys.ConsumerId;
import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import mcjty.xnet.XNet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

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
        public Function<FriendlyByteBuf, ChannelClientInfo> getDeserializer() {
            return buf -> {
                if (buf.readBoolean()) {
                    return new ChannelClientInfo(buf);
                } else {
                    return null;
                }
            };
        }

        @Override
        public BiConsumer<FriendlyByteBuf, ChannelClientInfo> getSerializer() {
            return (buf, info) -> {
                if (info == null) {
                    buf.writeBoolean(false);
                } else {
                    buf.writeBoolean(true);
                    info.writeToNBT(buf);
                }
            };
        }
    }

    public ChannelClientInfo(@Nonnull String channelName, @Nonnull IChannelType type, @Nonnull IChannelSettings channelSettings, boolean enabled) {
        this.channelName = channelName;
        this.type = type;
        this.channelSettings = channelSettings;
        this.enabled = enabled;
    }

    public ChannelClientInfo(@Nonnull FriendlyByteBuf buf) {
        channelName = NetworkTools.readStringUTF8(buf);
        enabled = buf.readBoolean();
        String id = buf.readUtf(32767);
        IChannelType t = XNet.xNetApi.findType(id);
        if (t == null) {
            throw new RuntimeException("Bad type: " + id);
        }
        type = t;
        channelSettings = type.createChannel();
        CompoundTag tag = buf.readNbt();
        channelSettings.readFromNBT(tag);

        int size = buf.readInt();
        for (int i = 0 ; i < size ; i++) {
            SidedConsumer key = new SidedConsumer(new ConsumerId(buf.readInt()), OrientationTools.DIRECTION_VALUES[buf.readByte()]);
            ConnectorClientInfo info = new ConnectorClientInfo(buf);
            connectors.put(key, info);
        }
    }

    public void writeToNBT(@Nonnull FriendlyByteBuf buf) {
        NetworkTools.writeStringUTF8(buf, channelName);
        buf.writeBoolean(enabled);
        buf.writeUtf(type.getID());
        CompoundTag tag = new CompoundTag();
        channelSettings.writeToNBT(tag);
        buf.writeNbt(tag);
        buf.writeInt(connectors.size());
        for (Map.Entry<SidedConsumer, ConnectorClientInfo> entry : connectors.entrySet()) {
            SidedConsumer key = entry.getKey();
            ConnectorClientInfo info = entry.getValue();

            buf.writeInt(key.consumerId().id());
            buf.writeByte(key.side().ordinal());

            info.writeToBuf(buf);
        }
    }

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
