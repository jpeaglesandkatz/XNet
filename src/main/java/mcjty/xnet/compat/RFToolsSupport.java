package mcjty.xnet.compat;

import mcjty.rftoolsbase.api.storage.IStorageScanner;
import mcjty.rftoolsbase.api.xnet.channels.IControllerContext;
import mcjty.xnet.apiimpl.items.ItemChannelSettings;
import mcjty.xnet.apiimpl.items.ItemConnectorSettings;
import mcjty.xnet.setup.Config;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Predicate;

public class RFToolsSupport {

    public static boolean isStorageScanner(BlockEntity te) {
        return te instanceof IStorageScanner;
    }

    public static void tickStorageScanner(IControllerContext context, ItemConnectorSettings settings, BlockEntity te,
                                          ItemChannelSettings channelSettings, Level world) {
        IStorageScanner scanner = (IStorageScanner) te;
        Predicate<ItemStack> extractMatcher = settings.getMatcher(context);

        Integer count = settings.getCount();
        int amount = 0;
        if (count != null) {
            amount = scanner.countItems(extractMatcher, true, count);
            if (amount < count) {
                return;
            }
        }
        int cnt = switch (settings.getStackMode()) {
            case SINGLE -> 1;
            case STACK -> 64;
            case COUNT -> settings.getExtractAmount();
        };
        ItemStack stack = scanner.requestItem(extractMatcher, true, cnt, true);
        if (stack.isEmpty()) {
            return;
        }
        // Now that we have a stack we first reduce the amount of the stack if we want to keep a certain
        // number of items
        int toextract = stack.getCount();
        if (count != null) {
            int canextract = amount-count;
            if (canextract <= 0) {
                return;
            }
            if (canextract < toextract) {
                toextract = canextract;
                stack.setCount(toextract);
            }
        }

        if (context.checkAndConsumeRF(Config.controllerOperationRFT.get())) {
            int remaining = channelSettings.insertStack(context, stack, world, -1);
            if (remaining != toextract) {
                scanner.requestItem(extractMatcher, false, toextract - remaining, true);
            }
        }
    }

    public static int countItems(BlockEntity te, Predicate<ItemStack> matcher, int count) {
        IStorageScanner scanner = (IStorageScanner) te;
        return scanner.countItems(matcher, true, count);
    }

    public static int countItems(BlockEntity te, ItemStack stack, int count) {
        IStorageScanner scanner = (IStorageScanner) te;
        return scanner.countItems(stack, true, count);
    }

    public static ItemStack insertItem(BlockEntity te, ItemStack stack, boolean simulate) {
        IStorageScanner scanner = (IStorageScanner) te;
        return scanner.insertItem(stack, simulate);
    }
}
