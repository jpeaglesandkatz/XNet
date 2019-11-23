package mcjty.xnet;


import mcjty.lib.base.ModBase;
import mcjty.xnet.apiimpl.XNetApi;
import mcjty.xnet.compat.TOPSupport;
import mcjty.xnet.compat.WAILASupport;
import mcjty.xnet.config.ConfigSetup;
import mcjty.xnet.items.manual.GuiXNetManual;
import mcjty.xnet.setup.ClientSetup;
import mcjty.xnet.setup.ModSetup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod(XNet.MODID)
public class XNet implements ModBase {

    public static final String MODID = "xnet";
    public static final String MODNAME = "XNet";

    public static final String MIN_FORGE11_VER = "13.19.0.2176";
    public static final String MIN_MCJTYLIB_VER = "3.5.0";
    public static final String MIN_RFTOOLS_VER = "7.50";

    public static ModSetup setup = new ModSetup();

    public ClientInfo clientInfo = new ClientInfo();

    public static XNet instance;

    public static XNetApi xNetApi = new XNetApi();

    public XNet() {
        instance = this;

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigSetup.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigSetup.COMMON_CONFIG);

        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLCommonSetupEvent event) -> setup.init(event));
        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLClientSetupEvent event) -> ClientSetup.initClient(event));

        ConfigSetup.loadConfig(ConfigSetup.CLIENT_CONFIG, FMLPaths.CONFIGDIR.get().resolve("xnet-client.toml"));
        ConfigSetup.loadConfig(ConfigSetup.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve("xnet-common.toml"));
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
}
