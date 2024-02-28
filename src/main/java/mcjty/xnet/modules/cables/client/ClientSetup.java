package mcjty.xnet.modules.cables.client;

import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.common.MinecraftForge;

public class ClientSetup {
    public static void initClient() {
        MinecraftForge.EVENT_BUS.addListener(CableWorldRenderer::tick);
    }

    public static void modelInit(ModelEvent.RegisterGeometryLoaders event) {
        CableModelLoader.register(event);
    }
}
