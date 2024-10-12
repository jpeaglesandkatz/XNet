package mcjty.xnet.modules.facade.blocks;

import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.xnet.modules.cables.blocks.GenericCableBlock;
import mcjty.xnet.modules.facade.FacadeModule;
import mcjty.xnet.modules.facade.IFacadeSupport;
import mcjty.xnet.modules.facade.MimicBlockSupport;
import mcjty.xnet.modules.facade.data.MimicData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.Nonnull;

public class FacadeTileEntity extends GenericTileEntity implements IFacadeSupport {

    public FacadeTileEntity(BlockPos pos, BlockState state) {
        super(FacadeModule.TYPE_FACADE.get(), pos, state);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider provider) {
        super.onDataPacket(net, pkt, provider);

        if (level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            requestModelDataUpdate();
        }
    }


    @Override
    public BlockState getMimicBlock() {
        return getData(FacadeModule.MIMIC_DATA).state();
    }

    @Nonnull
    @Override
    public ModelData getModelData() {
        return ModelData.builder()
                .with(GenericCableBlock.FACADEID, getMimicBlock())
                .build();
    }


    public void setMimicBlock(BlockState mimicBlock) {
        setData(FacadeModule.MIMIC_DATA, new MimicData(mimicBlock));
        markDirtyClient();
    }

    @Override
    public void saveClientDataToNBT(CompoundTag tagCompound, HolderLookup.Provider provider) {
        BlockState state = getData(FacadeModule.MIMIC_DATA).state();
        MimicBlockSupport.writeToNBT(tagCompound, state);
    }

    @Override
    public void loadClientDataFromNBT(CompoundTag tagCompound, HolderLookup.Provider provider) {
        BlockState state = MimicBlockSupport.readFromNBT(provider, tagCompound);
        setData(FacadeModule.MIMIC_DATA, new MimicData(state));
    }
}
