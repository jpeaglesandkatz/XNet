package mcjty.xnet.modules.various.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RedstoneProxyUBlock extends RedstoneProxyBlock {

    public RedstoneProxyUBlock() {
        super(Material.IRON);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new StringTextComponent("Acts as a proxy block for"));
        tooltip.add(new StringTextComponent("redstone. XNet can connect to this"));
        tooltip.add(new StringTextComponent(TextFormatting.YELLOW + "This version does a block update!"));
    }

    private Set<BlockPos> loopDetector = new HashSet<>();

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if(loopDetector.add(pos)) {
            try {
                worldIn.notifyNeighborsOfStateChange(pos, this);
            } finally {
                loopDetector.remove(pos);
            }
        }
    }
}
