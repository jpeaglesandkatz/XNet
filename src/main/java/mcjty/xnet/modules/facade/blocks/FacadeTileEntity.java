package mcjty.xnet.modules.facade.blocks;

import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.xnet.modules.cables.blocks.GenericCableBlock;
import mcjty.xnet.modules.facade.FacadeModule;
import mcjty.xnet.modules.facade.IFacadeSupport;
import mcjty.xnet.modules.facade.MimicBlockSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.Nonnull;

public class FacadeTileEntity extends GenericTileEntity implements IFacadeSupport {

    private final MimicBlockSupport mimicBlockSupport = new MimicBlockSupport();

    public FacadeTileEntity(BlockPos pos, BlockState state) {
        super(FacadeModule.TYPE_FACADE.get(), pos, state);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);

        if (level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            requestModelDataUpdate();
        }
    }


    @Override
    public BlockState getMimicBlock() {
        return mimicBlockSupport.getMimicBlock();
    }

    @Nonnull
    @Override
    public ModelData getModelData() {
        return ModelData.builder()
                .with(GenericCableBlock.FACADEID, getMimicBlock())
                .build();
    }


    public void setMimicBlock(BlockState mimicBlock) {
        mimicBlockSupport.setMimicBlock(mimicBlock);
        markDirtyClient();
    }

    @Override
    public void loadAdditional(CompoundTag tagCompound, HolderLookup.Provider provider) {
        super.loadAdditional(tagCompound, provider);
        mimicBlockSupport.readFromNBT(tagCompound);
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag tagCompound, HolderLookup.Provider provider) {
        super.saveAdditional(tagCompound, provider);
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
