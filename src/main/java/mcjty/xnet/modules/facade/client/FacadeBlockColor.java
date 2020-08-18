package mcjty.xnet.modules.facade.client;

import mcjty.xnet.modules.facade.IFacadeSupport;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

import javax.annotation.Nullable;

public class FacadeBlockColor implements IBlockColor {

    @Override
    public int getColor(BlockState blockState, @Nullable IBlockDisplayReader world, @Nullable BlockPos pos, int tint) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IFacadeSupport) {
            IFacadeSupport facade = (IFacadeSupport) te;
            BlockState mimic = facade.getMimicBlock();
            if (mimic != null) {
                return Minecraft.getInstance().getBlockColors().getColor(mimic, world, pos, tint);
            }
        }

        return -1;
    }
}
