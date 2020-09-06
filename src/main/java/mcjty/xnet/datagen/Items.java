package mcjty.xnet.datagen;

import mcjty.lib.datagen.BaseItemModelProvider;
import mcjty.xnet.XNet;
import mcjty.xnet.modules.controller.ControllerModule;
import mcjty.xnet.modules.router.RouterModule;
import mcjty.xnet.modules.various.VariousModule;
import mcjty.xnet.modules.wireless.WirelessRouterModule;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

public class Items extends BaseItemModelProvider {

    public Items(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, XNet.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        parentedItem(VariousModule.REDSTONE_PROXY_ITEM.get(), "block/redstone_proxy");
        parentedItem(VariousModule.REDSTONE_PROXY_UPD_ITEM.get(), "block/redstone_proxy_upd");
        parentedItem(ControllerModule.CONTROLLER_ITEM.get(), "block/controller");
        parentedItem(RouterModule.ROUTER_ITEM.get(), "block/router");
        parentedItem(WirelessRouterModule.WIRELESS_ROUTER_ITEM.get(), "block/wireless_router");
    }

    @Override
    public String getName() {
        return "XNet Item Models";
    }
}
