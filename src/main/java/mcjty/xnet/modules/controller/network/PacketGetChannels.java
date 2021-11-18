package mcjty.xnet.modules.controller.network;

import mcjty.lib.network.AbstractPacketGetListFromServer;
import mcjty.lib.network.PacketSendResultToClient;
import mcjty.lib.typed.TypedMap;
import mcjty.xnet.client.ChannelClientInfo;
import mcjty.xnet.setup.XNetMessages;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.List;

public class PacketGetChannels extends AbstractPacketGetListFromServer<ChannelClientInfo> {

    public PacketGetChannels(PacketBuffer buf) {
        super(buf);
    }

    public PacketGetChannels(BlockPos pos, String cmd) {
        super(pos, cmd, TypedMap.EMPTY);
    }

    @Override
    protected SimpleChannel getChannel() {
        return XNetMessages.INSTANCE;
    }

    @Override
    protected Class<ChannelClientInfo> getType() {
        return ChannelClientInfo.class;
    }

    @Override
    protected Object createReturnPacket(List<ChannelClientInfo> list) {
        return new PacketSendResultToClient(pos, command, list);
    }
}
