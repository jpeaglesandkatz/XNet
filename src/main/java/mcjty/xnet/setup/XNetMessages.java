package mcjty.xnet.setup;

import mcjty.lib.network.ChannelBoundHandler;
import mcjty.lib.network.PacketHandler;
import mcjty.lib.network.PacketRequestDataFromServer;
import mcjty.xnet.XNet;
import mcjty.xnet.modules.controller.network.PacketControllerError;
import mcjty.xnet.modules.controller.network.PacketJsonToClipboard;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class XNetMessages {
    public static SimpleChannel INSTANCE;

    public static void registerMessages(String name) {

        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(XNet.MODID, name))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.registerMessage(id(), PacketJsonToClipboard.class, PacketJsonToClipboard::toBytes, PacketJsonToClipboard::new, PacketJsonToClipboard::handle);
        net.registerMessage(id(), PacketControllerError.class, PacketControllerError::toBytes, PacketControllerError::new, PacketControllerError::handle);

        net.registerMessage(id(), PacketRequestDataFromServer.class, PacketRequestDataFromServer::toBytes, PacketRequestDataFromServer::new, new ChannelBoundHandler<>(net, PacketRequestDataFromServer::handle));

        PacketHandler.registerStandardMessages(id(), net);
    }

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }
}
