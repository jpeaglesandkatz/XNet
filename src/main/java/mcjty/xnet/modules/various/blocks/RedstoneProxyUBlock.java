package mcjty.xnet.modules.various.blocks;

import mcjty.lib.McJtyLib;
import mcjty.lib.tooltips.ITooltipSettings;
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

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RedstoneProxyUBlock extends RedstoneProxyBlock implements ITooltipSettings {

    public RedstoneProxyUBlock() {
        super(Material.METAL);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (McJtyLib.proxy.isSneaking()) {
            tooltip.add(new TranslationTextComponent("message.xnet.redstone_proxy_upd.header").withStyle(TextFormatting.GREEN));
            tooltip.add(new TranslationTextComponent("message.xnet.redstone_proxy_upd.gold").withStyle(TextFormatting.GOLD));
        } else {
            tooltip.add(new TranslationTextComponent("message.xnet.shiftmessage"));
        }
    }

    private Set<BlockPos> loopDetector = new HashSet<>();

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if(loopDetector.add(pos)) {
            try {
                worldIn.updateNeighborsAt(pos, this);
            } finally {
                loopDetector.remove(pos);
            }
        }
    }
}
