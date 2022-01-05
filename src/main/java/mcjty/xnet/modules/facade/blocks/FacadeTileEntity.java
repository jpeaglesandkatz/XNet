package mcjty.xnet.modules.facade.blocks;

import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.xnet.modules.cables.blocks.GenericCableBlock;
import mcjty.xnet.modules.facade.FacadeModule;
import mcjty.xnet.modules.facade.IFacadeSupport;
import mcjty.xnet.modules.facade.MimicBlockSupport;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

public class FacadeTileEntity extends GenericTileEntity implements IFacadeSupport {

    private MimicBlockSupport mimicBlockSupport = new MimicBlockSupport();

    public FacadeTileEntity() {
        super(FacadeModule.TYPE_FACADE.get());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        super.onDataPacket(net, packet);

        if (level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
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
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);
        mimicBlockSupport.readFromNBT(tagCompound);
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tagCompound) {
        super.saveAdditional(tagCompound);
        mimicBlockSupport.writeToNBT(tagCompound);
    }

    @Override
    public void saveClientDataToNBT(CompoundTag tagCompound) {
        mimicBlockSupport.writeToNBT(tagCompound);
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tagCompound) {
        mimicBlockSupport.readFromNBT(tagCompound);
    }
}
