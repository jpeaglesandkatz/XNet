package mcjty.xnet.datagen;

import mcjty.xnet.XNet;
import mcjty.xnet.modules.cables.CableModule;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.util.ResourceLocation;

public class ItemTags extends ItemTagsProvider {

    public ItemTags(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void registerTags() {
        getBuilder(CableModule.TAG_CABLES)
                .add(CableModule.NETCABLE_BLUE.get(), CableModule.NETCABLE_YELLOW.get(), CableModule.NETCABLE_GREEN.get(), CableModule.NETCABLE_RED.get(), CableModule.NETCABLE_ROUTING.get())
                .build(new ResourceLocation(XNet.MODID, "cables"));
        getBuilder(CableModule.TAG_CONNECTORS)
                .add(CableModule.CONNECTOR_BLUE.get(), CableModule.CONNECTOR_YELLOW.get(), CableModule.CONNECTOR_GREEN.get(), CableModule.CONNECTOR_RED.get(), CableModule.CONNECTOR_ROUTING.get())
                .build(new ResourceLocation(XNet.MODID, "connectors"));
        getBuilder(CableModule.TAG_ADVANCED_CONNECTORS)
                .add(CableModule.ADVANCED_CONNECTOR_BLUE.get(), CableModule.ADVANCED_CONNECTOR_YELLOW.get(), CableModule.ADVANCED_CONNECTOR_GREEN.get(), CableModule.ADVANCED_CONNECTOR_RED.get(), CableModule.ADVANCED_CONNECTOR_ROUTING.get())
                .build(new ResourceLocation(XNet.MODID, "advanced_connectors"));
    }

    @Override
    public String getName() {
        return "XNet Tags";
    }
}
