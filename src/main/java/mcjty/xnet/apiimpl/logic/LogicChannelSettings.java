package mcjty.xnet.apiimpl.logic;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.LevelTools;
import mcjty.rftoolsbase.api.xnet.channels.IChannelSettings;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.channels.IConnectorSettings;
import mcjty.rftoolsbase.api.xnet.channels.IControllerContext;
import mcjty.rftoolsbase.api.xnet.gui.IEditorGui;
import mcjty.rftoolsbase.api.xnet.gui.IndicatorIcon;
import mcjty.rftoolsbase.api.xnet.helper.DefaultChannelSettings;
import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import mcjty.xnet.XNet;
import mcjty.xnet.apiimpl.fluids.FluidChannelSettings;
import mcjty.xnet.modules.cables.blocks.ConnectorTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LogicChannelSettings extends DefaultChannelSettings implements IChannelSettings {

    public static final ResourceLocation iconGuiElements = ResourceLocation.fromNamespaceAndPath(XNet.MODID, "textures/gui/guielements.png");

    public int delay = 0;
    public int colors = 0;     // Colors for this channel
    private List<Pair<SidedConsumer, LogicConnectorSettings>> sensors = null;
    private List<Pair<SidedConsumer, LogicConnectorSettings>> outputs = null;

    @Override
    public IChannelType getType() {
        return XNet.setup.logicChannelType;
    }

    @Override
    public JsonObject writeToJson() {
        return new JsonObject();
    }

    @Override
    public void readFromJson(JsonObject data) {
    }


    @Override
    public void readFromNBT(CompoundTag tag) {
        delay = tag.getInt("delay");
        colors = tag.getInt("colors");
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        tag.putInt("delay", delay);
        tag.putInt("colors", colors);
    }

    @Override
    public int getColors() {
        return colors;
    }

    @Override
    public void tick(int channel, IControllerContext context) {
        delay--;
        if (delay <= 0) {
            delay = 200 * 6;      // Multiply of the different speeds we have
        }
        if (delay % 5 != 0) {
            return;
        }
        int d = delay / 5;
        updateCache(channel, context);
        Level world = context.getControllerWorld();

        colors = 0;
        for (Pair<SidedConsumer, LogicConnectorSettings> entry : sensors) {
            LogicConnectorSettings settings = entry.getValue();
            if (d % settings.getSpeed() != 0) {
                // Use the color settings from this connector as we last remembered it
                colors |= settings.getColorMask();
                continue;
            }
            int sensorColors = 0;
            BlockPos connectorPos = context.findConsumerPosition(entry.getKey().consumerId());
            if (connectorPos != null) {
                Direction side = entry.getKey().side();
                BlockPos pos = connectorPos.relative(side);
                if (!LevelTools.isLoaded(world, pos)) {
                    // If it is not chunkloaded we just use the color settings as we last remembered it
                    colors |= settings.getColorMask();
                    continue;
                }

                boolean sense = true;

                sense = !checkRedstone(world, settings, connectorPos);
                if (sense && !context.matchColor(settings.getColorsMask())) {
                    sense = false;
                }

                // If sense is false the sensor is disabled which means the colors from it will also be disabled
                if (sense) {
                    BlockEntity te = world.getBlockEntity(pos);

                    for (Sensor sensor : settings.getSensors()) {
                        if (sensor.test(te, world, pos, settings)) {
                            sensorColors |= 1 << sensor.getOutputColor().ordinal();
                        }
                    }
                }
            }
            settings.setColorMask(sensorColors);
            colors |= sensorColors;
        }

        for (Pair<SidedConsumer, LogicConnectorSettings> entry : outputs) {
            LogicConnectorSettings settings = entry.getValue();
            if (d % settings.getSpeed() != 0) {
                continue;
            }

            BlockPos connectorPos = context.findConsumerPosition(entry.getKey().consumerId());
            if (connectorPos != null) {
                Direction side = entry.getKey().side();
                if (!LevelTools.isLoaded(world, connectorPos)) {
                    continue;
                }

                BlockEntity te = world.getBlockEntity(connectorPos);
                if (te instanceof ConnectorTileEntity connectorTE) {
                    int powerOut;
                    if (checkRedstone(world, settings, connectorPos)) {
                        powerOut = 0;
                    } else if (!context.matchColor(settings.getColorsMask())) {
                        powerOut = 0;
                    } else {
                        powerOut = settings.getRedstoneOut() == null ? 0 : settings.getRedstoneOut();
                    }
                    connectorTE.setPowerOut(side, powerOut);
                }
            }
        }
    }

    private void updateCache(int channel, IControllerContext context) {
        if (sensors == null) {
            sensors = new ArrayList<>();
            outputs = new ArrayList<>();
            Map<SidedConsumer, IConnectorSettings> connectors = context.getConnectors(channel);
            for (Map.Entry<SidedConsumer, IConnectorSettings> entry : connectors.entrySet()) {
                LogicConnectorSettings con = (LogicConnectorSettings) entry.getValue();
                if (con.getLogicMode() == LogicConnectorSettings.LogicMode.SENSOR) {
                    sensors.add(Pair.of(entry.getKey(), con));
                } else {
                    outputs.add(Pair.of(entry.getKey(), con));
                }
            }

            connectors = context.getRoutedConnectors(channel);
            for (Map.Entry<SidedConsumer, IConnectorSettings> entry : connectors.entrySet()) {
                LogicConnectorSettings con = (LogicConnectorSettings) entry.getValue();
                if (con.getLogicMode() == LogicConnectorSettings.LogicMode.OUTPUT) {
                    outputs.add(Pair.of(entry.getKey(), con));
                }
            }
        }
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
