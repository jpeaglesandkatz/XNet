package mcjty.xnet.modules.cables.blocks;

import mcjty.xnet.modules.cables.CableColor;
import mcjty.xnet.modules.cables.CableSetup;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.Nullable;

public class ColorBlockItem extends BlockItem {

    private final CableColor color;

    public ColorBlockItem(Block blockIn, Properties builder, CableColor color) {
        super(blockIn, builder);
        this.color = color;
    }

    @Nullable
    @Override
    protected BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState blockstate = ((GenericCableBlock)this.getBlock()).calculateState(context.getWorld(),
                context.getPos(), this.getBlock().getDefaultState().with(GenericCableBlock.COLOR, color));
        if (canPlace(context, blockstate)) {
            return blockstate;
        } else {
            return null;
        }
    }

    @Override
    public String getTranslationKey() {
        // We don't want the translation key of the block
        return super.getDefaultTranslationKey();
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        items.add(new ItemStack(CableSetup.NETCABLE_BLUE));
        items.add(new ItemStack(CableSetup.NETCABLE_RED));
        items.add(new ItemStack(CableSetup.NETCABLE_YELLOW));
        items.add(new ItemStack(CableSetup.NETCABLE_GREEN));
        items.add(new ItemStack(CableSetup.NETCABLE_ROUTING));
        items.add(new ItemStack(CableSetup.CONNECTOR_BLUE));
        items.add(new ItemStack(CableSetup.CONNECTOR_RED));
        items.add(new ItemStack(CableSetup.CONNECTOR_YELLOW));
        items.add(new ItemStack(CableSetup.CONNECTOR_GREEN));
        items.add(new ItemStack(CableSetup.CONNECTOR_ROUTING));
        items.add(new ItemStack(CableSetup.ADVANCED_CONNECTOR_BLUE));
        items.add(new ItemStack(CableSetup.ADVANCED_CONNECTOR_RED));
        items.add(new ItemStack(CableSetup.ADVANCED_CONNECTOR_YELLOW));
        items.add(new ItemStack(CableSetup.ADVANCED_CONNECTOR_GREEN));
        items.add(new ItemStack(CableSetup.ADVANCED_CONNECTOR_ROUTING));
    }
}
