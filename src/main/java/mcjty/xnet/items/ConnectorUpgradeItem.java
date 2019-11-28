package mcjty.xnet.items;

import mcjty.xnet.XNet;
import mcjty.rftoolsbase.api.xnet.keys.ConsumerId;
import mcjty.xnet.blocks.cables.ConnectorBlock;
import mcjty.xnet.blocks.cables.ConnectorTileEntity;
import mcjty.xnet.blocks.cables.NetCableSetup;
import mcjty.xnet.blocks.generic.CableColor;
import mcjty.xnet.blocks.generic.GenericCableBlock;
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

import javax.annotation.Nullable;
import java.util.List;

public class ConnectorUpgradeItem extends Item {

    public ConnectorUpgradeItem() {
        super(new Properties()
                .group(XNet.setup.getTab())
        );
        setRegistryName("connector_upgrade");
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(new StringTextComponent(TextFormatting.BLUE + "Sneak right click this on a"));
        tooltip.add(new StringTextComponent(TextFormatting.BLUE + "normal connector to upgrade it"));
        tooltip.add(new StringTextComponent(TextFormatting.BLUE + "to an advanced connector"));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
        return super.onItemRightClick(worldIn, playerIn, hand);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();
        Block block = state.getBlock();

        if (block == NetCableSetup.CONNECTOR) {
            if (!world.isRemote) {
                TileEntity te = world.getTileEntity(pos);
                if (te instanceof ConnectorTileEntity) {
                    CompoundNBT tag = new CompoundNBT();
                    te.write(tag);
                    CableColor color = world.getBlockState(pos).get(GenericCableBlock.COLOR);

                    XNetBlobData blobData = XNetBlobData.get(world);
                    WorldBlob worldBlob = blobData.getWorldBlob(world);
                    ConsumerId consumer = worldBlob.getConsumerAt(pos);
                    ((ConnectorBlock)block).unlinkBlock(world, pos);
                    world.setBlockState(pos, NetCableSetup.ADVANCED_CONNECTOR.getDefaultState().with(GenericCableBlock.COLOR, color));
                    BlockState blockState = world.getBlockState(pos);
                    ((ConnectorBlock)blockState.getBlock()).createCableSegment(world, pos, consumer);

                    te = TileEntity.create(tag);
                    // @todo 1.14
//                    if (te != null) {
//                        world.getChunkFromBlockCoords(pos).addTileEntity(te);
//                        te.markDirty();
//                        world.notifyBlockUpdate(pos, blockState, blockState, 3);
//                        player.inventory.decrStackSize(player.inventory.currentItem, 1);
//                        player.openContainer.detectAndSendChanges();
//                        player.sendStatusMessage(new TextComponentString(TextFormatting.GREEN + "Connector was upgraded"), false);
//                    } else {
//                        player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "Something went wrong during upgrade!"), false);
//                        return EnumActionResult.FAIL;
//                    }
                }
            }
            return ActionResultType.SUCCESS;
        } else if (block == NetCableSetup.ADVANCED_CONNECTOR) {
            if (!world.isRemote) {
                player.sendStatusMessage(new StringTextComponent(TextFormatting.YELLOW + "This connector is already advanced!"), false);
            }
            return ActionResultType.SUCCESS;
        } else {
            if (!world.isRemote) {
                player.sendStatusMessage(new StringTextComponent(TextFormatting.RED + "Use this item on a connector to upgrade it!"), false);
            }
            return ActionResultType.SUCCESS;
        }
    }

}
