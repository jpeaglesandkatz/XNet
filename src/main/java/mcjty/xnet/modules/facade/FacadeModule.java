package mcjty.xnet.modules.facade;

import mcjty.lib.modules.IModule;
import mcjty.xnet.modules.cables.blocks.GenericCableBlock;
import mcjty.xnet.modules.facade.blocks.FacadeBlock;
import mcjty.xnet.modules.facade.blocks.FacadeBlockItem;
import mcjty.xnet.modules.facade.blocks.FacadeTileEntity;
import mcjty.xnet.modules.facade.client.ClientSetup;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static mcjty.xnet.setup.Registration.*;

public class FacadeModule implements IModule {

    public static final RegistryObject<FacadeBlock> FACADE = BLOCKS.register("facade", () -> new FacadeBlock(GenericCableBlock.CableBlockType.FACADE)); // @todo 1.14
    public static final RegistryObject<Item> FACADE_ITEM = ITEMS.register("facade", () -> new FacadeBlockItem(FACADE.get()));
    public static final RegistryObject<TileEntityType<?>> TYPE_FACADE = TILES.register("facade", () -> TileEntityType.Builder.create(FacadeTileEntity::new, FACADE.get()).build(null));

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
        ClientSetup.initClient();
    }

    @Override
    public void initConfig() {

    }
}
