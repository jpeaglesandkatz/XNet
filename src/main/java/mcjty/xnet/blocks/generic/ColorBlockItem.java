package mcjty.xnet.blocks.generic;

import mcjty.xnet.blocks.cables.NetCableSetup;
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
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        items.add(new ItemStack(NetCableSetup.NETCABLE_BLUE));
        items.add(new ItemStack(NetCableSetup.NETCABLE_RED));
        items.add(new ItemStack(NetCableSetup.NETCABLE_YELLOW));
        items.add(new ItemStack(NetCableSetup.NETCABLE_GREEN));
        items.add(new ItemStack(NetCableSetup.NETCABLE_ROUTING));
        items.add(new ItemStack(NetCableSetup.CONNECTOR_BLUE));
        items.add(new ItemStack(NetCableSetup.CONNECTOR_RED));
        items.add(new ItemStack(NetCableSetup.CONNECTOR_YELLOW));
        items.add(new ItemStack(NetCableSetup.CONNECTOR_GREEN));
        items.add(new ItemStack(NetCableSetup.CONNECTOR_ROUTING));
        items.add(new ItemStack(NetCableSetup.ADVANCED_CONNECTOR_BLUE));
        items.add(new ItemStack(NetCableSetup.ADVANCED_CONNECTOR_RED));
        items.add(new ItemStack(NetCableSetup.ADVANCED_CONNECTOR_YELLOW));
        items.add(new ItemStack(NetCableSetup.ADVANCED_CONNECTOR_GREEN));
        items.add(new ItemStack(NetCableSetup.ADVANCED_CONNECTOR_ROUTING));
    }
}
