package mcjty.xnet.datagen;

import mcjty.xnet.XNet;
import mcjty.xnet.modules.cables.CableSetup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.TagsProvider;
import net.minecraft.item.Item;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import java.nio.file.Path;

public class ItemTags extends TagsProvider<Item> {

    public ItemTags(DataGenerator generator) {
        super(generator, Registry.ITEM);
    }

    @Override
    protected void registerTags() {
        Tag.Builder.create()
                .add(CableSetup.NETCABLE_BLUE, CableSetup.NETCABLE_YELLOW, CableSetup.NETCABLE_GREEN, CableSetup.NETCABLE_RED, CableSetup.NETCABLE_ROUTING)
                .build(new ResourceLocation(XNet.MODID, "cables"));
    }

    @Override
    protected void setCollection(TagCollection<Item> colectionIn) {

    }

    @Override
    protected Path makePath(ResourceLocation id) {
        return this.generator.getOutputFolder().resolve("data/" + id.getNamespace() + "/tags/items/" + id.getPath() + ".json");
    }

    @Override
    public String getName() {
        return "XNet Tags";
    }
}
