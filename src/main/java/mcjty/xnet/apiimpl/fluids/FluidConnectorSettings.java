package mcjty.xnet.apiimpl.fluids;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.CompositeStreamCodec;
import mcjty.lib.varia.FluidTools;
import mcjty.lib.varia.JSonTools;
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
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class FluidConnectorSettings extends AbstractConnectorSettings {

    public static final ResourceLocation iconGuiElements = ResourceLocation.fromNamespaceAndPath(XNet.MODID, "textures/gui/guielements.png");

    public static final String TAG_MODE = "mode";
    public static final String TAG_RATE = "rate";
    public static final String TAG_MINMAX = "minmax";
    public static final String TAG_PRIORITY = "priority";
    public static final String TAG_FILTER = "flt";
    public static final String TAG_SPEED = "speed";

    public enum FluidMode implements StringRepresentable {
        INS,
        EXT;

        public static final Codec<FluidMode> CODEC = StringRepresentable.fromEnum(FluidMode::values);
        public static final StreamCodec<FriendlyByteBuf, FluidMode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(FluidMode.class);

        @Override
        public String getSerializedName() {
            return name();
        }
    }

    private FluidMode fluidMode = FluidMode.INS;

    @Nullable private Integer priority = 0;
    @Nullable private Integer rate = null;
    @Nullable private Integer minmax = null;
    private int speed = 2;
    private ItemStack filter = ItemStack.EMPTY;

    public static final MapCodec<FluidConnectorSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Direction.CODEC.fieldOf("side").forGetter(FluidConnectorSettings::getSide),
            FluidMode.CODEC.fieldOf("mode").forGetter(FluidConnectorSettings::getFluidMode),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(o -> o.priority),
            Codec.INT.optionalFieldOf("rate").forGetter(o -> Optional.ofNullable(o.rate)),
            Codec.INT.optionalFieldOf("minmax").forGetter(o -> Optional.ofNullable(o.minmax)),
            Codec.INT.fieldOf("speed").forGetter(FluidConnectorSettings::getSpeed),
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("filter", ItemStack.EMPTY).forGetter(o -> o.filter)
    ).apply(instance, FluidConnectorSettings::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidConnectorSettings> STREAM_CODEC = CompositeStreamCodec.composite(
            Direction.STREAM_CODEC, AbstractConnectorSettings::getSide,
            FluidMode.STREAM_CODEC, FluidConnectorSettings::getFluidMode,
            ByteBufCodecs.INT, s -> s.priority,
            ByteBufCodecs.INT, s -> s.rate,
            ByteBufCodecs.INT, s -> s.minmax,
            ByteBufCodecs.INT, s -> s.speed,
            ItemStack.OPTIONAL_STREAM_CODEC, s -> s.filter,
            (side, mode, priority, rate, minmax, speed, filter) -> new FluidConnectorSettings(side, mode, priority,
                    Optional.ofNullable(rate), Optional.ofNullable(minmax), speed, filter)
    );

    public FluidConnectorSettings(@Nonnull Direction side) {
        super(side);
    }

    public FluidConnectorSettings(@NotNull Direction side, FluidMode fluidMode, Integer priority,
                                  Optional<Integer> rate, Optional<Integer> minmax, int speed, ItemStack filter) {
        super(side);
        this.fluidMode = fluidMode;
        this.priority = priority;
        this.rate = rate.orElse(null);
        this.minmax = minmax.orElse(null);
        this.speed = speed;
        this.filter = filter;
    }

    public FluidMode getFluidMode() {
        return fluidMode;
    }

    public int getSpeed() {
        return speed;
    }

    @Override
    public IChannelType getType() {
        return XNet.setup.fluidChannelType;
    }

    @Nonnull
    public Integer getPriority() {
        return priority == null ? 0 : priority;
    }

    @Nonnull
    public Integer getRate() {
        return rate == null ? Config.maxFluidRateNormal.get() : rate;
    }

    @Nullable
    public Integer getMinmax() {
        return minmax;
    }

    @Nullable
    @Override
    public IndicatorIcon getIndicatorIcon() {
        return switch (fluidMode) {
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
        String[] speeds;
        int maxrate;
        if (advanced) {
            speeds = new String[] { "10", "20", "60", "100", "200" };
            maxrate = Config.maxFluidRateAdvanced.get();
        } else {
            speeds = new String[] { "20", "60", "100", "200" };
            maxrate = Config.maxFluidRateNormal.get();
        }

        sideGui(gui);
        colorsGui(gui);
        redstoneGui(gui);
        gui.nl()
                .choices(TAG_MODE, "Insert or extract mode", fluidMode, FluidMode.values())
                .choices(TAG_SPEED, "Number of ticks for each operation", Integer.toString(speed * 10), speeds)
                .nl()

                .label("Pri").integer(TAG_PRIORITY, "Insertion priority", priority, 36).nl()

                .label("Rate")
                .integer(TAG_RATE, fluidMode == FluidMode.EXT ? "Fluid extraction rate|(max " + maxrate + "mb)" : "Fluid insertion rate|(max " + maxrate + "mb)", rate, 36, maxrate)
                .shift(10)
                .label(fluidMode == FluidMode.EXT ? "Min" : "Max")
                .integer(TAG_MINMAX, fluidMode == FluidMode.EXT ? "Keep this amount of|fluid in tank" : "Disable insertion if|fluid level is too high", minmax, 36)
                .nl()
                .label("Filter")
                .ghostSlot(TAG_FILTER, filter);
    }

    private static final Set<String> INSERT_TAGS = ImmutableSet.of(TAG_MODE, TAG_RS, TAG_COLOR+"0", TAG_COLOR+"1", TAG_COLOR+"2", TAG_COLOR+"3", TAG_RATE, TAG_MINMAX, TAG_PRIORITY, TAG_FILTER);
    private static final Set<String> EXTRACT_TAGS = ImmutableSet.of(TAG_MODE, TAG_RS, TAG_COLOR+"0", TAG_COLOR+"1", TAG_COLOR+"2", TAG_COLOR+"3", TAG_RATE, TAG_MINMAX, TAG_PRIORITY, TAG_FILTER, TAG_SPEED);

    @Override
    public boolean isEnabled(String tag) {
        if (fluidMode == FluidMode.INS) {
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

    @Nullable
    public FluidStack getMatcher() {
        // @todo optimize/cache this?
        if (!filter.isEmpty()) {
            return FluidTools.convertBucketToFluid(filter);
        } else {
            return null;
        }
    }


    @Override
    public void update(Map<String, Object> data) {
        super.update(data);
        fluidMode = FluidMode.valueOf(((String)data.get(TAG_MODE)).toUpperCase());
        rate = (Integer) data.get(TAG_RATE);
        minmax = (Integer) data.get(TAG_MINMAX);
        priority = (Integer) data.get(TAG_PRIORITY);
        speed = Integer.parseInt((String) data.get(TAG_SPEED)) / 10;
        if (speed == 0) {
            speed = 2;
        }
        filter = (ItemStack) data.get(TAG_FILTER);
        if (filter == null) {
            filter = ItemStack.EMPTY;
        }
    }

    @Override
    public JsonObject writeToJson() {
        JsonObject object = new JsonObject();
        super.writeToJsonInternal(object);
        setEnumSafe(object, "fluidmode", fluidMode);
        setIntegerSafe(object, "priority", priority);
        setIntegerSafe(object, "rate", rate);
        setIntegerSafe(object, "minmax", minmax);
        setIntegerSafe(object, "speed", speed);
        if (!filter.isEmpty()) {
            object.add("filter", JSonTools.itemStackToJson(filter));
        }
        if (rate != null && rate > Config.maxFluidRateNormal.get()) {
            object.add("advancedneeded", new JsonPrimitive(true));
        }
        if (speed == 1) {
            object.add("advancedneeded", new JsonPrimitive(true));
        }
        return object;
    }

    @Override
    public void readFromJson(JsonObject object) {
        super.readFromJsonInternal(object);
        fluidMode = getEnumSafe(object, "fluidmode", EnumStringTranslators::getFluidMode);
        priority = getIntegerSafe(object, "priority");
        rate = getIntegerSafe(object, "rate");
        minmax = getIntegerSafe(object, "minmax");
        speed = getIntegerNotNull(object, "speed");
        if (object.has("filter")) {
            filter = JSonTools.jsonToItemStack(object.get("filter").getAsJsonObject());
        } else {
            filter = ItemStack.EMPTY;
        }
    }


    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        fluidMode = FluidMode.values()[tag.getByte("fluidMode")];
        if (tag.contains("priority")) {
            priority = tag.getInt("priority");
        } else {
            priority = null;
        }
        if (tag.contains("rate")) {
            rate = tag.getInt("rate");
        } else {
            rate = null;
        }
        if (tag.contains("minmax")) {
            minmax = tag.getInt("minmax");
        } else {
            minmax = null;
        }
        speed = tag.getInt("speed");
        if (speed == 0) {
            speed = 2;
        }
        // @todo 1.21 NBT
//        if (tag.contains("filter")) {
//            CompoundTag itemTag = tag.getCompound("filter");
//            filter = ItemStack.of(itemTag);
//        } else {
            filter = ItemStack.EMPTY;
//        }
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putByte("fluidMode", (byte) fluidMode.ordinal());
        if (priority != null) {
            tag.putInt("priority", priority);
        }
        if (rate != null) {
            tag.putInt("rate", rate);
        }
        if (minmax != null) {
            tag.putInt("minmax", minmax);
        }
        tag.putInt("speed", speed);
        if (!filter.isEmpty()) {
            CompoundTag itemTag = new CompoundTag();
            // @todo 1.21 NBT
//            filter.save(itemTag);
            tag.put("filter", itemTag);
        }
    }
}
