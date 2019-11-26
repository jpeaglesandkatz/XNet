package mcjty.xnet.datagen;

import mcjty.lib.datagen.BaseLootTableProvider;
import mcjty.xnet.blocks.cables.NetCableSetup;
import mcjty.xnet.init.ModBlocks;
import net.minecraft.data.DataGenerator;

public class LootTables extends BaseLootTableProvider {

    public LootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
        lootTables.put(ModBlocks.ANTENNA, createSimpleTable("antenna", ModBlocks.ANTENNA));
        lootTables.put(ModBlocks.ANTENNA_BASE, createSimpleTable("antenna_base", ModBlocks.ANTENNA_BASE));
        lootTables.put(ModBlocks.ANTENNA_DISH, createSimpleTable("antenna_dish", ModBlocks.ANTENNA_DISH));
        lootTables.put(ModBlocks.REDSTONE_PROXY, createSimpleTable("redstoneproxy", ModBlocks.REDSTONE_PROXY));
        lootTables.put(ModBlocks.REDSTONE_PROXY_UPD, createSimpleTable("redstoneproxy_upd", ModBlocks.REDSTONE_PROXY_UPD));
        lootTables.put(ModBlocks.CONTROLLER, createStandardTable("controller", ModBlocks.CONTROLLER));
        lootTables.put(ModBlocks.ROUTER, createStandardTable("router", ModBlocks.ROUTER));
        lootTables.put(ModBlocks.WIRELESS_ROUTER, createStandardTable("wireless_router", ModBlocks.WIRELESS_ROUTER));

        lootTables.put(NetCableSetup.NETCABLE, createSimpleTable("cable", NetCableSetup.NETCABLE));
        lootTables.put(NetCableSetup.CONNECTOR, createStandardTable("connector", NetCableSetup.CONNECTOR));
        lootTables.put(NetCableSetup.ADVANCED_CONNECTOR, createStandardTable("advanced_connector", NetCableSetup.ADVANCED_CONNECTOR));
    }

    @Override
    public String getName() {
        return "XNet LootTables";
    }
}
