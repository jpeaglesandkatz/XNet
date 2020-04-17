package mcjty.xnet.modules.various.client;

import mcjty.lib.gui.GuiItemScreen;
import mcjty.lib.varia.GuiTools;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextPage;
import mcjty.xnet.XNet;
import mcjty.xnet.setup.XNetMessages;
import net.minecraft.util.ResourceLocation;

import static mcjty.lib.gui.widgets.Widgets.*;

public class GuiXNetManual extends GuiItemScreen {

    /** The X size of the window in pixels. */
    private int xSize = 400;
    /** The Y size of the window in pixels. */
    private int ySize = 224;

    private Window window;
    private TextPage textPage;
    private Label pageLabel;
    private Button prevButton;
    private Button nextButton;

    public static int MANUAL_XNET = 3;
    private ResourceLocation manualText;

    // If this is set when the manual opens the given page will be located.
    public static String locatePage = null;

    private static final ResourceLocation manualXNetText = new ResourceLocation(XNet.MODID, "text/manual_xnet.txt");
    private static final ResourceLocation iconGuiElements = new ResourceLocation(XNet.MODID, "textures/gui/guielements.png");

    public GuiXNetManual(int manual) {
        super(XNet.instance, XNetMessages.INSTANCE, 400, 224, 0, "xxx");    // @todo 1.14 manual
        if (manual == MANUAL_XNET) {
            manualText = manualXNetText;
        }
    }

    @Override
    public void init() {
        super.init();

        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;

        textPage = new TextPage(XNet.instance).setText(manualText).arrowImage(iconGuiElements, 144, 0).craftingGridImage(iconGuiElements, 0, 192);

        prevButton = button("<").event(() -> {
            textPage.prevPage();
            window.setTextFocus(textPage);
        });
        pageLabel = label("0 / 0");
        nextButton = button(">").event(() -> {
            textPage.nextPage();
            window.setTextFocus(textPage);
        });
        Panel buttonPanel = horizontal().desiredHeight(16).children(prevButton, pageLabel, nextButton);

        Panel toplevel = vertical().filledRectThickness(2).children(textPage, buttonPanel);
        toplevel.bounds(k, l, xSize, ySize);

        window = new Window(this, toplevel);
        window.setTextFocus(textPage);

        if (locatePage != null) {
            textPage.gotoNode(locatePage);
            locatePage = null;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        boolean rc = super.mouseClicked(x, y, button);
        window.mouseClicked(x, y, button);
        return rc;
    }

//@todo 1.14
//    @Override
//    public void handleMouseInput() throws IOException {
//        super.handleMouseInput();
//        window.handleMouseInput();
//    }
//
//    @Override
//    protected void mouseReleased(int mouseX, int mouseY, int state) {
//        super.mouseReleased(mouseX, mouseY, state);
//        window.mouseMovedOrUp(mouseX, mouseY, state);
//    }
//    @Override
//    protected void keyTyped(char typedChar, int keyCode) throws IOException {
//        super.keyTyped(typedChar, keyCode);
//        window.keyTyped(typedChar, keyCode);
//    }


    @Override
    public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
        super.render(p_render_1_, p_render_2_, p_render_3_);

        int index = textPage.getPageIndex();
        int count = textPage.getPageCount();
        pageLabel.text((index + 1) + "/" + count);
        prevButton.enabled(index > 0);
        nextButton.enabled(index < count - 1);

        window.draw();
        java.util.List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int x = GuiTools.getRelativeX(this);
            int y = GuiTools.getRelativeY(this);
            int guiLeft = (this.width - this.xSize) / 2;
            int guiTop = (this.height - this.ySize) / 2;
            renderTooltip(tooltips, x-guiLeft, y-guiTop, minecraft.fontRenderer);
        }
    }
}
