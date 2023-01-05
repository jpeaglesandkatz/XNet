package mcjty.xnet.modules.cables;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.datagen.DataGen;
import mcjty.lib.datagen.Dob;
import mcjty.lib.modules.IModule;
import mcjty.xnet.XNet;
import mcjty.xnet.modules.cables.blocks.*;
import mcjty.xnet.modules.cables.blocks.GenericCableBlock.CableBlockType;
import mcjty.xnet.modules.cables.client.ClientSetup;
import mcjty.xnet.modules.cables.client.GuiConnector;
import mcjty.xnet.setup.Registration;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

import static mcjty.xnet.setup.Registration.*;

public class CableModule implements IModule {

    public static final RegistryObject<NetCableBlock> NETCABLE = BLOCKS.register("netcable", () -> new NetCableBlock(CableBlockType.CABLE));
    public static final RegistryObject<Item> NETCABLE_RED = ITEMS.register("netcable_red", () -> new ColorBlockItem(NETCABLE.get(), Registration.createStandardProperties(), CableColor.RED));
    public static final RegistryObject<Item> NETCABLE_GREEN = ITEMS.register("netcable_green", () -> new ColorBlockItem(NETCABLE.get(), Registration.createStandardProperties(), CableColor.GREEN));
    public static final RegistryObject<Item> NETCABLE_BLUE = ITEMS.register("netcable_blue", () -> new ColorBlockItem(NETCABLE.get(), Registration.createStandardProperties(), CableColor.BLUE));
    public static final RegistryObject<Item> NETCABLE_YELLOW = ITEMS.register("netcable_yellow", () -> new ColorBlockItem(NETCABLE.get(), Registration.createStandardProperties(), CableColor.YELLOW));
    public static final RegistryObject<Item> NETCABLE_ROUTING = ITEMS.register("netcable_routing", () -> new ColorBlockItem(NETCABLE.get(), Registration.createStandardProperties(), CableColor.ROUTING));

    public static final RegistryObject<ConnectorBlock> CONNECTOR = BLOCKS.register("connector", () -> new ConnectorBlock(CableBlockType.CONNECTOR));
    public static final RegistryObject<Item> CONNECTOR_RED = ITEMS.register("connector_red", () -> new ColorBlockItem(CONNECTOR.get(), Registration.createStandardProperties(), CableColor.RED));
    public static final RegistryObject<Item> CONNECTOR_GREEN = ITEMS.register("connector_green", () -> new ColorBlockItem(CONNECTOR.get(), Registration.createStandardProperties(), CableColor.GREEN));
    public static final RegistryObject<Item> CONNECTOR_BLUE = ITEMS.register("connector_blue", () -> new ColorBlockItem(CONNECTOR.get(), Registration.createStandardProperties(), CableColor.BLUE));
    public static final RegistryObject<Item> CONNECTOR_YELLOW = ITEMS.register("connector_yellow", () -> new ColorBlockItem(CONNECTOR.get(), Registration.createStandardProperties(), CableColor.YELLOW));
    public static final RegistryObject<Item> CONNECTOR_ROUTING = ITEMS.register("connector_routing", () -> new ColorBlockItem(CONNECTOR.get(), Registration.createStandardProperties(), CableColor.ROUTING));

    public static final RegistryObject<AdvancedConnectorBlock> ADVANCED_CONNECTOR = BLOCKS.register("advanced_connector", () -> new AdvancedConnectorBlock(CableBlockType.ADVANCED_CONNECTOR));
    public static final RegistryObject<Item> ADVANCED_CONNECTOR_RED = ITEMS.register("advanced_connector_red", () -> new ColorBlockItem(ADVANCED_CONNECTOR.get(), Registration.createStandardProperties(), CableColor.RED));
    public static final RegistryObject<Item> ADVANCED_CONNECTOR_GREEN = ITEMS.register("advanced_connector_green", () -> new ColorBlockItem(ADVANCED_CONNECTOR.get(), Registration.createStandardProperties(), CableColor.GREEN));
    public static final RegistryObject<Item> ADVANCED_CONNECTOR_BLUE = ITEMS.register("advanced_connector_blue", () -> new ColorBlockItem(ADVANCED_CONNECTOR.get(), Registration.createStandardProperties(), CableColor.BLUE));
    public static final RegistryObject<Item> ADVANCED_CONNECTOR_YELLOW = ITEMS.register("advanced_connector_yellow", () -> new ColorBlockItem(ADVANCED_CONNECTOR.get(), Registration.createStandardProperties(), CableColor.YELLOW));
    public static final RegistryObject<Item> ADVANCED_CONNECTOR_ROUTING = ITEMS.register("advanced_connector_routing", () -> new ColorBlockItem(ADVANCED_CONNECTOR.get(), Registration.createStandardProperties(), CableColor.ROUTING));

