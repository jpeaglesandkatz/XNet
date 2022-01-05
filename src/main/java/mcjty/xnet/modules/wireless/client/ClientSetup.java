package mcjty.xnet.modules.wireless.client;

import mcjty.xnet.modules.wireless.WirelessRouterModule;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;

public class ClientSetup {
    public static void initClient() {
        ItemBlockRenderTypes.setRenderLayer(WirelessRouterModule.ANTENNA.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(WirelessRouterModule.ANTENNA_DISH.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(WirelessRouterModule.ANTENNA_BASE.get(), RenderType.cutout());
    }
}
