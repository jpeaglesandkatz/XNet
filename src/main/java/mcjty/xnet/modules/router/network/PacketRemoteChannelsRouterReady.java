package mcjty.xnet.modules.router.network;

import mcjty.lib.network.AbstractPacketSendResultToClient;
import mcjty.xnet.client.ControllerChannelClientInfo;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class PacketRemoteChannelsRouterReady extends AbstractPacketSendResultToClient<ControllerChannelClientInfo> {

    public PacketRemoteChannelsRouterReady(PacketBuffer buf) {
        super(buf);
    }

    public PacketRemoteChannelsRouterReady(BlockPos pos, String command, List<ControllerChannelClientInfo> list) {
        super(pos, command, list);
    }

    @Override
    protected ControllerChannelClientInfo readElement(PacketBuffer buf) {
        if (buf.readBoolean()) {
            return new ControllerChannelClientInfo(buf);
        } else {
            return null;
        }
    }

    @Override
    protected void writeElement(PacketBuffer buf, ControllerChannelClientInfo element) {
        if (element == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            element.writeToNBT(buf);
        }
    }
}
