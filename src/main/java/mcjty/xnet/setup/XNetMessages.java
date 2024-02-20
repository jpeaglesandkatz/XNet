package mcjty.xnet.setup;

import mcjty.lib.network.IPayloadRegistrar;
import mcjty.lib.network.Networking;
import mcjty.xnet.XNet;
import mcjty.xnet.modules.controller.network.PacketControllerError;
import mcjty.xnet.modules.controller.network.PacketJsonToClipboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;

public class XNetMessages {

    private static IPayloadRegistrar registrar;

    public static void registerMessages() {
        registrar = Networking.registrar(XNet.MODID)
                .versioned("1.0")
                .optional();

        registrar.play(PacketJsonToClipboard.class, PacketJsonToClipboard::create, handler -> handler.client(PacketJsonToClipboard::handle));
        registrar.play(PacketControllerError.class, PacketControllerError::create, handler -> handler.client(PacketControllerError::handle));
    }

    public static <T> void sendToPlayer(T packet, Player player) {
        registrar.getChannel().sendTo(packet, ((ServerPlayer)player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <T> void sendToServer(T packet) {
        registrar.getChannel().sendToServer(packet);
    }
}
