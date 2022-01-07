package mcjty.rftoolsbase.api.control.parameters;

import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

/**
 * This class identifies a side of a network blocked. This basically
 * is an optional nodename and an optional side. If side is null then
 * it means the node or processor itself.
 */
public record BlockSide(@Nullable String nodeName, @Nullable EnumFacing side) {

    public BlockSide(@Nullable String name, @Nullable EnumFacing side) {
        this.nodeName = (name == null || name.isEmpty()) ? null : name;
        this.side = side;
    }

    public boolean hasNodeName() {
        return nodeName != null && !nodeName.isEmpty();
    }

    @Override
    public String toString() {
        if (side == null) {
            return "*";
        } else {
            return side.toString();
        }
    }

    public String getStringRepresentation() {
        EnumFacing facing = getSide();

        String s = facing == null ? "" : StringUtils.left(facing.getName().toUpperCase(), 1);
        if (getNodeName() == null) {
            return s;
        } else {
            return StringUtils.left(getNodeName(), 7) + " " + s;
        }
    }

}
