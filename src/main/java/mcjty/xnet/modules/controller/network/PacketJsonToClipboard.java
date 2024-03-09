package mcjty.xnet.modules.controller.network;

import mcjty.lib.network.NetworkTools;
import mcjty.xnet.XNet;
import mcjty.xnet.modules.controller.client.GuiController;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record PacketJsonToClipboard(String json) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(XNet.MODID, "jsontoclipboard");

    public static PacketJsonToClipboard create(FriendlyByteBuf buf) {
        return new PacketJsonToClipboard(NetworkTools.readStringUTF8(buf));
    }

    public static PacketJsonToClipboard create(String json) {
        return new PacketJsonToClipboard(json);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        NetworkTools.writeStringUTF8(buf, json);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public void handle(PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            GuiController.toClipboard(json);
        });
    }
}
