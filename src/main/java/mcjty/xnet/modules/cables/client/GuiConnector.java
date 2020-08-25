package mcjty.xnet.modules.cables.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.OrientationTools;
import mcjty.rftoolsbase.tools.ManualHelper;
import mcjty.xnet.modules.cables.blocks.ConnectorTileEntity;
import mcjty.xnet.setup.XNetMessages;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Direction;

import static mcjty.lib.gui.widgets.Widgets.*;
import static mcjty.xnet.modules.cables.blocks.ConnectorTileEntity.*;

public class GuiConnector extends GenericGuiContainer<ConnectorTileEntity, GenericContainer> {

    public static final int WIDTH = 220;
    public static final int HEIGHT = 50;

    private ToggleButton toggleButtons[] = new ToggleButton[6];

//    public GuiConnector(AdvancedConnectorTileEntity te, EmptyContainer container, PlayerInventory inventory) {
//        this((ConnectorTileEntity) te, container, inventory);
//    }

    public GuiConnector(ConnectorTileEntity tileEntity, GenericContainer container, PlayerInventory inventory) {
        super(tileEntity, container, inventory, ManualHelper.create("xnet:simple/connector"));

        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    public void init() {
        super.init();

        Panel toplevel = vertical().filledRectThickness(2);

        TextField nameField = new TextField().name("name").tooltips("Set the name of this connector");

        Panel namePanel = horizontal().children(label("Name:"), nameField);
        toplevel.children(namePanel);

        Panel togglePanel = horizontal().
                children(label("Directions:"));
        for (Direction facing : OrientationTools.DIRECTION_VALUES) {
            toggleButtons[facing.ordinal()] = new ToggleButton().text(facing.getString().substring(0, 1).toUpperCase())
                .event(() -> {
                    sendServerCommandTyped(XNetMessages.INSTANCE, ConnectorTileEntity.CMD_ENABLE,
                            TypedMap.builder()
                                    .put(PARAM_FACING, facing.ordinal())
                                    .put(PARAM_ENABLED, toggleButtons[facing.ordinal()].isPressed())
                                    .build());
                });
            toggleButtons[facing.ordinal()].pressed(tileEntity.isEnabled(facing));
            togglePanel.children(toggleButtons[facing.ordinal()]);
        }
        toplevel.children(togglePanel);

        toplevel.bounds(guiLeft, guiTop, WIDTH, HEIGHT);
        window = new Window(this, toplevel);

        window.bind(XNetMessages.INSTANCE, "name", tileEntity, VALUE_NAME.getName());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        drawWindow(matrixStack);
    }
}
