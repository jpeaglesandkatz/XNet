package mcjty.xnet.modules.controller.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mcjty.lib.McJtyLib;
import mcjty.lib.base.StyleConfig;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.WindowManager;
import mcjty.lib.gui.events.ButtonEvent;
import mcjty.lib.gui.events.DefaultSelectionEvent;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.Logging;
import mcjty.xnet.XNet;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.gui.IndicatorIcon;
import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import mcjty.rftoolsbase.api.xnet.keys.SidedPos;
import mcjty.xnet.modules.controller.blocks.TileEntityController;
import mcjty.xnet.client.ChannelClientInfo;
import mcjty.xnet.client.ConnectedBlockClientInfo;
import mcjty.xnet.client.ConnectorClientInfo;
import mcjty.xnet.modules.controller.network.PacketGetChannels;
import mcjty.xnet.modules.controller.network.PacketGetConnectedBlocks;
import mcjty.xnet.setup.XNetMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.energy.CapabilityEnergy;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.List;

import static mcjty.xnet.modules.controller.blocks.TileEntityController.*;
import static mcjty.xnet.modules.controller.ChannelInfo.MAX_CHANNELS;

public class GuiController extends GenericGuiContainer<TileEntityController, GenericContainer> {

    public static final String TAG_ENABLED = "enabled";
    public static final String TAG_NAME = "name";

    private WidgetList connectorList;
    private List<SidedPos> connectorPositions = new ArrayList<>();
    private int listDirty;
    private TextField searchBar;

    private int delayedSelectedChannel = -1;
    private int delayedSelectedLine = -1;
    private SidedPos delayedSelectedConnector = null;

    private Panel channelEditPanel;
    private Panel connectorEditPanel;

    private ToggleButton channelButtons[] = new ToggleButton[MAX_CHANNELS];

    private SidedPos editingConnector = null;
    private int editingChannel = -1;

    private int showingChannel = -1;
    private SidedPos showingConnector = null;

    private static GuiController openController = null;

    private EnergyBar energyBar;
    private Button copyConnector = null;

    // From server.
    public static List<ChannelClientInfo> fromServer_channels = null;
    public static List<ConnectedBlockClientInfo> fromServer_connectedBlocks = null;
    private boolean needsRefresh = true;

    public GuiController(TileEntityController controller, GenericContainer container, PlayerInventory inventory) {
        super(XNet.instance, controller, container, inventory, 0 /*@todo 1.14*/, "controller");
        openController = this;
    }

    @Override
    public void onClose() {
        super.onClose();
        openController = null;
    }

    @Override
    public void init() {
        window = new Window(this, tileEntity, XNetMessages.INSTANCE, new ResourceLocation(XNet.MODID, "gui/controller.gui"));
        super.init();

        initializeFields();
        setupEvents();

        editingConnector = null;
        editingChannel = -1;

        refresh();
        listDirty = 0;
    }

    private void setupEvents() {
        window.event("searchbar", (source, params) -> { needsRefresh = true; });
        for (int i = 0 ; i < MAX_CHANNELS ; i++) {
            String channel = "channel" + (i+1);
            int finalI = i;
            window.event(channel, (source, params) -> selectChannelEditor(finalI));
        }
    }

    private void initializeFields() {
        channelEditPanel = window.findChild("channeleditpanel");
        connectorEditPanel = window.findChild("connectoreditpanel");

        searchBar = window.findChild("searchbar");
        connectorList = window.findChild("connectors");

        connectorList.addSelectionEvent(new DefaultSelectionEvent() {
            @Override
            public void doubleClick(Widget<?> parent, int index) {
                hilightSelectedContainer(index);
            }
        });

        for (int i = 0 ; i < MAX_CHANNELS ; i++) {
            String name = "channel" + (i+1);
            channelButtons[i] = window.findChild(name);
        }

        energyBar = window.findChild("energybar");
    }

