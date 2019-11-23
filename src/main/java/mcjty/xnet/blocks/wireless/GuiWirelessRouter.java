package mcjty.xnet.blocks.wireless;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.xnet.XNet;
import mcjty.xnet.network.XNetMessages;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;

public class GuiWirelessRouter extends GenericGuiContainer<TileEntityWirelessRouter, GenericContainer> {

    public GuiWirelessRouter(TileEntityWirelessRouter router, GenericContainer container, PlayerInventory inventory) {
        super(XNet.instance, router, container, inventory, 0 /* @todo 1.14 GuiProxy.GUI_MANUAL_XNET*/, "wireless_router");
    }

    @Override
    public void init() {
        window = new Window(this, tileEntity, XNetMessages.INSTANCE, new ResourceLocation(XNet.MODID, "gui/wireless_router.gui"));
        super.init();
    }
}
