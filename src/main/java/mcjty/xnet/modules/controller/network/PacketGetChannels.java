package mcjty.xnet.modules.controller.network;

import mcjty.lib.network.ICommandHandler;
import mcjty.lib.network.TypedMapTools;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.xnet.modules.controller.blocks.TileEntityController;
import mcjty.xnet.client.ChannelClientInfo;
import mcjty.xnet.setup.XNetMessages;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class PacketGetChannels {

    protected BlockPos pos;
    protected TypedMap params;

    public PacketGetChannels() {
    }

    public PacketGetChannels(PacketBuffer buf) {
        pos = buf.readBlockPos();
        params = TypedMapTools.readArguments(buf);
    }

    public PacketGetChannels(BlockPos pos) {
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
            List<ChannelClientInfo> list = commandHandler.executeWithResultList(TileEntityController.CMD_GETCHANNELS, params, Type.create(ChannelClientInfo.class));
            XNetMessages.INSTANCE.sendTo(new PacketChannelsReady(pos, TileEntityController.CLIENTCMD_CHANNELSREADY, list), ctx.getSender().connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        });
        ctx.setPacketHandled(true);
    }
}
