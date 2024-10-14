package mcjty.xnet.modules.cables;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.modules.IModule;
import mcjty.lib.varia.TagTools;
import mcjty.xnet.XNet;
import mcjty.xnet.modules.cables.blocks.*;
import mcjty.xnet.modules.cables.blocks.GenericCableBlock.CableBlockType;
import mcjty.xnet.modules.cables.client.ClientSetup;
import mcjty.xnet.modules.cables.client.GuiConnector;
import mcjty.xnet.modules.cables.data.CableItemData;
import mcjty.xnet.modules.cables.data.ConnectorData;
import mcjty.xnet.setup.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;
import java.util.function.Supplier;

import static mcjty.lib.datagen.DataGen.has;
import static mcjty.xnet.XNet.tab;
import static mcjty.xnet.setup.Registration.*;

public class CableModule implements IModule {

    public static final DeferredBlock<NetCableBlock> NETCABLE = BLOCKS.register("netcable", () -> new NetCableBlock(CableBlockType.CABLE));
    public static final DeferredItem<Item> NETCABLE_RED = ITEMS.register("netcable_red", tab(() -> new ColorBlockItem(NETCABLE.get(), Registration.createStandardProperties(), CableColor.RED)));
    public static final DeferredItem<Item> NETCABLE_GREEN = ITEMS.register("netcable_green", tab(() -> new ColorBlockItem(NETCABLE.get(), Registration.createStandardProperties(), CableColor.GREEN)));
    public static final DeferredItem<Item> NETCABLE_BLUE = ITEMS.register("netcable_blue", tab(() -> new ColorBlockItem(NETCABLE.get(), Registration.createStandardProperties(), CableColor.BLUE)));
    public static final DeferredItem<Item> NETCABLE_YELLOW = ITEMS.register("netcable_yellow", tab(() -> new ColorBlockItem(NETCABLE.get(), Registration.createStandardProperties(), CableColor.YELLOW)));
    public static final DeferredItem<Item> NETCABLE_ROUTING = ITEMS.register("netcable_routing", tab(() -> new ColorBlockItem(NETCABLE.get(), Registration.createStandardProperties(), CableColor.ROUTING)));

    public static final DeferredBlock<ConnectorBlock> CONNECTOR = BLOCKS.register("connector", () -> new ConnectorBlock(CableBlockType.CONNECTOR));
    public static final DeferredItem<Item> CONNECTOR_RED = ITEMS.register("connector_red", tab(() -> new ColorBlockItem(CONNECTOR.get(), Registration.createStandardProperties(), CableColor.RED)));
    public static final DeferredItem<Item> CONNECTOR_GREEN = ITEMS.register("connector_green", tab(() -> new ColorBlockItem(CONNECTOR.get(), Registration.createStandardProperties(), CableColor.GREEN)));
    public static final DeferredItem<Item> CONNECTOR_BLUE = ITEMS.register("connector_blue", tab(() -> new ColorBlockItem(CONNECTOR.get(), Registration.createStandardProperties(), CableColor.BLUE)));
    public static final DeferredItem<Item> CONNECTOR_YELLOW = ITEMS.register("connector_yellow", tab(() -> new ColorBlockItem(CONNECTOR.get(), Registration.createStandardProperties(), CableColor.YELLOW)));
    public static final DeferredItem<Item> CONNECTOR_ROUTING = ITEMS.register("connector_routing", tab(() -> new ColorBlockItem(CONNECTOR.get(), Registration.createStandardProperties(), CableColor.ROUTING)));

