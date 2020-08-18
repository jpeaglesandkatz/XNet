package mcjty.xnet.datagen;

import mcjty.lib.datagen.BaseLootTableProvider;
import mcjty.xnet.modules.cables.CableColor;
import mcjty.xnet.modules.cables.CableSetup;
import mcjty.xnet.modules.cables.blocks.GenericCableBlock;
import mcjty.xnet.modules.controller.ControllerSetup;
import mcjty.xnet.modules.router.RouterSetup;
import mcjty.xnet.modules.various.VariousSetup;
import mcjty.xnet.modules.wireless.WirelessRouterSetup;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.conditions.BlockStateProperty;

public class LootTables extends BaseLootTableProvider {

    public LootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
        lootTables.put(WirelessRouterSetup.ANTENNA.get(), createSimpleTable("antenna", WirelessRouterSetup.ANTENNA.get()));
        lootTables.put(WirelessRouterSetup.ANTENNA_BASE.get(), createSimpleTable("antenna_base", WirelessRouterSetup.ANTENNA_BASE.get()));
        lootTables.put(WirelessRouterSetup.ANTENNA_DISH.get(), createSimpleTable("antenna_dish", WirelessRouterSetup.ANTENNA_DISH.get()));
        lootTables.put(VariousSetup.REDSTONE_PROXY.get(), createSimpleTable("redstoneproxy", VariousSetup.REDSTONE_PROXY.get()));
        lootTables.put(VariousSetup.REDSTONE_PROXY_UPD.get(), createSimpleTable("redstoneproxy_upd", VariousSetup.REDSTONE_PROXY_UPD.get()));
        lootTables.put(ControllerSetup.CONTROLLER.get(), createStandardTable("controller", ControllerSetup.CONTROLLER.get()));
        lootTables.put(RouterSetup.ROUTER.get(), createStandardTable("router", RouterSetup.ROUTER.get()));
        lootTables.put(WirelessRouterSetup.WIRELESS_ROUTER.get(), createStandardTable("wireless_router", WirelessRouterSetup.WIRELESS_ROUTER.get()));

        lootTables.put(CableSetup.NETCABLE.get(), LootTable.builder()
                .addLootPool(getLootTableEntry("cable_blue", CableSetup.NETCABLE.get(), CableSetup.NETCABLE_BLUE.get(), CableColor.BLUE))
                .addLootPool(getLootTableEntry("cable_red", CableSetup.NETCABLE.get(), CableSetup.NETCABLE_RED.get(), CableColor.RED))
                .addLootPool(getLootTableEntry("cable_green", CableSetup.NETCABLE.get(), CableSetup.NETCABLE_GREEN.get(), CableColor.GREEN))
                .addLootPool(getLootTableEntry("cable_yellow", CableSetup.NETCABLE.get(), CableSetup.NETCABLE_YELLOW.get(), CableColor.YELLOW))
                .addLootPool(getLootTableEntry("cable_routing", CableSetup.NETCABLE.get(), CableSetup.NETCABLE_ROUTING.get(), CableColor.ROUTING)));
        lootTables.put(CableSetup.CONNECTOR.get(), LootTable.builder()
                .addLootPool(getLootTableEntry("connector_blue", CableSetup.CONNECTOR.get(), CableSetup.CONNECTOR_BLUE.get(), CableColor.BLUE))
                .addLootPool(getLootTableEntry("connector_red", CableSetup.CONNECTOR.get(), CableSetup.CONNECTOR_RED.get(), CableColor.RED))
                .addLootPool(getLootTableEntry("connector_green", CableSetup.CONNECTOR.get(), CableSetup.CONNECTOR_GREEN.get(), CableColor.GREEN))
                .addLootPool(getLootTableEntry("connector_yellow", CableSetup.CONNECTOR.get(), CableSetup.CONNECTOR_YELLOW.get(), CableColor.YELLOW))
                .addLootPool(getLootTableEntry("connector_routing", CableSetup.CONNECTOR.get(), CableSetup.CONNECTOR_ROUTING.get(), CableColor.ROUTING)));
        lootTables.put(CableSetup.ADVANCED_CONNECTOR.get(), LootTable.builder()
                .addLootPool(getLootTableEntry("advanced_connector_blue", CableSetup.ADVANCED_CONNECTOR.get(), CableSetup.ADVANCED_CONNECTOR_BLUE.get(), CableColor.BLUE))
                .addLootPool(getLootTableEntry("advanced_connector_red", CableSetup.ADVANCED_CONNECTOR.get(), CableSetup.ADVANCED_CONNECTOR_RED.get(), CableColor.RED))
                .addLootPool(getLootTableEntry("advanced_connector_green", CableSetup.ADVANCED_CONNECTOR.get(), CableSetup.ADVANCED_CONNECTOR_GREEN.get(), CableColor.GREEN))
                .addLootPool(getLootTableEntry("advanced_connector_yellow", CableSetup.ADVANCED_CONNECTOR.get(), CableSetup.ADVANCED_CONNECTOR_YELLOW.get(), CableColor.YELLOW))
                .addLootPool(getLootTableEntry("advanced_connector_routing", CableSetup.ADVANCED_CONNECTOR.get(), CableSetup.ADVANCED_CONNECTOR_ROUTING.get(), CableColor.ROUTING)));
    }

    private LootPool.Builder getLootTableEntry(String cableName, Block cableBlock, Item cable, CableColor color) {
        return LootPool.builder()
                .name(cableName)
                .rolls(ConstantRange.of(1))
                .addEntry(ItemLootEntry.builder(cable))
                .acceptCondition(BlockStateProperty.builder(cableBlock)
                        .fromProperties(StatePropertiesPredicate.Builder.newBuilder().withProp(GenericCableBlock.COLOR, color)));
    }

    @Override
    public String getName() {
        return "XNet LootTables";
    }
}
