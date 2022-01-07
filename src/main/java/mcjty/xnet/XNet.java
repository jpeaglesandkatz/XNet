package mcjty.xnet;


import mcjty.lib.modules.Modules;
import mcjty.rftoolsbase.api.xnet.IXNet;
import mcjty.xnet.apiimpl.XNetApi;
import mcjty.xnet.modules.cables.CableModule;
import mcjty.xnet.modules.controller.ControllerModule;
import mcjty.xnet.modules.facade.FacadeModule;
import mcjty.xnet.modules.facade.client.ClientSetup;
import mcjty.xnet.modules.router.RouterModule;
import mcjty.xnet.modules.various.VariousModule;
import mcjty.xnet.modules.wireless.WirelessRouterModule;
import mcjty.xnet.setup.Config;
import mcjty.xnet.setup.ModSetup;
import mcjty.xnet.setup.Registration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.function.Function;
import java.util.function.Supplier;

@Mod(XNet.MODID)
public class XNet {

    public static final String MODID = "xnet";

    public static final ModSetup setup = new ModSetup();

    public static XNet instance;
    private final Modules modules = new Modules();

    public static final XNetApi xNetApi = new XNetApi();

    public XNet() {
        instance = this;
        setupModules();

        Config.register(modules);

        Registration.register();

        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();
        modbus.addListener(setup::init);
        modbus.addListener(modules::init);
        modbus.addListener(this::processIMC);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modbus.addListener(modules::initClient);
            modbus.addListener(ClientSetup::registerBlockColor);
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

    private void processIMC(final InterModProcessEvent event) {
        event.getIMCStream().forEach(message -> {
            if ("getXNet".equalsIgnoreCase(message.getMethod())) {
                Supplier<Function<IXNet, Void>> supplier = message.getMessageSupplier();
                supplier.get().apply(xNetApi);
            }
        });
    }

}
