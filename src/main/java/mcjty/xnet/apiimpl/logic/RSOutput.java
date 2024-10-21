package mcjty.xnet.apiimpl.logic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mcjty.lib.varia.CompositeStreamCodec;
import mcjty.rftoolsbase.api.xnet.channels.Color;
import mcjty.rftoolsbase.api.xnet.gui.IEditorGui;
import mcjty.xnet.apiimpl.logic.enums.LogicFilter;
import mcjty.xnet.utils.CastTools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Map;

import static mcjty.rftoolsbase.api.xnet.channels.Color.COLORS;
import static mcjty.rftoolsbase.api.xnet.channels.Color.OFF;
import static mcjty.xnet.apiimpl.Constants.*;
import static mcjty.xnet.utils.I18nConstants.*;

public class RSOutput {
    
    private boolean isAdvanced;
    private LogicFilter logicFilter = LogicFilter.DIRECT;
    private Color inputChannel1 = OFF;    // First input channel for logic filter
    private Color inputChannel2 = OFF;    // Second input channel for logic filter
    private int redstoneOut = 0;    // Redstone output value
    private boolean flipFlapState = false;  // If logicFilter == LATCH shows should we output redstone signal
    private boolean lastInputTrue = false;  // If logicFilter == LATCH shows should we toggle flipFlapState

    private boolean impulse = false; // Impulse output, available for some LogicFilter values
    private int impulseDuration = 1; // Impulse duration in ticks
    private int impulseRemaining = 0; // Remaining impulse duration in current context

    private int countingHolder = 0;  // Holds user value for counting filter
    private int countingCurrent = 0; // Current value for counting filter

    private int ticksHolder = 5;  // Holds user value for timer filter
    private int ticksCurrent = 5; // Current value for timer filter

