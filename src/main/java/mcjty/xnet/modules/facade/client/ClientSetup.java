package mcjty.xnet.modules.facade.client;

import mcjty.xnet.modules.cables.CableModule;
import mcjty.xnet.modules.facade.FacadeModule;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;

public class ClientSetup {

    public static void initClient() {
        ItemBlockRenderTypes.setRenderLayer(FacadeModule.FACADE.get(), (RenderType) -> true);
    }

    public static void registerBlockColor(RegisterColorHandlersEvent.Block event) {
        event.getBlockColors().register(new FacadeBlockColor(),
                FacadeModule.FACADE.get(), CableModule.CONNECTOR.get(), CableModule.ADVANCED_CONNECTOR.get());
    }
}
