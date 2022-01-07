package mcjty.xnet.apiimpl.items;

import mcjty.lib.varia.ItemStackList;
import mcjty.lib.varia.ItemStackTools;
import mcjty.xnet.compat.ForestrySupport;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ItemFilterCache {
    private final boolean matchDamage;
    private final boolean tagsMode;
    private final boolean blacklistMode;
    private final boolean nbtMode;
    private final ItemStackList stacks;
    private final Set<ResourceLocation> tagMatches = new HashSet<>();

    public ItemFilterCache(boolean matchDamage, boolean tagsMode, boolean blacklistMode, boolean nbtMode, @Nonnull ItemStackList stacks) {
        this.matchDamage = matchDamage;
        this.tagsMode = tagsMode;
        this.blacklistMode = blacklistMode;
        this.nbtMode = nbtMode;
        this.stacks = stacks;
        for (ItemStack s : stacks) {
            ItemStackTools.addCommonTags(s.getItem().getTags(), tagMatches);
        }
    }

    public boolean match(ItemStack stack) {
        if (!stack.isEmpty()) {
            boolean match = false;

            if (tagsMode) {
                Set<ResourceLocation> tags = stack.getItem().getTags();
                if (tags.isEmpty()) {
                    match = itemMatches(stack);
                } else {
                    if (!Collections.disjoint(tagMatches, tags)) {
                        match = true;
                    }
                }
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
                if (matchDamage && itemStack.getDamageValue() != stack.getDamageValue()) {
                    continue;
                }
                if (nbtMode) {
                    if((cleanedStack != null) && ForestrySupport.isBreedable(itemStack)) {
                        ItemStack cleanedItemStack = ForestrySupport.sanitize(itemStack, forestryFlags);
                        if(!ItemStack.tagMatches(cleanedItemStack, cleanedStack)) {
                    		continue;
                    	}
                    }
                    else if(!ItemStack.tagMatches(itemStack, stack)) {
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
