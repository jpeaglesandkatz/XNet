package mcjty.xnet.modules.various.blocks;

import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.SafeClientTools;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RedstoneProxyUBlock extends RedstoneProxyBlock implements ITooltipSettings {

    public RedstoneProxyUBlock() {
        super(Material.METAL);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable BlockGetter worldIn, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn) {
        if (SafeClientTools.isSneaking()) {
            tooltip.add(new TranslatableComponent("message.xnet.redstone_proxy_upd.header").withStyle(ChatFormatting.GREEN));
            tooltip.add(new TranslatableComponent("message.xnet.redstone_proxy_upd.gold").withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add(new TranslatableComponent("message.xnet.shiftmessage"));
        }
    }

    private final Set<BlockPos> loopDetector = new HashSet<>();

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(@Nonnull BlockState state, @Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos, boolean isMoving) {
        if(loopDetector.add(pos)) {
            try {
                worldIn.updateNeighborsAt(pos, this);
            } finally {
                loopDetector.remove(pos);
            }
        }
    }
}
