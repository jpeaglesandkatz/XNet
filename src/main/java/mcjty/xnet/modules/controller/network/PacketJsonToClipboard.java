package mcjty.xnet.modules.controller.network;

import mcjty.lib.network.NetworkTools;
import mcjty.xnet.modules.controller.client.GuiController;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketJsonToClipboard {

    private String json;

    public void toBytes(FriendlyByteBuf buf) {
        NetworkTools.writeStringUTF8(buf, json);
    }

    public PacketJsonToClipboard() {
    }

    public PacketJsonToClipboard(FriendlyByteBuf buf) {
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
