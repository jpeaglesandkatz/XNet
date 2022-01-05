package mcjty.xnet.logic;

import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.OrientationTools;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class RouterIterator<T extends GenericTileEntity> implements Iterator<T> {

    @Nonnull private final Level world;
    @Nonnull private final BlockPos pos;
    @Nonnull private final Class<T> clazz;

    private int facingIdx = 0;
    private T foundRouter = null;

    Stream<T> stream() {
        return StreamSupport.stream(Spliterators.spliterator(this, OrientationTools.DIRECTION_VALUES.length, Spliterator.ORDERED), false);
    }

    RouterIterator(@Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Class<T> clazz) {
        this.world = world;
        this.pos = pos;
        this.clazz = clazz;
        findNext();
    }

    private void findNext() {
        foundRouter = null;
        while (facingIdx != -1) {
            BlockPos routerPos = pos.relative(OrientationTools.DIRECTION_VALUES[facingIdx]);
            facingIdx++;
            if (facingIdx >= OrientationTools.DIRECTION_VALUES.length) {
                facingIdx = -1;
            }
            BlockEntity te = world.getBlockEntity(routerPos);
            if (clazz.isInstance(te)) {
                foundRouter = (T) te;
                return;
            }
        }
    }

    @Override
    public boolean hasNext() {
        return foundRouter != null;
    }

    @Override
    public T next() {
        T f = foundRouter;
        findNext();
        return f;
    }
}

