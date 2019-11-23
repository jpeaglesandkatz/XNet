package mcjty.xnet.blocks.cables;

import mcjty.xnet.XNet;
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ObjectHolder;

public class NetCableSetup {

    @ObjectHolder(XNet.MODID + ":netcable")
    public static NetCableBlock netCableBlock;
    @ObjectHolder(XNet.MODID + ":connector")
    public static ConnectorBlock connectorBlock;
    @ObjectHolder(XNet.MODID + ":advanced_connector")
    public static AdvancedConnectorBlock advancedConnectorBlock;

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new NetCableBlock());
        event.getRegistry().register(new ConnectorBlock());
        event.getRegistry().register(new AdvancedConnectorBlock());
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
