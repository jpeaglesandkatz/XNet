package mcjty.xnet.modules.cables.client;

import mcjty.xnet.modules.cables.CableModule;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.MinecraftForge;

public class ClientSetup {
    public static void initClient() {
        ItemBlockRenderTypes.setRenderLayer(CableModule.CONNECTOR.get(), (RenderType) -> true);
        ItemBlockRenderTypes.setRenderLayer(CableModule.ADVANCED_CONNECTOR.get(), (RenderType) -> true);
        MinecraftForge.EVENT_BUS.addListener(CableWorldRenderer::tick);
    }

    public static void modelInit(ModelEvent.RegisterGeometryLoaders event) {
        CableModelLoader.register(event);
    }
}
