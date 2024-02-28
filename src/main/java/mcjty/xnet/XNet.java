package mcjty.xnet;


import mcjty.lib.datagen.DataGen;
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
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.api.distmarker.Dist;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.eventbus.api.IEventBus;
import net.neoforged.neoforge.fml.common.Mod;
import net.neoforged.neoforge.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.fml.loading.FMLEnvironment;

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
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        Dist dist = FMLEnvironment.dist;

        instance = this;
        setupModules(bus, dist);

        Config.register(modules);

        Registration.register(bus);

        bus.addListener(setup::init);
        bus.addListener(modules::init);
        bus.addListener(this::processIMC);
        bus.addListener(this::onDataGen);

        if (dist.isClient()) {
            bus.addListener(modules::initClient);
            bus.addListener(ClientSetup::registerBlockColor);
        }
    }

    public static <T extends Item> Supplier<T> tab(Supplier<T> supplier) {
        return instance.setup.tab(supplier);
    }

    private void onDataGen(GatherDataEvent event) {
        DataGen datagen = new DataGen(MODID, event);
        modules.datagen(datagen);
        datagen.generate();
    }

    private void setupModules(IEventBus bus, Dist dist) {
        modules.register(new CableModule(bus, dist));
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
