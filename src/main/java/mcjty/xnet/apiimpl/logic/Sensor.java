package mcjty.xnet.apiimpl.logic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.FluidTools;
import mcjty.rftoolsbase.api.xnet.channels.Color;
import mcjty.rftoolsbase.api.xnet.gui.IEditorGui;
import mcjty.xnet.apiimpl.energy.EnergyChannelSettings;
import mcjty.xnet.apiimpl.fluids.FluidChannelSettings;
import mcjty.xnet.apiimpl.items.ItemChannelSettings;
import mcjty.xnet.compat.RFToolsSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;

import static mcjty.rftoolsbase.api.xnet.channels.Color.COLORS;
import static mcjty.rftoolsbase.api.xnet.channels.Color.OFF;

public class Sensor {

    public static final String TAG_MODE = "mode";
    public static final String TAG_OPERATOR = "op";
    public static final String TAG_AMOUNT = "amount";
    public static final String TAG_COLOR = "scolor";
    public static final String TAG_STACK = "stack";

    public enum SensorMode implements StringRepresentable {
        OFF,
        ITEM,
        FLUID,
        ENERGY,
        RS;

        public static final Codec<SensorMode> CODEC = StringRepresentable.fromEnum(SensorMode::values);
        public static final StreamCodec<FriendlyByteBuf, SensorMode> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(SensorMode.class);

        @Override
        public String getSerializedName() {
            return name();
        }
    }

    public enum Operator implements StringRepresentable{
        EQUAL("=", Integer::equals),
        NOTEQUAL("!=", (i1, i2) -> !i1.equals(i2)),
        LESS("<", (i1, i2) -> i1 < i2),
        GREATER(">", (i1, i2) -> i1 > i2),
        LESSOREQUAL("<=", (i1, i2) -> i1 <= i2),
        GREATOROREQUAL(">=", (i1, i2) -> i1 >= i2);

        public static final Codec<Operator> CODEC = StringRepresentable.fromEnum(Operator::values);
        public static final StreamCodec<FriendlyByteBuf, Operator> STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(Operator.class);

        private final String code;
        private final BiPredicate<Integer, Integer> matcher;

        private static final Map<String, Operator> OPERATOR_MAP = new HashMap<>();

        static {
            for (Operator operator : values()) {
                OPERATOR_MAP.put(operator.code, operator);
            }
        }

        Operator(String code, BiPredicate<Integer, Integer> matcher) {
            this.code = code;
            this.matcher = matcher;
        }

        public String getCode() {
            return code;
        }

        public boolean match(int i1, int i2) {
            return matcher.test(i1, i2);
        }

        @Override
        public String toString() {
            return code;
        }

        public static Operator valueOfCode(String code) {
            return OPERATOR_MAP.get(code);
        }


        @Override
        public String getSerializedName() {
            return name();
        }
    }

    private final int index;

    private SensorMode sensorMode = SensorMode.OFF;
    private Operator operator = Operator.EQUAL;
    private int amount = 0;
    private Color outputColor = OFF;
    private ItemStack filter = ItemStack.EMPTY;

