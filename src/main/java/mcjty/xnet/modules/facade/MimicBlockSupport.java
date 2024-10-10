package mcjty.xnet.modules.facade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.state.BlockState;

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


    public void readFromNBT(CompoundTag tagCompound) {
        if (tagCompound != null && tagCompound.contains("mimic")) {
            // @todo 1.21 NBT
            mimicBlock = null; //NBTTools.readBlockState(tagCompound.getCompound("mimic"));
        } else {
            mimicBlock = null;
        }
    }

    public void writeToNBT(CompoundTag tagCompound) {
        if (mimicBlock != null) {
            // @todo 1.21 NBT
            CompoundTag tag = NbtUtils.writeBlockState(mimicBlock);
            tagCompound.put("mimic", tag);
        }
    }
}
