package mcjty.xnet.apiimpl.logic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mcjty.rftoolsbase.api.xnet.channels.Color;
import mcjty.xnet.apiimpl.logic.enums.LogicFilter;
import mcjty.xnet.logic.LogicTools;
import mcjty.xnet.modules.controller.client.ConnectorEditorPanel;
import net.minecraft.nbt.CompoundTag;

import java.util.Map;

import static mcjty.rftoolsbase.api.xnet.channels.Color.COLORS;
import static mcjty.rftoolsbase.api.xnet.channels.Color.OFF;
import static mcjty.xnet.apiimpl.logic.LogicConnectorSettings.TAG_REDSTONE_OUT;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class RSOutput {

	public static final String TAG_RS_FILTER = "RSFilter";
	public static final String TAG_RS_CHANNEL_1 = "RSChannel1";
	public static final String TAG_RS_CHANNEL_2 = "RSChannel2";
	public static final String TAG_COUNTER = "RSCounter";
	public static final String TAG_TIMER = "RSTimer";

	private boolean isAdvanced;
	private LogicFilter logicFilter = LogicFilter.OFF;
	private Color inputChannel1 = OFF;    // First input channel for logic filter
	private Color inputChannel2 = OFF;    // Second input channel for logic filter
	private int redstoneOut = 0;    // Redstone output value
	private boolean flipFlapState = false;  // If logicFilter == LATCH shows should we output redstone signal
	private boolean lastInputTrue = false;  // If logicFilter == LATCH shows should we toggle flipFlapState

	private int countingHolder = 0;  // Holds user value for counting filter
	private int countingCurrent = 0; // Current value for counting filter

	private int ticksHolder = 5;  // Holds user value for timer filter
	private int ticksCurrent = 5; // Current value for timer filter


	public RSOutput(boolean isAdvanced) {
		this.isAdvanced = isAdvanced;
	}
	public void setCountingHolder(int countingHolder) {
		this.countingHolder = countingHolder;
		this.countingCurrent = countingHolder;
	}

	public void setTicksHolder(int ticksHolder) {
		this.ticksHolder = ticksHolder;
		this.ticksCurrent = ticksHolder;
	}

	public void createGui(ConnectorEditorPanel gui) {
		if (gui.isAdvanced()) {
			gui.choices(TAG_RS_FILTER, "Apply logic filter", logicFilter, LogicFilter.values());
			switch (logicFilter) {
				case OFF -> {}
				case COUNTER -> {
					gui.colors(TAG_RS_CHANNEL_1, "Input RS channel", inputChannel1.getColor(), COLORS);
					gui.integer(TAG_COUNTER, "Count inputs before output impulse", countingHolder, 50, Integer.MAX_VALUE, 0);
				}
				case LATCH, NOT -> {
					gui.colors(TAG_RS_CHANNEL_1, "Input RS channel", inputChannel1.getColor(), COLORS);
				}
				case TIMER -> {
					gui.integer(TAG_TIMER, "Count ticks before output impulse", ticksHolder, 50, Integer.MAX_VALUE, 5);
				}
				case OR, NOR, NAND, XOR, XNOR, AND -> {
					gui.colors(TAG_RS_CHANNEL_1, "Input RS channel 1", inputChannel1.getColor(), COLORS);
					gui.colors(TAG_RS_CHANNEL_2, "Input RS channel 2", inputChannel2.getColor(), COLORS);
				}
			}
		} else {
			gui.label("Redstone:");
		}

		gui.integer(TAG_REDSTONE_OUT, "Redstone output value", redstoneOut, 30, 15, 0)
				.nl();
	}

	public void update(Map<String, Object> data) {
		logicFilter = LogicTools.safeLogicFilter(data.get(TAG_RS_FILTER));
		inputChannel1 = LogicTools.safeColor(data.get(TAG_RS_CHANNEL_1));
		inputChannel2 = LogicTools.safeColor(data.get(TAG_RS_CHANNEL_2));
		countingHolder = LogicTools.safeIntOrValue(data.get(TAG_COUNTER), countingHolder);
		ticksHolder = LogicTools.safeIntOrValue(data.get(TAG_TIMER), ticksHolder);
		redstoneOut = LogicTools.safeIntOrValue(data.get(TAG_REDSTONE_OUT), redstoneOut);
	}

	public boolean isEnabled(String tag) {
		switch (tag) {
			case TAG_RS_FILTER, TAG_REDSTONE_OUT, TAG_RS_CHANNEL_1, TAG_RS_CHANNEL_2, TAG_COUNTER, TAG_TIMER -> {
				return true;
			}
			default -> {return false;}
		}
	}

	public void readFromNBT(CompoundTag tag) {
		logicFilter = LogicFilter.values()[tag.getByte("logicFilter")];
		inputChannel1 = Color.values()[tag.getByte("inputChannel1")];
		inputChannel2 = Color.values()[tag.getByte("inputChannel2")];
		setCountingHolder(tag.getInt("countingHolder"));
		setTicksHolder(tag.getInt("ticksHolder"));
		redstoneOut = tag.getInt("redstoneOutput");
	}

	public void writeToNBT(CompoundTag tag) {
		tag.putByte("logicFilter", (byte) logicFilter.ordinal());
		tag.putByte("inputChannel1", (byte) inputChannel1.ordinal());
		tag.putByte("inputChannel2", (byte) inputChannel2.ordinal());
		tag.putInt("countingHolder", countingHolder);
		tag.putInt("ticksHolder", ticksHolder);
		tag.putInt("redstoneOutput", redstoneOut);
	}
}
