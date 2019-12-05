package mcjty.xnet;


import mcjty.lib.base.ModBase;
import mcjty.xnet.apiimpl.XNetApi;
import mcjty.xnet.client.ClientInfo;
import mcjty.xnet.compat.TOPSupport;
import mcjty.xnet.compat.WAILASupport;
import mcjty.xnet.modules.cables.CableSetup;
import mcjty.xnet.modules.controller.ControllerSetup;
import mcjty.xnet.modules.facade.FacadeSetup;
import mcjty.xnet.modules.router.RouterSetup;
import mcjty.xnet.modules.various.VariousSetup;
import mcjty.xnet.modules.various.client.GuiXNetManual;
import mcjty.xnet.modules.wireless.WirelessRouterSetup;
import mcjty.xnet.setup.Config;
import mcjty.xnet.setup.ModSetup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(XNet.MODID)
public class XNet implements ModBase {

    public static final String MODID = "xnet";
    public static final String MODNAME = "XNet";

    public static ModSetup setup = new ModSetup();

    public ClientInfo clientInfo = new ClientInfo();

    public static XNet instance;

    public static XNetApi xNetApi = new XNetApi();

    public XNet() {
        instance = this;

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);

        VariousSetup.register();
        ControllerSetup.register();
        RouterSetup.register();
        WirelessRouterSetup.register();
        FacadeSetup.register();
        CableSetup.register();

        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLCommonSetupEvent event) -> setup.init(event));

        Config.loadConfig(Config.CLIENT_CONFIG, FMLPaths.CONFIGDIR.get().resolve("xnet-client.toml"));
        Config.loadConfig(Config.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve("xnet-common.toml"));
    }

//    @Mod.EventHandler
//    public void serverLoad(FMLServerStartingEvent event) {
//        event.registerServerCommand(new CommandDump());
////        event.registerServerCommand(new CommandGen());
//        event.registerServerCommand(new CommandRebuild());
//        event.registerServerCommand(new CommandCheck());
//    }

    @Override
    public String getModId() {
        return MODID;
    }

    @Override
    public void openManual(PlayerEntity player, int bookIndex, String page) {
        GuiXNetManual.locatePage = page;
        // @todo 1.14
//        player.openGui(XNet.instance, bookIndex, player.getEntityWorld(), (int) player.posX, (int) player.posY, (int) player.posZ);
    }

    // @todo 1.14
//    @Mod.EventHandler
//    public void imcCallback(FMLInterModComms.IMCEvent event) {
//        for (FMLInterModComms.IMCMessage message : event.getMessages()) {
//            if (message.key.equalsIgnoreCase("getXNet")) {
//                Optional<Function<IXNet, Void>> value = message.getFunctionValue(IXNet.class, Void.class);
//                if (value.isPresent()) {
//                    value.get().apply(xNetApi);
//                } else {
//                    setup.getLogger().warn("Some mod didn't return a valid result with getXNet!");
//                }
//            }
//        }
//    }

    @Override
    public void handleTopExtras() {
        TOPSupport.registerTopExtras();
    }

    @Override
    public void handleWailaExtras() {
        WAILASupport.registerWailaExtras();
    }

    public static Item.Properties createStandardProperties() {
        return new Item.Properties().group(setup.getTab());
    }

}
