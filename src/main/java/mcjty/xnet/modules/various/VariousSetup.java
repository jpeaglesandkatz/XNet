package mcjty.xnet.modules.various;

import mcjty.xnet.XNet;
import mcjty.xnet.modules.various.blocks.RedstoneProxyBlock;
import mcjty.xnet.modules.various.blocks.RedstoneProxyUBlock;
import mcjty.xnet.modules.various.items.ConnectorUpgradeItem;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static mcjty.xnet.XNet.MODID;

public class VariousSetup {

    public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, MODID);

    public static void register() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<RedstoneProxyBlock> REDSTONE_PROXY = BLOCKS.register("redstone_proxy", RedstoneProxyBlock::new);
    public static final RegistryObject<RedstoneProxyUBlock> REDSTONE_PROXY_UPD = BLOCKS.register("redstone_proxy_upd", RedstoneProxyUBlock::new);
    public static final RegistryObject<Item> REDSTONE_PROXY_ITEM = ITEMS.register("redstone_proxy", () -> new BlockItem(REDSTONE_PROXY.get(), XNet.createStandardProperties()));
    public static final RegistryObject<Item> REDSTONE_PROXY_UPD_ITEM = ITEMS.register("redstone_proxy_upd", () -> new BlockItem(REDSTONE_PROXY_UPD.get(), XNet.createStandardProperties()));

    public static final RegistryObject<ConnectorUpgradeItem> UPGRADE = ITEMS.register("connector_upgrade", ConnectorUpgradeItem::new);

//@todo 1.14
//    @SideOnly(Side.CLIENT)
//    public static void initColorHandlers(BlockColors blockColors) {
//        facadeBlock.initColorHandler(blockColors);
//        NetCableSetup.initColorHandlers(blockColors);
//    }
}