    public static final RegistryObject<BlockEntityType<?>> TYPE_CONNECTOR = TILES.register("connector", () -> BlockEntityType.Builder.of(ConnectorTileEntity::new, CONNECTOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<?>> TYPE_ADVANCED_CONNECTOR = TILES.register("advanced_connector", () -> BlockEntityType.Builder.of(AdvancedConnectorTileEntity::new, ADVANCED_CONNECTOR.get()).build(null));

    public static final RegistryObject<MenuType<GenericContainer>> CONTAINER_CONNECTOR = CONTAINERS.register("connector", GenericContainer::createContainerType);

    public static final TagKey<Item> TAG_CABLES = TagKey.create(Registry.ITEM.key(), new ResourceLocation(XNet.MODID, "cables"));
    public static final TagKey<Item> TAG_CONNECTORS = TagKey.create(Registry.ITEM.key(), new ResourceLocation(XNet.MODID, "connectors"));
    public static final TagKey<Item> TAG_ADVANCED_CONNECTORS = TagKey.create(Registry.ITEM.key(), new ResourceLocation(XNet.MODID, "advanced_connectors"));

    public CableModule() {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::modelInit);
        });
    }

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            GuiConnector.register();
        });
        ClientSetup.initClient();
    }

    @Override
    public void initConfig() {

    }

    @Override
    public void initDatagen(DataGen dataGen) {
        dataGen.add(
                Dob.blockBuilder(ADVANCED_CONNECTOR)
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
                        .shaped(ShapedRecipeBuilder.shaped(NETCABLE_BLUE.get(), 16)
                                        .define('g', Tags.Items.NUGGETS_GOLD)
                                        .define('s', Items.STRING)
                                        .define('1', Tags.Items.DYES_BLUE)
                                        .unlockedBy("nugget", DataGen.has(Items.GOLD_NUGGET)),
                                "s1s", "rgr", "srs"),
                Dob.itemBuilder(NETCABLE_GREEN)
                        .itemTags(List.of(TAG_CABLES))
                        .shaped(ShapedRecipeBuilder.shaped(NETCABLE_GREEN.get(), 16)
                                        .define('g', Tags.Items.NUGGETS_GOLD)
                                        .define('s', Items.STRING)
                                        .define('1', Tags.Items.DYES_GREEN)
                                        .unlockedBy("nugget", DataGen.has(Items.GOLD_NUGGET)),
                                "s1s", "rgr", "srs"),
                Dob.itemBuilder(NETCABLE_RED)
                        .itemTags(List.of(TAG_CABLES))
                        .shaped(ShapedRecipeBuilder.shaped(NETCABLE_RED.get(), 16)
                                        .define('g', Tags.Items.NUGGETS_GOLD)
                                        .define('s', Items.STRING)
                                        .define('1', Tags.Items.DYES_RED)
                                        .unlockedBy("nugget", DataGen.has(Items.GOLD_NUGGET)),
                                "s1s", "rgr", "srs"),
                Dob.itemBuilder(NETCABLE_YELLOW)
                        .itemTags(List.of(TAG_CABLES))
                        .shaped(ShapedRecipeBuilder.shaped(NETCABLE_YELLOW.get(), 16)
                                        .define('g', Tags.Items.NUGGETS_GOLD)
                                        .define('s', Items.STRING)
                                        .define('1', Tags.Items.DYES_YELLOW)
                                        .unlockedBy("nugget", DataGen.has(Items.GOLD_NUGGET)),
                                "s1s", "rgr", "srs"),
                Dob.itemBuilder(NETCABLE_ROUTING)
                        .itemTags(List.of(TAG_CABLES))
                        .shaped(ShapedRecipeBuilder.shaped(NETCABLE_ROUTING.get(), 32)
                                        .define('g', Tags.Items.NUGGETS_GOLD)
                                        .define('s', Items.STRING)
                                        .define('1', Tags.Items.DYES_BLACK)
                                        .unlockedBy("nugget", DataGen.has(Items.GOLD_NUGGET)),
                                "s1s", "rgr", "srs"),
                Dob.itemBuilder(CONNECTOR_BLUE)
                        .itemTags(List.of(TAG_CONNECTORS))
                        .shaped(ShapedRecipeBuilder.shaped(CONNECTOR_BLUE.get())
                                        .define('g', Tags.Items.INGOTS_GOLD)
                                        .define('1', Tags.Items.DYES_BLUE)
                                        .define('C', Tags.Items.CHESTS)
                                        .unlockedBy("chest", DataGen.has(Items.CHEST)),
                                "1C1", "rgr", "1r1"),
                Dob.itemBuilder(CONNECTOR_GREEN)
                        .itemTags(List.of(TAG_CONNECTORS))
                        .shaped(ShapedRecipeBuilder.shaped(CONNECTOR_GREEN.get())
                                        .define('g', Tags.Items.INGOTS_GOLD)
                                        .define('1', Tags.Items.DYES_GREEN)
                                        .define('C', Tags.Items.CHESTS)
                                        .unlockedBy("chest", DataGen.has(Items.CHEST)),
                                "1C1", "rgr", "1r1"),
                Dob.itemBuilder(CONNECTOR_RED)
                        .itemTags(List.of(TAG_CONNECTORS))
                        .shaped(ShapedRecipeBuilder.shaped(CONNECTOR_RED.get())
                                        .define('g', Tags.Items.INGOTS_GOLD)
                                        .define('1', Tags.Items.DYES_RED)
                                        .define('C', Tags.Items.CHESTS)
                                        .unlockedBy("chest", DataGen.has(Items.CHEST)),
                                "1C1", "rgr", "1r1"),
                Dob.itemBuilder(CONNECTOR_YELLOW)
                        .itemTags(List.of(TAG_CONNECTORS))
                        .shaped(ShapedRecipeBuilder.shaped(CONNECTOR_YELLOW.get())
                                        .define('g', Tags.Items.INGOTS_GOLD)
                                        .define('1', Tags.Items.DYES_YELLOW)
                                        .define('C', Tags.Items.CHESTS)
                                        .unlockedBy("chest", DataGen.has(Items.CHEST)),
                                "1C1", "rgr", "1r1"),
                Dob.itemBuilder(CONNECTOR_ROUTING)
                        .itemTags(List.of(TAG_CONNECTORS))
                        .shaped(ShapedRecipeBuilder.shaped(CONNECTOR_ROUTING.get())
                                        .define('g', Tags.Items.NUGGETS_GOLD)
                                        .define('C', TAG_CONNECTORS)
                                        .unlockedBy("chest", DataGen.has(Items.CHEST)),
                                "rrr", "gCg", "rrr"),
                Dob.itemBuilder(ADVANCED_CONNECTOR_BLUE)
                        .itemTags(List.of(TAG_ADVANCED_CONNECTORS))
                        .shaped(ShapedRecipeBuilder.shaped(ADVANCED_CONNECTOR_BLUE.get())
                                        .define('C', CONNECTOR_BLUE.get())
                                        .unlockedBy("chest", DataGen.has(Items.CHEST)),
                                "Co", "dr"),
                Dob.itemBuilder(ADVANCED_CONNECTOR_GREEN)
                        .itemTags(List.of(TAG_ADVANCED_CONNECTORS)),
                Dob.itemBuilder(ADVANCED_CONNECTOR_RED)
                        .itemTags(List.of(TAG_ADVANCED_CONNECTORS)),
                Dob.itemBuilder(ADVANCED_CONNECTOR_YELLOW)
                        .itemTags(List.of(TAG_ADVANCED_CONNECTORS))
                        .shaped(ShapedRecipeBuilder.shaped(ADVANCED_CONNECTOR_YELLOW.get())
                                        .define('C', CONNECTOR_YELLOW.get())
                                        .unlockedBy("chest", DataGen.has(Items.CHEST)),
                                "Co", "dr"),
                Dob.itemBuilder(ADVANCED_CONNECTOR_ROUTING)
                        .itemTags(List.of(TAG_ADVANCED_CONNECTORS))
        );
    }

}
