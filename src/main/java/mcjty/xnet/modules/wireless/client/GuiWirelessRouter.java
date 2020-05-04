package mcjty.xnet.modules.wireless.client;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.ManualEntry;
import mcjty.lib.gui.Window;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.xnet.XNet;
import mcjty.xnet.modules.wireless.blocks.TileEntityWirelessRouter;
import mcjty.xnet.setup.XNetMessages;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;

public class GuiWirelessRouter extends GenericGuiContainer<TileEntityWirelessRouter, GenericContainer> {

    public GuiWirelessRouter(TileEntityWirelessRouter router, GenericContainer container, PlayerInventory inventory) {
        super(XNet.instance, router, container, inventory, ManualHelper.create("xnet:simple/wireless/wireless_router"));
    }

    @Override
    public void init() {
        window = new Window(this, tileEntity, XNetMessages.INSTANCE, new ResourceLocation(XNet.MODID, "gui/wireless_router.gui"));
        super.init();
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawWindow();
    }
}
