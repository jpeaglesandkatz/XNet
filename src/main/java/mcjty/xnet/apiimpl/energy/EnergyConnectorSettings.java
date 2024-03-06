package mcjty.xnet.apiimpl.energy;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import mcjty.rftoolsbase.api.xnet.gui.IEditorGui;
import mcjty.rftoolsbase.api.xnet.gui.IndicatorIcon;
import mcjty.rftoolsbase.api.xnet.helper.AbstractConnectorSettings;
import mcjty.xnet.XNet;
import mcjty.xnet.apiimpl.EnumStringTranslators;
import mcjty.xnet.setup.Config;
import mcjty.xnet.utils.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

import static mcjty.xnet.apiimpl.Constants.TAG_ADVANCED_NEEDED;
import static mcjty.xnet.apiimpl.Constants.TAG_ENERGY_MODE;
import static mcjty.xnet.apiimpl.Constants.TAG_MINMAX;
import static mcjty.xnet.apiimpl.Constants.TAG_MODE;
import static mcjty.xnet.apiimpl.Constants.TAG_PRIORITY;
import static mcjty.xnet.apiimpl.Constants.TAG_RATE;

public class EnergyConnectorSettings extends AbstractConnectorSettings {

    public static final ResourceLocation iconGuiElements = new ResourceLocation(XNet.MODID, "textures/gui/guielements.png");

    public enum EnergyMode {
        INS,
        EXT
    }

    @Getter
    private EnergyMode energyMode = EnergyMode.INS;

    @Nullable private Integer priority = 0;
    @Nullable private Integer rate = null;
    @Nullable private Integer minmax = null;

    public EnergyConnectorSettings(@Nonnull Direction side) {
        super(side);
    }

    @Nullable
    @Override
    public IndicatorIcon getIndicatorIcon() {
        return switch (energyMode) {
            case INS -> new IndicatorIcon(iconGuiElements, 0, 70, 13, 10);
            case EXT -> new IndicatorIcon(iconGuiElements, 13, 70, 13, 10);
        };
    }

    @Override
    @Nullable
    public String getIndicator() {
        return null;
    }

    @Override
    public void createGui(IEditorGui gui) {
        advanced = gui.isAdvanced();
        sideGui(gui);
        colorsGui(gui);
        redstoneGui(gui);
        gui.nl()
                .choices(TAG_MODE, "Insert or extract mode", energyMode, EnergyMode.values())
                .nl()

                .label("Pri").integer(TAG_PRIORITY, "Insertion priority", priority, 30).nl()

                .label("Rate")
                .integer(TAG_RATE,
                        (energyMode == EnergyMode.EXT ? "Max energy extraction rate" : "Max energy insertion rate") +
                        "|(limited to " + (advanced ? Config.maxRfRateAdvanced.get() : Config.maxRfRateNormal.get()) + " per tick)", rate, 40)
                .shift(10)
                .label(energyMode == EnergyMode.EXT ? "Min" : "Max")
                .integer(TAG_MINMAX, energyMode == EnergyMode.EXT ? "Disable extraction if energy|is too low" : "Disable insertion if energy|is too high", minmax, 50);
    }

    private static final Set<String> INSERT_TAGS = ImmutableSet.of(TAG_MODE, TAG_RS, TAG_COLOR+"0", TAG_COLOR+"1", TAG_COLOR+"2", TAG_COLOR+"3", TAG_RATE, TAG_MINMAX, TAG_PRIORITY);
    private static final Set<String> EXTRACT_TAGS = ImmutableSet.of(TAG_MODE, TAG_RS, TAG_COLOR+"0", TAG_COLOR+"1", TAG_COLOR+"2", TAG_COLOR+"3", TAG_RATE, TAG_MINMAX, TAG_PRIORITY);

    @Override
    public boolean isEnabled(String tag) {
        if (energyMode == EnergyMode.INS) {
            if (tag.equals(TAG_FACING)) {
                return advanced;
            }
            return INSERT_TAGS.contains(tag);
        } else {
            if (tag.equals(TAG_FACING)) {
                return false;           // We cannot extract from different sides
            }
            return EXTRACT_TAGS.contains(tag);
        }
    }

    @Nonnull
    public Integer getPriority() {
        return priority == null ? 0 : priority;
    }

    @Nullable
    public Integer getRate() {
        return rate;
    }

    @Nullable
    public Integer getMinmax() {
        return minmax;
    }

    @Override
    public void update(Map<String, Object> data) {
        super.update(data);
        energyMode = EnergyMode.valueOf(((String)data.get(TAG_MODE)).toUpperCase());
        rate = (Integer) data.get(TAG_RATE);
        minmax = (Integer) data.get(TAG_MINMAX);
        priority = (Integer) data.get(TAG_PRIORITY);
    }

    @Override
    public JsonObject writeToJson() {
        JsonObject object = new JsonObject();
        super.writeToJsonInternal(object);
        setEnumSafe(object, TAG_ENERGY_MODE, energyMode);
        setIntegerSafe(object, TAG_PRIORITY, priority);
        setIntegerSafe(object, TAG_RATE, rate);
        setIntegerSafe(object, TAG_MINMAX, minmax);
        if (rate != null && rate > Config.maxRfRateNormal.get()) {
            object.add(TAG_ADVANCED_NEEDED, new JsonPrimitive(true));
        }
        return object;
    }

    @Override
    public void readFromJson(JsonObject object) {
        super.readFromJsonInternal(object);
        energyMode = getEnumSafe(object, TAG_ENERGY_MODE, EnumStringTranslators::getEnergyMode);
        priority = getIntegerSafe(object, TAG_PRIORITY);
        rate = getIntegerSafe(object, TAG_RATE);
        minmax = getIntegerSafe(object, TAG_MINMAX);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        byte energyModeByte = tag.contains(TAG_ENERGY_MODE) 
                                      ? tag.getByte(TAG_ENERGY_MODE) 
                                      : tag.getByte("itemMode"); // TODO: 06.03.2024 backward compatibility. DELETE THIS after 1.20.4_neo branch
        energyMode = EnergyMode.values()[energyModeByte];
        priority = TagUtils.getIntOrNull(tag, TAG_PRIORITY);
        rate = TagUtils.getIntOrNull(tag, TAG_RATE);
        minmax = TagUtils.getIntOrNull(tag, TAG_MINMAX);
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putByte(TAG_ENERGY_MODE, (byte) energyMode.ordinal());
        TagUtils.putIntIfNotNull(tag, TAG_PRIORITY, priority);
        TagUtils.putIntIfNotNull(tag, TAG_RATE, rate);
        TagUtils.putIntIfNotNull(tag, TAG_RATE, minmax);
    }
}
