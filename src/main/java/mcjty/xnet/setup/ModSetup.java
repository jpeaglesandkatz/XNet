package mcjty.xnet.setup;

import mcjty.lib.compat.MainCompatHandler;
import mcjty.lib.setup.DefaultModSetup;
import mcjty.lib.varia.Logging;
import mcjty.xnet.XNet;
import mcjty.xnet.apiimpl.energy.EnergyChannelType;
import mcjty.xnet.apiimpl.fluids.FluidChannelType;
import mcjty.xnet.apiimpl.items.ItemChannelType;
import mcjty.xnet.apiimpl.logic.LogicChannelType;
import mcjty.xnet.compat.TopExtras;
import mcjty.xnet.modules.controller.ControllerSetup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModSetup extends DefaultModSetup {

    public static boolean rftoolsControl = false;

    public ModSetup() {
        createTab("xnet", () -> new ItemStack(ControllerSetup.CONTROLLER.get()));
    }

    @Override
    public void init(FMLCommonSetupEvent e) {
        super.init(e);

        DeferredWorkQueue.runLater(() -> {
        });

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandlers());
//        CommandHandler.registerCommands();

        XNetMessages.registerMessages("xnet");

        XNet.xNetApi.registerConsumerProvider((world, blob, net) -> blob.getConsumers(net));
        XNet.xNetApi.registerChannelType(new ItemChannelType());
        XNet.xNetApi.registerChannelType(new EnergyChannelType());
        XNet.xNetApi.registerChannelType(new FluidChannelType());
        XNet.xNetApi.registerChannelType(new LogicChannelType());
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
