package mcjty.xnet.modules.facade;

import mcjty.xnet.XNet;
import mcjty.xnet.modules.facade.blocks.FacadeBlock;
import mcjty.xnet.modules.facade.blocks.FacadeTileEntity;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ObjectHolder;

public class FacadeSetup {

    @ObjectHolder(XNet.MODID + ":facade")
    public static FacadeBlock FACADE;
    @ObjectHolder(XNet.MODID + ":facade")
    public static TileEntityType<?> TYPE_FACADE;

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new FacadeBlock());
    }

    public static void registerItems(RegistryEvent.Register<Item> event) {
        Item.Properties properties = new Item.Properties().group(XNet.setup.getTab());
        event.getRegistry().register(new BlockItem(FacadeSetup.FACADE, properties).setRegistryName("facade"));
    }

    public static void registerTiles(final RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().register(TileEntityType.Builder.create(FacadeTileEntity::new, FacadeSetup.FACADE).build(null).setRegistryName("facade"));
    }

    public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event) {
    }

}
