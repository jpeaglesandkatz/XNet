package mcjty.xnet.datagen;

import mcjty.lib.datagen.BaseRecipeProvider;
import mcjty.rftoolsbase.items.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;

import java.util.function.Consumer;

public class Recipes extends BaseRecipeProvider {

    public Recipes(DataGenerator generatorIn) {
        super(generatorIn);
        add('F', ModItems.MACHINE_FRAME);
        add('A', ModItems.MACHINE_BASE);
        add('s', ModItems.DIMENSIONALSHARD);
        group("xnet");
    }

    // @todo 1.14
    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
//        build(consumer, ShapedRecipeBuilder.shapedRecipe(CoalGeneratorSetup.COALGENERATOR)
//                        .addCriterion("frame", InventoryChangeTrigger.Instance.forItems(ModItems.MACHINE_FRAME, Items.REDSTONE_TORCH)),
//                "cTc", "cFc", "cTc");
//        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(PowerCellSetup.CELL3)
//                        .key('K', mcjty.rftoolspower.items.ModItems.POWER_CORE3)
//                        .key('P', PowerCellSetup.CELL2)
//                        .addCriterion("cell", InventoryChangeTrigger.Instance.forItems(PowerCellSetup.CELL2)),
//                "rKr", "KPK", "rKr");
    }
}
