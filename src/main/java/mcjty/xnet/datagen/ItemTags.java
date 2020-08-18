package mcjty.xnet.datagen;

import mcjty.xnet.XNet;
import mcjty.xnet.modules.cables.CableSetup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.util.ResourceLocation;

public class ItemTags extends ItemTagsProvider {

    public ItemTags(DataGenerator generator) {
        super(generator, null); // @todo 1.16
    }

    @Override
    protected void registerTags() {
        // @todo 1.16
//        getOrCreateBuilder(CableSetup.TAG_CABLES)
//                .add(CableSetup.NETCABLE_BLUE.get(), CableSetup.NETCABLE_YELLOW.get(), CableSetup.NETCABLE_GREEN.get(), CableSetup.NETCABLE_RED.get(), CableSetup.NETCABLE_ROUTING.get())
//                .build(new ResourceLocation(XNet.MODID, "cables"));
//        getOrCreateBuilder(CableSetup.TAG_CABLES)
//                .add(CableSetup.NETCABLE_BLUE.get(), CableSetup.NETCABLE_YELLOW.get(), CableSetup.NETCABLE_GREEN.get(), CableSetup.NETCABLE_RED.get(), CableSetup.NETCABLE_ROUTING.get())
//                .build(new ResourceLocation(XNet.MODID, "cables"));
//        getOrCreateBuilder(CableSetup.TAG_CONNECTORS)
//                .add(CableSetup.CONNECTOR_BLUE.get(), CableSetup.CONNECTOR_YELLOW.get(), CableSetup.CONNECTOR_GREEN.get(), CableSetup.CONNECTOR_RED.get(), CableSetup.CONNECTOR_ROUTING.get())
//                .build(new ResourceLocation(XNet.MODID, "connectors"));
//        getOrCreateBuilder(CableSetup.TAG_ADVANCED_CONNECTORS)
//                .add(CableSetup.ADVANCED_CONNECTOR_BLUE.get(), CableSetup.ADVANCED_CONNECTOR_YELLOW.get(), CableSetup.ADVANCED_CONNECTOR_GREEN.get(), CableSetup.ADVANCED_CONNECTOR_RED.get(), CableSetup.ADVANCED_CONNECTOR_ROUTING.get())
//                .build(new ResourceLocation(XNet.MODID, "advanced_connectors"));
    }

    @Override
    public String getName() {
        return "XNet Tags";
    }
}