    public static final Codec<RSOutput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("advanced", false).forGetter(RSOutput::isAdvanced),
            LogicFilter.CODEC.fieldOf("filter").forGetter(RSOutput::getLogicFilter),
            Color.CODEC.fieldOf("channel1").forGetter(RSOutput::getInputChannel1),
            Color.CODEC.fieldOf("channel2").forGetter(RSOutput::getInputChannel2),
            Codec.INT.fieldOf("redstoneOut").forGetter(RSOutput::getRedstoneOut),
            Codec.INT.fieldOf("countingHolder").forGetter(RSOutput::getCountingHolder),
            Codec.INT.fieldOf("ticksHolder").forGetter(RSOutput::getTicksHolder),
            Codec.BOOL.fieldOf("flipFlapState").forGetter(RSOutput::isFlipFlapState),
            Codec.BOOL.fieldOf("lastInputTrue").forGetter(RSOutput::isLastInputTrue),
            Codec.INT.fieldOf("countingCurrent").forGetter(RSOutput::getCountingCurrent),   // 1.21 runtime
            Codec.INT.fieldOf("ticksCurrent").forGetter(RSOutput::getTicksCurrent), // 1.21 runtime
            Codec.BOOL.fieldOf("impulse").forGetter(RSOutput::isImpulse),
            Codec.INT.fieldOf("impulseDuration").forGetter(RSOutput::getImpulseDuration),
            Codec.INT.fieldOf("impulseRemaining").forGetter(RSOutput::getImpulseRemaining)  // 1.21 runtime
    ).apply(instance, RSOutput::new));

    public static final StreamCodec<FriendlyByteBuf, RSOutput> STREAM_CODEC = CompositeStreamCodec.composite(
            ByteBufCodecs.BOOL, RSOutput::isAdvanced,
            LogicFilter.STREAM_CODEC, RSOutput::getLogicFilter,
            Color.STREAM_CODEC, RSOutput::getInputChannel1,
            Color.STREAM_CODEC, RSOutput::getInputChannel2,
            ByteBufCodecs.INT, RSOutput::getRedstoneOut,
            ByteBufCodecs.INT, RSOutput::getCountingHolder,
            ByteBufCodecs.INT, RSOutput::getTicksHolder,
            ByteBufCodecs.BOOL, RSOutput::isFlipFlapState,
            ByteBufCodecs.BOOL, RSOutput::isLastInputTrue,
            ByteBufCodecs.INT, RSOutput::getCountingCurrent,  // 1.21 runtime
            ByteBufCodecs.INT, RSOutput::getTicksCurrent, // 1.21 runtime
            ByteBufCodecs.BOOL, RSOutput::isImpulse,
            ByteBufCodecs.INT, RSOutput::getImpulseDuration,
            ByteBufCodecs.INT, RSOutput::getImpulseRemaining,  // 1.21 runtime
            RSOutput::new
    );

    public RSOutput(boolean isAdvanced, LogicFilter logicFilter, Color inputChannel1, Color inputChannel2, int redstoneOut, int countingHolder, int ticksHolder, boolean flipFlapState, boolean lastInputTrue, int countingCurrent, int ticksCurrent, boolean impulse, int impulseDuration, int impulseRemaining) {
        this.isAdvanced = isAdvanced;
        this.logicFilter = logicFilter;
        this.inputChannel1 = inputChannel1;
        this.inputChannel2 = inputChannel2;
        this.redstoneOut = redstoneOut;
        this.countingHolder = countingHolder;
        this.ticksHolder = ticksHolder;
        this.flipFlapState = flipFlapState;
        this.lastInputTrue = lastInputTrue;
        this.countingCurrent = countingCurrent;
        this.ticksCurrent = ticksCurrent;
        this.impulse = impulse;
        this.impulseDuration = impulseDuration;
        this.impulseRemaining = impulseRemaining;
    }

    public RSOutput(boolean isAdvanced) {
        this.isAdvanced = isAdvanced;
    }

    public LogicFilter getLogicFilter() {
        return logicFilter;
    }

    public Color getInputChannel1() {
        return inputChannel1;
    }

    public Color getInputChannel2() {
        return inputChannel2;
    }

    public int getRedstoneOut() {
        return redstoneOut;
    }

    public int getCountingHolder() {
        return countingHolder;
    }

    public int getTicksHolder() {
        return ticksHolder;
    }

    public boolean isAdvanced() {
        return isAdvanced;
    }

    public boolean isFlipFlapState() {
        return flipFlapState;
    }

    public boolean isLastInputTrue() {
        return lastInputTrue;
    }

    public int getCountingCurrent() {
        return countingCurrent;
    }

    public int getTicksCurrent() {
        return ticksCurrent;
    }

    protected void setAdvanced(boolean advanced) {
        isAdvanced = advanced;
    }

    public void setFlipFlapState(boolean flipFlapState) {
        this.flipFlapState = flipFlapState;
    }

    public void setLastInputTrue(boolean lastInputTrue) {
        this.lastInputTrue = lastInputTrue;
    }

    public void setCountingCurrent(int countingCurrent) {
        this.countingCurrent = countingCurrent;
    }

    public void setTicksCurrent(int ticksCurrent) {
        this.ticksCurrent = ticksCurrent;
    }

    public void setCountingHolder(int countingHolder) {
        this.countingHolder = countingHolder;
        this.countingCurrent = countingHolder;
    }

    public void setTicksHolder(int ticksHolder) {
        this.ticksHolder = ticksHolder;
        this.ticksCurrent = ticksHolder;
    }

    public void setLogicFilter(LogicFilter logicFilter) {
        this.logicFilter = logicFilter;
    }

    public void setInputChannel1(Color inputChannel1) {
        this.inputChannel1 = inputChannel1;
    }

    public void setInputChannel2(Color inputChannel2) {
        this.inputChannel2 = inputChannel2;
    }

    public void setRedstoneOut(int redstoneOut) {
        this.redstoneOut = redstoneOut;
    }

    public boolean isImpulse() {
        return impulse;
    }

    public void setImpulse(boolean impulse) {
        this.impulse = impulse;
    }

    public int getImpulseDuration() {
        return impulseDuration;
    }

    public void setImpulseDuration(int impulseDuration) {
        this.impulseDuration = impulseDuration;
    }

    public int getImpulseRemaining() {
        return impulseRemaining;
    }

    public boolean decreaseImpulseRemaining() {
        if (impulseRemaining > 0) {
            impulseRemaining--;
            return true;
        }
        return false;
    }

    public void setAndDecreaseImpulseRemaining() {
        impulseRemaining = impulseDuration;
        impulseRemaining--;
        lastInputTrue = true;
    }

    public void setImpulseRemaining(int impulseRemaining) {
        this.impulseRemaining = impulseRemaining;
    }

    public void createGui(IEditorGui gui) {
        if (gui.isAdvanced()) {
            gui.translatableChoices(TAG_RS_FILTER, logicFilter, LogicFilter.values());
            logicFilter.createGui(this, gui);
        } else {
            gui.label(LOGIC_RS_LABEL.i18n());
            gui.colors(TAG_RS_CHANNEL_1, LOGIC_INPUT_CHANNEL_TOOLTIP.i18n(), inputChannel1.getColor(), COLORS);
            gui.integer(TAG_REDSTONE_OUT, LOGIC_RS_TOOLTIP.i18n(), redstoneOut, 30, 15, 0).nl();
        }
    }

    public void update(Map<String, Object> data) {
        logicFilter = CastTools.safeLogicFilter(data.get(TAG_RS_FILTER));
        inputChannel1 = CastTools.safeColor(data.get(TAG_RS_CHANNEL_1));
        inputChannel2 = CastTools.safeColor(data.get(TAG_RS_CHANNEL_2));
        countingHolder = CastTools.safeIntOrValue(data.get(TAG_RS_COUNTER), countingHolder);
        ticksHolder = CastTools.safeIntOrValue(data.get(TAG_RS_TIMER), ticksHolder);
        redstoneOut = CastTools.safeIntOrValue(data.get(TAG_REDSTONE_OUT), redstoneOut);
        impulse = CastTools.safeBoolean(data.get(TAG_IMPULSE));
        impulseDuration = CastTools.safeIntOrValue(data.get(TAG_IMPULSE_DUR), 1);
        impulseRemaining = CastTools.safeInt(data.get(TAG_IMPULSE_REM));
    }

    public boolean isEnabled(String tag) {
        switch (tag) {
            case TAG_RS_FILTER, TAG_REDSTONE_OUT, TAG_RS_CHANNEL_1, TAG_RS_CHANNEL_2, TAG_RS_COUNTER, TAG_RS_TIMER, TAG_IMPULSE -> {
                return true;
            }
            case TAG_IMPULSE_DUR -> {
                return impulse;
            }
            default -> {return false;}
        }
    }

    public void readFromNBT(CompoundTag tag) {
        logicFilter = LogicFilter.values()[tag.getByte(TAG_RS_FILTER)];
        inputChannel1 = Color.values()[tag.getByte(TAG_RS_CHANNEL_1)];
        inputChannel2 = Color.values()[tag.getByte(TAG_RS_CHANNEL_2)];
        setCountingHolder(tag.getInt(TAG_RS_COUNTING_HOLDER));
        setTicksHolder(tag.getInt(TAG_RS_TICKS_HOLDER));
        redstoneOut = tag.getInt(TAG_REDSTONE_OUT);
        impulse = tag.getBoolean(TAG_IMPULSE);
        impulseDuration = tag.getInt(TAG_IMPULSE_DUR);
        impulseRemaining = tag.getInt(TAG_IMPULSE_REM);
    }

    public void writeToNBT(CompoundTag tag) {
        tag.putByte(TAG_RS_FILTER, (byte) logicFilter.ordinal());
        tag.putByte(TAG_RS_CHANNEL_1, (byte) inputChannel1.ordinal());
        tag.putByte(TAG_RS_CHANNEL_2, (byte) inputChannel2.ordinal());
        tag.putInt(TAG_RS_COUNTING_HOLDER, countingHolder);
        tag.putInt(TAG_RS_TICKS_HOLDER, ticksHolder);
        tag.putInt(TAG_REDSTONE_OUT, redstoneOut);
        tag.putBoolean(TAG_IMPULSE, impulse);
        tag.putInt(TAG_IMPULSE_DUR, impulseDuration);
        tag.putInt(TAG_IMPULSE_REM, impulseRemaining);
    }
}
