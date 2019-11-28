package mcjty.xnet.compat;

import mcjty.lib.compat.theoneprobe.TOPCompatibility;
import mcjty.xnet.XNet;
import mcjty.xnet.modules.cables.CableSetup;
import mcjty.xnet.modules.facade.FacadeSetup;
import mcjty.xnet.modules.facade.blocks.FacadeTileEntity;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import static mcjty.theoneprobe.api.IProbeInfo.ENDLOC;
import static mcjty.theoneprobe.api.IProbeInfo.STARTLOC;
import static mcjty.theoneprobe.api.TextStyleClass.MODNAME;
import static mcjty.theoneprobe.api.TextStyleClass.NAME;

public class TOPSupport {

    public static void registerTopExtras() {
        TOPCompatibility.GetTheOneProbe.probe.registerBlockDisplayOverride((mode, probeInfo, player, world, blockState, data) -> {
            Block block = blockState.getBlock();
            if (block == FacadeSetup.FACADE) {
                String modid = XNet.MODNAME;

                ItemStack pickBlock = data.getPickBlock();
                TileEntity te = world.getTileEntity(data.getPos());
                if (te instanceof FacadeTileEntity) {
                    pickBlock = new ItemStack(CableSetup.NETCABLE, 1);
                }

                if (!pickBlock.isEmpty()) {
                    probeInfo.horizontal()
                            .item(pickBlock)
                            .vertical()
                            .itemLabel(pickBlock)
                            .text(MODNAME + modid);
                } else {
                    probeInfo.vertical()
                            .text(NAME + getBlockUnlocalizedName(block))
                            .text(MODNAME + modid);
                }

                return true;
            }
            return false;
        });
    }

    private static String getBlockUnlocalizedName(Block block) {
        return STARTLOC + block.getTranslationKey() + ".name" + ENDLOC;
    }
}
