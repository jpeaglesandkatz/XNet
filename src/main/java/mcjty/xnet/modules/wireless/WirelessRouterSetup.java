package mcjty.xnet.modules.wireless;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.container.GenericContainer;
import mcjty.xnet.XNet;
import mcjty.xnet.setup.Config;
import mcjty.xnet.modules.wireless.blocks.TileEntityWirelessRouter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static mcjty.xnet.XNet.MODID;

public class WirelessRouterSetup {

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

    public static final RegistryObject<BaseBlock> WIRELESS_ROUTER = BLOCKS.register("wireless_router", TileEntityWirelessRouter::createBlock);
    public static final RegistryObject<Item> WIRELESS_ROUTER_ITEM = ITEMS.register("wireless_router", () -> new BlockItem(WIRELESS_ROUTER.get(), XNet.createStandardProperties()));
    public static final RegistryObject<TileEntityType<?>> TYPE_WIRELESS_ROUTER = TILES.register("wireless_router", () -> TileEntityType.Builder.create(TileEntityWirelessRouter::new, WIRELESS_ROUTER.get()).build(null));
    public static final RegistryObject<ContainerType<GenericContainer>> CONTAINER_WIRELESS_ROUTER = CONTAINERS.register("wireless_router", GenericContainer::createContainerType);

    public static final RegistryObject<BaseBlock> ANTENNA = BLOCKS.register("antenna", WirelessRouterSetup::createAntennaBlock);
    public static final RegistryObject<Item> ANTENNA_ITEM = ITEMS.register("antenna", () -> new BlockItem(ANTENNA.get(), XNet.createStandardProperties()));
    public static final RegistryObject<BaseBlock> ANTENNA_BASE = BLOCKS.register("antenna_base", WirelessRouterSetup::createAntennaBaseBlock);
    public static final RegistryObject<Item> ANTENNA_BASE_ITEM = ITEMS.register("antenna_base", () -> new BlockItem(ANTENNA_BASE.get(), XNet.createStandardProperties()));
    public static final RegistryObject<BaseBlock> ANTENNA_DISH = BLOCKS.register("antenna_dish", WirelessRouterSetup::createAntennaDishBlock);
    public static final RegistryObject<Item> ANTENNA_DISH_ITEM = ITEMS.register("antenna_dish", () -> new BlockItem(ANTENNA_DISH.get(), XNet.createStandardProperties()));

    public static VoxelShape SMALLER_CUBE = VoxelShapes.create(0.01f, 0.01f, 0.01f, 0.99f, 0.99f, 0.99f);

    private static BaseBlock createAntennaDishBlock() {
        return new BaseBlock(new BlockBuilder()
                .info("message.xnet.shiftmessage")
                .infoExtended("message.xnet.antenna_dish")
                .infoExtendedParameter(stack -> Integer.toString(Config.wirelessRouterRfPerChannel[TileEntityWirelessRouter.TIER_INF].get()))
        ) {
            @Override
            public RotationType getRotationType() {
                return RotationType.HORIZROTATION;
            }

            @Override
            public VoxelShape getRenderShape(BlockState state, IBlockReader world, BlockPos pos) {
                return SMALLER_CUBE;
            }
        };
    }

    private static BaseBlock createAntennaBaseBlock() {
        return new BaseBlock(new BlockBuilder()
                .info("message.xnet.shiftmessage")
                .infoExtended("message.xnet.antenna_base")
        ) {
            @Override
            public RotationType getRotationType() {
                return RotationType.NONE;
            }

            @Override
            public VoxelShape getRenderShape(BlockState state, IBlockReader world, BlockPos pos) {
                return SMALLER_CUBE;
            }
        };
    }

    private static BaseBlock createAntennaBlock() {
        return new BaseBlock(new BlockBuilder()
                .info("message.xnet.shiftmessage")
                .infoExtended("message.xnet.antenna")
                .infoExtendedParameter(stack -> Integer.toString(Config.antennaTier1Range.get()))
                .infoExtendedParameter(stack -> Integer.toString(Config.wirelessRouterRfPerChannel[TileEntityWirelessRouter.TIER_1].get()))
                .infoExtendedParameter(stack -> Integer.toString(Config.antennaTier2Range.get()))
                .infoExtendedParameter(stack -> Integer.toString(Config.wirelessRouterRfPerChannel[TileEntityWirelessRouter.TIER_2].get()))
        ) {
            @Override
            public RotationType getRotationType() {
                return RotationType.HORIZROTATION;
            }

            @Override
            public VoxelShape getRenderShape(BlockState state, IBlockReader world, BlockPos pos) {
                return SMALLER_CUBE;
            }
        };
    }
}
