package mcjty.xnet.modules.router;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.blocks.RBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.modules.IModule;
import mcjty.lib.varia.OrientationTools;
import mcjty.rftoolsbase.modules.various.VariousModule;
import mcjty.xnet.modules.controller.blocks.TileEntityController;
import mcjty.xnet.modules.router.blocks.TileEntityRouter;
import mcjty.xnet.modules.router.client.GuiRouter;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;

import java.util.function.Supplier;

import static mcjty.lib.datagen.DataGen.has;
import static mcjty.xnet.setup.Registration.*;

public class RouterModule implements IModule {

    public static final RBlock<BaseBlock, BlockItem, TileEntityRouter> ROUTER = RBLOCKS.registerBlock("router",
            TileEntityRouter.class,
            TileEntityRouter::createBlock,
            block -> new BlockItem(block.get(), createStandardProperties()),
            TileEntityRouter::new
    );
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_ROUTER = CONTAINERS.register("router", GenericContainer::createContainerType);

    public RouterModule(IEventBus bus) {
        bus.addListener(this::registerScreens);
    }

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
    }

    public void registerScreens(RegisterMenuScreensEvent event) {
        GuiRouter.register(event);
    }

    @Override
    public void initConfig(IEventBus bus) {

    }

    @Override
    public void initDatagen(DataGen dataGen, HolderLookup.Provider provider) {
        dataGen.add(
                Dob.blockBuilder(ROUTER)
                        .ironPickaxeTags()
                        .parentedItem("block/router")
                        .standardLoot() // @todo 1.21 fix loot
                        .blockState(p -> {
                            ModelFile modelOk = p.frontBasedModel("router", p.modLoc("block/machine_router"));
                            ModelFile modelError = p.frontBasedModel("router_error", p.modLoc("block/machine_router_error"));
                            VariantBlockStateBuilder builder = p.getVariantBuilder(ROUTER.block().get());
                            for (Direction direction : OrientationTools.DIRECTION_VALUES) {
                                p.applyRotation(builder.partialState().with(BlockStateProperties.FACING, direction).with(TileEntityController.ERROR, false)
                                        .modelForState().modelFile(modelOk), direction);
                                p.applyRotation(builder.partialState().with(BlockStateProperties.FACING, direction).with(TileEntityController.ERROR, true)
                                        .modelForState().modelFile(modelError), direction);
                            }
                        })
                        .shaped(builder -> builder
                                        .define('F', VariousModule.MACHINE_FRAME.get())
                                        .define('I', Items.POWERED_RAIL)
                                        .define('C', Items.COMPARATOR)
                                        .unlockedBy("frame", has(VariousModule.MACHINE_FRAME.get())),
                                "ICI", "rFr", "ioi")
        );
    }
}
