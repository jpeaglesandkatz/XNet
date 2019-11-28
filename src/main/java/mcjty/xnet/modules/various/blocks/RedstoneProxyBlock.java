package mcjty.xnet.modules.various.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.List;

public class RedstoneProxyBlock extends Block {

    public RedstoneProxyBlock() {
        this(Material.IRON);
        setRegistryName("redstone_proxy");
    }

    public RedstoneProxyBlock(Material materialIn) {
        super(Properties.create(materialIn)
                .hardnessAndResistance(2.0f)
                .harvestLevel(0)
                .harvestTool(ToolType.PICKAXE)
                .sound(SoundType.METAL)
        );
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new StringTextComponent("Acts as a proxy block for"));
        tooltip.add(new StringTextComponent("redstone. XNet can connect to this"));
        tooltip.add(new StringTextComponent(TextFormatting.YELLOW + "This version does no block update!"));
    }

}
