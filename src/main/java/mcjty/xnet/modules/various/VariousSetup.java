package mcjty.xnet.modules.various;

import mcjty.xnet.modules.various.blocks.RedstoneProxyBlock;
import mcjty.xnet.modules.various.blocks.RedstoneProxyUBlock;
import mcjty.xnet.modules.various.items.ConnectorUpgradeItem;
import mcjty.xnet.setup.Registration;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;

import static mcjty.xnet.setup.Registration.BLOCKS;
import static mcjty.xnet.setup.Registration.ITEMS;

public class VariousSetup {

    public static void register() {
        // Needed to force class loading
    }

    public static final RegistryObject<RedstoneProxyBlock> REDSTONE_PROXY = BLOCKS.register("redstone_proxy", RedstoneProxyBlock::new);
    public static final RegistryObject<RedstoneProxyUBlock> REDSTONE_PROXY_UPD = BLOCKS.register("redstone_proxy_upd", RedstoneProxyUBlock::new);
    public static final RegistryObject<Item> REDSTONE_PROXY_ITEM = ITEMS.register("redstone_proxy", () -> new BlockItem(REDSTONE_PROXY.get(), Registration.createStandardProperties()));
    public static final RegistryObject<Item> REDSTONE_PROXY_UPD_ITEM = ITEMS.register("redstone_proxy_upd", () -> new BlockItem(REDSTONE_PROXY_UPD.get(), Registration.createStandardProperties()));

    public static final RegistryObject<ConnectorUpgradeItem> UPGRADE = ITEMS.register("connector_upgrade", ConnectorUpgradeItem::new);

//@todo 1.14
//    @SideOnly(Side.CLIENT)
//    public static void initColorHandlers(BlockColors blockColors) {
//        facadeBlock.initColorHandler(blockColors);
//        NetCableSetup.initColorHandlers(blockColors);
//    }
}
