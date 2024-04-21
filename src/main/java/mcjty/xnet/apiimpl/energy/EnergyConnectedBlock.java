package mcjty.xnet.apiimpl.energy;

import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import net.minecraft.core.BlockPos;

public record EnergyConnectedBlock (SidedConsumer sidedConsumer, EnergyConnectorSettings settings, BlockPos connectorPos, int rate) {
}
