package mcjty.xnet.modules.router;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.GenericContainer;
import mcjty.xnet.XNet;
import mcjty.xnet.modules.controller.blocks.TileEntityController;
import mcjty.xnet.modules.router.blocks.TileEntityRouter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ObjectHolder;

public class RouterSetup {

    @ObjectHolder(XNet.MODID + ":router")
    public static BaseBlock ROUTER;
    @ObjectHolder(XNet.MODID + ":router")
    public static TileEntityType<?> TYPE_ROUTER;
    @ObjectHolder(XNet.MODID + ":router")
    public static ContainerType<GenericContainer> CONTAINER_ROUTER;

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new BaseBlock("router", new BlockBuilder()
                .tileEntitySupplier(TileEntityRouter::new)
                .info("message.xnet.shiftmessage")
                .infoExtended("message.xnet.router")
        ) {
            @Override
            protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
                super.fillStateContainer(builder);
                builder.add(TileEntityController.ERROR);
            }
        });
    }

    public static void registerItems(RegistryEvent.Register<Item> event) {
        Item.Properties properties = new Item.Properties().group(XNet.setup.getTab());
        event.getRegistry().register(new BlockItem(ROUTER, properties).setRegistryName("router"));
    }

    public static void registerTiles(final RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().register(TileEntityType.Builder.create(TileEntityRouter::new, ROUTER).build(null).setRegistryName("router"));
    }

    public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().register(GenericContainer.createContainerType("router"));
    }

}
