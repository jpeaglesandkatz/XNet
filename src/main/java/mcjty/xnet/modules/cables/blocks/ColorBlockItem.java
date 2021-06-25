package mcjty.xnet.modules.cables.blocks;

import mcjty.xnet.XNet;
import mcjty.xnet.modules.cables.CableColor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.Nullable;

import net.minecraft.item.Item.Properties;

public class ColorBlockItem extends BlockItem {

    private final CableColor color;

    public ColorBlockItem(Block blockIn, Properties builder, CableColor color) {
        super(blockIn, builder);
        this.color = color;
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockItemUseContext context) {
        BlockState blockstate = ((GenericCableBlock)this.getBlock()).calculateState(context.getLevel(),
                context.getClickedPos(), this.getBlock().defaultBlockState().setValue(GenericCableBlock.COLOR, color));
        if (canPlace(context, blockstate)) {
            return blockstate;
        } else {
            return null;
        }
    }

    @Override
    public String getDescriptionId() {
        // We don't want the translation key of the block
        return super.getOrCreateDescriptionId();
    }

    @Override
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
        if (XNet.setup.getTab().equals(group)) {
            items.add(new ItemStack(this));
        }
    }
}
