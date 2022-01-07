package mcjty.xnet.modules.router;

import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;

public record LocalChannelId(@Nonnull BlockPos controllerPos, int index) {
}