    public static final DeferredBlock<AdvancedConnectorBlock> ADVANCED_CONNECTOR = BLOCKS.register("advanced_connector", () -> new AdvancedConnectorBlock(CableBlockType.ADVANCED_CONNECTOR));
    public static final DeferredItem<Item> ADVANCED_CONNECTOR_RED = ITEMS.register("advanced_connector_red", tab(() -> new ColorBlockItem(ADVANCED_CONNECTOR.get(), Registration.createStandardProperties(), CableColor.RED)));
    public static final DeferredItem<Item> ADVANCED_CONNECTOR_GREEN = ITEMS.register("advanced_connector_green", tab(() -> new ColorBlockItem(ADVANCED_CONNECTOR.get(), Registration.createStandardProperties(), CableColor.GREEN)));
    public static final DeferredItem<Item> ADVANCED_CONNECTOR_BLUE = ITEMS.register("advanced_connector_blue", tab(() -> new ColorBlockItem(ADVANCED_CONNECTOR.get(), Registration.createStandardProperties(), CableColor.BLUE)));
    public static final DeferredItem<Item> ADVANCED_CONNECTOR_YELLOW = ITEMS.register("advanced_connector_yellow", tab(() -> new ColorBlockItem(ADVANCED_CONNECTOR.get(), Registration.createStandardProperties(), CableColor.YELLOW)));
    public static final DeferredItem<Item> ADVANCED_CONNECTOR_ROUTING = ITEMS.register("advanced_connector_routing", tab(() -> new ColorBlockItem(ADVANCED_CONNECTOR.get(), Registration.createStandardProperties(), CableColor.ROUTING)));

    public static final Supplier<BlockEntityType<?>> TYPE_CONNECTOR = TILES.register("connector", () -> BlockEntityType.Builder.of(ConnectorTileEntity::new, CONNECTOR.get()).build(null));
    public static final Supplier<BlockEntityType<?>> TYPE_ADVANCED_CONNECTOR = TILES.register("advanced_connector", () -> BlockEntityType.Builder.of(AdvancedConnectorTileEntity::new, ADVANCED_CONNECTOR.get()).build(null));

    public static final Supplier<MenuType<GenericContainer>> CONTAINER_CONNECTOR = CONTAINERS.register("connector", GenericContainer::createContainerType);

    public static final TagKey<Item> TAG_CABLES = TagTools.createItemTagKey(ResourceLocation.fromNamespaceAndPath(XNet.MODID, "cables"));
    public static final TagKey<Item> TAG_CONNECTORS = TagTools.createItemTagKey(ResourceLocation.fromNamespaceAndPath(XNet.MODID, "connectors"));
    public static final TagKey<Item> TAG_ADVANCED_CONNECTORS = TagTools.createItemTagKey(ResourceLocation.fromNamespaceAndPath(XNet.MODID, "advanced_connectors"));

