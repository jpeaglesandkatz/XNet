package mcjty.xnet.apiimpl;

import mcjty.xnet.apiimpl.energy.EnergyConnectorSettings;
import mcjty.xnet.apiimpl.fluids.FluidChannelSettings;
import mcjty.xnet.apiimpl.fluids.FluidConnectorSettings;
import mcjty.xnet.apiimpl.items.ItemChannelSettings;
import mcjty.xnet.apiimpl.items.ItemConnectorSettings;
import mcjty.xnet.apiimpl.logic.LogicConnectorSettings;
import mcjty.xnet.apiimpl.logic.RSSensor;
import mcjty.xnet.apiimpl.logic.enums.LogicFilter;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class EnumStringTranslators {

    private static Map<String, ItemConnectorSettings.ItemMode> itemModeMap;
    private static Map<String, ItemConnectorSettings.ExtractMode> extractModeMap;
    private static Map<String, ItemConnectorSettings.StackMode> stackModeMap;
    private static Map<String, ItemChannelSettings.ChannelMode> itemChannelModeMap;
    private static Map<String, FluidChannelSettings.ChannelMode> fluidChannelModeMap;
    private static Map<String, FluidConnectorSettings.FluidMode> fluidModeMap;
    private static Map<String, EnergyConnectorSettings.EnergyMode> energyModeMap;
    private static Map<String, LogicConnectorSettings.LogicMode> logicModeMap;
    private static Map<String, RSSensor.SensorMode> sensorModeMap;
    private static Map<String, RSSensor.Operator> operatorMap;
    private static Map<String, LogicFilter> logicFilterMap;

    @Nullable
    public static RSSensor.Operator getOperator(String mode) {
        if (operatorMap == null) {
            operatorMap = new HashMap<>();
            for (RSSensor.Operator value : RSSensor.Operator.values()) {
                operatorMap.put(value.name(), value);
            }
        }
        return operatorMap.get(mode);
    }

    @Nullable
    public static LogicFilter getLogicFilter(String filter) {
        if (logicFilterMap == null) {
            logicFilterMap = new HashMap<>();
            for (LogicFilter value : LogicFilter.values()) {
                logicFilterMap.put(value.name(), value);
            }
        }
        return logicFilterMap.get(filter);
    }

    @Nullable
    public static RSSensor.SensorMode getSensorMode(String mode) {
        if (sensorModeMap == null) {
            sensorModeMap = new HashMap<>();
            for (RSSensor.SensorMode value : RSSensor.SensorMode.values()) {
                sensorModeMap.put(value.name(), value);
            }
        }
        return sensorModeMap.get(mode);
    }

    @Nullable
    public static LogicConnectorSettings.LogicMode getLogicMode(String mode) {
        if (logicModeMap == null) {
            logicModeMap = new HashMap<>();
            for (LogicConnectorSettings.LogicMode value : LogicConnectorSettings.LogicMode.values()) {
                logicModeMap.put(value.name(), value);
            }
        }
        return logicModeMap.get(mode);
    }

    @Nullable
    public static EnergyConnectorSettings.EnergyMode getEnergyMode(String mode) {
        if (energyModeMap == null) {
            energyModeMap = new HashMap<>();
            for (EnergyConnectorSettings.EnergyMode value : EnergyConnectorSettings.EnergyMode.values()) {
                energyModeMap.put(value.name(), value);
            }
        }
        return energyModeMap.get(mode);
    }

    @Nullable
    public static FluidConnectorSettings.FluidMode getFluidMode(String mode) {
        if (fluidModeMap == null) {
            fluidModeMap = new HashMap<>();
            for (FluidConnectorSettings.FluidMode value : FluidConnectorSettings.FluidMode.values()) {
                fluidModeMap.put(value.name(), value);
            }
        }
        return fluidModeMap.get(mode);
    }

    @Nullable
    public static FluidChannelSettings.ChannelMode getFluidChannelMode(String mode) {
        if (fluidChannelModeMap == null) {
            fluidChannelModeMap = new HashMap<>();
            for (FluidChannelSettings.ChannelMode value : FluidChannelSettings.ChannelMode.values()) {
                fluidChannelModeMap.put(value.name(), value);
            }
        }
        return fluidChannelModeMap.get(mode);
    }

    @Nullable
    public static ItemChannelSettings.ChannelMode getItemChannelMode(String mode) {
        if (itemChannelModeMap == null) {
            itemChannelModeMap = new HashMap<>();
            for (ItemChannelSettings.ChannelMode value : ItemChannelSettings.ChannelMode.values()) {
                itemChannelModeMap.put(value.name(), value);
            }
        }
        return itemChannelModeMap.get(mode);
    }

    @Nullable
    public static ItemConnectorSettings.ItemMode getItemMode(String mode) {
        if (itemModeMap == null) {
            itemModeMap = new HashMap<>();
            for (ItemConnectorSettings.ItemMode value : ItemConnectorSettings.ItemMode.values()) {
                itemModeMap.put(value.name(), value);
            }
        }
        return itemModeMap.get(mode);
    }

    @Nullable
    public static ItemConnectorSettings.ExtractMode getExtractMode(String mode) {
        if (extractModeMap == null) {
            extractModeMap = new HashMap<>();
            for (ItemConnectorSettings.ExtractMode value : ItemConnectorSettings.ExtractMode.values()) {
                extractModeMap.put(value.name(), value);
            }
        }
        return extractModeMap.get(mode);
    }

    @Nullable
    public static ItemConnectorSettings.StackMode getStackMode(String mode) {
        if (stackModeMap == null) {
            stackModeMap = new HashMap<>();
            for (ItemConnectorSettings.StackMode value : ItemConnectorSettings.StackMode.values()) {
                stackModeMap.put(value.name(), value);
            }
        }
        return stackModeMap.get(mode);
    }

}
