package mcjty.xnet.apiimpl.logic;

import com.google.gson.JsonObject;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.xnet.channels.IChannelSettings;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import mcjty.rftoolsbase.api.xnet.channels.IControllerContext;
import mcjty.rftoolsbase.api.xnet.gui.IEditorGui;
import mcjty.rftoolsbase.api.xnet.gui.IndicatorIcon;
import mcjty.rftoolsbase.api.xnet.helper.DefaultChannelSettings;
import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import mcjty.xnet.XNet;
import mcjty.xnet.apiimpl.ConnectedBlock;
import mcjty.xnet.apiimpl.logic.enums.LogicMode;
import mcjty.xnet.logic.LogicOperations;
import mcjty.xnet.logic.LogicTools;
import mcjty.xnet.modules.cables.blocks.ConnectorTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static mcjty.xnet.apiimpl.Constants.TAG_COLORS;

public class LogicChannelSettings extends DefaultChannelSettings implements IChannelSettings {

    public static final ResourceLocation iconGuiElements = new ResourceLocation(XNet.MODID, "textures/gui/guielements.png");
    private int colors = 0;     // Colors for this channel
    private List<ConnectedBlock<LogicConnectorSettings>> sensors = null;
    private List<ConnectedBlock<LogicConnectorSettings>> outputs = null;

    @Override
    public JsonObject writeToJson() {
        return new JsonObject();
    }

    @Override
    public void readFromJson(JsonObject data) {
    }


    @Override
    public void readFromNBT(CompoundTag tag) {
        colors = tag.getInt(TAG_COLORS);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        tag.putInt(TAG_COLORS, colors);
    }

    @Override
    public int getColors() {
        return colors;
    }

    @Override
    public void tick(int channel, IControllerContext context) {
        updateCache(channel, context);
        Level world = context.getControllerWorld();

        colors = 0;
        for (ConnectedBlock<LogicConnectorSettings> connectedBlock : sensors) {
            LogicConnectorSettings settings = connectedBlock.settings();
            int sensorColors = 0;
            BlockPos pos = connectedBlock.getConnectedEntity().getBlockPos();
            if (!LevelTools.isLoaded(world, pos)) {
                // If it is not chunkloaded we just use the color settings as we last remembered it
                colors |= settings.getColorMask();
                continue;
            }

            boolean sense = !checkRedstone(world, settings, connectedBlock.connectorPos());
            if (sense && !context.matchColor(settings.getColorsMask())) {
                sense = false;
            }

            // If sense is false the sensor is disabled which means the colors from it will also be disabled
            if (sense) {
                BlockEntity te = connectedBlock.getConnectedEntity();

                for (RSSensor sensor : settings.getSensors()) {
                    if (sensor.test(te, world, pos, settings)) {
                        sensorColors |= 1 << sensor.getOutputColor().ordinal();
                    }
                }
            }
            settings.setColorMask(sensorColors);
            colors |= sensorColors;
        }

        for (ConnectedBlock<LogicConnectorSettings> connector : outputs) {
            LogicConnectorSettings settings = connector.settings();

            BlockPos connectorPos = connector.connectorPos();
            if (!LevelTools.isLoaded(world, connectorPos)) {
                continue;
            }

            Direction side = connector.sidedConsumer().side();
            ConnectorTileEntity connectorTileEntity = connector.getConnectorEntity();
            int powerOut;
            if (checkRedstone(world, settings, connectorPos) || !context.matchColor(settings.getColorsMask())) {
                powerOut = 0;
            } else {
                RSOutput output = settings.getOutput();
                boolean[] colorsArray = LogicTools.intToBinary(colors);
                boolean input1 = colorsArray[output.getInputChannel1().ordinal()];
                boolean input2 = colorsArray[output.getInputChannel2().ordinal()];
                powerOut = LogicOperations.applyFilter(output, input1, input2) ? output.getRedstoneOut() : 0;
            }
            connectorTileEntity.setPowerOut(side, powerOut);
        }
    }

    private void updateCache(int channel, IControllerContext context) {
        if (sensors == null) {
            sensors = new ArrayList<>();
            outputs = new ArrayList<>();
            Level world = context.getControllerWorld();
            Map<SidedConsumer, IConnectorSettings> connectors = context.getConnectors(channel);
            for (Map.Entry<SidedConsumer, IConnectorSettings> entry : connectors.entrySet()) {
                LogicConnectorSettings con = (LogicConnectorSettings) entry.getValue();
                ConnectedBlock<LogicConnectorSettings> connectedBlock;
                connectedBlock = getConnectedBlockInfo(context, entry, world, con);
                if (connectedBlock == null) {
                    continue;
                }
                if (con.getLogicMode() == LogicMode.SENSOR) {
                    sensors.add(connectedBlock);
                } else {
                    outputs.add(connectedBlock);
                }
            }

            connectors = context.getRoutedConnectors(channel);
            for (Map.Entry<SidedConsumer, IConnectorSettings> entry : connectors.entrySet()) {
                LogicConnectorSettings con = (LogicConnectorSettings) entry.getValue();
                if (con.getLogicMode() == LogicMode.OUTPUT) {
                    ConnectedBlock<LogicConnectorSettings> connectedBlock;
                    connectedBlock = getConnectedBlockInfo(context, entry, world, con);
                    if (connectedBlock == null) {
                        continue;
                    }
                    outputs.add(connectedBlock);
                }
            }
        }
    }

    @Nullable
    private ConnectedBlock<LogicConnectorSettings> getConnectedBlockInfo(
            IControllerContext context, Map.Entry<SidedConsumer, IConnectorSettings> entry, Level world, LogicConnectorSettings con
    ) {
        BlockPos connectorPos = context.findConsumerPosition(entry.getKey().consumerId());
        if (connectorPos == null) {
            return null;
        }
        BlockPos connectedBlockPos = connectorPos.relative(entry.getKey().side());
        BlockEntity connectedEntity = world.getBlockEntity(connectedBlockPos);
        if (connectedEntity == null) {
            return null;
        }
        ConnectorTileEntity connectorEntity = (ConnectorTileEntity) world.getBlockEntity(connectorPos);
        if (connectorEntity == null) {
            return null;
        }
        return new ConnectedBlock<>(entry.getKey(), con, connectorPos, connectedEntity, connectorEntity);
    }

    @Override
    public void cleanCache() {
        sensors = null;
    }

    @Nullable
    @Override
    public IndicatorIcon getIndicatorIcon() {
        return new IndicatorIcon(iconGuiElements, 11, 90, 11, 10);
    }

    @Nullable
    @Override
    public String getIndicator() {
        return null;
    }

    @Override
    public boolean isEnabled(String tag) {
        return true;
    }

    @Override
    public void createGui(IEditorGui gui) {

    }

    @Override
    public void update(Map<String, Object> data) {

    }
}
