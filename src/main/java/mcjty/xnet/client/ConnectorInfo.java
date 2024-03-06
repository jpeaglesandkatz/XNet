package mcjty.xnet.client;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import net.minecraft.nbt.CompoundTag;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConnectorInfo {

    IChannelType type;
    SidedConsumer id;
    IConnectorSettings connectorSettings;
    boolean advanced;

    public ConnectorInfo(IChannelType type, SidedConsumer id, boolean advanced) {
        this.type = type;
        this.id = id;
        this.advanced = advanced;
        connectorSettings = type.createConnector(id.side().getOpposite());
    }

    public void writeToNBT(CompoundTag tag) {
        connectorSettings.writeToNBT(tag);
    }

    public void readFromNBT(CompoundTag tag) {
        connectorSettings.readFromNBT(tag);
    }
}
