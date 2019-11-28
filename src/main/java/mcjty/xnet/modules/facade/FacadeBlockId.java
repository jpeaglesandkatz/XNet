package mcjty.xnet.modules.facade;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class FacadeBlockId {
    private final String registryName;

    public FacadeBlockId(BlockState mimicBlock) {
        Block block = mimicBlock.getBlock();
        this.registryName = block.getRegistryName().toString();
    }

    public BlockState getBlockState() {
        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(registryName)).getDefaultState();
    }

    @Override
    public String toString() {
        return registryName;
    }
}
