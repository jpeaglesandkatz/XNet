package mcjty.xnet.network;

import mcjty.lib.network.NetworkTools;
import mcjty.xnet.modules.controller.client.GuiController;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;


public class PacketControllerError {

    private String error;

    public void toBytes(PacketBuffer buf) {
        NetworkTools.writeStringUTF8(buf, error);
    }

    public PacketControllerError() {
    }

    public PacketControllerError(PacketBuffer buf) {
        error = NetworkTools.readStringUTF8(buf);
    }

    public PacketControllerError(String error) {
        this.error = error;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            GuiController.showError(error);
        });
        ctx.setPacketHandled(true);
    }
}
