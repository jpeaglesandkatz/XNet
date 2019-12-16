package mcjty.xnet.modules.facade;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;

import javax.annotation.Nullable;

public class MimicBlockSupport {

    @Nullable
    private BlockState mimicBlock = null;

    @Nullable
    public BlockState getMimicBlock() {
        return mimicBlock;
    }

    public void setMimicBlock(@Nullable BlockState mimicBlock) {
        this.mimicBlock = mimicBlock;
    }


    public void readFromNBT(CompoundNBT tagCompound) {
        if (tagCompound.contains("mimic")) {
            mimicBlock = NBTUtil.readBlockState(tagCompound.getCompound("mimic"));
            if (mimicBlock == null) {
                mimicBlock = Blocks.COBBLESTONE.getDefaultState();
            }
        } else {
            mimicBlock = null;
        }
    }

    public void writeToNBT(CompoundNBT tagCompound) {
        if (mimicBlock != null) {
            CompoundNBT tag = NBTUtil.writeBlockState(mimicBlock);
            tagCompound.put("mimic", tag);
        }
    }
}
