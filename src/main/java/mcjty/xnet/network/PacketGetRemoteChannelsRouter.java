package mcjty.xnet.network;

import mcjty.lib.network.ICommandHandler;
import mcjty.lib.network.TypedMapTools;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.xnet.blocks.router.TileEntityRouter;
import mcjty.xnet.clientinfo.ControllerChannelClientInfo;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class PacketGetRemoteChannelsRouter {

    protected BlockPos pos;
    protected TypedMap params;

    public PacketGetRemoteChannelsRouter() {
    }

    public PacketGetRemoteChannelsRouter(PacketBuffer buf) {
        pos = buf.readBlockPos();
        params = TypedMapTools.readArguments(buf);
    }

    public PacketGetRemoteChannelsRouter(BlockPos pos) {
        this.pos = pos;
        this.params = TypedMap.EMPTY;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        TypedMapTools.writeArguments(buf, params);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = ctx.getSender().getEntityWorld().getTileEntity(pos);
            ICommandHandler commandHandler = (ICommandHandler) te;
            List<ControllerChannelClientInfo> list = commandHandler.executeWithResultList(TileEntityRouter.CMD_GETREMOTECHANNELS, params, Type.create(ControllerChannelClientInfo.class));
            XNetMessages.INSTANCE.sendTo(new PacketRemoteChannelsRouterReady(pos, TileEntityRouter.CLIENTCMD_CHANNELSREMOTEREADY, list), ctx.getSender().connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        });
        ctx.setPacketHandled(true);
    }
}
