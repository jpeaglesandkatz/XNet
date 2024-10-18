package mcjty.xnet.client;

import mcjty.lib.varia.OrientationTools;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import mcjty.rftoolsbase.api.xnet.keys.ConsumerId;
import mcjty.rftoolsbase.api.xnet.keys.SidedPos;
import mcjty.xnet.XNet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nonnull;

public class ConnectorClientInfo {

    /// The position of the block we are connecting too
    @Nonnull private final SidedPos pos;

    @Nonnull private final ConsumerId consumerId;

    @Nonnull private final IChannelType channelType;
    @Nonnull private final IConnectorSettings connectorSettings;

    public static final StreamCodec<RegistryFriendlyByteBuf, ConnectorClientInfo> STREAM_CODEC = StreamCodec.of(
            (buf, info) -> {
                buf.writeUtf(info.channelType.getID());
                StreamCodec<RegistryFriendlyByteBuf, IConnectorSettings> streamCodec = (StreamCodec<RegistryFriendlyByteBuf, IConnectorSettings>) info.channelType.getConnectorStreamCodec();
                streamCodec.encode(buf, info.connectorSettings);
                SidedPos.STREAM_CODEC.encode(buf, info.pos);
                buf.writeInt(info.consumerId.id());
            },
            buf -> {
                String id = buf.readUtf(32767);
                IChannelType type = XNet.xNetApi.findType(id);
                IConnectorSettings settings = type.getConnectorStreamCodec().decode(buf);
                SidedPos pos = SidedPos.STREAM_CODEC.decode(buf);
                ConsumerId consumerId = new ConsumerId(buf.readInt());
                return new ConnectorClientInfo(pos, consumerId, type, settings);
            }
    );


    public ConnectorClientInfo(@Nonnull SidedPos pos, @Nonnull ConsumerId consumerId,
                               @Nonnull IChannelType channelType,
                               @Nonnull IConnectorSettings connectorSettings) {
        this.pos = pos;
        this.consumerId = consumerId;
        this.channelType = channelType;
        this.connectorSettings = connectorSettings;
    }

    public ConnectorClientInfo(@Nonnull RegistryFriendlyByteBuf buf) {
        pos = new SidedPos(buf.readBlockPos(), OrientationTools.DIRECTION_VALUES[buf.readByte()]);
        consumerId = new ConsumerId(buf.readInt());
        IChannelType t = XNet.xNetApi.findType(buf.readUtf(32767));
        if (t == null) {
            throw new RuntimeException("Cannot happen!");
        }
        channelType = t;
        StreamCodec<RegistryFriendlyByteBuf, IConnectorSettings> codec = (StreamCodec<RegistryFriendlyByteBuf, IConnectorSettings>) channelType.getConnectorStreamCodec();
        connectorSettings = codec.decode(buf);
    }

    public void writeToBuf(@Nonnull RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos.pos());
        buf.writeByte(pos.side().ordinal());
        buf.writeInt(consumerId.id());
        buf.writeUtf(channelType.getID());
        StreamCodec<RegistryFriendlyByteBuf, IConnectorSettings> codec = (StreamCodec<RegistryFriendlyByteBuf, IConnectorSettings>) channelType.getConnectorStreamCodec();
        codec.encode(buf, connectorSettings);
    }

    @Nonnull
    public SidedPos getPos() {
        return pos;
    }

    @Nonnull
    public ConsumerId getConsumerId() {
        return consumerId;
    }

    @Nonnull
    public IConnectorSettings getConnectorSettings() {
        return connectorSettings;
    }
}
