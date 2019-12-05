package mcjty.xnet.modules.cables.client;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.OrientationTools;
import mcjty.xnet.XNet;
import mcjty.xnet.modules.cables.blocks.ConnectorTileEntity;
import mcjty.xnet.setup.XNetMessages;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Direction;

import java.awt.*;

import static mcjty.xnet.modules.cables.blocks.ConnectorTileEntity.*;

public class GuiConnector extends GenericGuiContainer<ConnectorTileEntity, GenericContainer> {

    public static final int WIDTH = 220;
    public static final int HEIGHT = 50;

    private ToggleButton toggleButtons[] = new ToggleButton[6];

//    public GuiConnector(AdvancedConnectorTileEntity te, EmptyContainer container, PlayerInventory inventory) {
//        this((ConnectorTileEntity) te, container, inventory);
//    }

    public GuiConnector(ConnectorTileEntity tileEntity, GenericContainer container, PlayerInventory inventory) {
        super(XNet.instance, tileEntity, container, inventory, 0 /*@todo 1.14 GuiProxy.GUI_MANUAL_XNET*/, "connector");

        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    public void init() {
        super.init();

        Panel toplevel = new Panel(minecraft, this).setFilledRectThickness(2).setLayout(new VerticalLayout());

        TextField nameField = new TextField(minecraft, this).setName("name").setTooltips("Set the name of this connector");

        Panel namePanel = new Panel(minecraft, this).setLayout(new HorizontalLayout()).
                addChild(new Label(minecraft, this).setText("Name:")).addChild(nameField);
        toplevel.addChild(namePanel);

        Panel togglePanel = new Panel(minecraft, this).setLayout(new HorizontalLayout()).
                addChild(new Label(minecraft, this).setText("Directions:"));
        for (Direction facing : OrientationTools.DIRECTION_VALUES) {
            toggleButtons[facing.ordinal()] = new ToggleButton(minecraft, this).setText(facing.getName().substring(0, 1).toUpperCase())
                .addButtonEvent(parent -> {
                    sendServerCommandTyped(XNetMessages.INSTANCE, ConnectorTileEntity.CMD_ENABLE,
                            TypedMap.builder()
                                    .put(PARAM_FACING, facing.ordinal())
                                    .put(PARAM_ENABLED, toggleButtons[facing.ordinal()].isPressed())
                                    .build());
                });
            toggleButtons[facing.ordinal()].setPressed(tileEntity.isEnabled(facing));
            togglePanel.addChild(toggleButtons[facing.ordinal()]);
        }
        toplevel.addChild(togglePanel);

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, WIDTH, HEIGHT));
        window = new Window(this, toplevel);

        window.bind(XNetMessages.INSTANCE, "name", tileEntity, VALUE_NAME.getName());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawWindow();
    }
}
