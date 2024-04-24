package mcjty.xnet.compat.jei;

import com.google.common.collect.ImmutableList;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.widgets.BlockRender;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.Widget;
import mcjty.xnet.modules.controller.client.GuiController;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class JeiGhostIngredientHandler<GUI extends GenericGuiContainer<?,?>> implements IGhostIngredientHandler<GUI> {


    @Override
    public <I> List<Target<I>> getTargets(GUI gui, I ingredient, boolean b) {
        if (ingredient instanceof ItemStack ingr) {
            if (ingr.isEmpty()) {
                return ImmutableList.of();
            }
            ImmutableList.Builder<Target<I>> builder = ImmutableList.builder();

                if (gui instanceof GuiController guiController) {
                    for (Widget<?> widget : guiController.getConnectorEditPanel().getChildren()) {
                    if (widget instanceof BlockRender blockRender) {
                        builder.add((Target<I>) new GhostSlotTarget(blockRender, (GuiController) gui));
                    }
                }
            }
            return builder.build();
        }
        return ImmutableList.of();
    }

    @Override
    public void onComplete() {

    }

    private static class GhostSlotTarget implements Target<ItemStack> {

        public static final int SIDE_WIDTH = 80; // width of sidegui.png

        final BlockRender slot;
        final GuiController gui;
        Rect2i area;
        int lastGuiLeft, lastGuiTop;

        public GhostSlotTarget(BlockRender slot, GuiController gui) {
            this.slot = slot;
            this.gui = gui;
            initRectangle();
        }

        private void initRectangle() {
            Panel connectorEditPanel = gui.getConnectorEditPanel();
            int pointX = connectorEditPanel.getBounds().x + slot.getBounds().x - SIDE_WIDTH;
            int pointY = connectorEditPanel.getBounds().y + slot.getBounds().y;
            area = new Rect2i(gui.getGuiLeft() + pointX, gui.getGuiTop() + pointY, 16, 16);
            lastGuiLeft = gui.getGuiLeft();
            lastGuiTop = gui.getGuiTop();
        }

        @Override
        public Rect2i getArea() {
            if (lastGuiLeft != gui.getGuiLeft() || lastGuiTop != gui.getGuiTop()) {
                initRectangle();
            }
            return area;
        }

        @Override
        public void accept(ItemStack ingredient) {
            slot.fireDraggedEvents(ingredient);
        }

    }
}
