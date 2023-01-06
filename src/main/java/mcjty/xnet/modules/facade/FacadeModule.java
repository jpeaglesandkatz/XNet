package mcjty.xnet.modules.facade;

import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.modules.IModule;
import mcjty.xnet.modules.cables.blocks.GenericCableBlock;
import mcjty.xnet.modules.facade.blocks.FacadeBlock;
import mcjty.xnet.modules.facade.blocks.FacadeBlockItem;
import mcjty.xnet.modules.facade.blocks.FacadeTileEntity;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import static mcjty.lib.datagen.DataGen.has;
import static mcjty.xnet.setup.Registration.*;

public class FacadeModule implements IModule {

    public static final RegistryObject<FacadeBlock> FACADE = BLOCKS.register("facade", () -> new FacadeBlock(GenericCableBlock.CableBlockType.FACADE)); // @todo 1.14
    public static final RegistryObject<Item> FACADE_ITEM = ITEMS.register("facade", () -> new FacadeBlockItem(FACADE.get()));
    public static final RegistryObject<BlockEntityType<?>> TYPE_FACADE = TILES.register("facade", () -> BlockEntityType.Builder.of(FacadeTileEntity::new, FACADE.get()).build(null));

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
    }

    @Override
    public void initConfig() {

    }

    @Override
    public void initDatagen(DataGen dataGen) {
        dataGen.add(
                Dob.blockBuilder(FACADE)
                        .ironPickaxeTags()
                        .shaped(builder -> builder
                                        .define('w', ItemTags.WOOL)
                                        .unlockedBy("glass", has(Items.GLASS)),
                                16,
                                "pwp", "wGw", "pwp")
        );
    }
}
