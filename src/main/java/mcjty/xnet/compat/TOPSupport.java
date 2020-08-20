package mcjty.xnet.compat;

import mcjty.lib.compat.theoneprobe.TOPCompatibility;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.xnet.XNet;
import mcjty.xnet.modules.cables.CableSetup;
import mcjty.xnet.modules.facade.FacadeSetup;
import mcjty.xnet.modules.facade.blocks.FacadeTileEntity;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.StringTextComponent;

import static mcjty.theoneprobe.api.TextStyleClass.MODNAME;

public class TOPSupport {

    public static void registerTopExtras() {
        TOPCompatibility.GetTheOneProbe.probe.registerBlockDisplayOverride((mode, probeInfo, player, world, blockState, data) -> {
            Block block = blockState.getBlock();
            if (block == FacadeSetup.FACADE.get()) {
                String modid = XNet.MODNAME;

                ItemStack pickBlock = data.getPickBlock();
                TileEntity te = world.getTileEntity(data.getPos());
                if (te instanceof FacadeTileEntity) {
                    pickBlock = new ItemStack(CableSetup.NETCABLE.get(), 1);
                }

                if (!pickBlock.isEmpty()) {
                    probeInfo.horizontal()
                            .item(pickBlock)
                            .vertical()
                            .itemLabel(pickBlock)
                            .text(CompoundText.create().style(MODNAME).text(modid));
                } else {
                    probeInfo.vertical()
                            .text(CompoundText.create().name(block.getTranslationKey()))
                            .text(CompoundText.create().style(MODNAME).text(modid));
                }

                return true;
            }
            return false;
        });
    }
}
