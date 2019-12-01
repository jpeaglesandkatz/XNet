package mcjty.xnet.datagen;

import mcjty.xnet.XNet;
import mcjty.xnet.modules.cables.CableSetup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

public class ItemTags extends ItemTagsProvider {

    public ItemTags(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void registerTags() {
        getBuilder(CableSetup.TAG_CABLES)
                .add(CableSetup.NETCABLE_BLUE, CableSetup.NETCABLE_YELLOW, CableSetup.NETCABLE_GREEN, CableSetup.NETCABLE_RED, CableSetup.NETCABLE_ROUTING)
                .build(new ResourceLocation(XNet.MODID, "cables"));
        getBuilder(CableSetup.TAG_CONNECTORS)
                .add(CableSetup.CONNECTOR_BLUE, CableSetup.CONNECTOR_YELLOW, CableSetup.CONNECTOR_GREEN, CableSetup.CONNECTOR_RED, CableSetup.CONNECTOR_ROUTING)
                .build(new ResourceLocation(XNet.MODID, "connectors"));
        getBuilder(CableSetup.TAG_ADVANCED_CONNECTORS)
                .add(CableSetup.ADVANCED_CONNECTOR_BLUE, CableSetup.ADVANCED_CONNECTOR_YELLOW, CableSetup.ADVANCED_CONNECTOR_GREEN, CableSetup.ADVANCED_CONNECTOR_RED, CableSetup.ADVANCED_CONNECTOR_ROUTING)
                .build(new ResourceLocation(XNet.MODID, "advanced_connectors"));
    }

    @Override
    public String getName() {
        return "XNet Tags";
    }
}
