package mcjty.xnet.modules.wireless;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.GenericContainer;
import mcjty.xnet.XNet;
import mcjty.xnet.config.ConfigSetup;
import mcjty.xnet.modules.controller.blocks.TileEntityController;
import mcjty.xnet.modules.router.blocks.TileEntityRouter;
import mcjty.xnet.modules.wireless.blocks.TileEntityWirelessRouter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ObjectHolder;

public class WirelessRouterSetup {


    @ObjectHolder(XNet.MODID + ":wireless_router")
    public static BaseBlock WIRELESS_ROUTER;
    @ObjectHolder(XNet.MODID + ":antenna")
    public static BaseBlock ANTENNA;
    @ObjectHolder(XNet.MODID + ":antenna_base")
    public static BaseBlock ANTENNA_BASE;
    @ObjectHolder(XNet.MODID + ":antenna_dish")
    public static BaseBlock ANTENNA_DISH;

    @ObjectHolder(XNet.MODID + ":wireless_router")
    public static TileEntityType<?> TYPE_WIRELESS_ROUTER;
    @ObjectHolder(XNet.MODID + ":wireless_router")
    public static ContainerType<GenericContainer> CONTAINER_WIRELESS_ROUTER;

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new BaseBlock("wireless_router", new BlockBuilder()
                .tileEntitySupplier(TileEntityWirelessRouter::new)
                .info("message.xnet.shiftmessage")
                .infoExtended("message.xnet.wireless_router")
        ) {
            @Override
            protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
                super.fillStateContainer(builder);
                builder.add(TileEntityController.ERROR);
            }
        });
        event.getRegistry().register(new BaseBlock("antenna", new BlockBuilder()
                .info("message.xnet.shiftmessage")
                .infoExtended("message.xnet.antenna")
                .infoExtendedParameter(stack -> Integer.toString(ConfigSetup.antennaTier1Range.get()))
                .infoExtendedParameter(stack -> Integer.toString(ConfigSetup.wirelessRouterRfPerChannel[TileEntityWirelessRouter.TIER_1].get()))
                .infoExtendedParameter(stack -> Integer.toString(ConfigSetup.antennaTier2Range.get()))
                .infoExtendedParameter(stack -> Integer.toString(ConfigSetup.wirelessRouterRfPerChannel[TileEntityWirelessRouter.TIER_2].get()))
        ) {
            @Override
            public RotationType getRotationType() {
                return RotationType.HORIZROTATION;
            }
        });
        event.getRegistry().register(new BaseBlock("antenna_base", new BlockBuilder()
                .info("message.xnet.shiftmessage")
                .infoExtended("message.xnet.antenna_base")
        ) {
            @Override
            public RotationType getRotationType() {
                return RotationType.NONE;
            }
        });
        event.getRegistry().register(new BaseBlock("antenna_dish", new BlockBuilder()
                .info("message.xnet.shiftmessage")
                .infoExtended("message.xnet.antenna_dish")
                .infoExtendedParameter(stack -> Integer.toString(ConfigSetup.wirelessRouterRfPerChannel[TileEntityWirelessRouter.TIER_INF].get()))
        ) {
            @Override
            public RotationType getRotationType() {
                return RotationType.HORIZROTATION;
            }
        });
    }

    public static void registerItems(RegistryEvent.Register<Item> event) {
        Item.Properties properties = new Item.Properties().group(XNet.setup.getTab());
        event.getRegistry().register(new BlockItem(WirelessRouterSetup.WIRELESS_ROUTER, properties).setRegistryName("wireless_router"));
        event.getRegistry().register(new BlockItem(WirelessRouterSetup.ANTENNA, properties).setRegistryName("antena"));
        event.getRegistry().register(new BlockItem(WirelessRouterSetup.ANTENNA_BASE, properties).setRegistryName("antena_base"));
        event.getRegistry().register(new BlockItem(WirelessRouterSetup.ANTENNA_DISH, properties).setRegistryName("antena_dish"));
    }

    public static void registerTiles(final RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().register(TileEntityType.Builder.create(TileEntityRouter::new, WirelessRouterSetup.WIRELESS_ROUTER).build(null).setRegistryName("wireless_router"));
    }

    public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().register(GenericContainer.createContainerType("wireless_router"));
    }

}
