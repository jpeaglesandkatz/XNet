package mcjty.xnet.modules.facade.blocks;

import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;

public class ReplaceBlockItemUseContext extends BlockItemUseContext {

    public ReplaceBlockItemUseContext(ItemUseContext context) {
        super(context);
        replaceClicked = true;
    }
}
