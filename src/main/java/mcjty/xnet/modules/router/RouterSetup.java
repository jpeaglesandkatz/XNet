package mcjty.xnet.modules.router;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.xnet.XNet;
import mcjty.xnet.modules.router.blocks.TileEntityRouter;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static mcjty.xnet.XNet.MODID;

public class RouterSetup {

    public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<TileEntityType<?>> TILES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, MODID);
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = new DeferredRegister<>(ForgeRegistries.CONTAINERS, MODID);

    public static void register() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<BaseBlock> ROUTER = BLOCKS.register("router", TileEntityRouter::createBlock);
    public static final RegistryObject<Item> ROUTER_ITEM = ITEMS.register("router", () -> new BlockItem(ROUTER.get(), XNet.createStandardProperties()));
    public static final RegistryObject<TileEntityType<?>> TYPE_ROUTER = TILES.register("router", () -> TileEntityType.Builder.create(TileEntityRouter::new, ROUTER.get()).build(null));
    public static final RegistryObject<ContainerType<GenericContainer>> CONTAINER_ROUTER = CONTAINERS.register("router", GenericContainer::createContainerType);
}
