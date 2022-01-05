package mcjty.xnet.modules.router;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.modules.IModule;
import mcjty.xnet.modules.router.blocks.TileEntityRouter;
import mcjty.xnet.modules.router.client.GuiRouter;
import mcjty.xnet.setup.Registration;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static mcjty.xnet.setup.Registration.*;

public class RouterModule implements IModule {

    public static final RegistryObject<BaseBlock> ROUTER = BLOCKS.register("router", TileEntityRouter::createBlock);
    public static final RegistryObject<Item> ROUTER_ITEM = ITEMS.register("router", () -> new BlockItem(ROUTER.get(), Registration.createStandardProperties()));
    public static final RegistryObject<BlockEntityType<?>> TYPE_ROUTER = TILES.register("router", () -> BlockEntityType.Builder.of(TileEntityRouter::new, ROUTER.get()).build(null));
    public static final RegistryObject<MenuType<GenericContainer>> CONTAINER_ROUTER = CONTAINERS.register("router", GenericContainer::createContainerType);

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GuiRouter.register();
        });
    }

    @Override
    public void initConfig() {

    }
}
