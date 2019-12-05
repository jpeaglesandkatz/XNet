package mcjty.xnet.client;

import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.OrientationTools;
import mcjty.xnet.XNet;
import mcjty.rftoolsbase.api.xnet.channels.IChannelSettings;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.keys.ConsumerId;
import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to communicate channels/connectors to the client (GUI)
 */
public class ChannelClientInfo {

    @Nonnull private final IChannelType type;
    @Nonnull private final IChannelSettings channelSettings;
    @Nonnull private final String channelName;
    private final boolean enabled;

    private final Map<SidedConsumer, ConnectorClientInfo> connectors = new HashMap<>();

    public ChannelClientInfo(@Nonnull String channelName, @Nonnull IChannelType type, @Nonnull IChannelSettings channelSettings, boolean enabled) {
        this.channelName = channelName;
        this.type = type;
        this.channelSettings = channelSettings;
        this.enabled = enabled;
    }

    public ChannelClientInfo(@Nonnull PacketBuffer buf) {
        channelName = NetworkTools.readStringUTF8(buf);
        enabled = buf.readBoolean();
        String id = buf.readString(32767);
        IChannelType t = XNet.xNetApi.findType(id);
        if (t == null) {
            throw new RuntimeException("Bad type: " + id);
        }
        type = t;
        channelSettings = type.createChannel();
        CompoundNBT tag = buf.readCompoundTag();
        channelSettings.readFromNBT(tag);

        int size = buf.readInt();
        for (int i = 0 ; i < size ; i++) {
            SidedConsumer key = new SidedConsumer(new ConsumerId(buf.readInt()), OrientationTools.DIRECTION_VALUES[buf.readByte()]);
            ConnectorClientInfo info = new ConnectorClientInfo(buf);
            connectors.put(key, info);
        }
    }

    public void writeToNBT(@Nonnull PacketBuffer buf) {
        NetworkTools.writeStringUTF8(buf, channelName);
        buf.writeBoolean(enabled);
        buf.writeString(type.getID());
        CompoundNBT tag = new CompoundNBT();
        channelSettings.writeToNBT(tag);
        buf.writeCompoundTag(tag);
        buf.writeInt(connectors.size());
        for (Map.Entry<SidedConsumer, ConnectorClientInfo> entry : connectors.entrySet()) {
            SidedConsumer key = entry.getKey();
            ConnectorClientInfo info = entry.getValue();

            buf.writeInt(key.getConsumerId().getId());
            buf.writeByte(key.getSide().ordinal());

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
