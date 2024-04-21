package mcjty.xnet.apiimpl;

import mcjty.rftoolsbase.api.xnet.keys.SidedConsumer;
import net.minecraft.core.BlockPos;

public record ConnectedBlock<T>(SidedConsumer sidedConsumer, T settings, BlockPos connectorPos){
}
