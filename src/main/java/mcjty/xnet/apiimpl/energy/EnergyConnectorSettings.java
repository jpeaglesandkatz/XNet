package mcjty.xnet.apiimpl.energy;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.rftoolsbase.api.xnet.channels.IChannelType;
import mcjty.rftoolsbase.api.xnet.gui.IEditorGui;
import mcjty.rftoolsbase.api.xnet.gui.IndicatorIcon;
import mcjty.rftoolsbase.api.xnet.helper.AbstractConnectorSettings;
import mcjty.xnet.XNet;
import mcjty.xnet.apiimpl.EnumStringTranslators;
import mcjty.xnet.setup.Config;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class EnergyConnectorSettings extends AbstractConnectorSettings {

    public static final ResourceLocation iconGuiElements = ResourceLocation.fromNamespaceAndPath(XNet.MODID, "textures/gui/guielements.png");

    public static final String TAG_MODE = "mode";
    public static final String TAG_RATE = "rate";
    public static final String TAG_MINMAX = "minmax";
    public static final String TAG_PRIORITY = "priority";

    public enum EnergyMode implements StringRepresentable {
        INS,
        EXT;

        public static final Codec<EnergyMode> CODEC = StringRepresentable.fromEnum(EnergyMode::values);
        public static final StreamCodec<FriendlyByteBuf, EnergyMode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(EnergyMode.class);

        @Override
        public String getSerializedName() {
            return name();
        }
    }

    private EnergyMode energyMode = EnergyMode.INS;

    @Nullable private Integer priority = 0;
    @Nullable private Integer rate = null;
    @Nullable private Integer minmax = null;

    public static final MapCodec<EnergyConnectorSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BaseSettings.CODEC.fieldOf("base").forGetter(settings -> settings.settings),
            Direction.CODEC.fieldOf("side").forGetter(EnergyConnectorSettings::getSide),
            EnergyMode.CODEC.fieldOf("mode").forGetter(EnergyConnectorSettings::getEnergyMode),
            Codec.INT.optionalFieldOf("priority").forGetter(settings -> Optional.ofNullable(settings.priority)),
            Codec.INT.optionalFieldOf("rate").forGetter(settings -> Optional.ofNullable(settings.rate)),
            Codec.INT.optionalFieldOf("minmax").forGetter(settings -> Optional.ofNullable(settings.minmax))
    ).apply(instance, EnergyConnectorSettings::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnergyConnectorSettings> STREAM_CODEC = StreamCodec.composite(
            BaseSettings.STREAM_CODEC, s -> s.settings,
            Direction.STREAM_CODEC, AbstractConnectorSettings::getSide,
            EnergyMode.STREAM_CODEC, EnergyConnectorSettings::getEnergyMode,
            ByteBufCodecs.optional(ByteBufCodecs.INT), s -> Optional.ofNullable(s.priority),
            ByteBufCodecs.optional(ByteBufCodecs.INT), s -> Optional.ofNullable(s.rate),
            ByteBufCodecs.optional(ByteBufCodecs.INT), s -> Optional.ofNullable(s.minmax),
            EnergyConnectorSettings::new
    );

    public EnergyConnectorSettings(@Nonnull Direction side) {
        super(DEFAULT_SETTINGS, side);
    }

    public EnergyConnectorSettings(@Nonnull BaseSettings base, @Nonnull Direction side, EnergyMode energyMode, Optional<Integer> priority, Optional<Integer> rate, Optional<Integer> minmax) {
        super(base, side);
        this.energyMode = energyMode;
        this.priority = priority.orElse(null);
        this.rate = rate.orElse(null);
        this.minmax = minmax.orElse(null);
    }

    public EnergyMode getEnergyMode() {
        return energyMode;
    }

    @Override
    public IChannelType getType() {
        return XNet.setup.energyChannelType;
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
        setEnumSafe(object, "energymode", energyMode);
        setIntegerSafe(object, "priority", priority);
        setIntegerSafe(object, "rate", rate);
        setIntegerSafe(object, "minmax", minmax);
        if (rate != null && rate > Config.maxRfRateNormal.get()) {
            object.add("advancedneeded", new JsonPrimitive(true));
        }
        return object;
    }

    @Override
    public void readFromJson(JsonObject object) {
        super.readFromJsonInternal(object);
        energyMode = getEnumSafe(object, "energymode", EnumStringTranslators::getEnergyMode);
        priority = getIntegerSafe(object, "priority");
        rate = getIntegerSafe(object, "rate");
        minmax = getIntegerSafe(object, "minmax");
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
