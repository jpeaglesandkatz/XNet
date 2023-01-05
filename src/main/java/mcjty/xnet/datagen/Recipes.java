package mcjty.xnet.datagen;

import mcjty.lib.datagen.BaseRecipeProvider;
import mcjty.xnet.XNet;
import mcjty.xnet.modules.cables.CableModule;
import mcjty.xnet.modules.controller.ControllerModule;
import mcjty.xnet.modules.facade.FacadeModule;
import mcjty.xnet.modules.router.RouterModule;
import mcjty.xnet.modules.various.VariousModule;
import mcjty.xnet.modules.wireless.WirelessRouterModule;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class Recipes extends BaseRecipeProvider {

    public Recipes(DataGenerator generatorIn) {
        super(generatorIn);
        add('F', mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_FRAME.get());
        add('A', mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_BASE.get());
    }

    @Override
    protected void buildCraftingRecipes(@Nonnull Consumer<FinishedRecipe> consumer) {
        build(consumer, ShapedRecipeBuilder.shaped(WirelessRouterModule.ANTENNA.get())
                        .define('I', Items.IRON_BARS)
                        .unlockedBy("bars", has(Items.IRON_BARS)),
                "IiI", "IiI", " i ");
        build(consumer, ShapedRecipeBuilder.shaped(WirelessRouterModule.ANTENNA_BASE.get())
                        .define('I', Items.IRON_BLOCK)
                        .unlockedBy("block", has(Items.IRON_BLOCK)),
                " i ", " i ", "iIi");
        build(consumer, ShapedRecipeBuilder.shaped(WirelessRouterModule.ANTENNA_DISH.get())
                        .define('I', Items.IRON_TRAPDOOR)
                        .unlockedBy("trapdoor", has(Items.IRON_TRAPDOOR)),
                "III", "IoI", " i ");
        build(consumer, ShapedRecipeBuilder.shaped(VariousModule.REDSTONE_PROXY.get())
                        .unlockedBy("frame", has(mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_FRAME.get())),
                "rrr", "rFr", "rrr");

        build(consumer, ShapedRecipeBuilder.shaped(ControllerModule.CONTROLLER.get())
                        .define('I', Items.REPEATER)
                        .define('C', Items.COMPARATOR)
                        .define('g', Tags.Items.INGOTS_GOLD)
                        .unlockedBy("frame", has(mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_FRAME.get())),
                "ICI", "rFr", "igi");
        build(consumer, ShapedRecipeBuilder.shaped(RouterModule.ROUTER.get())
                        .define('I', Items.POWERED_RAIL)
                        .define('C', Items.COMPARATOR)
                        .unlockedBy("frame", has(mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_FRAME.get())),
                "ICI", "rFr", "ioi");
        build(consumer, ShapedRecipeBuilder.shaped(WirelessRouterModule.WIRELESS_ROUTER.get())
                        .define('C', Items.COMPARATOR)
                        .unlockedBy("frame", has(mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_FRAME.get())),
                "oCo", "rFr", "oro");

        build(consumer, ShapedRecipeBuilder.shaped(FacadeModule.FACADE.get(), 16)
                        .define('w', ItemTags.WOOL)
                        .unlockedBy("glass", has(Items.GLASS)),
                "pwp", "wGw", "pwp");
        build(consumer, ShapedRecipeBuilder.shaped(VariousModule.UPGRADE.get())
                        .unlockedBy("pearl", has(Items.ENDER_PEARL)),
                "po", "dr");
        build(consumer, ShapedRecipeBuilder.shaped(CableModule.ADVANCED_CONNECTOR_RED.get())
                        .define('C', CableModule.CONNECTOR_RED.get())
                        .unlockedBy("chest", has(Items.CHEST)),
                "Co", "dr");
        build(consumer, ShapedRecipeBuilder.shaped(CableModule.ADVANCED_CONNECTOR_GREEN.get())
                        .define('C', CableModule.CONNECTOR_GREEN.get())
                        .unlockedBy("chest", has(Items.CHEST)),
                "Co", "dr");
        build(consumer, ShapedRecipeBuilder.shaped(CableModule.ADVANCED_CONNECTOR_ROUTING.get())
                        .define('C', CableModule.CONNECTOR_ROUTING.get())
                        .unlockedBy("chest", has(Items.CHEST)),
                "Co", "dr");
        build(consumer, new ResourceLocation(XNet.MODID, "netcable_blue_dye"), ShapelessRecipeBuilder.shapeless(CableModule.NETCABLE_BLUE.get())
                .requires(Tags.Items.DYES_BLUE)
                .requires(CableModule.TAG_CABLES)
                .unlockedBy("chest", has(Items.CHEST)));
        build(consumer, new ResourceLocation(XNet.MODID, "netcable_red_dye"), ShapelessRecipeBuilder.shapeless(CableModule.NETCABLE_RED.get())
                .requires(Tags.Items.DYES_RED)
                .requires(CableModule.TAG_CABLES)
                .unlockedBy("chest", has(Items.CHEST)));
        build(consumer, new ResourceLocation(XNet.MODID, "netcable_green_dye"), ShapelessRecipeBuilder.shapeless(CableModule.NETCABLE_GREEN.get())
                .requires(Tags.Items.DYES_GREEN)
                .requires(CableModule.TAG_CABLES)
                .unlockedBy("chest", has(Items.CHEST)));
        build(consumer, new ResourceLocation(XNet.MODID, "netcable_yellow_dye"), ShapelessRecipeBuilder.shapeless(CableModule.NETCABLE_YELLOW.get())
                .requires(Tags.Items.DYES_YELLOW)
                .requires(CableModule.TAG_CABLES)
                .unlockedBy("chest", has(Items.CHEST)));
        build(consumer, new ResourceLocation(XNet.MODID, "connector_blue_dye"), ShapelessRecipeBuilder.shapeless(CableModule.CONNECTOR_BLUE.get())
                .requires(Tags.Items.DYES_BLUE)
                .requires(CableModule.TAG_CONNECTORS)
                .unlockedBy("chest", has(Items.CHEST)));
        build(consumer, new ResourceLocation(XNet.MODID, "connector_red_dye"), ShapelessRecipeBuilder.shapeless(CableModule.CONNECTOR_RED.get())
                .requires(Tags.Items.DYES_RED)
                .requires(CableModule.TAG_CONNECTORS)
                .unlockedBy("chest", has(Items.CHEST)));
        build(consumer, new ResourceLocation(XNet.MODID, "connector_green_dye"), ShapelessRecipeBuilder.shapeless(CableModule.CONNECTOR_GREEN.get())
                .requires(Tags.Items.DYES_GREEN)
                .requires(CableModule.TAG_CONNECTORS)
                .unlockedBy("chest", has(Items.CHEST)));
        build(consumer, new ResourceLocation(XNet.MODID, "connector_yellow_dye"), ShapelessRecipeBuilder.shapeless(CableModule.CONNECTOR_YELLOW.get())
                .requires(Tags.Items.DYES_YELLOW)
                .requires(CableModule.TAG_CONNECTORS)
                .unlockedBy("chest", has(Items.CHEST)));
        build(consumer, new ResourceLocation(XNet.MODID, "advanced_connector_blue_dye"), ShapelessRecipeBuilder.shapeless(CableModule.ADVANCED_CONNECTOR_BLUE.get())
                .requires(Tags.Items.DYES_BLUE)
                .requires(CableModule.TAG_ADVANCED_CONNECTORS)
                .unlockedBy("chest", has(Items.CHEST)));
        build(consumer, new ResourceLocation(XNet.MODID, "advanced_connector_red_dye"), ShapelessRecipeBuilder.shapeless(CableModule.ADVANCED_CONNECTOR_RED.get())
                .requires(Tags.Items.DYES_RED)
                .requires(CableModule.TAG_ADVANCED_CONNECTORS)
                .unlockedBy("chest", has(Items.CHEST)));
        build(consumer, new ResourceLocation(XNet.MODID, "advanced_connector_green_dye"), ShapelessRecipeBuilder.shapeless(CableModule.ADVANCED_CONNECTOR_GREEN.get())
                .requires(Tags.Items.DYES_GREEN)
                .requires(CableModule.TAG_ADVANCED_CONNECTORS)
                .unlockedBy("chest", has(Items.CHEST)));
        build(consumer, new ResourceLocation(XNet.MODID, "advanced_connector_yellow_dye"), ShapelessRecipeBuilder.shapeless(CableModule.ADVANCED_CONNECTOR_YELLOW.get())
                .requires(Tags.Items.DYES_YELLOW)
                .requires(CableModule.TAG_ADVANCED_CONNECTORS)
                .unlockedBy("chest", has(Items.CHEST)));
        build(consumer, new ResourceLocation(XNet.MODID, "redstoneproxy_update"), ShapelessRecipeBuilder.shapeless(VariousModule.REDSTONE_PROXY_UPD.get())
                .requires(VariousModule.REDSTONE_PROXY.get())
                .requires(Items.REDSTONE_TORCH)
                .unlockedBy("torch", has(Items.REDSTONE_TORCH)));
//        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(PowerCellSetup.CELL3)
//                        .key('K', mcjty.rftoolspower.items.ModItems.POWER_CORE3)
//                        .key('P', PowerCellSetup.CELL2)
//                        .addCriterion("cell", hasItem()(PowerCellSetup.CELL2)),
//                "rKr", "KPK", "rKr");
    }
}
