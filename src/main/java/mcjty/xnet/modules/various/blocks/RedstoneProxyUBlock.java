package mcjty.xnet.modules.various.blocks;

import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.SafeClientTools;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

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
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable IBlockReader worldIn, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn) {
        if (SafeClientTools.isSneaking()) {
            tooltip.add(new TranslationTextComponent("message.xnet.redstone_proxy_upd.header").withStyle(TextFormatting.GREEN));
            tooltip.add(new TranslationTextComponent("message.xnet.redstone_proxy_upd.gold").withStyle(TextFormatting.GOLD));
        } else {
            tooltip.add(new TranslationTextComponent("message.xnet.shiftmessage"));
        }
    }

    private Set<BlockPos> loopDetector = new HashSet<>();

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(@Nonnull BlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos, boolean isMoving) {
        if(loopDetector.add(pos)) {
            try {
                worldIn.updateNeighborsAt(pos, this);
            } finally {
                loopDetector.remove(pos);
            }
        }
    }
}
