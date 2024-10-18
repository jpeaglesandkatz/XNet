package mcjty.xnet.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import mcjty.xnet.XNet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ConnectorInfo {

    private final IChannelType type;
    private final SidedConsumer id;
    private final IConnectorSettings connectorSettings;
    private final boolean advanced;

    private static final Codec<IConnectorSettings> CONNECTOR_SETTINGS_CODEC = Codec.lazyInitialized(() -> Codec.STRING.dispatch("type",
            e -> e.getType().getID(),
            s -> XNet.xNetApi.findType(s).getConnectorCodec()));

    public static final Codec<ConnectorInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CONNECTOR_SETTINGS_CODEC.fieldOf("settings").forGetter(ConnectorInfo::getConnectorSettings),
            SidedConsumer.CODEC.fieldOf("id").forGetter(ConnectorInfo::getId),
            Codec.BOOL.fieldOf("advanced").forGetter(ConnectorInfo::isAdvanced)
    ).apply(instance, ConnectorInfo::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ConnectorInfo> STREAM_CODEC = StreamCodec.of(
            (buf, info) -> {
                buf.writeUtf(info.type.getID());
                StreamCodec<RegistryFriendlyByteBuf, IConnectorSettings> streamCodec = (StreamCodec<RegistryFriendlyByteBuf, IConnectorSettings>) info.type.getConnectorStreamCodec();
                streamCodec.encode(buf, info.connectorSettings);
                SidedConsumer.STREAM_CODEC.encode(buf, info.id);
                buf.writeBoolean(info.isAdvanced());
            },
            buf -> {
                String id = buf.readUtf(32767);
                IChannelType type = XNet.xNetApi.findType(id);
                IConnectorSettings settings = type.getConnectorStreamCodec().decode(buf);
                SidedConsumer sidedConsumer = SidedConsumer.STREAM_CODEC.decode(buf);
                boolean advanced = buf.readBoolean();
                return new ConnectorInfo(settings, sidedConsumer, advanced);
            }
    );

    public ConnectorInfo(IConnectorSettings connectorSettings, SidedConsumer id, boolean advanced) {
        this.connectorSettings = connectorSettings;
        this.id = id;
        this.advanced = advanced;
        type = connectorSettings.getType();
    }

    public ConnectorInfo(IChannelType type, SidedConsumer id, boolean advanced) {
        this.type = type;
        this.id = id;
        this.advanced = advanced;
        connectorSettings = type.createConnector(id.side().getOpposite());
    }

    public IChannelType getType() {
        return type;
    }

    public boolean isAdvanced() {
        return advanced;
    }

    public IConnectorSettings getConnectorSettings() {
        return connectorSettings;
    }

    public SidedConsumer getId() {
        return id;
    }
}
