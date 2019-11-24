package mcjty.xnet.setup;


import mcjty.xnet.blocks.cables.NetCableSetup;
import mcjty.xnet.init.ModBlocks;
import mcjty.xnet.init.ModItems;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Registration {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        ModBlocks.registerBlocks(event);
        NetCableSetup.registerBlocks(event);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        ModBlocks.registerItems(event);
        ModItems.registerItems(event);
        NetCableSetup.registerItems(event);
    }

    @SubscribeEvent
    public static void registerTiles(final RegistryEvent.Register<TileEntityType<?>> event) {
        ModBlocks.registerTiles(event);
        NetCableSetup.registerTiles(event);
    }

    @SubscribeEvent
    public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event) {
        ModBlocks.registerContainers(event);
        NetCableSetup.registerContainers(event);
    }
}