package mcjty.xnet.datagen;

import mcjty.xnet.XNet;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;

public class ItemTags extends ItemTagsProvider {

    public ItemTags(DataGenerator generator, BlockTags blockTags, ExistingFileHelper helper) {
        super(generator, blockTags, XNet.MODID, helper);
    }

    @Override
    protected void addTags() {
    }

    @Nonnull
    @Override
    public String getName() {
        return "XNet Tags";
    }
}
