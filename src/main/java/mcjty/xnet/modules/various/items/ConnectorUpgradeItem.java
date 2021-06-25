package mcjty.xnet.modules.various.items;

import mcjty.rftoolsbase.api.xnet.keys.ConsumerId;
import mcjty.xnet.XNet;
import mcjty.xnet.modules.cables.CableColor;
import mcjty.xnet.modules.cables.CableModule;
import mcjty.xnet.modules.cables.blocks.ConnectorBlock;
import mcjty.xnet.modules.cables.blocks.ConnectorTileEntity;
import mcjty.xnet.modules.cables.blocks.GenericCableBlock;
import mcjty.xnet.multiblock.WorldBlob;
import mcjty.xnet.multiblock.XNetBlobData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.item.Item.Properties;

public class ConnectorUpgradeItem extends Item {

    public ConnectorUpgradeItem() {
        super(new Properties()
                .tab(XNet.setup.getTab())
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(new StringTextComponent(TextFormatting.BLUE + "Sneak right click this on a"));
        tooltip.add(new StringTextComponent(TextFormatting.BLUE + "normal connector to upgrade it"));
        tooltip.add(new StringTextComponent(TextFormatting.BLUE + "to an advanced connector"));
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand hand) {
        return super.use(worldIn, playerIn, hand);
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();
        Block block = state.getBlock();

        if (block == CableModule.CONNECTOR.get()) {
            if (!world.isClientSide) {
                TileEntity te = world.getBlockEntity(pos);
                if (te instanceof ConnectorTileEntity) {
                    CompoundNBT tag = new CompoundNBT();
                    te.save(tag);
                    CableColor color = world.getBlockState(pos).getValue(GenericCableBlock.COLOR);

                    XNetBlobData blobData = XNetBlobData.get(world);
                    WorldBlob worldBlob = blobData.getWorldBlob(world);
                    ConsumerId consumer = worldBlob.getConsumerAt(pos);
                    ((ConnectorBlock)block).unlinkBlock(world, pos);
                    world.setBlockAndUpdate(pos, CableModule.ADVANCED_CONNECTOR.get().defaultBlockState().setValue(GenericCableBlock.COLOR, color));
                    BlockState blockState = world.getBlockState(pos);
                    ((ConnectorBlock)blockState.getBlock()).createCableSegment(world, pos, consumer);

                    blockState = ((ConnectorBlock) block).calculateState(world, pos, blockState);
                    world.setBlock(pos, blockState, Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
                    player.inventory.removeItem(player.inventory.selected, 1);
                    player.containerMenu.broadcastChanges();
                    player.displayClientMessage(new StringTextComponent(TextFormatting.GREEN + "Connector was upgraded"), false);
                }
            }
            return ActionResultType.SUCCESS;
        } else if (block == CableModule.ADVANCED_CONNECTOR.get()) {
            if (!world.isClientSide) {
                player.displayClientMessage(new StringTextComponent(TextFormatting.YELLOW + "This connector is already advanced!"), false);
            }
            return ActionResultType.SUCCESS;
        } else {
            if (!world.isClientSide) {
                player.displayClientMessage(new StringTextComponent(TextFormatting.RED + "Use this item on a connector to upgrade it!"), false);
            }
            return ActionResultType.SUCCESS;
        }
    }

}
