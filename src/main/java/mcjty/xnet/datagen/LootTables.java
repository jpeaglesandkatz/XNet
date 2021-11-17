package mcjty.xnet.datagen;

import mcjty.lib.datagen.BaseLootTableProvider;
import mcjty.xnet.modules.cables.CableColor;
import mcjty.xnet.modules.cables.CableModule;
import mcjty.xnet.modules.cables.blocks.GenericCableBlock;
import mcjty.xnet.modules.controller.ControllerModule;
import mcjty.xnet.modules.router.RouterModule;
import mcjty.xnet.modules.various.VariousModule;
import mcjty.xnet.modules.wireless.WirelessRouterModule;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.conditions.BlockStateProperty;

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
        lootTables.put(ControllerModule.CONTROLLER.get(), createStandardTable("controller", ControllerModule.CONTROLLER.get()));
        lootTables.put(RouterModule.ROUTER.get(), createStandardTable("router", RouterModule.ROUTER.get()));
        lootTables.put(WirelessRouterModule.WIRELESS_ROUTER.get(), createStandardTable("wireless_router", WirelessRouterModule.WIRELESS_ROUTER.get()));

        lootTables.put(CableModule.NETCABLE.get(), LootTable.lootTable()
                .withPool(getLootTableEntry("cable_blue", CableModule.NETCABLE.get(), CableModule.NETCABLE_BLUE.get(), CableColor.BLUE))
                .withPool(getLootTableEntry("cable_red", CableModule.NETCABLE.get(), CableModule.NETCABLE_RED.get(), CableColor.RED))
                .withPool(getLootTableEntry("cable_green", CableModule.NETCABLE.get(), CableModule.NETCABLE_GREEN.get(), CableColor.GREEN))
                .withPool(getLootTableEntry("cable_yellow", CableModule.NETCABLE.get(), CableModule.NETCABLE_YELLOW.get(), CableColor.YELLOW))
                .withPool(getLootTableEntry("cable_routing", CableModule.NETCABLE.get(), CableModule.NETCABLE_ROUTING.get(), CableColor.ROUTING)));
        lootTables.put(CableModule.CONNECTOR.get(), LootTable.lootTable()
                .withPool(getLootTableEntry("connector_blue", CableModule.CONNECTOR.get(), CableModule.CONNECTOR_BLUE.get(), CableColor.BLUE))
                .withPool(getLootTableEntry("connector_red", CableModule.CONNECTOR.get(), CableModule.CONNECTOR_RED.get(), CableColor.RED))
                .withPool(getLootTableEntry("connector_green", CableModule.CONNECTOR.get(), CableModule.CONNECTOR_GREEN.get(), CableColor.GREEN))
                .withPool(getLootTableEntry("connector_yellow", CableModule.CONNECTOR.get(), CableModule.CONNECTOR_YELLOW.get(), CableColor.YELLOW))
                .withPool(getLootTableEntry("connector_routing", CableModule.CONNECTOR.get(), CableModule.CONNECTOR_ROUTING.get(), CableColor.ROUTING)));
        lootTables.put(CableModule.ADVANCED_CONNECTOR.get(), LootTable.lootTable()
                .withPool(getLootTableEntry("advanced_connector_blue", CableModule.ADVANCED_CONNECTOR.get(), CableModule.ADVANCED_CONNECTOR_BLUE.get(), CableColor.BLUE))
                .withPool(getLootTableEntry("advanced_connector_red", CableModule.ADVANCED_CONNECTOR.get(), CableModule.ADVANCED_CONNECTOR_RED.get(), CableColor.RED))
                .withPool(getLootTableEntry("advanced_connector_green", CableModule.ADVANCED_CONNECTOR.get(), CableModule.ADVANCED_CONNECTOR_GREEN.get(), CableColor.GREEN))
                .withPool(getLootTableEntry("advanced_connector_yellow", CableModule.ADVANCED_CONNECTOR.get(), CableModule.ADVANCED_CONNECTOR_YELLOW.get(), CableColor.YELLOW))
                .withPool(getLootTableEntry("advanced_connector_routing", CableModule.ADVANCED_CONNECTOR.get(), CableModule.ADVANCED_CONNECTOR_ROUTING.get(), CableColor.ROUTING)));
    }

    private LootPool.Builder getLootTableEntry(String cableName, Block cableBlock, Item cable, CableColor color) {
        return LootPool.lootPool()
                .name(cableName)
                .setRolls(ConstantRange.exactly(1))
                .add(ItemLootEntry.lootTableItem(cable))
                .when(BlockStateProperty.hasBlockStateProperties(cableBlock)
                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(GenericCableBlock.COLOR, color)));
    }

    @Nonnull
    @Override
    public String getName() {
        return "XNet LootTables";
    }
}
