package mcjty.xnet.datagen;

import mcjty.lib.datagen.BaseBlockStateProvider;
import mcjty.lib.varia.OrientationTools;
import mcjty.xnet.XNet;
import mcjty.xnet.modules.controller.ControllerModule;
import mcjty.xnet.modules.controller.blocks.TileEntityController;
import mcjty.xnet.modules.router.RouterModule;
import mcjty.xnet.modules.various.VariousModule;
import mcjty.xnet.modules.wireless.WirelessRouterModule;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import static net.minecraftforge.client.model.generators.ModelProvider.BLOCK_FOLDER;

public class BlockStates extends BaseBlockStateProvider {

    public BlockStates(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, XNet.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        singleTextureBlock(VariousModule.REDSTONE_PROXY.get(), BLOCK_FOLDER + "/redstone_proxy", "block/machine_proxy");
        singleTextureBlock(VariousModule.REDSTONE_PROXY_UPD.get(), BLOCK_FOLDER + "/redstone_proxy_upd", "block/machine_proxy");
        registerController();
        registerRouter();
        registerWirelessRouter();
    }

    private void registerController() {
        ModelFile modelOk = frontBasedModel("controller", modLoc("block/machine_controller"));
        ModelFile modelError = frontBasedModel("controller_error", modLoc("block/machine_controller_error"));
        VariantBlockStateBuilder builder = getVariantBuilder(ControllerModule.CONTROLLER.get());
        for (Direction direction : OrientationTools.DIRECTION_VALUES) {
            applyRotation(builder.partialState().with(BlockStateProperties.FACING, direction).with(TileEntityController.ERROR, false)
                    .modelForState().modelFile(modelOk), direction);
            applyRotation(builder.partialState().with(BlockStateProperties.FACING, direction).with(TileEntityController.ERROR, true)
                    .modelForState().modelFile(modelError), direction);
        }
    }

    private void registerRouter() {
        ModelFile modelOk = frontBasedModel("router", modLoc("block/machine_router"));
        ModelFile modelError = frontBasedModel("router_error", modLoc("block/machine_router_error"));
        VariantBlockStateBuilder builder = getVariantBuilder(RouterModule.ROUTER.get());
        for (Direction direction : OrientationTools.DIRECTION_VALUES) {
            applyRotation(builder.partialState().with(BlockStateProperties.FACING, direction).with(TileEntityController.ERROR, false)
                    .modelForState().modelFile(modelOk), direction);
            applyRotation(builder.partialState().with(BlockStateProperties.FACING, direction).with(TileEntityController.ERROR, true)
                    .modelForState().modelFile(modelError), direction);
        }
    }

    private void registerWirelessRouter() {
        ModelFile modelOk = frontBasedModel("wireless_router", modLoc("block/machine_wireless_router"));
        ModelFile modelError = frontBasedModel("wireless_router_error", modLoc("block/machine_wireless_router_error"));
        VariantBlockStateBuilder builder = getVariantBuilder(WirelessRouterModule.WIRELESS_ROUTER.get());
        for (Direction direction : OrientationTools.DIRECTION_VALUES) {
            applyRotation(builder.partialState().with(BlockStateProperties.FACING, direction).with(TileEntityController.ERROR, false)
                    .modelForState().modelFile(modelOk), direction);
            applyRotation(builder.partialState().with(BlockStateProperties.FACING, direction).with(TileEntityController.ERROR, true)
                    .modelForState().modelFile(modelError), direction);
        }
    }
}
