package mcjty.xnet.datagen;

import mcjty.lib.datagen.BaseBlockTagsProvider;
import mcjty.xnet.XNet;
import mcjty.xnet.modules.controller.ControllerModule;
import mcjty.xnet.modules.facade.FacadeModule;
import mcjty.xnet.modules.router.RouterModule;
import mcjty.xnet.modules.various.VariousModule;
import mcjty.xnet.modules.wireless.WirelessRouterModule;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;

public class BlockTags extends BaseBlockTagsProvider {

    public BlockTags(DataGenerator generator, ExistingFileHelper helper) {
        super(generator, XNet.MODID, helper);
    }

    @Override
    protected void addTags() {
        stonePickaxe(
                VariousModule.REDSTONE_PROXY, VariousModule.REDSTONE_PROXY_UPD
        );
        ironPickaxe(
                ControllerModule.CONTROLLER,
                FacadeModule.FACADE,
                RouterModule.ROUTER,
                WirelessRouterModule.WIRELESS_ROUTER, WirelessRouterModule.ANTENNA, WirelessRouterModule.ANTENNA_BASE, WirelessRouterModule.ANTENNA_DISH
        );
    }

    @Override
    @Nonnull
    public String getName() {
        return "XNet Tags";
    }
}
