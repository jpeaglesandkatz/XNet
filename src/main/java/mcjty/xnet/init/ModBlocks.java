package mcjty.xnet.init;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.xnet.XNet;
import mcjty.xnet.blocks.cables.NetCableSetup;
import mcjty.xnet.blocks.controller.TileEntityController;
import mcjty.xnet.blocks.facade.FacadeBlock;
import mcjty.xnet.blocks.redstoneproxy.RedstoneProxyBlock;
import mcjty.xnet.blocks.redstoneproxy.RedstoneProxyUBlock;
import mcjty.xnet.blocks.router.TileEntityRouter;
import mcjty.xnet.blocks.wireless.TileEntityWirelessRouter;
import mcjty.xnet.config.ConfigSetup;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ObjectHolder;

public class ModBlocks {

    @ObjectHolder(XNet.MODID + ":controller")
    public static BaseBlock controllerBlock;
    @ObjectHolder(XNet.MODID + ":router")
    public static BaseBlock routerBlock;
    @ObjectHolder(XNet.MODID + ":wireless_router")
    public static BaseBlock wirelessRouterBlock;

    @ObjectHolder(XNet.MODID + ":antenna")
    public static BaseBlock antennaBlock;
    @ObjectHolder(XNet.MODID + ":antenna_base")
    public static BaseBlock antennaBaseBlock;
    @ObjectHolder(XNet.MODID + ":antenna_dish")
    public static BaseBlock antennaDishBlock;

    @ObjectHolder(XNet.MODID + ":facade")
    public static FacadeBlock facadeBlock;

    @ObjectHolder(XNet.MODID + ":redstone_proxy")
    public static RedstoneProxyBlock redstoneProxyBlock;

    @ObjectHolder(XNet.MODID + ":redstone_proxy_upd")
    public static RedstoneProxyUBlock redstoneProxyUBlock;

    @ObjectHolder(XNet.MODID + ":controller")
    public static TileEntityType<?> TYPE_CONTROLLER;

    @ObjectHolder(XNet.MODID + ":connector")
    public static TileEntityType<?> TYPE_CONNECTOR;

    @ObjectHolder(XNet.MODID + ":router")
    public static TileEntityType<?> TYPE_ROUTER;

    @ObjectHolder(XNet.MODID + ":wireless_router")
    public static TileEntityType<?> TYPE_WIRELESS_ROUTER;

    @ObjectHolder(XNet.MODID + ":facade")
    public static TileEntityType<?> TYPE_FACADE;

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new FacadeBlock());
        event.getRegistry().register(new RedstoneProxyBlock());
        event.getRegistry().register(new RedstoneProxyUBlock());

        event.getRegistry().register(new BaseBlock("controller", new BlockBuilder()
                .tileEntitySupplier(TileEntityController::new)
                .info("message.xnet.shiftmessage")
                .infoExtended("message.xnet.controller")
//                .flags(BlockFlags.REDSTONE_CHECK)   // Not really for redstone check but to have TE.checkRedstone() being called
//                .guiId(GuiProxy.GUI_CONTROLLER)
//                .property(TileEntityController.ERROR)
        ));
        event.getRegistry().register(new BaseBlock("router", new BlockBuilder()
                .tileEntitySupplier(TileEntityRouter::new)
                .info("message.xnet.shiftmessage")
                .infoExtended("message.xnet.router")
//                .flags(BlockFlags.REDSTONE_CHECK)   // Not really for redstone check but to have TE.checkRedstone() being called
//                .guiId(GuiProxy.GUI_ROUTER)
//                .property(TileEntityRouter.ERROR)
        ));
        event.getRegistry().register(new BaseBlock("wireless_router", new BlockBuilder()
                .tileEntitySupplier(TileEntityWirelessRouter::new)
                .info("message.xnet.shiftmessage")
                .infoExtended("message.xnet.wireless_router")
//                .flags(BlockFlags.REDSTONE_CHECK)   // Not really for redstone check but to have TE.checkRedstone() being called
//                .guiId(GuiProxy.GUI_WIRELESS_ROUTER)
//                .property(TileEntityWirelessRouter.ERROR)
        ));
        event.getRegistry().register(new BaseBlock("antenna", new BlockBuilder()
                .info("message.xnet.shiftmessage")
//                .flags(BlockFlags.NON_OPAQUE)
                .infoExtended("message.xnet.antenna")
                .infoExtendedParameter(stack -> Integer.toString(ConfigSetup.antennaTier1Range.get()))
                .infoExtendedParameter(stack -> Integer.toString(ConfigSetup.wirelessRouterRfPerChannel[TileEntityWirelessRouter.TIER_1].get()))
                .infoExtendedParameter(stack -> Integer.toString(ConfigSetup.antennaTier2Range.get()))
                .infoExtendedParameter(stack -> Integer.toString(ConfigSetup.wirelessRouterRfPerChannel[TileEntityWirelessRouter.TIER_2].get()))
        ));
        event.getRegistry().register(new BaseBlock("antenna_base", new BlockBuilder()
                .info("message.xnet.shiftmessage")
//                .flags(BlockFlags.NON_OPAQUE)
                .infoExtended("message.xnet.antenna_base")
        ));
        event.getRegistry().register(new BaseBlock("antenna_dish", new BlockBuilder()
                .info("message.xnet.shiftmessage")
//                .flags(BlockFlags.NON_OPAQUE)
                .infoExtended("message.xnet.antenna_dish")
                .infoExtendedParameter(stack -> Integer.toString(ConfigSetup.wirelessRouterRfPerChannel[TileEntityWirelessRouter.TIER_INF].get()))
        ));
    }

//    @SideOnly(Side.CLIENT)
//    public static void initModels() {
//        controllerBlock.initModel();
//        controllerBlock.setGuiFactory(GuiController::new);
//
//        routerBlock.initModel();
//        routerBlock.setGuiFactory(GuiRouter::new);
//
//        wirelessRouterBlock.initModel();
//        wirelessRouterBlock.setGuiFactory(GuiWirelessRouter::new);
//
//        antennaBlock.initModel();
//        antennaBaseBlock.initModel();
//        antennaDishBlock.initModel();
//
//        facadeBlock.initModel();
//        redstoneProxyBlock.initModel();
//        redstoneProxyUBlock.initModel();
//        NetCableSetup.initClient();
//    }

//    @SideOnly(Side.CLIENT)
//    public static void initItemModels() {
//        facadeBlock.initItemModel();
//        NetCableSetup.initItemModels();
//    }
//@todo 1.14
//    @SideOnly(Side.CLIENT)
//    public static void initColorHandlers(BlockColors blockColors) {
//        facadeBlock.initColorHandler(blockColors);
//        NetCableSetup.initColorHandlers(blockColors);
//    }
}
