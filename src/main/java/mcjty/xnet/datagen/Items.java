package mcjty.xnet.datagen;

import mcjty.lib.datagen.BaseItemModelProvider;
import mcjty.xnet.XNet;
import mcjty.xnet.modules.controller.ControllerSetup;
import mcjty.xnet.modules.router.RouterSetup;
import mcjty.xnet.modules.various.VariousSetup;
import mcjty.xnet.modules.wireless.WirelessRouterSetup;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

public class Items extends BaseItemModelProvider {

    public Items(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, XNet.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        parentedItem(VariousSetup.REDSTONE_PROXY_ITEM.get(), "block/redstone_proxy");
        parentedItem(VariousSetup.REDSTONE_PROXY_UPD_ITEM.get(), "block/redstone_proxy_upd");
        parentedItem(ControllerSetup.CONTROLLER_ITEM.get(), "block/controller");
        parentedItem(RouterSetup.ROUTER_ITEM.get(), "block/router");
        parentedItem(WirelessRouterSetup.WIRELESS_ROUTER_ITEM.get(), "block/wireless_router");
    }

    @Override
    public String getName() {
        return "XNet Item Models";
    }
}
