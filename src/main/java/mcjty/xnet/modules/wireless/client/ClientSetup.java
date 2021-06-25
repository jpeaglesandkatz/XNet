package mcjty.xnet.modules.wireless.client;

import mcjty.xnet.modules.wireless.WirelessRouterModule;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;

public class ClientSetup {
    public static void initClient() {
        RenderTypeLookup.setRenderLayer(WirelessRouterModule.ANTENNA.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(WirelessRouterModule.ANTENNA_DISH.get(), RenderType.cutout());
        RenderTypeLookup.setRenderLayer(WirelessRouterModule.ANTENNA_BASE.get(), RenderType.cutout());
    }
}
