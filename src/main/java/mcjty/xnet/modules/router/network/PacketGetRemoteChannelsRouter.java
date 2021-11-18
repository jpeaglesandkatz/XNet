package mcjty.xnet.modules.router.network;

import mcjty.lib.network.AbstractPacketGetListFromServer;
import mcjty.lib.typed.TypedMap;
import mcjty.xnet.client.ControllerChannelClientInfo;
import mcjty.xnet.setup.XNetMessages;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.List;

public class PacketGetRemoteChannelsRouter extends AbstractPacketGetListFromServer<ControllerChannelClientInfo> {

    public PacketGetRemoteChannelsRouter(PacketBuffer buf) {
        super(buf);
    }

    public PacketGetRemoteChannelsRouter(BlockPos pos, String cmd) {
        super(pos, cmd, TypedMap.EMPTY);
    }

    @Override
    protected SimpleChannel getChannel() {
        return XNetMessages.INSTANCE;
    }

    @Override
    protected Class<ControllerChannelClientInfo> getType() {
        return ControllerChannelClientInfo.class;
    }

    @Override
    protected Object createReturnPacket(List<ControllerChannelClientInfo> list) {
        return new PacketRemoteChannelsRouterReady(pos, command, list);
    }
}
