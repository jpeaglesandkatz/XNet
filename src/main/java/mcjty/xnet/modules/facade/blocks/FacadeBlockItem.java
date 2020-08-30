package mcjty.xnet.modules.facade.blocks;

import mcjty.lib.builder.TooltipBuilder;
import mcjty.lib.tooltips.ITooltipSettings;
import mcjty.xnet.XNet;
import mcjty.xnet.modules.cables.CableModule;
import mcjty.xnet.modules.cables.blocks.ConnectorTileEntity;
import mcjty.xnet.modules.facade.FacadeModule;
import mcjty.xnet.modules.facade.IFacadeSupport;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static mcjty.lib.builder.TooltipBuilder.*;
import static mcjty.xnet.modules.cables.blocks.GenericCableBlock.*;

public class FacadeBlockItem extends BlockItem implements ITooltipSettings {

    private TooltipBuilder tooltipBuilder = new TooltipBuilder()
            .info(header(),
                    gold(stack -> !isMimicking(stack)),
                    parameter("info", FacadeBlockItem::isMimicking, FacadeBlockItem::getMimickingString));

    private static boolean isMimicking(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        return tag != null && tag.contains("mimic");
    }

    private static String getMimickingString(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag != null) {
            CompoundNBT mimic = tag.getCompound("mimic");
            Block value = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(mimic.getString("Name")));
            if (value != null) {
                ItemStack s = new ItemStack(value, 1);
                if (s.getItem() != null) {
                    return s.getDisplayName().getFormattedText();
                }
            }
        }
        return "<unset>";
    }


    public FacadeBlockItem(FacadeBlock block) {
        super(block, new Properties()
            .group(XNet.setup.getTab()));
    }

    private static void userSetMimicBlock(@Nonnull ItemStack item, BlockState mimicBlock, ItemUseContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        setMimicBlock(item, mimicBlock);
        if (world.isRemote) {
            player.sendStatusMessage(new StringTextComponent("Facade is now mimicking " + mimicBlock.getBlock().getTranslationKey()), false);
        }
    }

    public static void setMimicBlock(@Nonnull ItemStack item, BlockState mimicBlock) {
        CompoundNBT tagCompound = new CompoundNBT();
        CompoundNBT nbt = NBTUtil.writeBlockState(mimicBlock);
        tagCompound.put("mimic", nbt);
        item.setTag(tagCompound);
    }

    public static BlockState getMimicBlock(@Nonnull ItemStack stack) {
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null || !tagCompound.contains("mimic")) {
            return Blocks.COBBLESTONE.getDefaultState();
        } else {
            return NBTUtil.readBlockState(tagCompound.getCompound("mimic"));
        }
    }

    @Override
    protected boolean canPlace(BlockItemUseContext context, BlockState state) {
        return true;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        PlayerEntity player = context.getPlayer();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        ItemStack itemstack = context.getItem();

        if (!itemstack.isEmpty()) {

            if (block == CableModule.NETCABLE.get()) {
                FacadeBlock facadeBlock = (FacadeBlock) this.getBlock();
                BlockItemUseContext blockContext = new ReplaceBlockItemUseContext(context);
                BlockState placementState = facadeBlock.getStateForPlacement(blockContext)
                        .with(COLOR, state.get(COLOR))
                        .with(NORTH, state.get(NORTH))
                        .with(SOUTH, state.get(SOUTH))
                        .with(WEST, state.get(WEST))
                        .with(EAST, state.get(EAST))
                        .with(UP, state.get(UP))
                        .with(DOWN, state.get(DOWN))
                        ;

                if (placeBlock(blockContext, placementState)) {
                    SoundType soundtype = world.getBlockState(pos).getBlock().getSoundType(world.getBlockState(pos), world, pos, player);
                    world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                    TileEntity te = world.getTileEntity(pos);
                    if (te instanceof FacadeTileEntity) {
                        ((FacadeTileEntity) te).setMimicBlock(getMimicBlock(itemstack));
                    }
                    int amount = -1;
                    itemstack.grow(amount);
                }
            } else if (block == CableModule.CONNECTOR.get() || block == CableModule.ADVANCED_CONNECTOR.get()) {
                TileEntity te = world.getTileEntity(pos);
                if (te instanceof ConnectorTileEntity) {
                    ConnectorTileEntity connectorTileEntity = (ConnectorTileEntity) te;
                    if (connectorTileEntity.getMimicBlock() == null) {
                        connectorTileEntity.setMimicBlock(getMimicBlock(itemstack));
                        SoundType soundtype = world.getBlockState(pos).getBlock().getSoundType(world.getBlockState(pos), world, pos, player);
                        world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                        int amount = -1;
                        itemstack.grow(amount);
                    } else {
                        userSetMimicBlock(itemstack, connectorTileEntity.getMimicBlock(), context);
                    }
                }
            } else if (block == FacadeModule.FACADE.get()) {
                TileEntity te = world.getTileEntity(pos);
                if (!(te instanceof IFacadeSupport)) {
                    return ActionResultType.FAIL;
                }
                IFacadeSupport facade = (IFacadeSupport) te;
                if (facade.getMimicBlock() == null) {
                    return ActionResultType.FAIL;
                }
                userSetMimicBlock(itemstack, facade.getMimicBlock(), context);
            } else {
                userSetMimicBlock(itemstack, state, context);
            }
            return ActionResultType.SUCCESS;
        } else {
            return ActionResultType.FAIL;
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, worldIn, tooltip, flag);
        tooltipBuilder.makeTooltip(getRegistryName(), stack, tooltip, flag);
    }
}
