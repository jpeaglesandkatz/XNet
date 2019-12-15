package mcjty.xnet.setup;


import com.google.common.collect.Lists;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.varia.Tools;
import mcjty.xnet.XNet;
import mcjty.xnet.client.RenderWorldLastEventHandler;
import mcjty.xnet.modules.cables.CableSetup;
import mcjty.xnet.modules.cables.client.GenericCableBakedModel;
import mcjty.xnet.modules.cables.client.GuiConnector;
import mcjty.xnet.modules.controller.ControllerSetup;
import mcjty.xnet.modules.controller.client.GuiController;
import mcjty.xnet.modules.router.RouterSetup;
import mcjty.xnet.modules.router.client.GuiRouter;
import mcjty.xnet.modules.wireless.WirelessRouterSetup;
import mcjty.xnet.modules.wireless.client.GuiWirelessRouter;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static mcjty.xnet.modules.cables.blocks.GenericCableBlock.*;

@Mod.EventBusSubscriber(modid = XNet.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistration {

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
//        InformationScreenRenderer.register();
        GenericGuiContainer.register(ControllerSetup.CONTAINER_CONTROLLER.get(), GuiController::new);
        GenericGuiContainer.register(RouterSetup.CONTAINER_ROUTER.get(), GuiRouter::new);
        GenericGuiContainer.register(WirelessRouterSetup.CONTAINER_WIRELESS_ROUTER.get(), GuiWirelessRouter::new);
        GenericGuiContainer.register(CableSetup.CONTAINER_CONNECTOR.get(), GuiConnector::new);
        OBJLoader.INSTANCE.addDomain(XNet.MODID);
        MinecraftForge.EVENT_BUS.addListener(RenderWorldLastEventHandler::tick);
    }


    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if (!event.getMap().getBasePath().equals("textures")) {
            return;
        }

        event.addSprite(new ResourceLocation(XNet.MODID, "block/connector_side"));

        for (int i = 0 ; i <= 4 ; i++) {
            event.addSprite(new ResourceLocation(XNet.MODID, "block/cable"+i+"/advanced_connector"));
            event.addSprite(new ResourceLocation(XNet.MODID, "block/cable"+i+"/connector"));
            event.addSprite(new ResourceLocation(XNet.MODID, "block/cable"+i+"/normal_corner_netcable"));
            event.addSprite(new ResourceLocation(XNet.MODID, "block/cable"+i+"/normal_cross_netcable"));
            event.addSprite(new ResourceLocation(XNet.MODID, "block/cable"+i+"/normal_end_netcable"));
            event.addSprite(new ResourceLocation(XNet.MODID, "block/cable"+i+"/normal_netcable"));
            event.addSprite(new ResourceLocation(XNet.MODID, "block/cable"+i+"/normal_none_netcable"));
            event.addSprite(new ResourceLocation(XNet.MODID, "block/cable"+i+"/normal_three_netcable"));
        }
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        GenericCableBakedModel model = new GenericCableBakedModel(DefaultVertexFormats.BLOCK);
        Lists.newArrayList("netcable", "connector", "advanced_connector", "facade").stream()
                .forEach(name -> {
                    ResourceLocation rl = new ResourceLocation(XNet.MODID, name);
                    event.getModelRegistry().put(new ModelResourceLocation(rl, ""), model);
                    Tools.permutateProperties(s -> event.getModelRegistry().put(new ModelResourceLocation(rl, s), model),
                            COLOR, DOWN, EAST, NORTH, SOUTH, UP, WEST);
                });
    }
}
