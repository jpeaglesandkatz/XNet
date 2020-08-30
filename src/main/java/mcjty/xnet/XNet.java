package mcjty.xnet;


import mcjty.lib.modules.Modules;
import mcjty.xnet.apiimpl.XNetApi;
import mcjty.xnet.modules.cables.CableModule;
import mcjty.xnet.modules.controller.ControllerModule;
import mcjty.xnet.modules.facade.FacadeModule;
import mcjty.xnet.modules.router.RouterModule;
import mcjty.xnet.modules.various.VariousModule;
import mcjty.xnet.modules.wireless.WirelessRouterModule;
import mcjty.xnet.setup.Config;
import mcjty.xnet.setup.ModSetup;
import mcjty.xnet.setup.Registration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(XNet.MODID)
public class XNet {

    public static final String MODID = "xnet";

    public static ModSetup setup = new ModSetup();

    public static XNet instance;
    private Modules modules = new Modules();

    public static XNetApi xNetApi = new XNetApi();

    public XNet() {
        instance = this;
        setupModules();

        Config.register(modules);

        Registration.register();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(setup::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(modules::init);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(modules::initClient);
        });
    }

    private void setupModules() {
        modules.register(new CableModule());
        modules.register(new ControllerModule());
        modules.register(new FacadeModule());
        modules.register(new RouterModule());
        modules.register(new WirelessRouterModule());
        modules.register(new VariousModule());
    }
}
