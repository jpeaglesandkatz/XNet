package mcjty.xnet.modules.various.blocks;

import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.lib.varia.SafeClientTools;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class RedstoneProxyBlock extends Block implements ITooltipSettings {

    public RedstoneProxyBlock() {
        this(Material.METAL);
    }

    public RedstoneProxyBlock(Material materialIn) {
        super(Properties.of(materialIn)
                .strength(2.0f)
                .sound(SoundType.METAL)
        );
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable BlockGetter worldIn, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn) {
        if (SafeClientTools.isSneaking()) {
            tooltip.add(new TranslatableComponent("message.xnet.redstone_proxy.header").withStyle(ChatFormatting.GREEN));
            tooltip.add(new TranslatableComponent("message.xnet.redstone_proxy.gold").withStyle(ChatFormatting.GOLD));
        } else {
            tooltip.add(new TranslatableComponent("message.xnet.shiftmessage"));
        }
    }

}
