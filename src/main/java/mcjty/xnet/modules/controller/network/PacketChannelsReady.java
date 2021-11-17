package mcjty.xnet.modules.controller.network;

import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.xnet.client.ChannelClientInfo;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketChannelsReady {

    private BlockPos pos;
    private List<ChannelClientInfo> list;
    private String command;

    public PacketChannelsReady() {
    }

    public PacketChannelsReady(PacketBuffer buf) {
        pos = buf.readBlockPos();
        command = buf.readUtf(32767);

        int size = buf.readInt();
        if (size != -1) {
            list = new ArrayList<>(size);
            for (int i = 0 ; i < size ; i++) {
                ChannelClientInfo result;
                if (buf.readBoolean()) {
                    result = new ChannelClientInfo(buf);
                } else {
                    result = null;
                }
                ChannelClientInfo item = result;
                list.add(item);
            }
        } else {
            list = null;
        }
    }

    public PacketChannelsReady(BlockPos pos, String command, List<ChannelClientInfo> list) {
        this.pos = pos;
        this.command = command;
        this.list = new ArrayList<>();
        this.list.addAll(list);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            GenericTileEntity.executeClientCommandHelper(pos, command, list);
        });
        ctx.setPacketHandled(true);
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        buf.writeUtf(command);

        if (list == null) {
            buf.writeInt(-1);
        } else {
            buf.writeInt(list.size());
            for (ChannelClientInfo item : list) {
                if (item == null) {
                    buf.writeBoolean(false);
                } else {
                    buf.writeBoolean(true);
                    item.writeToNBT(buf);
                }
            }
        }
    }
}
