package mcjty.xnet.modules.controller.network;

import mcjty.xnet.XNet;
import mcjty.xnet.modules.controller.client.GuiController;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public record PacketControllerError(String error) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(XNet.MODID, "controllererror");
    public static final CustomPacketPayload.Type<PacketControllerError> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, PacketControllerError> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PacketControllerError::error,
            PacketControllerError::new);

    public static PacketControllerError create(String error) {
        return new PacketControllerError(error);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            GuiController.showError(error);
        });
    }
}
