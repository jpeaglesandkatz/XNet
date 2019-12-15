package mcjty.xnet.modules.facade.blocks;

import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.xnet.modules.cables.blocks.GenericCableBlock;
import mcjty.xnet.modules.facade.FacadeSetup;
import mcjty.xnet.modules.facade.IFacadeSupport;
import mcjty.xnet.modules.facade.MimicBlockSupport;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

public class FacadeTileEntity extends GenericTileEntity implements IFacadeSupport {

    private MimicBlockSupport mimicBlockSupport = new MimicBlockSupport();

    public FacadeTileEntity() {
        super(FacadeSetup.TYPE_FACADE.get());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        BlockState oldMimicBlock = mimicBlockSupport.getMimicBlock();

        super.onDataPacket(net, packet);

        if (world.isRemote) {
            world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
            ModelDataManager.requestModelDataRefresh(this);
        }
    }


    @Override
    public BlockState getMimicBlock() {
        return mimicBlockSupport.getMimicBlock();
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder()
                .withInitial(GenericCableBlock.FACADEID, getMimicBlock())
                .build();
    }


    public void setMimicBlock(BlockState mimicBlock) {
        mimicBlockSupport.setMimicBlock(mimicBlock);
        markDirtyClient();
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        mimicBlockSupport.readFromNBT(tagCompound);
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        mimicBlockSupport.writeToNBT(tagCompound);
        return tagCompound;
    }
}
