package mcjty.xnet.modules.controller.network;

import mcjty.lib.network.NetworkTools;
import mcjty.xnet.XNet;
import mcjty.xnet.modules.controller.client.GuiController;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;


public record PacketControllerError(String error) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(XNet.MODID, "controllererror");

    public static PacketControllerError create(FriendlyByteBuf buf) {
        return new PacketControllerError(NetworkTools.readStringUTF8(buf));
    }

    public static PacketControllerError create(String error) {
        return new PacketControllerError(error);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        NetworkTools.writeStringUTF8(buf, error);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            GuiController.showError(error);
        });
    }
}
