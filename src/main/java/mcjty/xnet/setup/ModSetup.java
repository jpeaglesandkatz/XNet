package mcjty.xnet.setup;

import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.setup.DefaultModSetup;
import mcjty.lib.varia.Logging;
import mcjty.xnet.XNet;
import mcjty.xnet.apiimpl.energy.EnergyChannelType;
import mcjty.xnet.apiimpl.fluids.FluidChannelType;
import mcjty.xnet.apiimpl.items.ItemChannelType;
import mcjty.xnet.apiimpl.logic.LogicChannelType;
import mcjty.xnet.apiimpl.none.NoneChannelType;
import mcjty.xnet.compat.TopExtras;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModSetup extends DefaultModSetup {

    public static boolean rftoolsControl = false;

    public final ItemChannelType itemChannelType = new ItemChannelType();
    public final EnergyChannelType energyChannelType = new EnergyChannelType();
    public final FluidChannelType fluidChannelType = new FluidChannelType();
    public final LogicChannelType logicChannelType = new LogicChannelType();
    public final NoneChannelType noneChannelType = new NoneChannelType();

    @Override
    public void init(FMLCommonSetupEvent e) {
        super.init(e);

        NeoForge.EVENT_BUS.register(new ForgeEventHandlers());
//        CommandHandler.registerCommands();

        XNet.xNetApi.registerConsumerProvider((world, blob, net) -> blob.getConsumers(net));
        XNet.xNetApi.registerChannelType(itemChannelType);
        XNet.xNetApi.registerChannelType(energyChannelType);
        XNet.xNetApi.registerChannelType(fluidChannelType);
        XNet.xNetApi.registerChannelType(logicChannelType);
        XNet.xNetApi.registerChannelType(noneChannelType);
    }

    @Override
    protected void setupModCompat() {
        rftoolsControl = ModList.get().isLoaded("rftoolscontrol");

        if (rftoolsControl) {
            Logging.log("XNet Detected RFTools Control: enabling support");
            // @todo 1.14
//            FMLInterModComms.sendFunctionMessage("rftoolscontrol", "getOpcodeRegistry", "mcjty.xnet.compat.rftoolscontrol.RFToolsControlSupport$GetOpcodeRegistry");
        }

        MainCompatHandler.registerWaila();
        MainCompatHandler.registerTOP();

        if (ModList.get().isLoaded("theoneprobe")) {
            TopExtras.register();
        }
    }
}
