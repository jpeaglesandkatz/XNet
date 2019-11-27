package mcjty.xnet.blocks.cables;

import mcjty.lib.container.GenericContainer;
import mcjty.xnet.XNet;
import mcjty.xnet.blocks.generic.CableColor;
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

    @ObjectHolder(XNet.MODID + ":netcable_red")
    public static Item NETCABLE_RED;
    @ObjectHolder(XNet.MODID + ":netcable_green")
    public static Item NETCABLE_GREEN;
    @ObjectHolder(XNet.MODID + ":netcable_blue")
    public static Item NETCABLE_BLUE;
    @ObjectHolder(XNet.MODID + ":netcable_yellow")
    public static Item NETCABLE_YELLOW;
    @ObjectHolder(XNet.MODID + ":netcable_routing")
    public static Item NETCABLE_ROUTING;

    @ObjectHolder(XNet.MODID + ":connector")
    public static ConnectorBlock CONNECTOR;

    @ObjectHolder(XNet.MODID + ":connector_red")
    public static Item CONNECTOR_RED;
    @ObjectHolder(XNet.MODID + ":connector_green")
    public static Item CONNECTOR_GREEN;
    @ObjectHolder(XNet.MODID + ":connector_blue")
    public static Item CONNECTOR_BLUE;
    @ObjectHolder(XNet.MODID + ":connector_yellow")
    public static Item CONNECTOR_YELLOW;
    @ObjectHolder(XNet.MODID + ":connector_routing")
    public static Item CONNECTOR_ROUTING;

    @ObjectHolder(XNet.MODID + ":advanced_connector")
    public static AdvancedConnectorBlock ADVANCED_CONNECTOR;

    @ObjectHolder(XNet.MODID + ":advanced_connector_red")
    public static Item ADVANCED_CONNECTOR_RED;
    @ObjectHolder(XNet.MODID + ":advanced_connector_green")
    public static Item ADVANCED_CONNECTOR_GREEN;
    @ObjectHolder(XNet.MODID + ":advanced_connector_blue")
    public static Item ADVANCED_CONNECTOR_BLUE;
    @ObjectHolder(XNet.MODID + ":advanced_connector_yellow")
    public static Item ADVANCED_CONNECTOR_YELLOW;
    @ObjectHolder(XNet.MODID + ":advanced_connector_routing")
    public static Item ADVANCED_CONNECTOR_ROUTING;

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
        for (CableColor color : CableColor.VALUES) {
            event.getRegistry().register(new ColorBlockItem(NETCABLE, properties, color).setRegistryName(NETCABLE.getRegistryName() + "_" + color.getName()));
            event.getRegistry().register(new ColorBlockItem(CONNECTOR, properties, color).setRegistryName(CONNECTOR.getRegistryName() + "_" + color.getName()));
            event.getRegistry().register(new ColorBlockItem(ADVANCED_CONNECTOR, properties, color).setRegistryName(ADVANCED_CONNECTOR.getRegistryName() + "_" + color.getName()));
        }
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
