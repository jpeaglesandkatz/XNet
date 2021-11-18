package mcjty.xnet.setup;

import mcjty.lib.network.ChannelBoundHandler;
import mcjty.lib.network.PacketHandler;
import mcjty.lib.network.PacketRequestDataFromServer;
import mcjty.xnet.XNet;
import mcjty.xnet.modules.controller.network.PacketControllerError;
import mcjty.xnet.modules.controller.network.PacketGetChannels;
import mcjty.xnet.modules.controller.network.PacketGetConnectedBlocks;
import mcjty.xnet.modules.controller.network.PacketJsonToClipboard;
import mcjty.xnet.modules.router.network.PacketGetLocalChannelsRouter;
import mcjty.xnet.modules.router.network.PacketGetRemoteChannelsRouter;
import mcjty.xnet.modules.router.network.PacketLocalChannelsRouterReady;
import mcjty.xnet.modules.router.network.PacketRemoteChannelsRouterReady;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

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

        // Server side
        net.registerMessage(id(), PacketGetChannels.class, PacketGetChannels::toBytes, PacketGetChannels::new, PacketGetChannels::handle);
        net.registerMessage(id(), PacketGetLocalChannelsRouter.class, PacketGetLocalChannelsRouter::toBytes, PacketGetLocalChannelsRouter::new, PacketGetLocalChannelsRouter::handle);
        net.registerMessage(id(), PacketGetRemoteChannelsRouter.class, PacketGetRemoteChannelsRouter::toBytes, PacketGetRemoteChannelsRouter::new, PacketGetRemoteChannelsRouter::handle);
        net.registerMessage(id(), PacketGetConnectedBlocks.class, PacketGetConnectedBlocks::toBytes, PacketGetConnectedBlocks::new, PacketGetConnectedBlocks::handle);

        // Client side
        net.registerMessage(id(), PacketLocalChannelsRouterReady.class, PacketLocalChannelsRouterReady::toBytes, PacketLocalChannelsRouterReady::new, PacketLocalChannelsRouterReady::handle);
        net.registerMessage(id(), PacketRemoteChannelsRouterReady.class, PacketRemoteChannelsRouterReady::toBytes, PacketRemoteChannelsRouterReady::new, PacketRemoteChannelsRouterReady::handle);
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
