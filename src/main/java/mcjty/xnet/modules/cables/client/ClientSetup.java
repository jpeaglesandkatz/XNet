package mcjty.xnet.modules.cables.client;

import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.MinecraftForge;

public class ClientSetup {
    public static void initClient() {
        MinecraftForge.EVENT_BUS.addListener(CableWorldRenderer::tick);
    }

    public static void modelInit(ModelEvent.RegisterGeometryLoaders event) {
        CableModelLoader.register(event);
    }
}
