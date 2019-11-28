package mcjty.xnet.datagen;

import mcjty.lib.datagen.BaseLootTableProvider;
import mcjty.xnet.modules.cables.CableSetup;
import mcjty.xnet.modules.various.VariousSetup;
import mcjty.xnet.modules.controller.ControllerSetup;
import mcjty.xnet.modules.router.RouterSetup;
import mcjty.xnet.modules.wireless.WirelessRouterSetup;
import net.minecraft.data.DataGenerator;

public class LootTables extends BaseLootTableProvider {

    public LootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
        lootTables.put(WirelessRouterSetup.ANTENNA, createSimpleTable("antenna", WirelessRouterSetup.ANTENNA));
        lootTables.put(WirelessRouterSetup.ANTENNA_BASE, createSimpleTable("antenna_base", WirelessRouterSetup.ANTENNA_BASE));
        lootTables.put(WirelessRouterSetup.ANTENNA_DISH, createSimpleTable("antenna_dish", WirelessRouterSetup.ANTENNA_DISH));
        lootTables.put(VariousSetup.REDSTONE_PROXY, createSimpleTable("redstoneproxy", VariousSetup.REDSTONE_PROXY));
        lootTables.put(VariousSetup.REDSTONE_PROXY_UPD, createSimpleTable("redstoneproxy_upd", VariousSetup.REDSTONE_PROXY_UPD));
        lootTables.put(ControllerSetup.CONTROLLER, createStandardTable("controller", ControllerSetup.CONTROLLER));
        lootTables.put(RouterSetup.ROUTER, createStandardTable("router", RouterSetup.ROUTER));
        lootTables.put(WirelessRouterSetup.WIRELESS_ROUTER, createStandardTable("wireless_router", WirelessRouterSetup.WIRELESS_ROUTER));

        // @todo 1.14 the loottables below are not correct with regards to color!
        lootTables.put(CableSetup.NETCABLE, createSimpleTable("cable", CableSetup.NETCABLE_BLUE));
        lootTables.put(CableSetup.CONNECTOR, createStandardTable("connector", CableSetup.CONNECTOR));
        lootTables.put(CableSetup.ADVANCED_CONNECTOR, createStandardTable("advanced_connector", CableSetup.ADVANCED_CONNECTOR));
    }

    @Override
    public String getName() {
        return "XNet LootTables";
    }
}
