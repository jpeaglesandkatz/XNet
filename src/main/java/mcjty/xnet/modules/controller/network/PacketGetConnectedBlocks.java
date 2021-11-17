package mcjty.xnet.modules.controller.network;

import mcjty.lib.network.TypedMapTools;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.xnet.client.ConnectedBlockClientInfo;
import mcjty.xnet.modules.controller.blocks.TileEntityController;
import mcjty.xnet.setup.XNetMessages;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class PacketGetConnectedBlocks {

    protected BlockPos pos;
    protected TypedMap params;

    public PacketGetConnectedBlocks() {
    }

    public PacketGetConnectedBlocks(PacketBuffer buf) {
        pos = buf.readBlockPos();
        params = TypedMapTools.readArguments(buf);
    }

    public PacketGetConnectedBlocks(BlockPos pos) {
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
            World world = ctx.getSender().getCommandSenderWorld();
            if (world.hasChunkAt(pos)) {
                TileEntity te = world.getBlockEntity(pos);
                if (te instanceof GenericTileEntity) {
                    List<ConnectedBlockClientInfo> list = ((GenericTileEntity) te).executeServerCommandList(TileEntityController.CMD_GETCONNECTEDBLOCKS.getName(), ctx.getSender(), params, ConnectedBlockClientInfo.class);
                    XNetMessages.INSTANCE.sendTo(new PacketConnectedBlocksReady(pos, TileEntityController.CMD_GETCONNECTEDBLOCKS.getName(), list), ctx.getSender().connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