    public static final Codec<Sensor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("index").forGetter(s -> s.index),
            SensorMode.CODEC.fieldOf("sensorMode").forGetter(Sensor::getSensorMode),
            Operator.CODEC.fieldOf("operator").forGetter(Sensor::getOperator),
            Codec.INT.fieldOf("amount").forGetter(Sensor::getAmount),
            Color.CODEC.fieldOf("outputColor").forGetter(Sensor::getOutputColor),
            ItemStack.OPTIONAL_CODEC.fieldOf("filter").forGetter(Sensor::getFilter)
    ).apply(instance, Sensor::new));

    public final static StreamCodec<RegistryFriendlyByteBuf, Sensor> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, s -> s.index,
            SensorMode.STREAM_CODEC, Sensor::getSensorMode,
            Operator.STREAM_CODEC, Sensor::getOperator,
            ByteBufCodecs.INT, Sensor::getAmount,
            Color.STREAM_CODEC, Sensor::getOutputColor,
            ItemStack.OPTIONAL_STREAM_CODEC, Sensor::getFilter,
            Sensor::new
    );

    public Sensor(int index, SensorMode sensorMode, Operator operator, int amount, Color outputColor, ItemStack filter) {
        this.index = index;
        this.sensorMode = sensorMode;
        this.operator = operator;
        this.amount = amount;
        this.outputColor = outputColor;
        this.filter = filter;
    }

    public Sensor(int index) {
        this.index = index;
    }

    public SensorMode getSensorMode() {
        return sensorMode;
    }

    public void setSensorMode(SensorMode sensorMode) {
        this.sensorMode = sensorMode;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public ItemStack getFilter() {
        return filter;
    }

    public void setFilter(ItemStack filter) {
        this.filter = filter;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Color getOutputColor() {
        return outputColor;
    }

    public void setOutputColor(Color outputColor) {
        this.outputColor = outputColor;
    }

    public boolean isEnabled(String tag) {
        if ((TAG_MODE + index).equals(tag)) {
            return true;
        }
        if ((TAG_OPERATOR + index).equals(tag)) {
            return true;
        }
        if ((TAG_AMOUNT + index).equals(tag)) {
            return true;
        }
        if ((TAG_COLOR + index).equals(tag)) {
            return true;
        }
        if ((TAG_STACK + index).equals(tag)) {
            return sensorMode == SensorMode.FLUID || sensorMode == SensorMode.ITEM;
        }
        return false;
    }

    public void createGui(IEditorGui gui) {
        gui
                .choices(TAG_MODE + index, "Sensor mode", sensorMode, SensorMode.values())
                .choices(TAG_OPERATOR + index, "Operator", operator, Operator.values())
                .integer(TAG_AMOUNT + index, "Amount to compare with", amount, 46)
                .colors(TAG_COLOR + index, "Output color", outputColor.getColor(), COLORS)
                .ghostSlot(TAG_STACK + index, filter)
                .nl();
    }

    public boolean test(@Nullable BlockEntity te, @Nonnull Level world, @Nonnull BlockPos pos, LogicConnectorSettings settings) {
        switch (sensorMode) {
            case ITEM -> {
                if (RFToolsSupport.isStorageScanner(te)) {
                    int cnt = RFToolsSupport.countItems(te, filter, amount + 1);
                    return operator.match(cnt, amount);
                } else {
                    IItemHandler handler = ItemChannelSettings.getItemHandlerAt(te, settings.getFacing());
                    if (handler == null) {
                        return false;
                    }
                    int cnt = countItem(handler, filter, amount + 1);
                    return operator.match(cnt, amount);
                }
            }
            case FLUID -> {
                IFluidHandler fluidHandler = FluidChannelSettings.getFluidHandlerAt(te, settings.getFacing());
                if (fluidHandler == null) {
                    return false;
                } else {
                    int cnt = countFluid(fluidHandler, filter, amount + 1);
                    return operator.match(cnt, amount);
                }
            }
            case ENERGY -> {
                if (EnergyChannelSettings.isEnergyTE(te, settings.getFacing())) {
                    int cnt = EnergyChannelSettings.getEnergyLevel(te, settings.getFacing());
                    return operator.match(cnt, amount);
                }
            }
            case RS -> {
                int cnt = world.getSignal(pos, settings.getFacing());
                return operator.match(cnt, amount);
            }
            case OFF -> {
            }
        }

        return false;
    }

    private int safeInt(Object o) {
        if (o instanceof Integer) {
            return (Integer) o;
        } else {
            return 0;
        }
    }

    public void update(Map<String, Object> data) {
        Object sm = data.get(TAG_MODE + index);
        if (sm != null) {
            sensorMode = SensorMode.valueOf(((String) sm).toUpperCase());
        } else {
            sensorMode = SensorMode.OFF;
        }
        Object op = data.get(TAG_OPERATOR + index);
        if (op != null) {
            operator = Operator.valueOfCode(((String) op).toUpperCase());
        } else {
            operator = Operator.EQUAL;
        }
        amount = safeInt(data.get(TAG_AMOUNT + index));
        Object co = data.get(TAG_COLOR + index);
        if (co != null) {
            outputColor = Color.colorByValue((Integer) co);
        } else {
            outputColor = OFF;
        }
        filter = (ItemStack) data.get(TAG_STACK + index);
        if (filter == null) {
            filter = ItemStack.EMPTY;
        }
    }

    // Count items. We will stop early if we have enough to satisfy the sensor
    private int countItem(@Nonnull IItemHandler handler, ItemStack matcher, int maxNeeded) {
        int cnt = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                if (!matcher.isEmpty()) {
                    // @todo 1.14 oredict?
                    if (ItemStack.isSameItem(matcher, stack)) {
                        cnt += stack.getCount();
                        if (cnt >= maxNeeded) {
                            return cnt;
                        }
                    }
                } else {
                    cnt += stack.getCount();
                    if (cnt >= maxNeeded) {
                        return cnt;
                    }
                }
            }
        }
        return cnt;
    }

    private int countFluid(@Nonnull IFluidHandler handler, ItemStack matcher, int maxNeeded) {
        FluidStack fluidStack;
        if (!matcher.isEmpty()) {
            fluidStack = FluidTools.convertBucketToFluid(matcher);
        } else {
            fluidStack = null;
        }
        int cnt = 0;
        for (int i = 0 ; i < handler.getTanks() ; i++) {
            FluidStack contents = handler.getFluidInTank(i);
            if (!contents.isEmpty()) {
                if (fluidStack != null) {
                    if (FluidStack.isSameFluidSameComponents(fluidStack, contents)) {
                        cnt += contents.getAmount();
                        if (cnt >= maxNeeded) {
                            return cnt;
                        }
                    }
                } else {
                    cnt += contents.getAmount();
                    if (cnt >= maxNeeded) {
                        return cnt;
                    }
                }
            }
        }
        return cnt;
    }
}
