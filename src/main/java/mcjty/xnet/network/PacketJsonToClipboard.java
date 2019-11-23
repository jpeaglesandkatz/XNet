package mcjty.xnet.network;

import mcjty.lib.network.NetworkTools;
import mcjty.xnet.blocks.controller.gui.GuiController;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketJsonToClipboard {

    private String json;

    public void toBytes(PacketBuffer buf) {
        NetworkTools.writeStringUTF8(buf, json);
    }

    public PacketJsonToClipboard() {
    }

    public PacketJsonToClipboard(PacketBuffer buf) {
        json = NetworkTools.readStringUTF8(buf);
    }

    public PacketJsonToClipboard(String json) {
        this.json = json;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            GuiController.toClipboard(json);
        });
        ctx.setPacketHandled(true);
    }
}