    private void hilightSelectedContainer(int index) {
        if (index < 0) {
            return;
        }
        ConnectedBlockClientInfo c = fromServer_connectedBlocks.get(index);
        if (c != null) {
            XNet.instance.clientInfo.hilightBlock(c.getPos().getPos(), System.currentTimeMillis() + 1000 * 5);
            Logging.message(minecraft.player, "The block is now highlighted");
            minecraft.player.closeScreen();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (handleClipboard(keyCode)) {
            return true;
        }
        if (handleKeyUpDown(keyCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void keyTypedFromEvent(int keyCode, int scanCode) {
        if (handleClipboard(keyCode)) {
            return;
        }
        if (handleKeyUpDown(keyCode)) {
            return;
        }
        super.keyTypedFromEvent(keyCode, scanCode);
    }

    private boolean handleKeyUpDown(int keyCode) {
        if (getSelectedChannel() == -1) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_UP) {
            int sel = connectorList.getSelected();
            if (sel > 0) {
                sel--;
                connectorList.setSelected(sel);
                selectConnectorEditor(connectorPositions.get(sel), getSelectedChannel());
            }
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
            int sel = connectorList.getSelected();
            if (sel != -1) {
                if (sel < connectorList.getChildCount() - 1) {
                    sel++;
                    connectorList.setSelected(sel);
                    selectConnectorEditor(connectorPositions.get(sel), getSelectedChannel());
                }
            }
            return true;
        }
        return false;
    }


    private boolean handleClipboard(int keyCode) {
        if (McJtyLib.proxy.isCtrlKeyDown()) {
            if (keyCode == GLFW.GLFW_KEY_C) {
                if (getSelectedChannel() != -1) {
                    copyConnector();
                } else {
                    showMessage(minecraft, this, getWindowManager(), 50, 50, TextFormatting.RED + "Nothing selected!");
                }
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_V) {
                if (getSelectedChannel() != -1) {
                    pasteConnector();
                } else {
                    showMessage(minecraft, this, getWindowManager(), 50, 50, TextFormatting.RED + "Nothing selected!");
                }
                return true;
            }
        }
        return false;
    }

    private void selectChannelEditor(int finalI) {
        editingChannel = -1;
        showingConnector = null;
        for (int j = 0 ; j < MAX_CHANNELS ; j++) {
            if (j != finalI) {
                channelButtons[j].setPressed(false);
                editingChannel = finalI;
            }
        }
    }

    private void removeConnector(SidedPos sidedPos) {
        sendServerCommandTyped(XNetMessages.INSTANCE, TileEntityController.CMD_REMOVECONNECTOR,
                TypedMap.builder()
                        .put(PARAM_CHANNEL, getSelectedChannel())
                        .put(PARAM_POS, sidedPos.getPos())
                        .put(PARAM_SIDE, sidedPos.getSide().ordinal())
                        .build());
        refresh();
    }

    private void createConnector(SidedPos sidedPos) {
        sendServerCommandTyped(XNetMessages.INSTANCE, TileEntityController.CMD_CREATECONNECTOR,
                TypedMap.builder()
                        .put(PARAM_CHANNEL, getSelectedChannel())
                        .put(PARAM_POS, sidedPos.getPos())
                        .put(PARAM_SIDE, sidedPos.getSide().ordinal())
                        .build());
        refresh();
    }

    private void removeChannel() {
        showMessage(minecraft, this, getWindowManager(), 50, 50, TextFormatting.RED + "Really remove channel " + (getSelectedChannel() + 1) + "?", parent -> {
            sendServerCommandTyped(XNetMessages.INSTANCE, TileEntityController.CMD_REMOVECHANNEL,
                    TypedMap.builder()
                            .put(PARAM_INDEX, getSelectedChannel())
                            .build());
            refresh();
        });
    }

    private void createChannel(String typeId) {
        sendServerCommandTyped(XNetMessages.INSTANCE, TileEntityController.CMD_CREATECHANNEL,
                TypedMap.builder()
                        .put(PARAM_INDEX, getSelectedChannel())
                        .put(PARAM_TYPE, typeId)
                        .build());
        refresh();
    }

    public void refresh() {
        fromServer_channels = null;
        fromServer_connectedBlocks = null;
        showingChannel = -1;
        showingConnector = null;
        needsRefresh = true;
        listDirty = 3;
        requestListsIfNeeded();
    }

    private void selectConnectorEditor(SidedPos sidedPos, int finalI) {
        editingConnector = sidedPos;
        selectChannelEditor(finalI);
    }

    private void refreshChannelEditor() {
        if (!listsReady()) {
            return;
        }
        if (editingChannel != -1 && showingChannel != editingChannel) {
            showingChannel = editingChannel;
            channelButtons[editingChannel].setPressed(true);

            copyConnector = null;
            channelEditPanel.removeChildren();
            if (channelButtons[editingChannel].isPressed()) {
                ChannelClientInfo info = fromServer_channels.get(editingChannel);
                if (info != null) {
                    ChannelEditorPanel editor = new ChannelEditorPanel(channelEditPanel, minecraft, this, editingChannel);
                    editor.label("Channel " + (editingChannel + 1))
                            .shift(5)
                            .toggle(TAG_ENABLED, "Enable processing on this channel", info.isEnabled())
                            .shift(5)
                            .text(TAG_NAME, "Channel name", info.getChannelName(), 65);
                    info.getChannelSettings().createGui(editor);

                    Button remove = new Button(minecraft, this).setText("x")
                            .setTextOffset(0, -1)
                            .setTooltips("Remove this channel")
                            .setLayoutHint(new PositionalLayout.PositionalHint(151, 1, 9, 10))
                            .addButtonEvent(parent -> removeChannel());
                    channelEditPanel.addChild(remove);
                    editor.setState(info.getChannelSettings());

                    Button copyChannel = new Button(minecraft, this)
                            .setText("C")
                            .setTooltips("Copy this channel to", "the clipboard")
                            .setLayoutHint(new PositionalLayout.PositionalHint(134, 19, 25, 14))
                            .addButtonEvent(parent -> copyChannel());
                    channelEditPanel.addChild(copyChannel);

                    copyConnector = new Button(minecraft, this)
                            .setText("C")
                            .setTooltips("Copy this connector", "to the clipboard")
                            .setLayoutHint(new PositionalLayout.PositionalHint(114, 19, 25, 14))
                            .addButtonEvent(parent -> copyConnector());
                    channelEditPanel.addChild(copyConnector);

                } else {
                    ChoiceLabel type = new ChoiceLabel(minecraft, this)
                            .setLayoutHint(new PositionalLayout.PositionalHint(5, 3, 95, 14));
                    for (IChannelType channelType : XNet.xNetApi.getChannels().values()) {
                        type.addChoices(channelType.getID());       // Show names?
                    }
                    Button create = new Button(minecraft, this)
                            .setText("Create")
                            .setLayoutHint(new PositionalLayout.PositionalHint(100, 3, 53, 14))
                            .addButtonEvent(parent -> createChannel(type.getCurrentChoice()));

                    Button paste = new Button(minecraft, this)
                            .setText("Paste")
                            .setTooltips("Create a new channel", "from the clipboard")
                            .setLayoutHint(new PositionalLayout.PositionalHint(100, 17, 53, 14))
                            .addButtonEvent(parent -> pasteChannel());

                    channelEditPanel.addChild(type).addChild(create).addChild(paste);
                }
            }
        } else if (showingChannel != -1 && editingChannel == -1) {
            showingChannel = -1;
            channelEditPanel.removeChildren();
        }
    }

    public static void showMessage(Minecraft mc, Screen gui, WindowManager windowManager, int x, int y, String title) {
        showMessage(mc, gui, windowManager, x, y, title, null);
    }

    public static void showMessage(Minecraft mc, Screen gui, WindowManager windowManager, int x, int y, String title, ButtonEvent okEvent) {
        Panel ask = new Panel(mc, gui)
                .setLayout(new VerticalLayout())
                .setFilledBackground(0xff666666, 0xffaaaaaa)
                .setFilledRectThickness(1);
        ask.setBounds(new Rectangle(x, y, 200, 40));
        Window askWindow = windowManager.createModalWindow(ask);
        ask.addChild(new Label(mc, gui).setText(title));
        Panel buttons = new Panel(mc, gui).setLayout(new HorizontalLayout()).setDesiredWidth(100).setDesiredHeight(18);
        if (okEvent != null) {
            buttons.addChild(new Button(mc, gui).setText("Cancel").addButtonEvent((parent -> {
                windowManager.closeWindow(askWindow);
            })));
            buttons.addChild(new Button(mc, gui).setText("OK").addButtonEvent(parent -> {
                windowManager.closeWindow(askWindow);
                okEvent.buttonClicked(parent);
            }));
        } else {
            buttons.addChild(new Button(mc, gui).setText("OK").addButtonEvent((parent -> {
                windowManager.closeWindow(askWindow);
            })));
        }
        ask.addChild(buttons);
    }

    private void copyConnector() {
        if (editingConnector != null) {
            sendServerCommandTyped(XNetMessages.INSTANCE, TileEntityController.CMD_COPYCONNECTOR,
                    TypedMap.builder()
                            .put(PARAM_INDEX, getSelectedChannel())
                            .put(PARAM_POS, editingConnector.getPos())
                            .put(PARAM_SIDE, editingConnector.getSide().ordinal())
                            .build());
        }
    }


    private void copyChannel() {
        showMessage(minecraft, this, getWindowManager(), 50, 50, TextFormatting.GREEN + "Copied channel");
        sendServerCommandTyped(XNetMessages.INSTANCE, TileEntityController.CMD_COPYCHANNEL,
                TypedMap.builder()
                        .put(PARAM_INDEX, getSelectedChannel())
                        .build());
    }

    public static void showError(String error) {
        if (openController != null) {
            Minecraft mc = Minecraft.getInstance();
            showMessage(mc, openController, openController.getWindowManager(), 50, 50, TextFormatting.RED + error);
        }
    }

    public static void toClipboard(String json) {
        try {
            StringSelection selection = new StringSelection(json);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
        } catch (Exception e) {
            showError("Error copying to clipboard!");
        }
    }

    private void pasteConnector() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            String json = (String) clipboard.getData(DataFlavor.stringFlavor);
            if (json.length() > 26000) {
                showMessage(minecraft, this, getWindowManager(), 50, 50, TextFormatting.RED + "Clipboard too large!");
                return;
            }
            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(json).getAsJsonObject();
            String type = root.get("type").getAsString();
            IChannelType channelType = XNet.xNetApi.findType(type);
            if (channelType == null) {
                showMessage(minecraft, this, getWindowManager(), 50, 50, TextFormatting.RED + "Unsupported channel type: " + type + "!");
                return;
            }
            sendServerCommandTyped(XNetMessages.INSTANCE, TileEntityController.CMD_PASTECONNECTOR,
                    TypedMap.builder()
                            .put(PARAM_INDEX, getSelectedChannel())
                            .put(PARAM_POS, editingConnector.getPos())
                            .put(PARAM_SIDE, editingConnector.getSide().ordinal())
                            .put(PARAM_JSON, json)
                            .build());
            if (connectorList.getSelected() != -1) {
                delayedSelectedChannel = getSelectedChannel();
                delayedSelectedLine = connectorList.getSelected();
                delayedSelectedConnector = connectorPositions.get(connectorList.getSelected());
            }
            refresh();
        } catch (UnsupportedFlavorException e) {
            showMessage(minecraft, this, getWindowManager(), 50, 50, TextFormatting.RED + "Clipboard does not contain connector!");
        } catch (Exception e) {
            showMessage(minecraft, this, getWindowManager(), 50, 50, TextFormatting.RED + "Error reading from clipboard!");
        }
    }

    private void pasteChannel() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            String json = (String) clipboard.getData(DataFlavor.stringFlavor);
            if (json.length() > 26000) {
                showMessage(minecraft, this, getWindowManager(), 50, 50, TextFormatting.RED + "Clipboard too large!");
                return;
            }
            JsonParser parser = new JsonParser();
            JsonObject root = parser.parse(json).getAsJsonObject();
            String type = root.get("type").getAsString();
            IChannelType channelType = XNet.xNetApi.findType(type);
            if (channelType == null) {
                showMessage(minecraft, this, getWindowManager(), 50, 50, TextFormatting.RED + "Unsupported channel type: " + type + "!");
                return;
            }
            sendServerCommandTyped(XNetMessages.INSTANCE, TileEntityController.CMD_PASTECHANNEL,
                    TypedMap.builder()
                            .put(PARAM_INDEX, getSelectedChannel())
                            .put(PARAM_JSON, json)
                            .build());
            refresh();
        } catch (UnsupportedFlavorException e) {
            showMessage(minecraft, this, getWindowManager(), 50, 50, TextFormatting.RED + "Clipboard does not contain channel!");
        } catch (Exception e) {
            showMessage(minecraft, this, getWindowManager(), 50, 50, TextFormatting.RED + "Error reading from clipboard!");
        }
    }

    private ConnectorClientInfo findClientInfo(ChannelClientInfo info, SidedPos p) {
        for (ConnectorClientInfo connector : info.getConnectors().values()) {
            if (connector.getPos().equals(p)) {
                return connector;
            }
        }
        return null;
    }

    private void refreshConnectorEditor() {
        if (!listsReady()) {
            return;
        }
        if (editingConnector != null && !editingConnector.equals(showingConnector)) {
            showingConnector = editingConnector;
            connectorEditPanel.removeChildren();
            ChannelClientInfo info = fromServer_channels.get(editingChannel);
            if (info != null) {
                ConnectorClientInfo clientInfo = findClientInfo(info, editingConnector);
                if (clientInfo != null) {
                    Direction side = clientInfo.getPos().getSide();
                    SidedConsumer sidedConsumer = new SidedConsumer(clientInfo.getConsumerId(), side.getOpposite());
                    ConnectorClientInfo connectorInfo = info.getConnectors().get(sidedConsumer);

                    Button remove = new Button(minecraft, this).setText("x")
                            .setTextOffset(0, -1)
                            .setTooltips("Remove this connector")
                            .setLayoutHint(new PositionalLayout.PositionalHint(151, 1, 9, 10))
                            .addButtonEvent(parent -> removeConnector(editingConnector));

                    ConnectorEditorPanel editor = new ConnectorEditorPanel(connectorEditPanel, minecraft, this, editingChannel, editingConnector);

                    connectorInfo.getConnectorSettings().createGui(editor);
                    connectorEditPanel.addChild(remove);
                    editor.setState(connectorInfo.getConnectorSettings());
                } else {
                    Button create = new Button(minecraft, this)
                            .setText("Create")
                            .setLayoutHint(new PositionalLayout.PositionalHint(85, 20, 60, 14))
                            .addButtonEvent(parent -> createConnector(editingConnector));
                    connectorEditPanel.addChild(create);

                    Button paste = new Button(minecraft, this)
                            .setText("Paste")
                            .setTooltips("Create a new connector", "from the clipboard")
                            .setLayoutHint(new PositionalLayout.PositionalHint(85, 40, 60, 14))
                            .addButtonEvent(parent -> pasteConnector());
                    connectorEditPanel.addChild(paste);
                }
            }
        } else if (showingConnector != null && editingConnector == null) {
            showingConnector = null;
            connectorEditPanel.removeChildren();
        }
    }



    private void requestListsIfNeeded() {
        if (fromServer_channels != null && fromServer_connectedBlocks != null) {
            return;
        }
        listDirty--;
        if (listDirty <= 0) {
            XNetMessages.INSTANCE.sendToServer(new PacketGetChannels(tileEntity.getPos()));
            XNetMessages.INSTANCE.sendToServer(new PacketGetConnectedBlocks(tileEntity.getPos()));
            listDirty = 10;
            showingChannel = -1;
            showingConnector = null;
        }
    }

    private int getSelectedChannel() {
        for (int i = 0 ; i < MAX_CHANNELS ; i++) {
            if (channelButtons[i].isPressed()) {
                return i;
            }
        }
        return -1;
    }

    private void populateList() {
        if (!listsReady()) {
            return;
        }
        if (!needsRefresh) {
            return;
        }
        needsRefresh = false;

        connectorList.removeChildren();
        connectorPositions.clear();

        int sel = connectorList.getSelected();
        BlockPos prevPos = null;

        String selectedText = searchBar.getText().trim().toLowerCase();

        for (ConnectedBlockClientInfo connectedBlock : fromServer_connectedBlocks) {
            SidedPos sidedPos = connectedBlock.getPos();
            BlockPos coordinate = sidedPos.getPos();
            String name = connectedBlock.getName();
            String blockUnlocName = connectedBlock.getBlockUnlocName();
            String blockName = I18n.format(blockUnlocName).trim();

            int color = StyleConfig.colorTextInListNormal;

            Panel panel = new Panel(minecraft, this).setLayout(new HorizontalLayout().setHorizontalMargin(0).setSpacing(0));
            if (!selectedText.isEmpty()) {
                if (blockName.toLowerCase().contains(selectedText)) {
                    panel.setFilledBackground(0xffddeeaa);
                }
            }
            BlockRender br;
            if (coordinate.equals(prevPos)) {
                br = new BlockRender(minecraft, this);
            } else {
                br = new BlockRender(minecraft, this).setRenderItem(connectedBlock.getConnectedBlock());
                prevPos = coordinate;
            }
            br.setUserObject("block");
            panel.addChild(br);
            if (!name.isEmpty()) {
                br.setTooltips(TextFormatting.GREEN + "Connector: " + TextFormatting.WHITE + name,
                        TextFormatting.GREEN + "Block: " + TextFormatting.WHITE + blockName,
                        TextFormatting.GREEN + "Position: " + TextFormatting.WHITE + BlockPosTools.toString(coordinate),
                        TextFormatting.WHITE + "(doubleclick to highlight)");
            } else {
                br.setTooltips(TextFormatting.GREEN + "Block: " + TextFormatting.WHITE + blockName,
                        TextFormatting.GREEN + "Position: " + TextFormatting.WHITE + BlockPosTools.toString(coordinate),
                        TextFormatting.WHITE + "(doubleclick to highlight)");
            }

            panel.addChild(new Label(minecraft, this).setText(sidedPos.getSide().getName().substring(0, 1).toUpperCase()).setColor(color).setDesiredWidth(18));
            for (int i = 0 ; i < MAX_CHANNELS ; i++) {
                Button but = new Button(minecraft, this).setDesiredWidth(14);
                ChannelClientInfo info = fromServer_channels.get(i);
                if (info != null) {
                    ConnectorClientInfo clientInfo = findClientInfo(info, sidedPos);
                    if (clientInfo != null) {
                        IndicatorIcon icon = clientInfo.getConnectorSettings().getIndicatorIcon();
                        if (icon != null) {
                            but.setImage(icon.getImage(), icon.getU(), icon.getV(), icon.getIw(), icon.getIh());
                        }
                        String indicator = clientInfo.getConnectorSettings().getIndicator();
                        but.setText(indicator != null ? indicator : "");
                    }
                }
                int finalI = i;
                but.addButtonEvent(parent -> selectConnectorEditor(sidedPos, finalI));
                panel.addChild(but);
            }
            connectorList.addChild(panel);
            connectorPositions.add(sidedPos);
        }

        connectorList.setSelected(sel);
        if (delayedSelectedChannel != -1) {
            connectorList.setSelected(delayedSelectedLine);
            selectConnectorEditor(delayedSelectedConnector, delayedSelectedChannel);
        }
        delayedSelectedChannel = -1;
        delayedSelectedLine = -1;
        delayedSelectedConnector = null;
    }

    private boolean listsReady() {
        return fromServer_channels != null && fromServer_connectedBlocks != null;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int x1, int x2) {
        requestListsIfNeeded();
        populateList();
        refreshChannelEditor();
        refreshConnectorEditor();
        if (listsReady() && copyConnector != null && editingChannel != -1) {
            ChannelClientInfo info = fromServer_channels.get(editingChannel);
            ConnectorClientInfo clientInfo = findClientInfo(info, editingConnector);
            copyConnector.setEnabled(clientInfo != null);
        }
        if (fromServer_channels != null) {
            for (int i = 0; i < MAX_CHANNELS; i++) {
                String channel = String.valueOf(i + 1);
                ChannelClientInfo info = fromServer_channels.get(i);
                if (info != null) {
                    IndicatorIcon icon = info.getChannelSettings().getIndicatorIcon();
                    if (icon != null) {
                        channelButtons[i].setImage(icon.getImage(), icon.getU(), icon.getV(), icon.getIw(), icon.getIh());
                    }
                    String indicator = info.getChannelSettings().getIndicator();
                    if (indicator != null) {
                        channelButtons[i].setText(indicator + channel);
                    } else {
                        channelButtons[i].setText(channel);
                    }
                } else {
                    channelButtons[i].setImage(null, 0, 0, 0, 0);
                    channelButtons[i].setText(channel);
                }
            }
        }
        drawWindow();
        int channel = getSelectedChannel();
        if (channel != -1) {
            int x = (int) window.getToplevel().getBounds().getX();
            int y = (int) window.getToplevel().getBounds().getY();
            RenderHelper.drawVerticalGradientRect(x+channel * 14 + 41, y+22, x+channel * 14 + 41+12, y+230, 0x33aaffff, 0x33aaffff);
        }
        tileEntity.getCapability(CapabilityEnergy.ENERGY).ifPresent(h -> {
            long currentRF = h.getEnergyStored();
            int max = h.getMaxEnergyStored();
            energyBar.setValue(currentRF).setMaxValue(max);
        });
    }

    @Override
    protected void drawStackTooltips(int mouseX, int mouseY) {
        int x = (mouseX * width / minecraft.mainWindow.getWidth());
        int y = (mouseY * height / minecraft.mainWindow.getHeight() - 1);
        Widget<?> widget = window.getToplevel().getWidgetAtPosition(x, y);
        if (widget instanceof BlockRender) {
            if ("block".equals(widget.getUserObject())) {
                //System.out.println("GuiController.drawStackTooltips");
                return;     // Don't do the normal tooltip rendering
            }
        }
        super.drawStackTooltips(mouseX, mouseY);
    }
}
