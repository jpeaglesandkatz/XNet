package mcjty.xnet.modules.controller.network;

import mcjty.lib.network.AbstractPacketGetListFromServer;
import mcjty.lib.network.PacketSendResultToClient;
import mcjty.lib.typed.TypedMap;
import mcjty.xnet.client.ConnectedBlockClientInfo;
import mcjty.xnet.setup.XNetMessages;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.List;

public class PacketGetConnectedBlocks extends AbstractPacketGetListFromServer<ConnectedBlockClientInfo> {

    public PacketGetConnectedBlocks(PacketBuffer buf) {
        super(buf);
    }

    public PacketGetConnectedBlocks(BlockPos pos, String cmd) {
        super(pos, cmd, TypedMap.EMPTY);
    }

    @Override
    protected SimpleChannel getChannel() {
        return XNetMessages.INSTANCE;
    }

    @Override
    protected Class<ConnectedBlockClientInfo> getType() {
        return ConnectedBlockClientInfo.class;
    }

    @Override
    protected Object createReturnPacket(List<ConnectedBlockClientInfo> list) {
        return new PacketSendResultToClient(pos, command, list);
    }
}
