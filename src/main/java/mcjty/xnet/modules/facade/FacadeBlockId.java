package mcjty.xnet.modules.facade;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class FacadeBlockId {
    private final String registryName;

    public FacadeBlockId(BlockState mimicBlock) {
        Block block = mimicBlock.getBlock();
        this.registryName = block.getRegistryName().toString();
    }

    public BlockState getBlockState() {
        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(registryName)).defaultBlockState();
    }

    @Override
    public String toString() {
        return registryName;
    }
}
