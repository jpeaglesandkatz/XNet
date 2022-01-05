package mcjty.xnet.modules.controller;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.modules.IModule;
import mcjty.xnet.modules.controller.blocks.TileEntityController;
import mcjty.xnet.modules.controller.client.GuiController;
import mcjty.xnet.setup.Registration;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static mcjty.xnet.setup.Registration.*;

public class ControllerModule implements IModule {

    public static final RegistryObject<BaseBlock> CONTROLLER = BLOCKS.register("controller", TileEntityController::createBlock);
    public static final RegistryObject<Item> CONTROLLER_ITEM = ITEMS.register("controller", () -> new BlockItem(CONTROLLER.get(), Registration.createStandardProperties()));
    public static final RegistryObject<BlockEntityType<?>> TYPE_CONTROLLER = TILES.register("controller", () -> BlockEntityType.Builder.of(TileEntityController::new, CONTROLLER.get()).build(null));
    public static final RegistryObject<MenuType<GenericContainer>> CONTAINER_CONTROLLER = CONTAINERS.register("controller", GenericContainer::createContainerType);

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GuiController.register();
        });
    }

    @Override
    public void initConfig() {

    }
}
