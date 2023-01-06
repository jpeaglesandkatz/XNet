package mcjty.xnet.datagen;

import mcjty.lib.datagen.BaseRecipeProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class Recipes extends BaseRecipeProvider {

    public Recipes(DataGenerator generatorIn) {
        super(generatorIn);
        add('F', mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_FRAME.get());
        add('A', mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_BASE.get());
    }

    @Override
    protected void buildCraftingRecipes(@Nonnull Consumer<FinishedRecipe> consumer) {
    }
}
