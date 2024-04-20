package mcjty.xnet.modules.cables.client;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.VerticalAlignment;
import mcjty.lib.gui.widgets.BlockRender;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.lib.network.PacketRequestDataFromServer;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.OrientationTools;
import mcjty.xnet.modules.cables.CableModule;
import mcjty.xnet.modules.cables.ConnectorType;
import mcjty.xnet.modules.cables.blocks.ConnectorTileEntity;
import mcjty.xnet.setup.XNetMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

import static mcjty.lib.gui.widgets.Widgets.horizontal;
import static mcjty.lib.gui.widgets.Widgets.label;
import static mcjty.lib.gui.widgets.Widgets.vertical;
import static mcjty.xnet.apiimpl.Constants.TAG_NAME;
import static mcjty.xnet.modules.cables.blocks.ConnectorTileEntity.CMD_ENABLE;
import static mcjty.xnet.modules.cables.blocks.ConnectorTileEntity.CMD_GET_NAME;
import static mcjty.xnet.modules.cables.blocks.ConnectorTileEntity.PARAM_ENABLED;
import static mcjty.xnet.modules.cables.blocks.ConnectorTileEntity.PARAM_FACING;
import static mcjty.xnet.utils.I18nConstants.CONNECTOR_NAME_TOOLTIP;
import static mcjty.xnet.utils.I18nConstants.DIRECTIONS_LABEL;
import static mcjty.xnet.utils.I18nConstants.NAME_LABEL;

public class GuiConnector extends GenericGuiContainer<ConnectorTileEntity, GenericContainer> {

    public static final int WIDTH = 230;
    public static final int HEIGHT = 60;

    private final ToggleButton[] toggleButtons = new ToggleButton[6];
    private final BlockRender[] connectedBlockRenders = new BlockRender[6];

//    public GuiConnector(AdvancedConnectorTileEntity te, EmptyContainer container, PlayerInventory inventory) {
//        this((ConnectorTileEntity) te, container, inventory);
//    }

    public GuiConnector(ConnectorTileEntity tileEntity, GenericContainer container, Inventory inventory) {
        super(tileEntity, container, inventory, CableModule.CONNECTOR.get().getManualEntry());
        Level level = tileEntity.getLevel();
        if (level == null) {
            return;
        }
        XNetMessages.INSTANCE.sendToServer(new PacketRequestDataFromServer(level.dimension(), tileEntity.getBlockPos(), CMD_GET_NAME.name(), TypedMap.EMPTY, false));
        imageWidth = WIDTH;
        imageHeight = HEIGHT;
    }

    public static void register() {
        register(CableModule.CONTAINER_CONNECTOR.get(), GuiConnector::new);
    }

    @Override
    public void init() {
        super.init();

        Panel toplevel = vertical().filledRectThickness(2);
        Level level = tileEntity.getLevel();
        if (level == null) {
            return;
        }
        BlockEntity te = level.getBlockEntity(tileEntity.getBlockPos());
        if (te instanceof ConnectorTileEntity) {
            tileEntity.setConnectorName(((ConnectorTileEntity) te).getConnectorName());
        }
        String connectorName = tileEntity.getConnectorName();
        TextField nameField = new TextField().name(TAG_NAME).tooltips(CONNECTOR_NAME_TOOLTIP.i18n());

        if (connectorName != null && ! connectorName.isEmpty()) {
            nameField.text(tileEntity.getConnectorName());
        }
        Label nameLabel = label(NAME_LABEL.i18n())
                                  .verticalAlignment(VerticalAlignment.ALIGN_CENTER)
                                  .horizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
        Panel namePanel = horizontal().children(nameLabel, nameField).desiredHeight(20);
        toplevel.children(namePanel);
        Label directionsLabel = label(DIRECTIONS_LABEL.i18n())
                                        .desiredWidth(70)
                                        .horizontalAlignment(HorizontalAlignment.ALIGN_LEFT);
        Panel directionsPanel = horizontal().children(directionsLabel);
        Panel directionValuesPanel = vertical();
        Panel directionNamesPanel = horizontal(6, 5);
        Panel directionBlocksPanel = horizontal();
        for (Direction facing : OrientationTools.DIRECTION_VALUES) {
            BlockPos consumerPos = tileEntity.getBlockPos().relative(facing);

            BlockState state = level.getBlockState(consumerPos);
            ItemStack item = state.getBlock().getCloneItemStack(level, consumerPos, state);
            connectedBlockRenders[facing.ordinal()] = new BlockRender().renderItem(item).userObject("block");
            toggleButtons[facing.ordinal()] = new ToggleButton().text(facing.getSerializedName().substring(0, 1).toUpperCase())
                                                      .desiredWidth(16)
                                                      .verticalAlignment(VerticalAlignment.ALIGN_CENTER)
                                                      .horizontalAlignment(HorizontalAlignment.ALIGN_CENTER)
                .event(() -> {
                    sendServerCommandTyped(CMD_ENABLE,
                            TypedMap.builder()
                                    .put(PARAM_FACING, facing.ordinal())
                                    .put(PARAM_ENABLED, toggleButtons[facing.ordinal()].isPressed())
                                    .build());
                });
            boolean isEnabled = !tileEntity.getBlockState().getValue(EnumProperty.create(facing.getName(), ConnectorType.class)).equals(ConnectorType.NONE);
            toggleButtons[facing.ordinal()].pressed(isEnabled);
            directionNamesPanel.children(toggleButtons[facing.ordinal()]);
        }
        directionBlocksPanel.children(connectedBlockRenders);
        directionValuesPanel.children(directionBlocksPanel, directionNamesPanel);
        directionsPanel.children(directionValuesPanel);
        toplevel.children(directionsPanel);
        toplevel.bounds(leftPos, topPos, WIDTH, HEIGHT);
        window = new Window(this, toplevel);

        window.bind(TAG_NAME, tileEntity, TAG_NAME);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        drawWindow(graphics);
    }
}
