package mcjty.xnet.network;

import mcjty.lib.McJtyLib;
import mcjty.lib.network.IClientCommandHandler;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.Logging;
import mcjty.xnet.client.ConnectedBlockClientInfo;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketConnectedBlocksReady {

    public BlockPos pos;
    public List<ConnectedBlockClientInfo> list;
    public String command;

    public PacketConnectedBlocksReady() {
    }

    public PacketConnectedBlocksReady(PacketBuffer buf) {
        pos = buf.readBlockPos();
        command = buf.readString(32767);

        int size = buf.readInt();
        if (size != -1) {
            list = new ArrayList<>(size);
            for (int i = 0 ; i < size ; i++) {
                ConnectedBlockClientInfo item = new ConnectedBlockClientInfo(buf);
                list.add(item);
            }
        } else {
            list = null;
        }
    }

    public PacketConnectedBlocksReady(BlockPos pos, String command, List<ConnectedBlockClientInfo> list) {
        this.pos = pos;
        this.command = command;
        this.list = new ArrayList<>();
        this.list.addAll(list);
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeString(command);

        if (list == null) {
            buf.writeInt(-1);
        } else {
            buf.writeInt(list.size());
            for (mcjty.xnet.client.ConnectedBlockClientInfo item : list) {
                item.writeToBuf(buf);
            }
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = McJtyLib.proxy.getClientWorld().getTileEntity(pos);
            IClientCommandHandler clientCommandHandler = (IClientCommandHandler) te;
            if (!clientCommandHandler.receiveListFromServer(command, list, Type.create(ConnectedBlockClientInfo.class))) {
                Logging.log("Command " + command + " was not handled!");
            }
        });
        ctx.setPacketHandled(true);
    }
}
