package mcjty.xnet.modules.controller;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.xnet.modules.controller.blocks.TileEntityController;
import mcjty.xnet.setup.Registration;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;

import static mcjty.xnet.setup.Registration.*;

public class ControllerSetup {

    public static void register() {
        // Needed to force class loading
    }

    public static final RegistryObject<BaseBlock> CONTROLLER = BLOCKS.register("controller", TileEntityController::createBlock);
    public static final RegistryObject<Item> CONTROLLER_ITEM = ITEMS.register("controller", () -> new BlockItem(CONTROLLER.get(), Registration.createStandardProperties()));
    public static final RegistryObject<TileEntityType<?>> TYPE_CONTROLLER = TILES.register("controller", () -> TileEntityType.Builder.create(TileEntityController::new, CONTROLLER.get()).build(null));
    public static final RegistryObject<ContainerType<GenericContainer>> CONTAINER_CONTROLLER = CONTAINERS.register("controller", GenericContainer::createContainerType);
}
