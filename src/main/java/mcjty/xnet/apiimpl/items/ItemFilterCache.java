package mcjty.xnet.apiimpl.items;

import mcjty.lib.varia.ItemStackList;
import mcjty.xnet.compat.ForestrySupport;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemFilterCache {
    private boolean matchDamage = true;
    private boolean oredictMode = false;
    private boolean blacklistMode = true;
    private boolean nbtMode = false;
    private ItemStackList stacks;
//    private Set<Integer> oredictMatches = new HashSet<>();

    public ItemFilterCache(boolean matchDamage, boolean oredictMode, boolean blacklistMode, boolean nbtMode, @Nonnull ItemStackList stacks) {
        this.matchDamage = matchDamage;
        this.oredictMode = oredictMode;
        this.blacklistMode = blacklistMode;
        this.nbtMode = nbtMode;
        this.stacks = stacks;
        // @todo 1.14
//        for (ItemStack s : stacks) {
//            for (int id : OreDictionary.getOreIDs(s)) {
//                oredictMatches.add(id);
//            }
//        }
    }

    public boolean match(ItemStack stack) {
        if (!stack.isEmpty()) {
            boolean match = false;

            if (oredictMode) {
                // @todo 1.14
//                int[] oreIDs = OreDictionary.getOreIDs(stack);
//                if (oreIDs.length == 0) {
//                    match = itemMatches(stack);
//                } else {
//                    for (int id : oreIDs) {
//                        if (oredictMatches.contains(id)) {
//                            match = true;
//                            break;
//                        }
//                    }
//                }
            } else {
                match = itemMatches(stack);
            }
            return match != blacklistMode;
        }
        return false;
    }

    private boolean itemMatches(ItemStack stack) {
        if (stacks != null) {
            int forestryFlags = ForestrySupport.Tag.GEN.getFlag() | ForestrySupport.Tag.IS_ANALYZED.getFlag();
            ItemStack cleanedStack = null;
            if(nbtMode && ForestrySupport.isLoaded() && ForestrySupport.isBreedable(stack)) {
                cleanedStack = ForestrySupport.sanitize(stack, forestryFlags);
            }
            for (ItemStack itemStack : stacks) {
                if (matchDamage && itemStack.getDamage() != stack.getDamage()) {
                    continue;
                }
                if (nbtMode) {
                    if((cleanedStack != null) && ForestrySupport.isBreedable(itemStack)) {
                        ItemStack cleanedItemStack = ForestrySupport.sanitize(itemStack, forestryFlags);
                        if(!ItemStack.areItemStackTagsEqual(cleanedItemStack, cleanedStack)) {
                    		continue;
                    	}
                    }
                    else if(!ItemStack.areItemStackTagsEqual(itemStack, stack)) {
                        continue;
                    }
                }
                if (itemStack.getItem().equals(stack.getItem())) {
                    return true;
                }
            }
        }
        return false;
    }
}
