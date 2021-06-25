package mcjty.xnet.modules.wireless;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RotationType;
import mcjty.lib.builder.BlockBuilder;
import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.modules.IModule;
import mcjty.xnet.modules.wireless.blocks.TileEntityWirelessRouter;
import mcjty.xnet.modules.wireless.client.ClientSetup;
import mcjty.xnet.modules.wireless.client.GuiWirelessRouter;
import mcjty.xnet.setup.Config;
import mcjty.xnet.setup.Registration;
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
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static mcjty.xnet.setup.Registration.*;

public class WirelessRouterModule implements IModule {

    public static final RegistryObject<BaseBlock> WIRELESS_ROUTER = BLOCKS.register("wireless_router", TileEntityWirelessRouter::createBlock);
    public static final RegistryObject<Item> WIRELESS_ROUTER_ITEM = ITEMS.register("wireless_router", () -> new BlockItem(WIRELESS_ROUTER.get(), Registration.createStandardProperties()));
    public static final RegistryObject<TileEntityType<?>> TYPE_WIRELESS_ROUTER = TILES.register("wireless_router", () -> TileEntityType.Builder.of(TileEntityWirelessRouter::new, WIRELESS_ROUTER.get()).build(null));
    public static final RegistryObject<ContainerType<GenericContainer>> CONTAINER_WIRELESS_ROUTER = CONTAINERS.register("wireless_router", GenericContainer::createContainerType);

    public static final RegistryObject<BaseBlock> ANTENNA = BLOCKS.register("antenna", WirelessRouterModule::createAntennaBlock);
    public static final RegistryObject<Item> ANTENNA_ITEM = ITEMS.register("antenna", () -> new BlockItem(ANTENNA.get(), Registration.createStandardProperties()));
    public static final RegistryObject<BaseBlock> ANTENNA_BASE = BLOCKS.register("antenna_base", WirelessRouterModule::createAntennaBaseBlock);
    public static final RegistryObject<Item> ANTENNA_BASE_ITEM = ITEMS.register("antenna_base", () -> new BlockItem(ANTENNA_BASE.get(), Registration.createStandardProperties()));
    public static final RegistryObject<BaseBlock> ANTENNA_DISH = BLOCKS.register("antenna_dish", WirelessRouterModule::createAntennaDishBlock);
    public static final RegistryObject<Item> ANTENNA_DISH_ITEM = ITEMS.register("antenna_dish", () -> new BlockItem(ANTENNA_DISH.get(), Registration.createStandardProperties()));

    public static VoxelShape SMALLER_CUBE = VoxelShapes.box(0.01f, 0.01f, 0.01f, 0.99f, 0.99f, 0.99f);

    private static BaseBlock createAntennaDishBlock() {
        return new BaseBlock(new BlockBuilder()
                .info(TooltipBuilder.key("message.xnet.shiftmessage"))
                .infoShift(TooltipBuilder.header(),
                        TooltipBuilder.parameter("info", stack -> Integer.toString(Config.wirelessRouterRfPerChannel[TileEntityWirelessRouter.TIER_INF].get()) + " rf/t/channel"))
        ) {
            @Override
            public RotationType getRotationType() {
                return RotationType.HORIZROTATION;
            }

            @Override
            public VoxelShape getOcclusionShape(BlockState state, IBlockReader world, BlockPos pos) {
                return SMALLER_CUBE;
            }
        };
    }

    private static BaseBlock createAntennaBaseBlock() {
        return new BaseBlock(new BlockBuilder()
                .info(TooltipBuilder.key("message.xnet.shiftmessage"))
                .infoShift(TooltipBuilder.header())
        ) {
            @Override
            public RotationType getRotationType() {
                return RotationType.NONE;
            }

            @Override
            public VoxelShape getOcclusionShape(BlockState state, IBlockReader world, BlockPos pos) {
                return SMALLER_CUBE;
            }
        };
    }

    private static BaseBlock createAntennaBlock() {
        return new BaseBlock(new BlockBuilder()
                .info(TooltipBuilder.key("message.xnet.shiftmessage"))
                .infoShift(TooltipBuilder.header(),
                        TooltipBuilder.parameter("one", stack -> "range " + Integer.toString(Config.antennaTier1Range.get()) + " (" + Integer.toString(Config.wirelessRouterRfPerChannel[TileEntityWirelessRouter.TIER_1].get()) + " rf/t/channel)"),
                        TooltipBuilder.parameter("two", stack -> "range " + Integer.toString(Config.antennaTier2Range.get()) + " (" + Integer.toString(Config.wirelessRouterRfPerChannel[TileEntityWirelessRouter.TIER_2].get()) + " rf/t/channel)")
                )
        ) {
            @Override
            public RotationType getRotationType() {
                return RotationType.HORIZROTATION;
            }

            @Override
            public VoxelShape getOcclusionShape(BlockState state, IBlockReader world, BlockPos pos) {
                return SMALLER_CUBE;
            }
        };
    }

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GuiWirelessRouter.register();
        });

        ClientSetup.initClient();
    }

    @Override
    public void initConfig() {

    }
}
