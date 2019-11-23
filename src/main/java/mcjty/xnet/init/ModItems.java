package mcjty.xnet.init;

import mcjty.xnet.XNet;
import mcjty.xnet.items.ConnectorUpgradeItem;
import mcjty.xnet.items.manual.XNetManualItem;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ObjectHolder;

public class ModItems {

    @ObjectHolder(XNet.MODID + ":xnet_manual")
    public static XNetManualItem xNetManualItem;

    @ObjectHolder(XNet.MODID + ":connector_upgrade")
    public static ConnectorUpgradeItem upgradeItem;

    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new XNetManualItem());
        event.getRegistry().register(new ConnectorUpgradeItem());
    }

//    @SideOnly(Side.CLIENT)
//    public static void initModels() {
//        xNetManualItem.initModel();
//        upgradeItem.initModel();
//    }
}
