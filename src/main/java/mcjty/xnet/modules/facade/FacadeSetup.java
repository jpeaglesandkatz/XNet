package mcjty.xnet.modules.facade;

import mcjty.xnet.XNet;
import mcjty.xnet.modules.cables.blocks.GenericCableBlock;
import mcjty.xnet.modules.facade.blocks.FacadeBlock;
import mcjty.xnet.modules.facade.blocks.FacadeItemBlock;
import mcjty.xnet.modules.facade.blocks.FacadeTileEntity;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;

import static mcjty.xnet.XNet.MODID;

public class FacadeSetup {

    public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<TileEntityType<?>> TILES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, MODID);

    public static void register() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<FacadeBlock> FACADE = BLOCKS.register("facade", () -> new FacadeBlock(GenericCableBlock.CableBlockType.FACADE)); // @todo 1.14
    public static final RegistryObject<Item> FACADE_ITEM = ITEMS.register("facade", () -> new FacadeItemBlock(FACADE.get()));
    public static final RegistryObject<TileEntityType<?>> TYPE_FACADE = TILES.register("facade", () -> TileEntityType.Builder.create(FacadeTileEntity::new, FACADE.get()).build(null));
}
