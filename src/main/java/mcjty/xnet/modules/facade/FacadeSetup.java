package mcjty.xnet.modules.facade;

import mcjty.xnet.modules.cables.blocks.GenericCableBlock;
import mcjty.xnet.modules.facade.blocks.FacadeBlock;
import mcjty.xnet.modules.facade.blocks.FacadeBlockItem;
import mcjty.xnet.modules.facade.blocks.FacadeTileEntity;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;

import static mcjty.xnet.setup.Registration.*;

public class FacadeSetup {

    public static void register() {
        // Needed to force class loading
    }

    public static final RegistryObject<FacadeBlock> FACADE = BLOCKS.register("facade", () -> new FacadeBlock(GenericCableBlock.CableBlockType.FACADE)); // @todo 1.14
    public static final RegistryObject<Item> FACADE_ITEM = ITEMS.register("facade", () -> new FacadeBlockItem(FACADE.get()));
    public static final RegistryObject<TileEntityType<?>> TYPE_FACADE = TILES.register("facade", () -> TileEntityType.Builder.create(FacadeTileEntity::new, FACADE.get()).build(null));
}
