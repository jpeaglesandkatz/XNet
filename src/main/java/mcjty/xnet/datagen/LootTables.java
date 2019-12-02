package mcjty.xnet.datagen;

import mcjty.lib.datagen.BaseLootTableProvider;
import mcjty.xnet.modules.cables.CableColor;
import mcjty.xnet.modules.cables.CableSetup;
import mcjty.xnet.modules.cables.blocks.GenericCableBlock;
import mcjty.xnet.modules.various.VariousSetup;
import mcjty.xnet.modules.controller.ControllerSetup;
import mcjty.xnet.modules.router.RouterSetup;
import mcjty.xnet.modules.wireless.WirelessRouterSetup;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.world.storage.loot.ConstantRange;
import net.minecraft.world.storage.loot.ItemLootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.conditions.BlockStateProperty;

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

        lootTables.put(CableSetup.NETCABLE, LootTable.builder()
                .addLootPool(getLootTableEntry("cable_blue", CableSetup.NETCABLE, CableSetup.NETCABLE_BLUE, CableColor.BLUE))
                .addLootPool(getLootTableEntry("cable_red", CableSetup.NETCABLE, CableSetup.NETCABLE_RED, CableColor.RED))
                .addLootPool(getLootTableEntry("cable_green", CableSetup.NETCABLE, CableSetup.NETCABLE_GREEN, CableColor.GREEN))
                .addLootPool(getLootTableEntry("cable_yellow", CableSetup.NETCABLE, CableSetup.NETCABLE_YELLOW, CableColor.YELLOW))
                .addLootPool(getLootTableEntry("cable_routing", CableSetup.NETCABLE, CableSetup.NETCABLE_ROUTING, CableColor.ROUTING)));
        lootTables.put(CableSetup.CONNECTOR, LootTable.builder()
                .addLootPool(getLootTableEntry("connector_blue", CableSetup.CONNECTOR, CableSetup.CONNECTOR_BLUE, CableColor.BLUE))
                .addLootPool(getLootTableEntry("connector_red", CableSetup.CONNECTOR, CableSetup.CONNECTOR_RED, CableColor.RED))
                .addLootPool(getLootTableEntry("connector_green", CableSetup.CONNECTOR, CableSetup.CONNECTOR_GREEN, CableColor.GREEN))
                .addLootPool(getLootTableEntry("connector_yellow", CableSetup.CONNECTOR, CableSetup.CONNECTOR_YELLOW, CableColor.YELLOW))
                .addLootPool(getLootTableEntry("connector_routing", CableSetup.CONNECTOR, CableSetup.CONNECTOR_ROUTING, CableColor.ROUTING)));
        lootTables.put(CableSetup.ADVANCED_CONNECTOR, LootTable.builder()
                .addLootPool(getLootTableEntry("advanced_connector_blue", CableSetup.ADVANCED_CONNECTOR, CableSetup.ADVANCED_CONNECTOR_BLUE, CableColor.BLUE))
                .addLootPool(getLootTableEntry("advanced_connector_red", CableSetup.ADVANCED_CONNECTOR, CableSetup.ADVANCED_CONNECTOR_RED, CableColor.RED))
                .addLootPool(getLootTableEntry("advanced_connector_green", CableSetup.ADVANCED_CONNECTOR, CableSetup.ADVANCED_CONNECTOR_GREEN, CableColor.GREEN))
                .addLootPool(getLootTableEntry("advanced_connector_yellow", CableSetup.ADVANCED_CONNECTOR, CableSetup.ADVANCED_CONNECTOR_YELLOW, CableColor.YELLOW))
                .addLootPool(getLootTableEntry("advanced_connector_routing", CableSetup.ADVANCED_CONNECTOR, CableSetup.ADVANCED_CONNECTOR_ROUTING, CableColor.ROUTING)));
    }

    private LootPool.Builder getLootTableEntry(String cable_blue, Block cableBlock, Item cable, CableColor blue) {
        return LootPool.builder()
                .name(cable_blue)
                .rolls(ConstantRange.of(1))
                .addEntry(ItemLootEntry.builder(cable))
                .acceptCondition(BlockStateProperty.builder(cableBlock).with(GenericCableBlock.COLOR, blue));
    }

    @Override
    public String getName() {
        return "XNet LootTables";
    }
}
