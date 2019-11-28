package mcjty.xnet.modules.controller;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.GenericContainer;
import mcjty.xnet.XNet;
import mcjty.xnet.modules.controller.blocks.TileEntityController;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ObjectHolder;

public class ControllerSetup {

    @ObjectHolder(XNet.MODID + ":controller")
    public static BaseBlock CONTROLLER;
    @ObjectHolder(XNet.MODID + ":controller")
    public static TileEntityType<?> TYPE_CONTROLLER;
    @ObjectHolder(XNet.MODID + ":controller")
    public static ContainerType<GenericContainer> CONTAINER_CONTROLLER;

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(new BaseBlock("controller", new BlockBuilder()
                .tileEntitySupplier(TileEntityController::new)
                .info("message.xnet.shiftmessage")
                .infoExtended("message.xnet.controller")
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
        event.getRegistry().register(new BlockItem(ControllerSetup.CONTROLLER, properties).setRegistryName("controller"));
    }

    public static void registerTiles(final RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().register(TileEntityType.Builder.create(TileEntityController::new, ControllerSetup.CONTROLLER).build(null).setRegistryName("controller"));
    }

    public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().register(GenericContainer.createContainerType("controller"));
    }

}
