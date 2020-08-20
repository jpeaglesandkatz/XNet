package mcjty.xnet.setup;


import mcjty.lib.gui.GenericGuiContainer;
import mcjty.xnet.XNet;
import mcjty.xnet.client.RenderWorldLastEventHandler;
import mcjty.xnet.modules.cables.CableSetup;
import mcjty.xnet.modules.cables.client.CableModelLoader;
import mcjty.xnet.modules.cables.client.GuiConnector;
import mcjty.xnet.modules.controller.ControllerSetup;
import mcjty.xnet.modules.controller.client.GuiController;
import mcjty.xnet.modules.facade.FacadeSetup;
import mcjty.xnet.modules.facade.client.FacadeBlockColor;
import mcjty.xnet.modules.router.RouterSetup;
import mcjty.xnet.modules.router.client.GuiRouter;
import mcjty.xnet.modules.wireless.WirelessRouterSetup;
import mcjty.xnet.modules.wireless.client.GuiWirelessRouter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = XNet.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistration {

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        GenericGuiContainer.register(ControllerSetup.CONTAINER_CONTROLLER.get(), GuiController::new);
        GenericGuiContainer.register(RouterSetup.CONTAINER_ROUTER.get(), GuiRouter::new);
        GenericGuiContainer.register(WirelessRouterSetup.CONTAINER_WIRELESS_ROUTER.get(), GuiWirelessRouter::new);
        GenericGuiContainer.register(CableSetup.CONTAINER_CONNECTOR.get(), GuiConnector::new);
        MinecraftForge.EVENT_BUS.addListener(RenderWorldLastEventHandler::tick);
        RenderTypeLookup.setRenderLayer(WirelessRouterSetup.ANTENNA.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(WirelessRouterSetup.ANTENNA_DISH.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(WirelessRouterSetup.ANTENNA_BASE.get(), RenderType.getCutout());
        RenderTypeLookup.setRenderLayer(FacadeSetup.FACADE.get(), (RenderType) -> true);
        RenderTypeLookup.setRenderLayer(CableSetup.CONNECTOR.get(), (RenderType) -> true);
        RenderTypeLookup.setRenderLayer(CableSetup.ADVANCED_CONNECTOR.get(), (RenderType) -> true);
        Minecraft.getInstance().getBlockColors().register(new FacadeBlockColor(),
                FacadeSetup.FACADE.get(), CableSetup.CONNECTOR.get(), CableSetup.ADVANCED_CONNECTOR.get());
    }

    @SubscribeEvent
    public static void onModelLoad(ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(new ResourceLocation(XNet.MODID, "cableloader"), new CableModelLoader());
    }
}
