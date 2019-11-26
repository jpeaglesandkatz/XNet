package mcjty.xnet.blocks.cables;

import mcjty.lib.container.GenericContainer;
import mcjty.xnet.XNet;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ObjectHolder;

public class NetCableSetup {

    @ObjectHolder(XNet.MODID + ":netcable")
    public static NetCableBlock NETCABLE;
    @ObjectHolder(XNet.MODID + ":connector")
    public static ConnectorBlock CONNECTOR;
    @ObjectHolder(XNet.MODID + ":advanced_connector")
    public static AdvancedConnectorBlock ADVANCED_CONNECTOR;

    @ObjectHolder(XNet.MODID + ":connector")
    public static TileEntityType<?> TYPE_CONNECTOR;
    @ObjectHolder(XNet.MODID + ":advanced_connector")
    public static TileEntityType<?> TYPE_ADVANCED_CONNECTOR;

    @ObjectHolder(XNet.MODID + ":connector")
    public static ContainerType<GenericContainer> CONTAINER_CONNECTOR;

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new NetCableBlock());
        event.getRegistry().register(new ConnectorBlock());
        event.getRegistry().register(new AdvancedConnectorBlock());
    }

    public static void registerItems(RegistryEvent.Register<Item> event) {
        Item.Properties properties = new Item.Properties().group(XNet.setup.getTab());
        event.getRegistry().register(new BlockItem(NETCABLE, properties).setRegistryName("netcable"));
        event.getRegistry().register(new BlockItem(CONNECTOR, properties).setRegistryName("connector"));
        event.getRegistry().register(new BlockItem(ADVANCED_CONNECTOR, properties).setRegistryName("advanced_connector"));
    }

    public static void registerTiles(final RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().register(TileEntityType.Builder.create(ConnectorTileEntity::new, CONNECTOR).build(null).setRegistryName("connector"));
        event.getRegistry().register(TileEntityType.Builder.create(AdvancedConnectorTileEntity::new, ADVANCED_CONNECTOR).build(null).setRegistryName("advanced_connector"));
    }

    public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().register(GenericContainer.createContainerType("connector"));
    }



    // @todo 1.14
//    @SideOnly(Side.CLIENT)
//    public static void initClient() {
//        netCableBlock.initModel();
//        connectorBlock.initModel();
//        advancedConnectorBlock.initModel();
//    }
//
//    @SideOnly(Side.CLIENT)
//    public static void initItemModels() {
//        netCableBlock.initItemModel();
//        connectorBlock.initItemModel();
//        advancedConnectorBlock.initItemModel();
//    }
//
//    @SideOnly(Side.CLIENT)
//    public static void initColorHandlers(BlockColors blockColors) {
//        connectorBlock.initColorHandler(blockColors);
//        advancedConnectorBlock.initColorHandler(blockColors);
//    }

    public static void initCrafting() {


        // @todo recipes
//        for (CableColor source : CableColor.VALUES) {
//            if (source != CableColor.ROUTING) {
//                for (CableColor dest : CableColor.VALUES) {
//                    if (dest != source && dest != CableColor.ROUTING) {
//                        MyGameReg.addRecipe(new ItemStack(netCableBlock, 1, dest.ordinal()), new ItemStack(netCableBlock, 1, source.ordinal()), dest.getDye());
//                        MyGameReg.addRecipe(new ItemStack(connectorBlock, 1, dest.ordinal()), new ItemStack(connectorBlock, 1, source.ordinal()), dest.getDye());
//                        MyGameReg.addRecipe(new ItemStack(advancedConnectorBlock, 1, dest.ordinal()), new ItemStack(advancedConnectorBlock, 1, source.ordinal()), dest.getDye());
//                    }
//                }
//            }
//        }
    }
}