    public static final Supplier<AttachmentType<ConnectorData>> CONNECTOR_DATA = ATTACHMENT_TYPES.register(
            "connector_data", () -> AttachmentType.builder(() -> ConnectorData.EMPTY)
                    .serialize(ConnectorData.CODEC)
                    .build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ConnectorData>> ITEM_CONNECTOR_DATA = COMPONENTS.registerComponentType(
            "connector_data",
            builder -> builder
                    .persistent(ConnectorData.CODEC)
                    .networkSynchronized(ConnectorData.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CableItemData>> ITEM_CABLE_ITEM_DATA = COMPONENTS.registerComponentType(
            "cable_item_data",
            builder -> builder
                    .persistent(CableItemData.CODEC)
                    .networkSynchronized(CableItemData.STREAM_CODEC));

    public CableModule(IEventBus bus, Dist dist) {
        if (dist.isClient()) {
            bus.addListener(ClientSetup::modelInit);
        }
        bus.addListener(this::registerScreens);
    }

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        ClientSetup.initClient();
    }

    public void registerScreens(RegisterMenuScreensEvent event) {
        GuiConnector.register(event);
    }

    @Override
    public void initConfig(IEventBus bus) {

    }

    @Override
    public void initDatagen(DataGen dataGen, HolderLookup.Provider provider) {
        dataGen.add(
                Dob.blockBuilder(ADVANCED_CONNECTOR)
                        // @todo 1.21 correct loot tables for copy of data
                        .loot(p -> {
                            p.addLootTable(ADVANCED_CONNECTOR.get(), LootTable.lootTable()
                                    .withPool(DataGenHelper.getLootTableEntry("advanced_connector_blue", ADVANCED_CONNECTOR.get(), ADVANCED_CONNECTOR_BLUE.get(), CableColor.BLUE))
                                    .withPool(DataGenHelper.getLootTableEntry("advanced_connector_red", ADVANCED_CONNECTOR.get(), ADVANCED_CONNECTOR_RED.get(), CableColor.RED))
                                    .withPool(DataGenHelper.getLootTableEntry("advanced_connector_green", ADVANCED_CONNECTOR.get(), ADVANCED_CONNECTOR_GREEN.get(), CableColor.GREEN))
                                    .withPool(DataGenHelper.getLootTableEntry("advanced_connector_yellow", ADVANCED_CONNECTOR.get(), ADVANCED_CONNECTOR_YELLOW.get(), CableColor.YELLOW))
                                    .withPool(DataGenHelper.getLootTableEntry("advanced_connector_routing", ADVANCED_CONNECTOR.get(), ADVANCED_CONNECTOR_ROUTING.get(), CableColor.ROUTING)));

                        })
                        .stonePickaxeTags(),
                Dob.blockBuilder(CONNECTOR)
                        // @todo 1.21 correct loot tables for copy of data
                        .loot(p -> {
                            p.addLootTable(CONNECTOR.get(), LootTable.lootTable()
                                    .withPool(DataGenHelper.getLootTableEntry("connector_blue", CONNECTOR.get(), CONNECTOR_BLUE.get(), CableColor.BLUE))
                                    .withPool(DataGenHelper.getLootTableEntry("connector_red", CONNECTOR.get(), CONNECTOR_RED.get(), CableColor.RED))
                                    .withPool(DataGenHelper.getLootTableEntry("connector_green", CONNECTOR.get(), CONNECTOR_GREEN.get(), CableColor.GREEN))
                                    .withPool(DataGenHelper.getLootTableEntry("connector_yellow", CONNECTOR.get(), CONNECTOR_YELLOW.get(), CableColor.YELLOW))
                                    .withPool(DataGenHelper.getLootTableEntry("connector_routing", CONNECTOR.get(), CONNECTOR_ROUTING.get(), CableColor.ROUTING)));

                        })
                        .stonePickaxeTags(),
                Dob.blockBuilder(NETCABLE)
                        .loot(p -> {
                            p.addLootTable(NETCABLE.get(), LootTable.lootTable()
                                    .withPool(DataGenHelper.getLootTableEntry("cable_blue", NETCABLE.get(), NETCABLE_BLUE.get(), CableColor.BLUE))
                                    .withPool(DataGenHelper.getLootTableEntry("cable_red", NETCABLE.get(), NETCABLE_RED.get(), CableColor.RED))
                                    .withPool(DataGenHelper.getLootTableEntry("cable_green", NETCABLE.get(), NETCABLE_GREEN.get(), CableColor.GREEN))
                                    .withPool(DataGenHelper.getLootTableEntry("cable_yellow", NETCABLE.get(), NETCABLE_YELLOW.get(), CableColor.YELLOW))
                                    .withPool(DataGenHelper.getLootTableEntry("cable_routing", NETCABLE.get(), NETCABLE_ROUTING.get(), CableColor.ROUTING)));
                        })
                        .stonePickaxeTags(),

                Dob.itemBuilder(NETCABLE_BLUE)
                        .itemTags(List.of(TAG_CABLES))
                        .shaped(builder -> builder
                                        .define('g', Tags.Items.NUGGETS_GOLD)
                                        .define('s', Items.STRING)
                                        .define('1', Tags.Items.DYES_BLUE)
                                        .unlockedBy("nugget", has(Items.GOLD_NUGGET)),
                                16,
                                "s1s", "rgr", "srs")
                        .shapeless("netcable_blue_dye", builder -> builder
                                .requires(Tags.Items.DYES_BLUE)
                                .requires(CableModule.TAG_CABLES)
                                .unlockedBy("chest", has(Items.CHEST))),
                Dob.itemBuilder(NETCABLE_GREEN)
                        .itemTags(List.of(TAG_CABLES))
                        .shaped(builder -> builder
                                        .define('g', Tags.Items.NUGGETS_GOLD)
                                        .define('s', Items.STRING)
                                        .define('1', Tags.Items.DYES_GREEN)
                                        .unlockedBy("nugget", has(Items.GOLD_NUGGET)),
                                16,
                                "s1s", "rgr", "srs")
                        .shapeless("netcable_green_dye", builder -> builder
                                .requires(Tags.Items.DYES_GREEN)
                                .requires(CableModule.TAG_CABLES)
                                .unlockedBy("chest", has(Items.CHEST))),
                Dob.itemBuilder(NETCABLE_RED)
                        .itemTags(List.of(TAG_CABLES))
                        .shaped(builder -> builder
                                        .define('g', Tags.Items.NUGGETS_GOLD)
                                        .define('s', Items.STRING)
                                        .define('1', Tags.Items.DYES_RED)
                                        .unlockedBy("nugget", has(Items.GOLD_NUGGET)),
                                16,
                                "s1s", "rgr", "srs")
                        .shapeless("netcable_red_dye", builder -> builder
                                .requires(Tags.Items.DYES_RED)
                                .requires(CableModule.TAG_CABLES)
                                .unlockedBy("chest", has(Items.CHEST))),
                Dob.itemBuilder(NETCABLE_YELLOW)
                        .itemTags(List.of(TAG_CABLES))
                        .shaped(builder -> builder
                                        .define('g', Tags.Items.NUGGETS_GOLD)
                                        .define('s', Items.STRING)
                                        .define('1', Tags.Items.DYES_YELLOW)
                                        .unlockedBy("nugget", has(Items.GOLD_NUGGET)),
                                16,
                                "s1s", "rgr", "srs")
                        .shapeless("netcable_yellow_dye", builder -> builder
                                .requires(Tags.Items.DYES_YELLOW)
                                .requires(CableModule.TAG_CABLES)
                                .unlockedBy("chest", has(Items.CHEST))),
                Dob.itemBuilder(NETCABLE_ROUTING)
                        .itemTags(List.of(TAG_CABLES))
                        .shaped(builder -> builder
                                        .define('g', Tags.Items.NUGGETS_GOLD)
                                        .define('s', Items.STRING)
                                        .define('1', Tags.Items.DYES_BLACK)
                                        .unlockedBy("nugget", has(Items.GOLD_NUGGET)),
                                32,
                                "s1s", "rgr", "srs"),
                Dob.itemBuilder(CONNECTOR_BLUE)
                        .itemTags(List.of(TAG_CONNECTORS))
                        .shaped(builder -> builder
                                        .define('g', Tags.Items.INGOTS_GOLD)
                                        .define('1', Tags.Items.DYES_BLUE)
                                        .define('C', Tags.Items.CHESTS)
                                        .unlockedBy("chest", has(Items.CHEST)),
                                "1C1", "rgr", "1r1")
                        .shapeless("connector_blue_dye", builder -> builder
                                .requires(Tags.Items.DYES_BLUE)
                                .requires(CableModule.TAG_CONNECTORS)
                                .unlockedBy("chest", has(Items.CHEST))),
                Dob.itemBuilder(CONNECTOR_GREEN)
                        .itemTags(List.of(TAG_CONNECTORS))
                        .shaped(builder -> builder
                                        .define('g', Tags.Items.INGOTS_GOLD)
                                        .define('1', Tags.Items.DYES_GREEN)
                                        .define('C', Tags.Items.CHESTS)
                                        .unlockedBy("chest", has(Items.CHEST)),
                                "1C1", "rgr", "1r1")
                        .shapeless("connector_green_dye", builder -> builder
                                .requires(Tags.Items.DYES_GREEN)
                                .requires(CableModule.TAG_CONNECTORS)
                                .unlockedBy("chest", has(Items.CHEST))),
                Dob.itemBuilder(CONNECTOR_RED)
                        .itemTags(List.of(TAG_CONNECTORS))
                        .shaped(builder -> builder
                                        .define('g', Tags.Items.INGOTS_GOLD)
                                        .define('1', Tags.Items.DYES_RED)
                                        .define('C', Tags.Items.CHESTS)
                                        .unlockedBy("chest", has(Items.CHEST)),
                                "1C1", "rgr", "1r1")
                        .shapeless("connector_red_dye", builder -> builder
                                .requires(Tags.Items.DYES_RED)
                                .requires(CableModule.TAG_CONNECTORS)
                                .unlockedBy("chest", has(Items.CHEST))),
                Dob.itemBuilder(CONNECTOR_YELLOW)
                        .itemTags(List.of(TAG_CONNECTORS))
                        .shaped(builder -> builder
                                        .define('g', Tags.Items.INGOTS_GOLD)
                                        .define('1', Tags.Items.DYES_YELLOW)
                                        .define('C', Tags.Items.CHESTS)
                                        .unlockedBy("chest", has(Items.CHEST)),
                                "1C1", "rgr", "1r1")
                        .shapeless("connector_yellow_dye", builder -> builder
                                .requires(Tags.Items.DYES_YELLOW)
                                .requires(CableModule.TAG_CONNECTORS)
                                .unlockedBy("chest", has(Items.CHEST))),
                Dob.itemBuilder(CONNECTOR_ROUTING)
                        .itemTags(List.of(TAG_CONNECTORS))
                        .shaped(builder -> builder
                                        .define('g', Tags.Items.NUGGETS_GOLD)
                                        .define('C', TAG_CONNECTORS)
                                        .unlockedBy("chest", has(Items.CHEST)),
                                "rrr", "gCg", "rrr"),
                Dob.itemBuilder(ADVANCED_CONNECTOR_BLUE)
                        .itemTags(List.of(TAG_ADVANCED_CONNECTORS))
                        .shaped(builder -> builder
                                        .define('C', CONNECTOR_BLUE.get())
                                        .unlockedBy("chest", has(Items.CHEST)),
                                "Co", "dr")
                        .shapeless("advanced_connector_blue_dye", builder -> builder
                                .requires(Tags.Items.DYES_BLUE)
                                .requires(CableModule.TAG_ADVANCED_CONNECTORS)
                                .unlockedBy("chest", has(Items.CHEST))),
                Dob.itemBuilder(ADVANCED_CONNECTOR_GREEN)
                        .itemTags(List.of(TAG_ADVANCED_CONNECTORS))
                        .shaped(builder -> builder
                                        .define('C', CONNECTOR_GREEN.get())
                                        .unlockedBy("chest", has(Items.CHEST)),
                                "Co", "dr")
                        .shapeless("advanced_connector_green_dye", builder -> builder
                                .requires(Tags.Items.DYES_GREEN)
                                .requires(CableModule.TAG_ADVANCED_CONNECTORS)
                                .unlockedBy("chest", has(Items.CHEST))),
                Dob.itemBuilder(ADVANCED_CONNECTOR_RED)
                        .itemTags(List.of(TAG_ADVANCED_CONNECTORS))
                        .shaped(builder -> builder
                                        .define('C', CONNECTOR_RED.get())
                                        .unlockedBy("chest", has(Items.CHEST)),
                                "Co", "dr")
                        .shapeless("advanced_connector_red_dye", builder -> builder
                                .requires(Tags.Items.DYES_RED)
                                .requires(CableModule.TAG_ADVANCED_CONNECTORS)
                                .unlockedBy("chest", has(Items.CHEST))),
                Dob.itemBuilder(ADVANCED_CONNECTOR_YELLOW)
                        .itemTags(List.of(TAG_ADVANCED_CONNECTORS))
                        .shaped(builder -> builder
                                        .define('C', CONNECTOR_YELLOW.get())
                                        .unlockedBy("chest", has(Items.CHEST)),
                                "Co", "dr")
                        .shapeless("advanced_connector_yellow_dye", builder -> builder
                                .requires(Tags.Items.DYES_YELLOW)
                                .requires(CableModule.TAG_ADVANCED_CONNECTORS)
                                .unlockedBy("chest", has(Items.CHEST))),
                Dob.itemBuilder(ADVANCED_CONNECTOR_ROUTING)
                        .itemTags(List.of(TAG_ADVANCED_CONNECTORS))
                        .shaped(builder -> builder
                                        .define('C', CONNECTOR_ROUTING.get())
                                        .unlockedBy("chest", has(Items.CHEST)),
                                "Co", "dr")
        );
    }

}
