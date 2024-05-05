package mcjty.xnet.compat.jei;

import mcjty.lib.McJtyLib;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;

import javax.annotation.Nonnull;
import java.util.List;

@JeiPlugin
public class JeiCompat implements IModPlugin {

    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(McJtyLib.MODID, "xnet");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(GenericGuiContainer.class, new JeiCompat.Handler<GenericContainer>());
        registration.addGhostIngredientHandler(GenericGuiContainer.class, new JeiGhostIngredientHandler<>());
    }

    static class Handler<T extends AbstractContainerMenu> implements IGuiContainerHandler<GenericGuiContainer<?,T>> {
        @Nonnull
        @Override
        public List<Rect2i> getGuiExtraAreas(GenericGuiContainer containerScreen) {
            return containerScreen.getExtraWindowBounds();
        }
    }

}
