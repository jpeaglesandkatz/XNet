package mcjty.xnet.client;

import mcjty.lib.varia.OrientationTools;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import mcjty.rftoolsbase.api.xnet.keys.ConsumerId;
import mcjty.rftoolsbase.api.xnet.keys.SidedPos;
import mcjty.xnet.XNet;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nonnull;

public class ConnectorClientInfo {

    /// The position of the block we are connecting too
    @Nonnull private final SidedPos pos;

    @Nonnull private final ConsumerId consumerId;

    @Nonnull private final IChannelType channelType;
    @Nonnull private final IConnectorSettings connectorSettings;

    public ConnectorClientInfo(@Nonnull SidedPos pos, @Nonnull ConsumerId consumerId,
                               @Nonnull IChannelType channelType,
                               @Nonnull IConnectorSettings connectorSettings) {
        this.pos = pos;
        this.consumerId = consumerId;
        this.channelType = channelType;
        this.connectorSettings = connectorSettings;
    }

    public ConnectorClientInfo(@Nonnull PacketBuffer buf) {
        pos = new SidedPos(buf.readBlockPos(), OrientationTools.DIRECTION_VALUES[buf.readByte()]);
        consumerId = new ConsumerId(buf.readInt());
        IChannelType t = XNet.xNetApi.findType(buf.readUtf(32767));
        if (t == null) {
            throw new RuntimeException("Cannot happen!");
        }
        channelType = t;
        CompoundNBT tag = buf.readNbt();
        connectorSettings = channelType.createConnector(pos.getSide());
        connectorSettings.readFromNBT(tag);
    }

    public void writeToBuf(@Nonnull PacketBuffer buf) {
        buf.writeBlockPos(pos.getPos());
        buf.writeByte(pos.getSide().ordinal());
        buf.writeInt(consumerId.getId());
        buf.writeUtf(channelType.getID());
        CompoundNBT tag = new CompoundNBT();
        connectorSettings.writeToNBT(tag);
        buf.writeNbt(tag);
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
