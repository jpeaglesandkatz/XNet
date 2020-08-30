package mcjty.xnet.compat;

import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.xnet.modules.cables.CableModule;
import mcjty.xnet.modules.facade.FacadeModule;
import mcjty.xnet.modules.facade.blocks.FacadeTileEntity;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.InterModComms;

import java.util.function.Function;

import static mcjty.theoneprobe.api.TextStyleClass.MODNAME;

public class TopExtras {

    private static boolean registered;

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        InterModComms.sendTo("theoneprobe", "getTheOneProbe", GetTheOneProbe::new);
    }

    private static class GetTheOneProbe implements Function<ITheOneProbe, Void> {

        @Override
        public Void apply(ITheOneProbe probe) {
            registerTopExtras(probe);
            return null;
        }
    }

    private static void registerTopExtras(ITheOneProbe probe) {
        probe.registerBlockDisplayOverride((mode, probeInfo, player, world, blockState, data) -> {
            Block block = blockState.getBlock();
            if (block == FacadeModule.FACADE.get()) {
                String modid = "XNet";

                ItemStack pickBlock = data.getPickBlock();
                TileEntity te = world.getTileEntity(data.getPos());
                if (te instanceof FacadeTileEntity) {
                    pickBlock = new ItemStack(CableModule.NETCABLE.get(), 1);
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

//    private static String getBlockUnlocalizedName(Block block) {
//        return STARTLOC + block.getTranslationKey() + ".name" + ENDLOC;
//    }
}
