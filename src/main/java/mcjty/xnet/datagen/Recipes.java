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
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class Recipes extends BaseRecipeProvider {

    public Recipes(DataGenerator generatorIn) {
        super(generatorIn);
        add('F', mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_FRAME.get());
        add('A', mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_BASE.get());
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        build(consumer, ShapedRecipeBuilder.shapedRecipe(WirelessRouterModule.ANTENNA.get())
                        .key('I', Items.IRON_BARS)
                        .addCriterion("bars", hasItem(Items.IRON_BARS)),
                "IiI", "IiI", " i ");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(WirelessRouterModule.ANTENNA_BASE.get())
                        .key('I', Items.IRON_BLOCK)
                        .addCriterion("block", hasItem(Items.IRON_BLOCK)),
                " i ", " i ", "iIi");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(WirelessRouterModule.ANTENNA_DISH.get())
                        .key('I', Items.IRON_TRAPDOOR)
                        .addCriterion("trapdoor", hasItem(Items.IRON_TRAPDOOR)),
                "III", "IoI", " i ");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(VariousModule.REDSTONE_PROXY.get())
                        .addCriterion("frame", hasItem(mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_FRAME.get())),
                "rrr", "rFr", "rrr");

        build(consumer, ShapedRecipeBuilder.shapedRecipe(ControllerModule.CONTROLLER.get())
                        .key('I', Items.REPEATER)
                        .key('C', Items.COMPARATOR)
                        .key('g', Tags.Items.INGOTS_GOLD)
                        .addCriterion("frame", hasItem(mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_FRAME.get())),
                "ICI", "rFr", "igi");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(RouterModule.ROUTER.get())
                        .key('I', Items.POWERED_RAIL)
                        .key('C', Items.COMPARATOR)
                        .addCriterion("frame", hasItem(mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_FRAME.get())),
                "ICI", "rFr", "ioi");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(WirelessRouterModule.WIRELESS_ROUTER.get())
                        .key('C', Items.COMPARATOR)
                        .addCriterion("frame", hasItem(mcjty.rftoolsbase.modules.various.VariousModule.MACHINE_FRAME.get())),
                "oCo", "rFr", "oro");

        build(consumer, ShapedRecipeBuilder.shapedRecipe(FacadeModule.FACADE.get(), 16)
                        .key('w', ItemTags.WOOL)
                        .addCriterion("glass", hasItem(Items.GLASS)),
                "pwp", "wGw", "pwp");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(CableModule.NETCABLE_BLUE.get(), 16)
                        .key('g', Tags.Items.NUGGETS_GOLD)
                        .key('s', Items.STRING)
                        .key('1', Tags.Items.DYES_BLUE)
                        .addCriterion("nugget", hasItem(Items.GOLD_NUGGET)),
                "s1s", "rgr", "srs");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(CableModule.NETCABLE_YELLOW.get(), 16)
                        .key('g', Tags.Items.NUGGETS_GOLD)
                        .key('s', Items.STRING)
                        .key('1', Tags.Items.DYES_YELLOW)
                        .addCriterion("nugget", hasItem(Items.GOLD_NUGGET)),
                "s1s", "rgr", "srs");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(CableModule.NETCABLE_RED.get(), 16)
                        .key('g', Tags.Items.NUGGETS_GOLD)
                        .key('s', Items.STRING)
                        .key('1', Tags.Items.DYES_RED)
                        .addCriterion("nugget", hasItem(Items.GOLD_NUGGET)),
                "s1s", "rgr", "srs");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(CableModule.NETCABLE_GREEN.get(), 16)
                        .key('g', Tags.Items.NUGGETS_GOLD)
                        .key('s', Items.STRING)
                        .key('1', Tags.Items.DYES_GREEN)
                        .addCriterion("nugget", hasItem(Items.GOLD_NUGGET)),
                "s1s", "rgr", "srs");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(CableModule.NETCABLE_ROUTING.get(), 32)
                        .key('g', Tags.Items.NUGGETS_GOLD)
                        .key('s', Items.STRING)
                        .key('1', Tags.Items.DYES_BLACK)
                        .addCriterion("nugget", hasItem(Items.GOLD_NUGGET)),
                "s1s", "rgr", "srs");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(VariousModule.UPGRADE.get())
                        .addCriterion("pearl", hasItem(Items.ENDER_PEARL)),
                "po", "dr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(CableModule.CONNECTOR_BLUE.get())
                        .key('g', Tags.Items.INGOTS_GOLD)
                        .key('1', Tags.Items.DYES_BLUE)
                        .key('C', Tags.Items.CHESTS)
                        .addCriterion("chest", hasItem(Items.CHEST)),
                "1C1", "rgr", "1r1");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(CableModule.CONNECTOR_RED.get())
                        .key('g', Tags.Items.INGOTS_GOLD)
                        .key('1', Tags.Items.DYES_RED)
                        .key('C', Tags.Items.CHESTS)
                        .addCriterion("chest", hasItem(Items.CHEST)),
                "1C1", "rgr", "1r1");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(CableModule.CONNECTOR_GREEN.get())
                        .key('g', Tags.Items.INGOTS_GOLD)
                        .key('1', Tags.Items.DYES_GREEN)
                        .key('C', Tags.Items.CHESTS)
                        .addCriterion("chest", hasItem(Items.CHEST)),
                "1C1", "rgr", "1r1");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(CableModule.CONNECTOR_YELLOW.get())
                        .key('g', Tags.Items.INGOTS_GOLD)
                        .key('1', Tags.Items.DYES_YELLOW)
                        .key('C', Tags.Items.CHESTS)
                        .addCriterion("chest", hasItem(Items.CHEST)),
                "1C1", "rgr", "1r1");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(CableModule.CONNECTOR_ROUTING.get())
                        .key('g', Tags.Items.NUGGETS_GOLD)
                        .key('C', CableModule.TAG_CONNECTORS)
                        .addCriterion("chest", hasItem(Items.CHEST)),
                "rrr", "gCg", "rrr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(CableModule.ADVANCED_CONNECTOR_BLUE.get())
                        .key('C', CableModule.CONNECTOR_BLUE.get())
                        .addCriterion("chest", hasItem(Items.CHEST)),
                "Co", "dr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(CableModule.ADVANCED_CONNECTOR_YELLOW.get())
                        .key('C', CableModule.CONNECTOR_YELLOW.get())
                        .addCriterion("chest", hasItem(Items.CHEST)),
                "Co", "dr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(CableModule.ADVANCED_CONNECTOR_RED.get())
                        .key('C', CableModule.CONNECTOR_RED.get())
                        .addCriterion("chest", hasItem(Items.CHEST)),
                "Co", "dr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(CableModule.ADVANCED_CONNECTOR_GREEN.get())
                        .key('C', CableModule.CONNECTOR_GREEN.get())
                        .addCriterion("chest", hasItem(Items.CHEST)),
                "Co", "dr");
        build(consumer, ShapedRecipeBuilder.shapedRecipe(CableModule.ADVANCED_CONNECTOR_ROUTING.get())
                        .key('C', CableModule.CONNECTOR_ROUTING.get())
                        .addCriterion("chest", hasItem(Items.CHEST)),
                "Co", "dr");
        build(consumer, new ResourceLocation(XNet.MODID, "netcable_blue_dye"), ShapelessRecipeBuilder.shapelessRecipe(CableModule.NETCABLE_BLUE.get())
                .addIngredient(Tags.Items.DYES_BLUE)
                .addIngredient(CableModule.TAG_CABLES)
                .addCriterion("chest", hasItem(Items.CHEST)));
        build(consumer, new ResourceLocation(XNet.MODID, "netcable_red_dye"), ShapelessRecipeBuilder.shapelessRecipe(CableModule.NETCABLE_RED.get())
                .addIngredient(Tags.Items.DYES_RED)
                .addIngredient(CableModule.TAG_CABLES)
                .addCriterion("chest", hasItem(Items.CHEST)));
        build(consumer, new ResourceLocation(XNet.MODID, "netcable_green_dye"), ShapelessRecipeBuilder.shapelessRecipe(CableModule.NETCABLE_GREEN.get())
                .addIngredient(Tags.Items.DYES_GREEN)
                .addIngredient(CableModule.TAG_CABLES)
                .addCriterion("chest", hasItem(Items.CHEST)));
        build(consumer, new ResourceLocation(XNet.MODID, "netcable_yellow_dye"), ShapelessRecipeBuilder.shapelessRecipe(CableModule.NETCABLE_YELLOW.get())
                .addIngredient(Tags.Items.DYES_YELLOW)
                .addIngredient(CableModule.TAG_CABLES)
                .addCriterion("chest", hasItem(Items.CHEST)));
        build(consumer, new ResourceLocation(XNet.MODID, "connector_blue_dye"), ShapelessRecipeBuilder.shapelessRecipe(CableModule.CONNECTOR_BLUE.get())
                .addIngredient(Tags.Items.DYES_BLUE)
                .addIngredient(CableModule.TAG_CONNECTORS)
                .addCriterion("chest", hasItem(Items.CHEST)));
        build(consumer, new ResourceLocation(XNet.MODID, "connector_red_dye"), ShapelessRecipeBuilder.shapelessRecipe(CableModule.CONNECTOR_RED.get())
                .addIngredient(Tags.Items.DYES_RED)
                .addIngredient(CableModule.TAG_CONNECTORS)
                .addCriterion("chest", hasItem(Items.CHEST)));
        build(consumer, new ResourceLocation(XNet.MODID, "connector_green_dye"), ShapelessRecipeBuilder.shapelessRecipe(CableModule.CONNECTOR_GREEN.get())
                .addIngredient(Tags.Items.DYES_GREEN)
                .addIngredient(CableModule.TAG_CONNECTORS)
                .addCriterion("chest", hasItem(Items.CHEST)));
        build(consumer, new ResourceLocation(XNet.MODID, "connector_yellow_dye"), ShapelessRecipeBuilder.shapelessRecipe(CableModule.CONNECTOR_YELLOW.get())
                .addIngredient(Tags.Items.DYES_YELLOW)
                .addIngredient(CableModule.TAG_CONNECTORS)
                .addCriterion("chest", hasItem(Items.CHEST)));
        build(consumer, new ResourceLocation(XNet.MODID, "advanced_connector_blue_dye"), ShapelessRecipeBuilder.shapelessRecipe(CableModule.ADVANCED_CONNECTOR_BLUE.get())
                .addIngredient(Tags.Items.DYES_BLUE)
                .addIngredient(CableModule.TAG_ADVANCED_CONNECTORS)
                .addCriterion("chest", hasItem(Items.CHEST)));
        build(consumer, new ResourceLocation(XNet.MODID, "advanced_connector_red_dye"), ShapelessRecipeBuilder.shapelessRecipe(CableModule.ADVANCED_CONNECTOR_RED.get())
                .addIngredient(Tags.Items.DYES_RED)
                .addIngredient(CableModule.TAG_ADVANCED_CONNECTORS)
                .addCriterion("chest", hasItem(Items.CHEST)));
        build(consumer, new ResourceLocation(XNet.MODID, "advanced_connector_green_dye"), ShapelessRecipeBuilder.shapelessRecipe(CableModule.ADVANCED_CONNECTOR_GREEN.get())
                .addIngredient(Tags.Items.DYES_GREEN)
                .addIngredient(CableModule.TAG_ADVANCED_CONNECTORS)
                .addCriterion("chest", hasItem(Items.CHEST)));
        build(consumer, new ResourceLocation(XNet.MODID, "advanced_connector_yellow_dye"), ShapelessRecipeBuilder.shapelessRecipe(CableModule.ADVANCED_CONNECTOR_YELLOW.get())
                .addIngredient(Tags.Items.DYES_YELLOW)
                .addIngredient(CableModule.TAG_ADVANCED_CONNECTORS)
                .addCriterion("chest", hasItem(Items.CHEST)));
        build(consumer, new ResourceLocation(XNet.MODID, "redstoneproxy_update"), ShapelessRecipeBuilder.shapelessRecipe(VariousModule.REDSTONE_PROXY_UPD.get())
                .addIngredient(VariousModule.REDSTONE_PROXY.get())
                .addIngredient(Items.REDSTONE_TORCH)
                .addCriterion("torch", hasItem(Items.REDSTONE_TORCH)));
//        build(consumer, CopyNBTRecipeBuilder.shapedRecipe(PowerCellSetup.CELL3)
//                        .key('K', mcjty.rftoolspower.items.ModItems.POWER_CORE3)
//                        .key('P', PowerCellSetup.CELL2)
//                        .addCriterion("cell", hasItem()(PowerCellSetup.CELL2)),
//                "rKr", "KPK", "rKr");
    }
}
