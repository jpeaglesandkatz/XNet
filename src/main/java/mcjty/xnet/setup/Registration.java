package mcjty.xnet.setup;


import mcjty.xnet.XNet;
import mcjty.xnet.modules.cables.CableSetup;
import mcjty.xnet.modules.controller.ControllerSetup;
import mcjty.xnet.modules.facade.FacadeSetup;
import mcjty.xnet.modules.router.RouterSetup;
import mcjty.xnet.modules.various.VariousSetup;
import mcjty.xnet.modules.wireless.WirelessRouterSetup;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = XNet.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Registration {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        VariousSetup.registerBlocks(event);
        ControllerSetup.registerBlocks(event);
        RouterSetup.registerBlocks(event);
        WirelessRouterSetup.registerBlocks(event);
        FacadeSetup.registerBlocks(event);
        CableSetup.registerBlocks(event);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        VariousSetup.registerItems(event);
        ControllerSetup.registerItems(event);
        RouterSetup.registerItems(event);
        WirelessRouterSetup.registerItems(event);
        FacadeSetup.registerItems(event);
        CableSetup.registerItems(event);
    }

    @SubscribeEvent
    public static void registerTiles(final RegistryEvent.Register<TileEntityType<?>> event) {
        VariousSetup.registerTiles(event);
        ControllerSetup.registerTiles(event);
        RouterSetup.registerTiles(event);
        WirelessRouterSetup.registerTiles(event);
        FacadeSetup.registerTiles(event);
        CableSetup.registerTiles(event);
    }

    @SubscribeEvent
    public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event) {
        VariousSetup.registerContainers(event);
        ControllerSetup.registerContainers(event);
        RouterSetup.registerContainers(event);
        WirelessRouterSetup.registerContainers(event);
        FacadeSetup.registerContainers(event);
        CableSetup.registerContainers(event);
    }
}