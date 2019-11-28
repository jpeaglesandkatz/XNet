package mcjty.xnet.modules.various;

import mcjty.xnet.XNet;
import mcjty.xnet.modules.various.items.ConnectorUpgradeItem;
import mcjty.xnet.modules.various.items.XNetManualItem;
import mcjty.xnet.modules.various.blocks.RedstoneProxyBlock;
import mcjty.xnet.modules.various.blocks.RedstoneProxyUBlock;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ObjectHolder;

public class VariousSetup {

    @ObjectHolder(XNet.MODID + ":redstone_proxy")
    public static RedstoneProxyBlock REDSTONE_PROXY;
    @ObjectHolder(XNet.MODID + ":redstone_proxy_upd")
    public static RedstoneProxyUBlock REDSTONE_PROXY_UPD;

    @ObjectHolder(XNet.MODID + ":xnet_manual")
    public static XNetManualItem MANUAL;
    @ObjectHolder(XNet.MODID + ":connector_upgrade")
    public static ConnectorUpgradeItem UPGRADE;

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new RedstoneProxyBlock());
        event.getRegistry().register(new RedstoneProxyUBlock());

    }

    public static void registerItems(RegistryEvent.Register<Item> event) {
        Item.Properties properties = new Item.Properties().group(XNet.setup.getTab());
        event.getRegistry().register(new BlockItem(REDSTONE_PROXY, properties).setRegistryName("redstone_proxy"));
        event.getRegistry().register(new BlockItem(REDSTONE_PROXY_UPD, properties).setRegistryName("redstone_proxy_upd"));

        event.getRegistry().register(new XNetManualItem());
        event.getRegistry().register(new ConnectorUpgradeItem());
    }

    public static void registerTiles(final RegistryEvent.Register<TileEntityType<?>> event) {
    }

    public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event) {
    }

//@todo 1.14
//    @SideOnly(Side.CLIENT)
//    public static void initColorHandlers(BlockColors blockColors) {
//        facadeBlock.initColorHandler(blockColors);
//        NetCableSetup.initColorHandlers(blockColors);
//    }
}
