package mcjty.xnet.apiimpl.logic;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.CompositeStreamCodec;
import mcjty.lib.varia.JSonTools;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.gui.IEditorGui;
import mcjty.rftoolsbase.api.xnet.gui.IndicatorIcon;
import mcjty.rftoolsbase.api.xnet.helper.AbstractConnectorSettings;
import mcjty.xnet.XNet;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LogicConnectorSettings extends AbstractConnectorSettings {

    public static final ResourceLocation iconGuiElements = ResourceLocation.fromNamespaceAndPath(XNet.MODID, "textures/gui/guielements.png");

    public static final String TAG_MODE = "mode";
    public static final String TAG_SPEED = "speed";
    public static final String TAG_REDSTONE_OUT = "rsout";

    public enum LogicMode implements StringRepresentable {
        SENSOR,
        OUTPUT;

        public static final Codec<LogicMode> CODEC = StringRepresentable.fromEnum(LogicMode::values);
        public static final StreamCodec<FriendlyByteBuf, LogicMode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(LogicMode.class);

        @Override
        public String getSerializedName() {
            return name();
        }
    }

    public static final int SENSORS = 4;

    private List<Sensor> sensors = null;

    private LogicMode logicMode = LogicMode.SENSOR;
    private int colors;         // Current colormask
    private int speed = 2;
    private Integer redstoneOut;    // Redstone output value

    public static final MapCodec<LogicConnectorSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BaseSettings.CODEC.fieldOf("base").forGetter(settings -> settings.settings),
            Direction.CODEC.fieldOf("side").forGetter(LogicConnectorSettings::getSide),
            LogicMode.CODEC.fieldOf("mode").forGetter(LogicConnectorSettings::getLogicMode),
            Codec.INT.fieldOf("colors").forGetter(settings -> settings.colors),
            Codec.INT.fieldOf("speed").forGetter(settings -> settings.speed),
            Codec.INT.fieldOf("rsout").forGetter(settings -> settings.redstoneOut),
            Codec.list(Sensor.CODEC).fieldOf("sensors").forGetter(LogicConnectorSettings::getSensors)
    ).apply(instance, LogicConnectorSettings::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LogicConnectorSettings> STREAM_CODEC = CompositeStreamCodec.composite(
            BaseSettings.STREAM_CODEC, s -> s.settings,
            Direction.STREAM_CODEC, AbstractConnectorSettings::getSide,
            LogicMode.STREAM_CODEC, LogicConnectorSettings::getLogicMode,
            ByteBufCodecs.INT, s -> s.colors,
            ByteBufCodecs.INT, s -> s.speed,
            ByteBufCodecs.INT, s -> s.redstoneOut,
            Sensor.STREAM_CODEC.apply(ByteBufCodecs.list()), LogicConnectorSettings::getSensors,
            LogicConnectorSettings::new
    );

    public LogicConnectorSettings(@Nonnull BaseSettings base, @Nonnull Direction side, LogicMode logicMode, int colors, int speed, Integer redstoneOut, List<Sensor> sensors) {
        this(base, side);
        this.logicMode = logicMode;
        this.colors = colors;
        this.speed = speed;
        this.redstoneOut = redstoneOut;
        this.sensors = sensors;
    }

    public LogicConnectorSettings(@Nonnull BaseSettings settings, @Nonnull Direction side) {
        super(settings, side);
        sensors = new ArrayList<>(SENSORS);
        for (int i = 0 ; i < SENSORS ; i++) {
            sensors.add(new Sensor(i));
        }
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public void setColorMask(int colors) {
        this.colors = colors;
    }

    public int getColorMask() {
        return colors;
    }

    public Integer getRedstoneOut() {
        return redstoneOut;
    }

    @Override
    public IChannelType getType() {
        return XNet.setup.logicChannelType;
    }

    @Nullable
    @Override
    public IndicatorIcon getIndicatorIcon() {
        return switch (logicMode) {
            case SENSOR -> new IndicatorIcon(iconGuiElements, 26, 70, 13, 10);
            case OUTPUT -> new IndicatorIcon(iconGuiElements, 39, 70, 13, 10);
        };
    }

    @Nullable
    @Override
    public String getIndicator() {
        return null;
    }

    private static final Set<String> TAGS = ImmutableSet.of(TAG_REDSTONE_OUT, TAG_MODE, TAG_RS, TAG_COLOR+"0", TAG_COLOR+"1", TAG_COLOR+"2", TAG_COLOR+"3");

    @Override
    public boolean isEnabled(String tag) {
        if (tag.equals(TAG_FACING)) {
            return advanced && logicMode != LogicMode.OUTPUT;
        }
        if (tag.equals(TAG_SPEED)) {
            return true;
        }
        for (Sensor sensor : sensors) {
            if (sensor.isEnabled(tag)) {
                return true;
            }
        }

        return TAGS.contains(tag);
    }

    public int getSpeed() {
        return speed;
    }

    public LogicMode getLogicMode() {
        return logicMode;
    }


    @Override
    public void createGui(IEditorGui gui) {
        advanced = gui.isAdvanced();
        String[] speeds;
        if (advanced) {
            speeds = new String[] { "5", "10", "20", "60", "100", "200" };
        } else {
            speeds = new String[] { "10", "20", "60", "100", "200" };
        }
        sideGui(gui);
        colorsGui(gui);
        redstoneGui(gui);
        gui.nl()
                .choices(TAG_MODE, "Sensor or Output mode", logicMode, LogicMode.values())
                .choices(TAG_SPEED, (logicMode == LogicMode.SENSOR ? "Number of ticks for each check" : "Number of ticks for each operation"), Integer.toString(speed * 5), speeds)
                .nl();
        if (logicMode == LogicMode.SENSOR) {
            for (Sensor sensor : sensors) {
                sensor.createGui(gui);
            }
        } else {
            gui.label("Redstone:")
                    .integer(TAG_REDSTONE_OUT, "Redstone output value", redstoneOut, 40, 16)
                    .nl();
        }
    }

    @Override
    public void update(Map<String, Object> data) {
        super.update(data);
        logicMode = LogicMode.valueOf(((String)data.get(TAG_MODE)).toUpperCase());
        String facing = (String) data.get(TAG_FACING);
        // @todo suspicious

        speed = Integer.parseInt((String) data.get(TAG_SPEED)) / 5;
        if (speed == 0) {
            speed = 2;
        }
        if (logicMode == LogicMode.SENSOR) {
            for (Sensor sensor : sensors) {
                sensor.update(data);
            }
        } else {
            redstoneOut = (Integer) data.get(TAG_REDSTONE_OUT);
        }
    }

    @Override
    public JsonObject writeToJson() {
        JsonObject object = new JsonObject();
        super.writeToJsonInternal(object);
        setEnumSafe(object, "logicmode", logicMode);
        setIntegerSafe(object, "speed", speed);
        JsonArray sensorArray = new JsonArray();
        for (Sensor sensor : sensors) {
            JsonObject o = new JsonObject();
            setEnumSafe(o, "sensormode", sensor.getSensorMode());
            setEnumSafe(o, "outputcolor", sensor.getOutputColor());
            setEnumSafe(o, "operator", sensor.getOperator());
            setIntegerSafe(o, "amount", sensor.getAmount());
            if (!sensor.getFilter().isEmpty()) {
                o.add("filter", JSonTools.itemStackToJson(sensor.getFilter()));
            }
            sensorArray.add(o);
        }
        object.add("sensors", sensorArray);
        if (speed == 1) {
            object.add("advancedneeded", new JsonPrimitive(true));
        }
        return object;
    }

    @Override
    public void readFromJson(JsonObject object) {
        super.readFromJsonInternal(object);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
    }

}
