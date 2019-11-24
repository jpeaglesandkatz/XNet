package mcjty.xnet.setup;


import mcjty.lib.gui.GenericGuiContainer;
import mcjty.xnet.XNet;
import mcjty.xnet.blocks.cables.GuiConnector;
import mcjty.xnet.blocks.cables.NetCableSetup;
import mcjty.xnet.blocks.controller.gui.GuiController;
import mcjty.xnet.blocks.router.GuiRouter;
import mcjty.xnet.blocks.wireless.GuiWirelessRouter;
import mcjty.xnet.init.ModBlocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = XNet.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistration {

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
//        InformationScreenRenderer.register();
        GenericGuiContainer.register(ModBlocks.CONTAINER_CONTROLLER, GuiController::new);
        GenericGuiContainer.register(ModBlocks.CONTAINER_ROUTER, GuiRouter::new);
        GenericGuiContainer.register(ModBlocks.CONTAINER_WIRELESS_ROUTER, GuiWirelessRouter::new);
        GenericGuiContainer.register(NetCableSetup.CONTAINER_CONNECTOR, GuiConnector::new);
        OBJLoader.INSTANCE.addDomain(XNet.MODID);
    }


//    @SubscribeEvent
//    public static void registerModels(ModelRegistryEvent event) {
//        ModItems.initModels();
//        ModBlocks.initModels();
//    }

}
