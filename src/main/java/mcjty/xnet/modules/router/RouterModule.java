package mcjty.xnet.modules.router;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.modules.IModule;
import mcjty.lib.varia.OrientationTools;
import mcjty.rftoolsbase.modules.various.VariousModule;
import mcjty.xnet.modules.controller.blocks.TileEntityController;
import mcjty.xnet.modules.router.blocks.TileEntityRouter;
import mcjty.xnet.modules.router.client.GuiRouter;
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
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import static mcjty.lib.datagen.DataGen.has;
import static mcjty.xnet.XNet.tab;
import static mcjty.xnet.apiimpl.Constants.ITEM_ROUTER;
import static mcjty.xnet.setup.Registration.BLOCKS;
import static mcjty.xnet.setup.Registration.CONTAINERS;
import static mcjty.xnet.setup.Registration.ITEMS;
import static mcjty.xnet.setup.Registration.TILES;

public class RouterModule implements IModule {

    public static final RegistryObject<BaseBlock> ROUTER = BLOCKS.register(ITEM_ROUTER, TileEntityRouter::createBlock);
    public static final RegistryObject<Item> ROUTER_ITEM = ITEMS.register(ITEM_ROUTER, tab(() -> new BlockItem(ROUTER.get(), Registration.createStandardProperties())));
    public static final RegistryObject<BlockEntityType<?>> TYPE_ROUTER = TILES.register(ITEM_ROUTER, () -> BlockEntityType.Builder.of(TileEntityRouter::new, ROUTER.get()).build(null));
    public static final RegistryObject<MenuType<GenericContainer>> CONTAINER_ROUTER = CONTAINERS.register(ITEM_ROUTER, GenericContainer::createContainerType);

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

    @Override
    public void initDatagen(DataGen dataGen) {
        dataGen.add(
                Dob.blockBuilder(ROUTER)
                        .ironPickaxeTags()
                        .parentedItem("block/router")
                        .standardLoot(TYPE_ROUTER)
                        .blockState(p -> {
                            ModelFile modelOk = p.frontBasedModel(ITEM_ROUTER, p.modLoc("block/machine_router"));
                            ModelFile modelError = p.frontBasedModel("router_error", p.modLoc("block/machine_router_error"));
                            VariantBlockStateBuilder builder = p.getVariantBuilder(ROUTER.get());
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
