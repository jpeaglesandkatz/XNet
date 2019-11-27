package mcjty.xnet.setup;


import com.google.common.collect.Lists;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.xnet.XNet;
import mcjty.xnet.blocks.cables.ConnectorType;
import mcjty.xnet.blocks.cables.GuiConnector;
import mcjty.xnet.blocks.cables.NetCableSetup;
import mcjty.xnet.blocks.controller.gui.GuiController;
import mcjty.xnet.blocks.generic.CableColor;
import mcjty.xnet.blocks.generic.GenericCableBakedModel;
import mcjty.xnet.blocks.router.GuiRouter;
import mcjty.xnet.blocks.wireless.GuiWirelessRouter;
import mcjty.xnet.init.ModBlocks;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
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


    @SubscribeEvent
    public static void onTextureStitch(TextureStitchEvent.Pre event) {
        if (!event.getMap().getBasePath().equals("textures")) {
            return;
        }

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
                    for (CableColor color : CableColor.VALUES) {
                        for (ConnectorType up : ConnectorType.VALUES) {
                            for (ConnectorType down : ConnectorType.VALUES) {
                                for (ConnectorType north : ConnectorType.VALUES) {
                                    for (ConnectorType south : ConnectorType.VALUES) {
                                        for (ConnectorType east : ConnectorType.VALUES) {
                                            for (ConnectorType west : ConnectorType.VALUES) {
                                                event.getModelRegistry().put(new ModelResourceLocation(rl,
                                                        "color=" + color.getName() +
                                                                ",down=" + down.getName() +
                                                                ",east=" + east.getName() +
                                                                ",north=" + north.getName() +
                                                                ",south=" + south.getName() +
                                                                ",up=" + up.getName() +
                                                                ",west=" + west.getName()), model);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
    }
//    }

}
