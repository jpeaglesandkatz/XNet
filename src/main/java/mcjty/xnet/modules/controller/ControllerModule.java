package mcjty.xnet.modules.controller;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.modules.IModule;
import mcjty.lib.setup.DeferredBlock;
import mcjty.lib.setup.DeferredItem;
import mcjty.lib.varia.OrientationTools;
import mcjty.rftoolsbase.modules.various.VariousModule;
import mcjty.xnet.modules.controller.blocks.TileEntityController;
import mcjty.xnet.modules.controller.client.GuiController;
import mcjty.xnet.setup.Registration;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.function.Supplier;

import static mcjty.lib.datagen.DataGen.has;
import static mcjty.xnet.XNet.tab;
import static mcjty.xnet.setup.Registration.*;

public class ControllerModule implements IModule {

    public static final DeferredBlock<BaseBlock> CONTROLLER = BLOCKS.register("controller", TileEntityController::createBlock);
    public static final DeferredItem<Item> CONTROLLER_ITEM = ITEMS.register("controller", tab(() -> new BlockItem(CONTROLLER.get(), Registration.createStandardProperties())));
    public static final Supplier<BlockEntityType<?>> TYPE_CONTROLLER = TILES.register("controller", () -> BlockEntityType.Builder.of(TileEntityController::new, CONTROLLER.get()).build(null));
    public static final Supplier<MenuType<GenericContainer>> CONTAINER_CONTROLLER = CONTAINERS.register("controller", GenericContainer::createContainerType);

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
    public void initConfig(IEventBus bus) {

    }

    @Override
    public void initDatagen(DataGen dataGen) {
        dataGen.add(
                Dob.blockBuilder(CONTROLLER)
                        .ironPickaxeTags()
                        .parentedItem("block/controller")
                        .standardLoot(TYPE_CONTROLLER)
                        .blockState(p -> {
                            ModelFile modelOk = p.frontBasedModel("controller", p.modLoc("block/machine_controller"));
                            ModelFile modelError = p.frontBasedModel("controller_error", p.modLoc("block/machine_controller_error"));
                            VariantBlockStateBuilder builder = p.getVariantBuilder(ControllerModule.CONTROLLER.get());
                            for (Direction direction : OrientationTools.DIRECTION_VALUES) {
                                p.applyRotation(builder.partialState().with(BlockStateProperties.FACING, direction).with(TileEntityController.ERROR, false)
                                        .modelForState().modelFile(modelOk), direction);
                                p.applyRotation(builder.partialState().with(BlockStateProperties.FACING, direction).with(TileEntityController.ERROR, true)
                                        .modelForState().modelFile(modelError), direction);
                            }
                        })
                        .shaped(builder -> builder
                                        .define('F', mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_FRAME.get())
                                        .define('I', Items.REPEATER)
                                        .define('C', Items.COMPARATOR)
                                        .define('g', Tags.Items.INGOTS_GOLD)
                                        .unlockedBy("frame", has(VariousModule.MACHINE_FRAME.get())),
                                "ICI", "rFr", "igi"));
    }
}
