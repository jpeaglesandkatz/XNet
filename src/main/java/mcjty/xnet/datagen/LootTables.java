package mcjty.xnet.datagen;

import mcjty.lib.datagen.BaseLootTableProvider;
import mcjty.xnet.modules.controller.ControllerModule;
import mcjty.xnet.modules.router.RouterModule;
import mcjty.xnet.modules.various.VariousModule;
import mcjty.xnet.modules.wireless.WirelessRouterModule;
import net.minecraft.data.DataGenerator;

import javax.annotation.Nonnull;

public class LootTables extends BaseLootTableProvider {

    public LootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
        lootTables.put(WirelessRouterModule.ANTENNA.get(), createSimpleTable("antenna", WirelessRouterModule.ANTENNA.get()));
        lootTables.put(WirelessRouterModule.ANTENNA_BASE.get(), createSimpleTable("antenna_base", WirelessRouterModule.ANTENNA_BASE.get()));
        lootTables.put(WirelessRouterModule.ANTENNA_DISH.get(), createSimpleTable("antenna_dish", WirelessRouterModule.ANTENNA_DISH.get()));
        lootTables.put(VariousModule.REDSTONE_PROXY.get(), createSimpleTable("redstoneproxy", VariousModule.REDSTONE_PROXY.get()));
        lootTables.put(VariousModule.REDSTONE_PROXY_UPD.get(), createSimpleTable("redstoneproxy_upd", VariousModule.REDSTONE_PROXY_UPD.get()));
        lootTables.put(ControllerModule.CONTROLLER.get(), createStandardTable("controller", ControllerModule.CONTROLLER.get(), ControllerModule.TYPE_CONTROLLER.get()));
        lootTables.put(RouterModule.ROUTER.get(), createStandardTable("router", RouterModule.ROUTER.get(), RouterModule.TYPE_ROUTER.get()));
        lootTables.put(WirelessRouterModule.WIRELESS_ROUTER.get(), createStandardTable("wireless_router", WirelessRouterModule.WIRELESS_ROUTER.get(), WirelessRouterModule.TYPE_WIRELESS_ROUTER.get()));
    }

    @Nonnull
    @Override
    public String getName() {
        return "XNet LootTables";
    }
}
