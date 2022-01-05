package mcjty.xnet.modules.cables.blocks;

import mcjty.xnet.XNet;
import mcjty.xnet.modules.cables.CableColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.item.Item.Properties;

public class ColorBlockItem extends BlockItem {

    private final CableColor color;

    public ColorBlockItem(Block blockIn, Properties builder, CableColor color) {
        super(blockIn, builder);
        this.color = color;
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState blockstate = ((GenericCableBlock)this.getBlock()).calculateState(context.getLevel(),
                context.getClickedPos(), this.getBlock().defaultBlockState().setValue(GenericCableBlock.COLOR, color));
        if (canPlace(context, blockstate)) {
            return blockstate;
        } else {
            return null;
        }
    }

    @Override
    @Nonnull
    public String getDescriptionId() {
        // We don't want the translation key of the block
        return super.getOrCreateDescriptionId();
    }

    @Override
    public void fillItemCategory(@Nonnull CreativeModeTab group, @Nonnull NonNullList<ItemStack> items) {
        if (XNet.setup.getTab().equals(group)) {
            items.add(new ItemStack(this));
        }
    }
}
