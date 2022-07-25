package mcjty.xnet.modules.facade.client;

import mcjty.xnet.modules.cables.CableModule;
import mcjty.xnet.modules.facade.FacadeModule;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;

public class ClientSetup {

    public static void registerBlockColor(RegisterColorHandlersEvent.Block event) {
        event.register(new FacadeBlockColor(),
                FacadeModule.FACADE.get(), CableModule.CONNECTOR.get(), CableModule.ADVANCED_CONNECTOR.get());
    }
}
