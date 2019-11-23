package mcjty.xnet.setup;

import mcjty.lib.font.TrueTypeFont;
import mcjty.lib.setup.DefaultClientProxy;
import mcjty.xnet.RenderWorldLastEventHandler;
import mcjty.xnet.XNet;
import mcjty.xnet.blocks.generic.BakedModelLoader;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup extends DefaultClientProxy {

    public static TrueTypeFont font;

    public static void initClient(FMLClientSetupEvent e) {
        MinecraftForge.EVENT_BUS.register(new ClientSetup());
        OBJLoader.INSTANCE.addDomain(XNet.MODID);
        // @todo 1.14
//        ModelLoaderRegistry.registerLoader(new BakedModelLoader());
    }

//    @SubscribeEvent
//    public void colorHandlerEventBlock(ColorHandlerEvent.Block event) {
//        ModBlocks.initColorHandlers(event.getBlockColors());
//    }
//
//    @Override
//    public void postInit(FMLPostInitializationEvent e) {
//        super.postInit(e);
//        ModBlocks.initItemModels();
//    }

    @SubscribeEvent
    public void renderWorldLastEvent(RenderWorldLastEvent evt) {
        RenderWorldLastEventHandler.tick(evt);
    }
}
