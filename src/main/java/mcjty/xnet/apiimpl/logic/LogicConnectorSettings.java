package mcjty.xnet.apiimpl.logic;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import lombok.Setter;
import mcjty.lib.varia.JSonTools;
import mcjty.rftoolsbase.api.xnet.gui.IEditorGui;
import mcjty.rftoolsbase.api.xnet.gui.IndicatorIcon;
import mcjty.rftoolsbase.api.xnet.helper.AbstractConnectorSettings;
import mcjty.rftoolsbase.api.xnet.helper.BaseStringTranslators;
import mcjty.xnet.XNet;
import mcjty.xnet.apiimpl.EnumStringTranslators;
import mcjty.xnet.apiimpl.logic.enums.LogicFilter;
import mcjty.xnet.modules.controller.client.ConnectorEditorPanel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class LogicConnectorSettings extends AbstractConnectorSettings {

    public static final ResourceLocation iconGuiElements = new ResourceLocation(XNet.MODID, "textures/gui/guielements.png");

    public static final String TAG_MODE = "mode";
    public static final String TAG_REDSTONE_OUT = "rsout";

    public enum LogicMode {
        SENSOR,
        OUTPUT
    }

    public static final int SENSORS = 4;

    private LogicMode logicMode = LogicMode.SENSOR;
    @Getter
    private List<RSSensor> sensors = null;
    @Getter
    private RSOutput output = null;

    private int colorMask;         // Current colormask

    public LogicConnectorSettings(@Nonnull Direction side) {
        super(side);
        sensors = new ArrayList<>(SENSORS);
        for (int i = 0 ; i < SENSORS ; i++) {
            sensors.add(new RSSensor(i));
        }
        output = new RSOutput(this.advanced);
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
        for (RSSensor sensor : sensors) {
            if (sensor.isEnabled(tag)) {
                return true;
            }
        }

        if (output.isEnabled(tag)) {
            return true;
        }

        return TAGS.contains(tag);
    }

    @Override
    public void createGui(IEditorGui gui) {
        advanced = gui.isAdvanced();
        sideGui(gui);
        colorsGui(gui);
        redstoneGui(gui);
        gui.nl()
                .choices(TAG_MODE, "Sensor or Output mode", logicMode, LogicMode.values())
                .nl();
        if (logicMode == LogicMode.SENSOR) {
            for (RSSensor sensor : sensors) {
                sensor.createGui(gui);
            }
        } else {
            output.createGui((ConnectorEditorPanel) gui); // TODO: 06.03.2024 remove ConnectorEditorPanel cast after rftoolbase update
        }
    }

    @Override
    public void update(Map<String, Object> data) {
        super.update(data);
        logicMode = LogicMode.valueOf(((String)data.get(TAG_MODE)).toUpperCase());

        if (logicMode == LogicMode.SENSOR) {
            for (RSSensor sensor : sensors) {
                sensor.update(data);
            }
        } else {
            output.update(data);
        }
    }

    @Override
    public JsonObject writeToJson() {
        JsonObject object = new JsonObject();
        super.writeToJsonInternal(object);
        setEnumSafe(object, "logicmode", logicMode);
        JsonArray sensorArray = new JsonArray();
        for (RSSensor sensor : sensors) {
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
        if (!output.getLogicFilter().equals(LogicFilter.OFF)) {
            object.add("advancedneeded", new JsonPrimitive(true));
        }
        JsonObject outputJSON = new JsonObject();
        setEnumSafe(outputJSON, "logicFilter", output.getLogicFilter());
        setEnumSafe(outputJSON, "inputChannel1", output.getInputChannel1());
        setEnumSafe(outputJSON, "inputChannel2", output.getInputChannel2());
        outputJSON.addProperty("countingHolder", output.getCountingHolder());
        outputJSON.addProperty("ticksHolder", output.getTicksHolder());
        outputJSON.addProperty("redstoneOutput", output.getRedstoneOut());
        object.add("output", outputJSON);

        return object;
    }

    @Override
    public void readFromJson(JsonObject object) {
        super.readFromJsonInternal(object);
        logicMode = getEnumSafe(object, "logicmode", EnumStringTranslators::getLogicMode);
        JsonArray sensorArray = object.get("sensors").getAsJsonArray();
        sensors.clear();
        for (JsonElement oe : sensorArray) {
            JsonObject o = oe.getAsJsonObject();
            RSSensor sensor = new RSSensor(sensors.size());
            sensor.setAmount(getIntegerNotNull(o, "amount"));
            sensor.setOperator(getEnumSafe(o, "operator", EnumStringTranslators::getOperator));
            sensor.setOutputColor(getEnumSafe(o, "outputcolor", BaseStringTranslators::getColor));
            sensor.setSensorMode(getEnumSafe(o, "sensormode", EnumStringTranslators::getSensorMode));
            if (o.has("filter")) {
                sensor.setFilter(JSonTools.jsonToItemStack(o.get("filter").getAsJsonObject()));
            } else {
                sensor.setFilter(ItemStack.EMPTY);
            }
            sensors.add(sensor);
        }
        JsonObject outputJSON = object.getAsJsonObject("output");
        output = new RSOutput(advanced);
        output.setLogicFilter(getEnumSafe(outputJSON, "logicFilter", EnumStringTranslators::getLogicFilter));
        output.setInputChannel1(getEnumSafe(outputJSON, "inputChannel1", BaseStringTranslators::getColor));
        output.setInputChannel2(getEnumSafe(outputJSON, "inputChannel2", BaseStringTranslators::getColor));
        output.setCountingHolder(getIntegerNotNull(outputJSON, "countingHolder"));
        output.setTicksHolder(getIntegerNotNull(outputJSON, "ticksHolder"));
        output.setRedstoneOut(getIntegerNotNull(outputJSON, "redstoneOutput"));
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        logicMode = LogicMode.values()[tag.getByte("logicMode")];
        colorMask = tag.getInt("colors");
        for (RSSensor sensor : sensors) {
            sensor.readFromNBT(tag);
        }
        output.readFromNBT(tag);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putByte("logicMode", (byte) logicMode.ordinal());
        tag.putInt("colors", colorMask);
        for (RSSensor sensor : sensors) {
            sensor.writeToNBT(tag);
        }
        output.writeToNBT(tag);
    }

}
